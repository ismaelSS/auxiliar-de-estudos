# Phase 6: Scoring System & Question ID Rework - Research

**Researched:** 2026-07-08
**Domain:** Scoring engine redesign, question identity management, stats data migration
**Confidence:** HIGH

## Summary

The current scoring system tracks correct/incorrect counts per question-text string and computes a hit rate percentage. This phase replaces it with a score-accumulation model: each correct answer adds +2 (capped at +5), each wrong answer subtracts -3 (floored at -10). The "hit rate" display is replaced by an "aproveitamento" weight — negative-score questions weigh -3, zero-score questions weigh 0, positive-score questions weigh +2. All question references in the stats layer shift from question-text keys to unique `id` fields sourced from the theme JSON files.

**Primary risk:** The existing `flashcard-stats.json` stores question stats keyed by question text with `answered`/`correct` fields. A data migration is required to convert to `id`-keyed entries with `score` fields, because the old hit-rate data cannot be mechanically translated to the new bounded-score model. The user's existing progress data for question-level stats will be reset to score=0 on migration. Theme-level aggregate stats (totalAnswered/totalCorrect) can be preserved.

**Primary recommendation:** Execute in 4 waves: (1) add `id` to Question model and theme JSONs, (2) refactor StatsData/StatsService to score-based model with migration logic, (3) update reinforcement round selection and views, (4) update tests and verify.

## User Constraints (from CONTEXT.md)

### Locked Decisions
(none — all items are specified as requirements, not locked tool/library choices)

### the agent's Discretion
- Choice of `id` data type in Question model (String vs int)
- Migration strategy for flashcard-stats.json (start fresh vs convert)
- Exact method naming in the new API
- UI label text for "aproveitamento" display

### Deferred Ideas (OUT OF SCOPE)
(none)

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| ADD-ID | Add `id` field to Question model; update theme JSONs | Question.java currently lacks `id`; JSON files have no `id` per question |
| NEG-SCORE | Wrong answer deducts -3 from question score, floor -10 | QuestionStats stores answered/correct; must change to bounded int score |
| POS-SCORE | Right answer adds +2 to question score, cap +5 | Same structure change; scoring applied in `StatsService.recordRound()` |
| APROVEITAMENTO | Replace hit rate display with aproveitamento weight | Hit rate calc in `getHitRate()`; new method needed; `getHighestErrorQuestions()` → `getLowestScoreQuestions()` |
| REINFORCEMENT | RoundState.createReinforcementRound uses score-based selection | Currently uses error rate from `getHighestErrorQuestions()`; must switch to `getLowestScoreQuestions()` and match by ID |
| STATS-FILE | flashcard-stats.json backward compatible on read | Old JSON format has different field names; migration handler needed |

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Question identity (`id`) | Data model | Theme JSON files | `id` is a field on Question POJO; source of truth is JSON on disk |
| Score accumulation | Service | Data model | `StatsService.recordRound()` computes deltas; `StatsData` stores them |
| Score bounds enforcement | Service | — | Service layer clamps score to [-10, +5] before persisting |
| Aproveitamento formula | Service | — | Read-only computation from stored per-question scores |
| Reinforcement round selection | Model | Service | `RoundState.createReinforcementRound()` queries service for lowest-score questions |
| Migration of old stats file | Service | — | `StatsService` constructor detects and converts on first load |
| UI display of scores | View | Controller | ThemeSelectionView and ReportsView show computed scores; controllers fetch them |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Java (OpenJDK) | 25 | Language runtime | Project target, set in pom.xml |
| JavaFX | 25 | Desktop UI | Project dependency (javafx-controls) |
| Jackson | 2.17.2 | JSON serialize/deserialize | Existing project dep (jackson-databind) |
| JUnit Jupiter | 5.11.0 | Testing | Existing project dep, scope=test |

### Supporting — No new libraries needed
This phase adds zero new dependencies. All changes are within the existing Java model/service/view/controller layers and the JSON data files.

## Package Legitimacy Audit

> This phase installs no new external packages. All changes are in existing project code and JSON data files. No audit needed.

## Architecture Patterns

### System Architecture Data Flow

```
Theme JSON (themes/*.json)
  │  [Jackson reads questions with new id field]
  ▼
Question  ◄── id: String
  │
  ▼
StudyRoundController
  │  [user answers → checkAnswer()]
  ▼
RoundResult(id, themeName, wasCorrect)
  │
  ▼
StatsService.recordRound()
  │  [correct: score += 2 (cap +5)]
  │  [wrong:   score -= 3 (floor -10)]
  ▼
StatsData
  ├─ themes[themeName].questions[questionId].score
  └─ (theme-level aggregates optional)
  │
  ▼  [ThemeSelectionController calls getAproveitamento(themeName)]
ThemeSelectionView — shows "Aproveitamento: +5" instead of "Hit rate: 67%"
  │
  ▼  [ReportsController calls getLowestScoreQuestions()]
ReportsView — shows lowest-score questions instead of highest-error
  │
  ▼  [createReinforcementRound queries getLowestScoreQuestions()]
RoundState — selects lowest-score questions first for reinforcement mode
```

### Recommended Project Structure — No changes
```
src/main/java/org/IsmaelSS/
├── model/
│   ├── Question.java          # ADD id field
│   ├── StatsData.java         # REWRITE QuestionStats→QuestionScore with score field
│   ├── RoundState.java        # UPDATE createReinforcementRound to use score-based
│   ├── RoundResult.java       # ADD questionId field
│   └── Theme.java             # No change
├── service/
│   ├── StatsService.java      # REWRITE scoring logic, add migration
│   └── ThemeLoader.java       # No change (id is just another field)
├── controller/
│   ├── ThemeSelectionController.java  # UPDATE refreshHitRates→refreshScores
│   ├── StudyRoundController.java      # UPDATE to pass questionId in RoundResult
│   └── ReportsController.java         # UPDATE to use score-based methods
└── view/
    ├── ThemeSelectionView.java        # UPDATE hit rate label→aproveitamento
    ├── StudyRoundView.java            # No change
    └── ReportsView.java               # UPDATE error label→lowest score label
themes/
├── java-record.json           # ADD id to every question
└── java-modificadores.json    # ADD id to every question
src/test/java/org/IsmaelSS/
├── service/StatsServiceTest.java      # REWRITE for score-based assertions
├── model/StatsDataTest.java           # UPDATE for QuestionScore
└── model/RoundStateReinforcementTest.java  # UPDATE for score-based selection
```

### Pattern 1: Bounded Score Accumulation + Aproveitamento Formula

**What:** Each question has an integer score starting at 0. Correct answers add +2 (cap at +5). Wrong answers subtract -3 (floor at -10). Periodically, the "aproveitamento" metric computes a weight for each question: negative scores → weight -3, zero/neutral → weight 0, positive → weight +2. These are summed for the theme.

**When to use:** Any flashcard/spaced-repetition system where you want to emphasize questions that the user struggles with while avoiding runaway positive scores.

**Example scoring logic (service layer):**
```java
// Scoring constants
private static final int SCORE_CORRECT_DELTA = 2;
private static final int SCORE_MAX = 5;
private static final int SCORE_WRONG_DELTA = -3;
private static final int SCORE_MIN = -10;

// Applying a result:
int newScore = currentScore + (wasCorrect ? SCORE_CORRECT_DELTA : SCORE_WRONG_DELTA);
newScore = Math.max(SCORE_MIN, Math.min(SCORE_MAX, newScore));

// Aproveitamento:
if (score < 0) weight = -3;
else if (score == 0) weight = 0;
else weight = 2;
```

### Anti-Patterns to Avoid
- **Using question text as map key:** After this phase, always key question stats by `id` not by question text. Question text can change (typo fixes, translation), but `id` is stable.
- **Skipping the migration:** Loading old JSON format silently loses user data. Always detect and migrate.
- **Hardcoding scoring constants:** Use `private static final int` fields so boundary values can be tested and changed.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JSON serialization | Custom JSON parser | Jackson (existing dep) | Already in the project; handles nested POJOs, unknown field ignoring |
| Test framework | Custom test runner | JUnit 5 (existing dep) | Already configured in pom.xml with surefire plugin |
| UI framework | Swing/AWT | JavaFX (existing dep) | Already the app's UI framework |
| Data migration framework | Custom versioned migrator | Simple if/else in StatsService constructor | Only one migration needed (old→new), not worth a library |

**Key insight:** This phase has zero new dependency surface. Every required capability (JSON, testing, UI) is already in the project.

## Runtime State Inventory

> Required for refactor phases. This is a data model refactor, not a rename, but the stats file format changes.

| Category | Items Found | Action Required |
|----------|-------------|------------------|
| Stored data | `flashcard-stats.json` in project root — 44 lines, 2 themes, per-question `answered`/`correct` counters keyed by question text | **Data migration in code:** On first load with new version, detect old format, convert question-text keys to question-id keys, replace `answered`/`correct` with `score: 0`, write new format. Theme-level totals optional. |
| Live service config | None — no external services | None |
| OS-registered state | None | None |
| Secrets/env vars | None | None |
| Build artifacts | Standard Maven target/ directory | `mvn clean compile` after changes; no old-name artifacts to worry about |

**Nothing found in other categories:** Verified by git status and project inspection.

**Migration detail:** The old JSON has `"questions": { "question text": { "answered": N, "correct": N } }`. The new format should be `"questions": { "question-id-string": { "score": N } }`. Per-question stats cannot be migrated meaningfully because:
- The scoring model changed from counters to bounded accumulation
- Hit rate data doesn't tell us the last action sequence needed to reconstruct a score
- **Recommendation:** Reset all question scores to 0 on migration; preserve the `totalAnswered`/`totalCorrect` theme aggregates as informational metadata

## Common Pitfalls

### Pitfall 1: Question-text map keys break after migration
**What goes wrong:** Old stats JSON keys questions by their text; new code keys by `id`. After migration, if the question ID differs from the text, lookups fail.
**Why it happens:** The old `StatsService.recordRound()` uses `result.questionText()` as the Map key. The new code must use `result.questionId()`.
**How to avoid:** Change `RoundResult` to carry `questionId` instead of (or in addition to) `questionText`. Update all consumers of `RoundResult` (StatsService, test factories, RoundState).
**Warning signs:** `NullPointerException` or `NullPointerException` when theme stats show zero questions after a round.

### Pitfall 2: Score cap/floor creates non-obvious plateaus
**What goes wrong:** A question with score -10 gets 4 wrong answers in a row — still -10. Developer wonders if scoring is broken.
**Why it happens:** Floor at -10 means repeated wrong answers stop accumulating after -10.
**How to avoid:** Log scoring deltas at level FINE. Unit test the boundary: assert that 10 wrong answers still yields -10, not -30.
**Warning signs:** Testing shows wrong answers not decreasing score below -10 (correct behavior, but surprising).

### Pitfall 3: Reinforcement round matching fails with ID mismatch
**What goes wrong:** `createReinforcementRound` receives a list of lowest-score question IDs, then tries to match them to Question objects in the theme. If theme JSON IDs don't match the IDs in the stats file (because migration changed them), no questions are selected.
**Why it happens:** The selection algorithm matches by question text currently. Switching to ID match without ensuring both sides agree.
**How to avoid:** Use `Map<String, Question>` indexed by `id` for O(1) lookup. Write a test that creates questions with known IDs, records rounds, and verifies the reinforcement round picks them up by ID.
**Warning signs:** Reinforcement mode returns fewer questions than expected, or returns random questions.

### Pitfall 4: ThemeLoader does not validate id uniqueness
**What goes wrong:** Two questions in the same theme JSON have the same `id` value. Second overwrites the first in the stats map. User sees unexpected behavior.
**Why it happens:** No uniqueness validation in `ThemeLoader.loadTheme()`.
**How to avoid:** Add a check in `ThemeLoader.loadTheme()` that all question `id` values within a file are unique. If duplicates found, log warning and skip the file (return null).
**Warning signs:** Stats show only one entry for both questions, or reinforcement round skips one.

### Pitfall 5: ReportsController.highestErrorQuestions→getLowestScoreQuestions name change in view
**What goes wrong:** The view's errorBox is labeled "Questões com Maior Taxa de Erro" but now shows score data.
**Why it happens:** Not updating the UI label in `ReportsView.java`.
**How to avoid:** Update label text to "Questões com Menor Pontuação" or similar. The VBox field name `errorBox` can stay for backward compat but should have explanatory comment.
**Warning signs:** Reports show correct data under wrong heading.

## Code Examples

### Question model with `id` field
```java
// Question.java — add id field
public class Question {
    private String id;            // NEW: unique ID within the theme
    private String question;
    private List<String> options;
    private int correct;

    public Question() {}

    public Question(@JsonProperty("id") String id,
                    @JsonProperty("question") String question,
                    @JsonProperty("options") List<String> options,
                    @JsonProperty("correct") int correct) {
        this.id = id;
        this.question = question;
        this.options = options;
        this.correct = correct;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    // ... rest unchanged
}
```

### StatsData new structure (QuestionScore)
```java
// StatsData.java — inner class changes
public static class QuestionScore {
    private int score;   // -10..+5, replaces answered/correct

    public QuestionScore() {}

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    /**
     * Applies a correct answer: +2, capped at +5.
     * Returns the new score (for testing convenience).
     */
    public int recordCorrect() {
        this.score = Math.min(5, this.score + 2);
        return this.score;
    }

    /**
     * Applies a wrong answer: -3, floored at -10.
     * Returns the new score (for testing convenience).
     */
    public int recordWrong() {
        this.score = Math.max(-10, this.score - 3);
        return this.score;
    }
}

// ThemeStats simplified (totalAnswered/totalCorrect retained for migration compat)
public static class ThemeStats {
    private int totalAnswered;      // KEPT for backward compat during migration
    private int totalCorrect;       // KEPT for backward compat during migration
    private Map<String, QuestionScore> questions = new HashMap<>();  // keyed by question ID

    // getters/setters unchanged
}
```

### StatsService scoring logic
```java
// In StatsService.recordRound() — replace current logic with:
public void recordRound(List<RoundResult> results) {
    for (RoundResult result : results) {
        ThemeStats themeStats = data.getThemes()
                .computeIfAbsent(result.themeName(), k -> new ThemeStats());
        themeStats.setTotalAnswered(themeStats.getTotalAnswered() + 1);
        if (result.wasCorrect()) {
            themeStats.setTotalCorrect(themeStats.getTotalCorrect() + 1);
        }

        QuestionScore qScore = themeStats.getQuestions()
                .computeIfAbsent(result.questionId(), k -> new QuestionScore());
        if (result.wasCorrect()) {
            qScore.recordCorrect();
        } else {
            qScore.recordWrong();
        }
    }
    recalculateOverall();
    save();
}
```

### Aproveitamento calculation
```java
// In StatsService — new method replacing getHitRate
public String getAproveitamento(String themeName) {
    ThemeStats ts = data.getThemes().get(themeName);
    if (ts == null || ts.getQuestions().isEmpty()) return "N/A";
    
    int weightSum = 0;
    for (QuestionScore qs : ts.getQuestions().values()) {
        int score = qs.getScore();
        if (score < 0) weightSum += -3;
        else if (score > 0) weightSum += 2;
        // score == 0 → weight 0
    }
    return String.valueOf(weightSum);
}
```

### Lowest-score question selection
```java
// In StatsService — replaces getHighestErrorQuestions
public List<Map.Entry<String, Integer>> getLowestScoreQuestions(int limit) {
    List<Map.Entry<String, Integer>> entries = new ArrayList<>();
    for (Map.Entry<String, ThemeStats> themeEntry : data.getThemes().entrySet()) {
        for (Map.Entry<String, QuestionScore> questionEntry : 
                themeEntry.getValue().getQuestions().entrySet()) {
            entries.add(Map.entry(
                questionEntry.getKey(),                    // question ID
                questionEntry.getValue().getScore()        // question score
            ));
        }
    }
    entries.sort(Map.Entry.comparingByValue());            // ascending by score
    return entries.subList(0, Math.min(limit, entries.size()));
}
```

### Reinforcement round — score-based selection
```java
// In RoundState.createReinforcementRound — updated logic
public static RoundState createReinforcementRound(
        List<Theme> themes, int questionsPerTheme, StatsService statsService) {
    
    List<Map.Entry<String, Integer>> lowestScores = statsService.getLowestScoreQuestions(50);
    Set<String> lowScoreIds = lowestScores.stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    
    List<RoundQuestion> questions = new ArrayList<>();
    for (Theme theme : themes) {
        List<Question> themeQuestions = new ArrayList<>(theme.getQuestions());
        List<Question> lowQuestions = new ArrayList<>();
        List<Question> freshQuestions = new ArrayList<>();

        for (Question q : themeQuestions) {
            if (lowScoreIds.contains(q.getId())) {
                lowQuestions.add(q);
            } else {
                freshQuestions.add(q);
            }
        }

        // Sort low-scoring questions by score ascending (worst first)
        Map<String, Integer> scoreMap = statsService.getLowestScoreQuestions(50).stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        lowQuestions.sort(Comparator.comparingInt(q -> scoreMap.getOrDefault(q.getId(), 0)));

        int take = Math.min(questionsPerTheme, themeQuestions.size());
        List<Question> selected = new ArrayList<>();
        for (Question q : lowQuestions) {
            if (selected.size() >= take) break;
            selected.add(q);
        }
        Collections.shuffle(freshQuestions);
        for (Question q : freshQuestions) {
            if (selected.size() >= take) break;
            selected.add(q);
        }

        for (Question q : selected) {
            // ... same shuffled-option logic as current implementation
        }
    }
    Collections.shuffle(questions);
    return new RoundState(questions, true);
}
```

### Migration logic in StatsService constructor
```java
// In StatsService — detect and migrate old format
private StatsData load() {
    File file = new File(STATS_FILE);
    if (!file.exists()) {
        return new StatsData();
    }
    try {
        StatsData data = mapper.readValue(file, StatsData.class);
        // Detect old format: check if any ThemeStats has questions
        // keyed by text rather than ID (heuristic: first key doesn't look like an ID)
        if (isOldFormat(data)) {
            LOG.info("Detected old stats format — migrating to new format...");
            data = migrateOldFormat(data);
            // Write migrated data immediately
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        }
        return data;
    } catch (IOException e) {
        LOG.warning("Could not read stats file, starting fresh: " + e.getMessage());
        return new StatsData();
    }
}

private boolean isOldFormat(StatsData data) {
    // Heuristic: if any theme has questions and the first key contains
    // a full sentence (spaces + length > 30), it's text-keyed old format
    for (ThemeStats ts : data.getThemes().values()) {
        for (String key : ts.getQuestions().keySet()) {
            // Old format = long sentence keys; new format = short IDs like "rec-01"
            return key.length() > 20 || key.contains(" ");
        }
    }
    return false;
}

private StatsData migrateOldFormat(StatsData oldData) {
    StatsData newData = new StatsData();
    // Theme-level totals preserved
    for (Map.Entry<String, ThemeStats> themeEntry : oldData.getThemes().entrySet()) {
        ThemeStats oldTs = themeEntry.getValue();
        ThemeStats newTs = new ThemeStats();
        newTs.setTotalAnswered(oldTs.getTotalAnswered());
        newTs.setTotalCorrect(oldTs.getTotalCorrect());
        // Per-question scores reset to 0 — old answered/correct not convertible
        // to bounded score. Questions are empty; they'll be populated as user plays.
        newTs.setQuestions(new HashMap<>());
        newData.getThemes().put(themeEntry.getKey(), newTs);
    }
    // Overall preserved (was computed from themes)
    newData.getOverall().setTotalAnswered(oldData.getOverall().getTotalAnswered());
    newData.getOverall().setTotalCorrect(oldData.getOverall().getTotalCorrect());
    return newData;
}
```

### Theme JSON with `id`
```json
[
  {
    "id": "rec-intro-01",
    "question": "Qual é o principal objetivo de um record em Java?",
    "options": [
      "Criar classes abstratas",
      "Representar dados de forma concisa e imutável",
      "Substituir interfaces",
      "Criar classes utilitárias",
      "Melhorar o desempenho da JVM"
    ],
    "correct": 1
  }
]
```

### RoundResult with questionId
```java
// RoundResult.java — add questionId field
public record RoundResult(String themeName, String questionText, 
                          String questionId, boolean wasCorrect) {}
```

### ThemeSelectionView label update
```java
// In ThemeSelectionView.java — updateHitRate becomes updateAproveitamento
public void updateAproveitamento(String themeName, String score) {
    Label info = themeLabels.get(themeName);
    if (info != null) {
        Theme theme = themeMap.get(themeName);
        if (theme != null) {
            info.setText(theme.getName() + " (" + theme.getQuestionCount() 
                + " perguntas) — Aproveitamento: " + score);
        }
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| QuestionStats (answered/correct) | QuestionScore (bounded int -10..+5) | This phase | Fundamental scoring model change |
| Question map keyed by text | Question map keyed by `id` | This phase | Enables question text edits without losing stats |
| Hit rate percentage display | Aproveitamento weight display | This phase | Changes all three views and their controllers |
| Error rate ranking | Lowest-score ranking | This phase | Reinforcement round targets different questions |

**Deprecated/outdated:**
- `StatsData.QuestionStats` — replaced by `QuestionScore`. Delete the old inner class after migration logic is stable.
- `StatsService.getHitRate()` — replaced by `getAproveitamento()`. Keep as package-private for migration if needed, or delete.
- `StatsService.getHighestErrorQuestions()` — replaced by `getLowestScoreQuestions()`. Delete after all callers updated.
- `RoundResult` (no `questionId` field) — replaced by new record with `questionId`. Update all constructors.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Jackson ignores unknown JSON fields by default (so old `answered`/`correct` fields don't break deserialization) | Common Pitfalls, Migration | If Jackson fails on unknown fields, the old stats file won't load and migration breaks. Mitigation: config `mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)` explicitly. |
| A2 | Question IDs use the format `"{theme-abbreviation}-{nn}"` e.g. `rec-01`, `mod-01` | Code Examples | Different ID convention requires updating migration heuristic in `isOldFormat()`. |
| A3 | ThemeLoader's validation (5 options, valid correct index) still applies | Common Pitfalls | If `id` validation is also needed (e.g., no nulls), the loader must check it. |
| A4 | `flashcard-stats.json` is in the project root, not in the user's home directory | Runtime State Inventory | If the file path changes, migration needs path config. Currently hardcoded in `StatsService.STATS_FILE`. |

**If this table is empty:** All claims in this research were verified or cited — no user confirmation needed. (Not empty — see A1–A4 above.)

## Open Questions

1. **What ID format should be used?**
   - What we know: Each question needs a unique string ID within its theme file.
   - What's unclear: Should IDs be descriptive slugs (`"record-intro"`) or numbered (`"rec-01"`)? Both work. Descriptive is more readable in debugging; numbered is easier to auto-generate.
   - Recommendation: Use hyphenated descriptive English slugs like `"record-primary-purpose"`. These are stable, human-readable, and self-documenting in the JSON.

2. **Should ThemeStats keep totalAnswered/totalCorrect after migration?**
   - What we know: These fields exist in the old format and can be preserved.
   - What's unclear: The new scoring model could compute themes' total questions with low score for the same purpose. These fields may be redundant.
   - Recommendation: Keep them during migration for backward compatibility (they're useful for "X questions answered" displays). Remove in a future cleanup phase.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java (java) | Compile & run | ✓ | 25 | — |
| Maven (mvn) | Build & test | — | — | — |

**Missing dependencies with no fallback:**
- Maven: Check `mvn --version` during execution. If unavailable, install via `winget install Apache.Maven` or use `mvnw` (Maven Wrapper). The project needs Maven for compilation (`mvn compile`) and tests (`mvn test`).

**Missing dependencies with fallback:**
- None identified.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit Jupiter 5.11.0 |
| Config file | pom.xml (surefire plugin 3.2.5) |
| Quick run command | `mvn test -pl . -Dtest=StatsServiceTest#testName -DfailIfNoTests=false` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| ADD-ID | Question default constructor sets id to null | unit | `mvn test -Dtest=StatsDataTest#emptyStatsData` | ❌ Wave 0 (existing tests need rewrite) |
| NEG-SCORE | Score floors at -10 after repeated wrong answers | unit | `mvn test -Dtest=StatsServiceTest#scoreFloorsAtMinusTen` | ❌ Wave 0 |
| POS-SCORE | Score caps at +5 after repeated correct answers | unit | `mvn test -Dtest=StatsServiceTest#scoreCapsAtFive` | ❌ Wave 0 |
| APROVEITAMENTO | Aproveitamento returns correct weight sum | unit | `mvn test -Dtest=StatsServiceTest#aproveitamentoCalculation` | ❌ Wave 0 |
| REINFORCEMENT | Reinforcement round selects lowest-score questions | unit | `mvn test -Dtest=RoundStateReinforcementTest#createReinforcementRoundPrioritizesLowestScore` | ❌ Wave 0 (test exists for old behavior) |
| STATS-FILE | Old format migration preserves theme totals | unit | `mvn test -Dtest=StatsServiceTest#migrateOldFormatPreservesThemeTotals` | ❌ Wave 0 |
| STATS-FILE | New format round-trip serialization works | unit | `mvn test -Dtest=StatsDataTest#roundTripSerialization` | ❌ Wave 0 (test exists for old format) |

### Sampling Rate
- **Per task commit:** `mvn test -Dtest=StatsServiceTest#testName` for the specific task being worked
- **Per wave merge:** `mvn test` full suite
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `StatsServiceTest.java` — existing tests use `getHitRate()`, `getHighestErrorQuestions()`, old `QuestionStats`. **Must rewrite entirely.**
- [ ] `StatsDataTest.java` — `roundTripSerialization` test uses old `QuestionStats`. Must update to `QuestionScore`.
- [ ] `RoundStateReinforcementTest.java` — tests use question text in RoundResult. Must update to use ID.

*(If no gaps: "None — existing test infrastructure covers all phase requirements")*

## Security Domain

> This is a desktop swing/JavaFX application with no network, auth, or session management. `security_enforcement: false` (not applicable to a local flashcard app).

### Applicable ASVS Categories — None apply.

| ASVS Category | Applies | Reason |
|---------------|---------|--------|
| V2 Authentication | no | Desktop app, no user authentication |
| V3 Session Management | no | No sessions |
| V4 Access Control | no | Single-user local app |
| V5 Input Validation | partial | Theme JSON loading validates question structure; scoring input is well-typed (enum: correct/wrong) |
| V6 Cryptography | no | No sensitive data at rest or in transit |
| V7 Error Handling | no | Standard try/catch/LOG already in place |
| V8 Data Protection | no | No PII stored |
| V9 Communications | no | No network |

### Known Threat Patterns

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Corrupt stats JSON file on disk | Tampering | Try-catch in `StatsService.load()` — starts fresh on error. Already implemented. |
| Duplicate question IDs in theme JSON | Tampering | Add validation in `ThemeLoader.loadTheme()` — reject file with duplicate IDs. To be added in this phase. |

## Wave Plan for Implementation

### Wave 1: Question identity layer
**Files:** `Question.java`, both theme JSON files, `RoundResult.java`, `ThemeLoader.java`
- Add `id: String` field to `Question.java` (new constructor param, getter/setter)
- Add `id` to every question entry in `themes/java-record.json` and `themes/java-modificadores.json`
- Add ID validation (uniqueness per file) to `ThemeLoader.loadTheme()`
- Add `questionId: String` field to `RoundResult.java` record
- Update `StudyRoundController.handleOptionClick()` to pass `question.getId()` instead of `question.getQuestion()` in RoundResult
- Update test factories to supply IDs

### Wave 2: StatsData and StatsService rewrite
**Files:** `StatsData.java`, `StatsService.java`
- Create `QuestionScore` inner class with `score` field + `recordCorrect()`/`recordWrong()` methods
- Update `ThemeStats` to use `QuestionScore` instead of `QuestionStats`
- Add migration logic to `StatsService.load()` (detect old format, convert, rewrite file)
- Rewrite `recordRound()` to apply score deltas
- Add `getAproveitamento(themeName)` method
- Add `getLowestScoreQuestions(limit)` method  
- Remove or deprecate old methods (`getHitRate`, `getHighestErrorQuestions`, etc.)
- Remove `QuestionStats` inner class

### Wave 3: Controllers and views
**Files:** `ThemeSelectionView.java`, `ThemeSelectionController.java`, `ReportsView.java`, `ReportsController.java`
- Rename `updateHitRate()` to `updateAproveitamento()` in ThemeSelectionView; update label text
- Rename `refreshHitRates()` to `refreshScores()` in ThemeSelectionController
- Update ReportsController to call `getLowestScoreQuestions()` and display scores
- Update ReportsView label "Questões com Maior Taxa de Erro" → "Questões com Menor Pontuação"

### Wave 4: Tests and verification
**Files:** `StatsServiceTest.java`, `StatsDataTest.java`, `RoundStateReinforcementTest.java`
- Rewrite all tests to target new scoring model
- Add tests for: scoring bounds (-10 floor, +5 cap), aproveitamento formula, reinforcement priority by score, migration from old format, empty-stats handling
- Run `mvn test` — all green
- Run full manual walkthrough: play rounds, verify scores persist, verify reinforcement selects low-score questions, verify reports show correct data

## Sources

### Primary (HIGH confidence)
- Codebase inspection of all 15+ Java files and both theme JSON files — verified structure, data flow, serialization
- `flashcard-stats.json` for existing data format — verified field names and structure

### Secondary (MEDIUM confidence)
— (No external documentation needed; this is a codebase-internal refactor)

### Tertiary (LOW confidence)
— (No web-based assumptions about library APIs — Jackson/JavaFX usage already established in repo)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — zero new libraries, all stack verified against pom.xml
- Architecture: HIGH — full codebase read before analysis; all data flows traced
- Pitfalls: HIGH — based on concrete code patterns observed in the repo; cap/floor plateaus and ID mismatch are real failure modes from the current code structure
- Migration strategy: MEDIUM — the exact heuristic for detecting old format (A1) is untested; may need adjustment during implementation

**Research date:** 2026-07-08
**Valid until:** 2026-09-08 (stable — Java 25, Jackson 2.17.2, JavaFX 25 are all GA releases with long support)
