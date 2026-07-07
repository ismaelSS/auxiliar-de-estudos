# Phase 3: Performance Tracking & Reports — Research

**Researched:** 2026-07-07
**Domain:** Java persistence + JavaFX reports display (single-user desktop app)
**Confidence:** HIGH

## Summary

This phase adds performance persistence to the flashcard app by introducing a `StatsService` that reads/writes `flashcard-stats.json` via Jackson, and a reports screen that displays three sections: overall summary, per-theme breakdown, and highest-error question ranking. The architecture cleanly extends the existing MVC pattern — StatsService is a singleton POJO service (like ThemeLoader), the stats models are Jackson-annotated POJOs, and the reports view is a programmatic ScrollPane+VBox layout consistent with existing views. The critical integration point is that `RoundState` needs minor modification to track which theme each question belongs to, and `StudyRoundController` needs to accumulate `RoundResult` records and pass them to `StatsService` on round completion or exit.

**Primary recommendation:** Build StatsService first (models + read/write), then wire it into StudyRoundController for data collection, then build the ReportsView/ReportsController, and finally update ThemeSelectionView hit rates and navigation.

## User Constraints (from CONTEXT.md)

### Locked Decisions
- Stats file: `flashcard-stats.json` in working directory (same as `themes/`)
- Questions keyed by question text (no unique ID — acceptable for single-user app)
- Stats Schema as defined in CONTEXT.md (themes map, per-question answered/correct, overall agg)
- Stats are incremental (append-only updates, never overwrite full history)
- StatsService: singleton or instance passed via Main → controllers
- RoundResult record: `(String themeName, String questionText, boolean wasCorrect)`
- Reports view: programmatic JavaFX scene (no FXML, consistent with Phase 1-2)
- Three report sections: overall summary, per-theme breakdown, highest-error ranking
- "Voltar" button on reports returns to theme selection
- No changes to ThemeLoader or Question/Theme models

### The agent's Discretion
- Choice of VBox vs Grid for per-theme breakdown layout
- Choice of Separator vs TitledPane for section headers
- Number of top-N questions to show (recommend 5-10)
- Whether StatsService handles `load()` in constructor or lazily

### Deferred Ideas (OUT OF SCOPE)
- Spaced repetition algorithm (SM-2) — Phase 2 deferral
- Statistics charts/visualizations — v2 deferred
- Multi-user/authentication — out of scope
- Cloud sync — out of scope
- Question editing UI — out of scope

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| STATS-01 | Performance metrics saved to `flashcard-stats.json` (Jackson, separate file) | Section: Standard Stack (Jackson 2.17.2), Code Examples (Jackson read/write) |
| STATS-02 | Per-theme stats: questions answered, hit rate, per-question answer history | Section: StatsData POJO schema, Hit Rate Calculation |
| STATS-03 | Overall stats aggregated across all themes | Section: StatsData POJO schema (OverallStats) |
| STATS-04 | Identification of questions with highest error rate | Section: Highest-Error Query Pattern |
| REPORT-01 | Report screen showing error/hit summary | Section: Reports Display Architecture |
| REPORT-02 | Report screen showing highest-error questions ranking | Section: Reports Display Architecture, Code Examples |
| REPORT-03 | Report screen showing per-theme performance breakdown | Section: Reports Display Architecture, Code Examples |
| THEME-04 (indirect) | Replace "N/A" stub with real hit rate | Section: ThemeSelectionView Hit Rate Update |

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Stats file read/write | Data Access (StatsService) | — | File I/O separated from UI |
| Stats data model | Model (StatsData POJOs) | — | Plain objects with Jackson annotations |
| Stats aggregation/calculation | Service (StatsService) | — | Business logic for hit rate, ranking |
| Round result collection | Controller (StudyRoundController) | — | Captures per-answer results during round |
| Reports display | View (ReportsView) | Controller (ReportsController) | View renders; controller loads data |
| Reports navigation | Controller (ScreenController) | — | Scene switching via ScreenController |
| ThemeSelection hit rate update | Controller (ThemeSelectionController) | — | Populates labels with StatsService data |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Jackson databind | 2.17.2 | JSON serialization/deserialization | Already in pom.xml, used by ThemeLoader |
| JavaFX Controls | 25 | UI components (ScrollPane, VBox, Label, etc.) | Already in pom.xml, used by all views |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Jackson `@JsonProperty` | — | POJO annotations for serialization | If field names differ from JSON keys, or for explicit mapping |
| Jackson `TypeReference` | — | Deserialize generic collections | Only if reading raw maps; POJO approach avoids this |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Jackson POJO (StatsData) | Jackson `JsonNode` tree model | POJO is type-safe, easier to maintain; tree model is more flexible but loses compile-time safety |
| VBox layout for reports | TableView | TableView is overkill for 3-section layout; VBox gives full control over section headers and styling |
| Per-round file write | Batch writes at app exit | Writing after each round ensures data survives crashes; performance cost is negligible for single-user |

**Installation:**
No new dependencies — Jackson 2.17.2 and JavaFX 25 are already in `pom.xml`.

**Version verification:**
```bash
# Jackson 2.17.2 — confirmed in pom.xml
mvn dependency:tree | findstr jackson
# JavaFX 25 — confirmed in pom.xml
mvn dependency:tree | findstr javafx
```

## Package Legitimacy Audit

No external packages need to be installed for this phase. All dependencies (Jackson databind 2.17.2, JavaFX Controls 25) are already present in `pom.xml` and were verified in Phase 1.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| jackson-databind | Maven Central | 10+ yrs | 100M+/mo | github.com/FasterXML/jackson-databind | OK | Already in pom.xml |
| javafx-controls | Maven Central | 10+ yrs | 5M+/mo | openjfx.io | OK | Already in pom.xml |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                          Main.java                              │
│  Creates: ThemeLoader, ScreenController, StatsService            │
│  Wires StatsService → ThemeSelectionController                   │
│  ThemeSelectionController wires StatsService → StudyRoundCtrl    │
└──────────┬─────────────────────────────────────┬─────────────────┘
           │                                     │
           ▼                                     ▼
┌─────────────────────┐           ┌──────────────────────────┐
│  ThemeSelectionView  │           │   StudyRoundController    │
│  - Theme list + hit  │◄─────────│   - Collects RoundResult  │
│    rates             │  passes   │   - On complete/exit:     │
│  - "Relatorios" btn  │  Stats   │     recordRound(results)  │
└──────────┬──────────┘  Service  └──────────┬───────────────┘
           │                                  │
           │         ┌──────────────────┐      │
           │         │  StatsService     │◄────┘
           │         │  - load()         │
           │         │  - save()         │
           │         │  - recordRound()  │
           │         │  - getThemeStats()│
           │         │  - getOverall()   │
           │         │  - getTopErrors() │
           │         └──────┬───────────┘
           │                │
           ▼                ▼
┌─────────────────────┐  ┌──────────────────────┐
│  ReportsController   │  │  flashcard-stats.json │
│  - Loads data from   │  │  (Jackson serialized) │
│    StatsService      │  │                      │
│  - Populates view    │  │  themes: {            │
└──────────┬──────────┘  │    "matematica": {    │
           │             │      ...              │
           ▼             │    },                 │
┌─────────────────────┐  │    "historia": { ... }│
│  ReportsView         │  │  },                  │
│  - ScrollPane + VBox │  │  overall: { ... }    │
│  - 3 sections        │  └──────────────────────┘
│  - "Voltar" button   │
└─────────────────────┘
```

### Data Flow Per Round

1. StudyRoundController initializes `List<RoundResult>` (empty)
2. Each answer → `roundState.checkAnswer(index)` returns boolean
3. StudyRoundController checks `roundState.getCurrentThemeName()` and `roundState.getCurrentQuestionText()` to build `RoundResult`
4. Appends `new RoundResult(themeName, questionText, wasCorrect)` to list
5. On round complete OR user exits → calls `statsService.recordRound(results)`
6. StatsService updates in-memory StatsData and writes to `flashcard-stats.json`

### Recommended Project Structure

```
src/main/java/org/IsmaelSS/
├── Main.java                                    # +StatsService wiring
├── model/
│   ├── Question.java                            (unchanged)
│   ├── Theme.java                               (unchanged)
│   ├── RoundState.java                          (+themeName in RoundQuestion)
│   ├── RoundResult.java                         (NEW — simple record)
│   └── StatsData.java                           (NEW — root POJO)
│       └── ThemeStats.java                      (NEW — inner or separate)
│       └── QuestionStats.java                   (NEW — inner or separate)
│       └── OverallStats.java                    (NEW — inner or separate)
├── service/
│   ├── ThemeLoader.java                         (unchanged)
│   └── StatsService.java                        (NEW — stats CRUD)
├── view/
│   ├── ThemeSelectionView.java                  (+updateHitRate, +"Relatorios" btn)
│   ├── StudyRoundView.java                      (unchanged)
│   └── ReportsView.java                         (NEW — scrollable 3-section layout)
└── controller/
    ├── ScreenController.java                    (unchanged)
    ├── ThemeSelectionController.java             (+StatsService, +report navigation)
    ├── StudyRoundController.java                (+RoundResult collection, +StatsService)
    └── ReportsController.java                   (NEW — data loading + nav)
```

### Pattern 1: StatsData POJO Hierarchy (Jackson-serializable)

**What:** Nested POJOs representing the `flashcard-stats.json` structure. Jackson's default serialization handles the `Map<String, ThemeStats>` pattern natively — no custom deserializers needed.

**When to use:** For the root stats data structure. Inner classes in StatsData.java keep the model cohesive.

**Example:**

```java
// StatsData.java — root POJO
public class StatsData {
    private Map<String, ThemeStats> themes = new HashMap<>();
    private OverallStats overall = new OverallStats();

    // Jackson: public no-arg constructor, getters/setters
    public StatsData() {}

    public Map<String, ThemeStats> getThemes() { return themes; }
    public void setThemes(Map<String, ThemeStats> themes) { this.themes = themes; }
    public OverallStats getOverall() { return overall; }
    public void setOverall(OverallStats overall) { this.overall = overall; }

    public static class ThemeStats {
        private int totalAnswered;
        private int totalCorrect;
        private Map<String, QuestionStats> questions = new HashMap<>();

        public ThemeStats() {}
        // getters/setters...
    }

    public static class QuestionStats {
        private int answered;
        private int correct;

        public QuestionStats() {}
        // getters/setters...
    }

    public static class OverallStats {
        private int totalAnswered;
        private int totalCorrect;

        public OverallStats() {}
        // getters/setters...
    }
}
```

### Pattern 2: RoundResult Record

**What:** Lightweight data carrier for a single answer attempt. Used by StudyRoundController to pass results to StatsService.

**When to use:** When transferring per-answer data from controller to service.

**Example:**

```java
// RoundResult.java
public record RoundResult(String themeName, String questionText, boolean wasCorrect) {}
```

### Pattern 3: Highest-Error Query

**What:** Compute error rate per question across all themes, sort descending, return top N.

**When to use:** For the highest-error questions report section (REPORT-02).

**Example:**

```java
public List<Map.Entry<String, Double>> getHighestErrorQuestions(int limit) {
    List<Map.Entry<String, Double>> entries = new ArrayList<>();
    for (var themeEntry : data.getThemes().entrySet()) {
        for (var questionEntry : themeEntry.getValue().getQuestions().entrySet()) {
            QuestionStats qs = questionEntry.getValue();
            if (qs.getAnswered() == 0) continue; // skip unanswered
            double errorRate = 1.0 - ((double) qs.getCorrect() / qs.getAnswered());
            entries.add(Map.entry(questionEntry.getKey(), errorRate));
        }
    }
    entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
    return entries.subList(0, Math.min(limit, entries.size()));
}
```

### Anti-Patterns to Avoid

- **Storing question index instead of text as key:** Index-based keys break when JSON files are modified (questions added/removed). Question text is stable enough for single-user.
- **Reading stats file before every write:** Read once at startup, keep in memory, write after each round. Single-user app means no concurrent access concerns.
- **Using TableView for the reports:** TableView adds complexity (cell factories, selection model) for a simple data display. VBox with styled Labels is sufficient and consistent with existing views.
- **Mixing stats models with business logic:** Keep StatsData as pure POJOs. StatsService handles all aggregation and ranking logic.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JSON serialization | Custom JSON writer | Jackson ObjectMapper | Edge cases: nested maps, pretty print, error handling — Jackson has 10+ years of hardening |
| Scene navigation | Manual Stage.setScene tracking | ScreenController (already exists) | Already built in Phase 1; just register new scenes |
| Answer feedback delay | Thread.sleep() | PauseTransition (already in use) | Already implemented in Phase 2; blocks UI thread if done wrong |
| File path resolution | Hardcoded paths | `System.getProperty("user.dir")` + File.separator | Already used by ThemeLoader; consistent pattern |

**Key insight:** This phase involves composing existing patterns (Jackson POJO mapping, programmatic JavaFX views, ScreenController navigation) that are already established in Phases 1-2. The only genuinely new pattern is the StatsService aggregation logic (hit rate, error ranking) — and that is simple arithmetic, not framework-specific complexity.

## Runtime State Inventory

> **Not a rename/refactor phase.** Runtime State Inventory is not applicable. The stats file (`flashcard-stats.json`) does not exist yet — this phase creates it.

## Common Pitfalls

### Pitfall 1: Question Text as Key — Collisions
**What goes wrong:** If two questions in different themes (or the same theme) have identical text, stats merge into a single entry. For a single-user app this is acceptable (duplicate questions are rare), but it means stats are shared.
**Why it happens:** Questions are keyed by text, not by ID, per the architecture decision.
**How to avoid:** Accept this behavior. If it becomes problematic in the future, introduce a UUID per question, but that would require changes to the JSON schema and is outside scope.
**Warning signs:** Two questions with identical text show the same stats.

### Pitfall 2: Stats File Not Found on First Run
**What goes wrong:** When the app starts for the first time, `flashcard-stats.json` does not exist. StatsService.load() throws FileNotFoundException or returns null.
**Why it happens:** The file is created only after the first round completes.
**How to avoid:** Check `new File("flashcard-stats.json").exists()` before reading. If not found, start with an empty StatsData (all counters at 0, empty maps).
**Warning signs:** App crashes on first launch with file-not-found error.

### Pitfall 3: Corrupted Stats File
**What goes wrong:** If the JSON file is manually edited and becomes invalid JSON, or if a write operation is interrupted, Jackson throws an exception on the next read.
**Why it happens:** The file is human-editable, so users might modify it incorrectly. File write could be interrupted (power loss, etc.).
**How to avoid:** Wrap `mapper.readValue()` in try-catch. On IOException or JsonProcessingException, log a warning and start fresh with empty StatsData. Optionally back up the corrupted file as `flashcard-stats.json.bak`.
**Warning signs:** Jackson parse exceptions at startup.

### Pitfall 4: Forgetting to Record Stats on Exit
**What goes wrong:** If the user exits the round via the "Sair" button, their partial progress is lost because `statsService.recordRound()` is not called.
**Why it happens:** The "Sair" handler in StudyRoundController currently just switches scenes.
**How to avoid:** Call `statsService.recordRound(results)` in the exit handler (before switching scenes), just like the round-complete path. The results list will contain whatever answers were given so far.
**Warning signs:** Stats show fewer answers than expected after exiting mid-round.

### Pitfall 5: Hit Rate Division by Zero
**What goes wrong:** If a theme has 0 answers, computing `correct / answered` throws ArithmeticException.
**Why it happens:** New themes or themes no one has studied yet have no stats.
**How to avoid:** Check `totalAnswered == 0` before computing hit rate. Return "N/A" or display `—` instead of a percentage. This is the same pattern used by the current "Hit rate: N/A" stub.
**Warning signs:** NaN or Infinity displayed in hit rates.

## Code Examples

### Jackson Read/Write — StatsService

```java
// Source: Standard Jackson pattern (confirmed via ThemeLoader in Phase 1)
// and official Jackson javadoc for ObjectMapper.writerWithDefaultPrettyPrinter()

public class StatsService {
    private static final String STATS_FILE = System.getProperty("user.dir")
            + File.separator + "flashcard-stats.json";
    private final ObjectMapper mapper;
    private StatsData data;

    public StatsService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT); // pretty-print
        this.data = load();
    }

    private StatsData load() {
        File file = new File(STATS_FILE);
        if (!file.exists()) {
            return new StatsData(); // fresh start
        }
        try {
            return mapper.readValue(file, StatsData.class);
        } catch (IOException e) {
            Logger.getLogger(getClass()).warning(
                "Could not read stats file, starting fresh: " + e.getMessage());
            return new StatsData();
        }
    }

    private void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                  .writeValue(new File(STATS_FILE), data);
        } catch (IOException e) {
            Logger.getLogger(getClass()).severe(
                "Failed to save stats: " + e.getMessage());
        }
    }

    public void recordRound(List<RoundResult> results) {
        for (RoundResult result : results) {
            recordSingleAnswer(result);
        }
        // Update overall stats
        recalculateOverall();
        save();
    }

    private void recordSingleAnswer(RoundResult result) {
        // Get or create ThemeStats
        ThemeStats themeStats = data.getThemes()
            .computeIfAbsent(result.themeName(), k -> new ThemeStats());

        // Update theme totals
        themeStats.setTotalAnswered(themeStats.getTotalAnswered() + 1);
        if (result.wasCorrect()) {
            themeStats.setTotalCorrect(themeStats.getTotalCorrect() + 1);
        }

        // Update per-question stats
        QuestionStats qStats = themeStats.getQuestions()
            .computeIfAbsent(result.questionText(), k -> new QuestionStats());
        qStats.setAnswered(qStats.getAnswered() + 1);
        if (result.wasCorrect()) {
            qStats.setCorrect(qStats.getCorrect() + 1);
        }
    }

    private void recalculateOverall() {
        int totalAnswered = 0;
        int totalCorrect = 0;
        for (ThemeStats ts : data.getThemes().values()) {
            totalAnswered += ts.getTotalAnswered();
            totalCorrect += ts.getTotalCorrect();
        }
        data.getOverall().setTotalAnswered(totalAnswered);
        data.getOverall().setTotalCorrect(totalCorrect);
    }

    public String getHitRate(String themeName) {
        ThemeStats ts = data.getThemes().get(themeName);
        if (ts == null || ts.getTotalAnswered() == 0) return "N/A";
        double rate = (double) ts.getTotalCorrect() / ts.getTotalAnswered() * 100;
        return String.format("%.0f%%", rate);
    }

    public String getOverallHitRate() {
        OverallStats o = data.getOverall();
        if (o.getTotalAnswered() == 0) return "N/A";
        double rate = (double) o.getTotalCorrect() / o.getTotalAnswered() * 100;
        return String.format("%.0f%%", rate);
    }

    public List<Map.Entry<String, Double>> getHighestErrorQuestions(int limit) {
        // See Pattern 3 above
    }
}
```

### RoundState Modification — Track Theme Per Question

```java
// In RoundState.java, modify the RoundQuestion inner class:
private static class RoundQuestion {
    final String themeName;        // NEW — which theme this question comes from
    final Question question;
    final List<String> shuffledOptions;
    final int correctIndex;

    RoundQuestion(String themeName, Question question,
                  List<String> shuffledOptions, int correctIndex) {
        this.themeName = themeName;
        this.question = question;
        this.shuffledOptions = shuffledOptions;
        this.correctIndex = correctIndex;
    }
}

// Add getter:
public String getCurrentThemeName() {
    return roundQuestions.get(currentIndex).themeName;
}

// Update constructor to pass theme name:
for (Theme theme : themes) {
    for (Question q : theme.getQuestions().subList(0, take)) {
        // ...shuffle logic...
        roundQuestions.add(new RoundQuestion(
            theme.getName(), q, shuffled, newCorrect));
    }
}
```

### StudyRoundController — Collect Results

```java
// In StudyRoundController:
public class StudyRoundController {
    // ...existing fields...
    private final List<RoundResult> results = new ArrayList<>();
    private final StatsService statsService;  // NEW
    private final String sourceThemeName;     // pass from ThemeSelectionController

    // In handleOptionClick, after roundState.checkAnswer(index):
    results.add(new RoundResult(
        roundState.getCurrentThemeName(),
        roundState.getCurrentQuestion().getQuestion(),
        correct
    ));

    // In showRoundComplete, before showing completion:
    statsService.recordRound(results);

    // In handleExit, before switching:
    if (!results.isEmpty()) {
        statsService.recordRound(results);
    }
}
```

### ReportsView — Scrollable Three-Section Layout

```java
// Source: JavaFX ScrollPane official docs + VBox layout pattern
// confirmed via openjfx.io/javadoc/23/javafx.controls/javafx/scene/control/ScrollPane.html

public class ReportsView {
    private final Scene scene;
    private final VBox root;
    private final Button voltarButton;

    public ReportsView() {
        // Content container
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label title = new Label("Relatórios de Desempenho");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        content.getChildren().add(title);

        // Section 1: Overall Summary
        Label section1 = new Label("Resumo Geral");
        section1.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        content.getChildren().addAll(section1, new Separator());

        VBox overallBox = new VBox(5);
        overallBox.setPadding(new Insets(5, 0, 10, 10));
        // populated by controller
        content.getChildren().add(overallBox);

        // Section 2: Per-Theme Breakdown
        Label section2 = new Label("Desempenho por Tema");
        section2.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        content.getChildren().addAll(section2, new Separator());

        VBox themeBox = new VBox(5);
        themeBox.setPadding(new Insets(5, 0, 10, 10));
        // populated by controller
        content.getChildren().add(themeBox);

        // Section 3: Highest-Error Questions
        Label section3 = new Label("Questões com Maior Taxa de Erro");
        section3.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        content.getChildren().addAll(section3, new Separator());

        VBox errorBox = new VBox(5);
        errorBox.setPadding(new Insets(5, 0, 10, 10));
        // populated by controller
        content.getChildren().add(errorBox);

        // Back button
        voltarButton = new Button("Voltar");
        content.getChildren().add(voltarButton);

        // Wrap in ScrollPane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root = new VBox(scrollPane);
        scene = new Scene(root, 600, 500);
    }

    // Controller populates these containers:
    public VBox getOverallBox() { ... }
    public VBox getThemeBox() { ... }
    public VBox getErrorBox() { ... }
    public Button getVoltarButton() { ... }
    public Scene getScene() { ... }
}
```

### ThemeSelectionView — Update Hit Rate Method

```java
// In ThemeSelectionView.java, add:
public void updateHitRate(String themeName, String hitRateText) {
    Label info = themeLabels.get(themeName);
    if (info != null) {
        Theme theme = themeMap.get(themeName);
        String count = theme != null ? " (" + theme.getQuestionCount() + " perguntas)" : "";
        info.setText(themeName + count + " — Hit rate: " + hitRateText);
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Hit rate "N/A" stub | Real hit rate from StatsService | Phase 3 | THEME-04 fulfilled |
| No stats persistence | `flashcard-stats.json` with Jackson | Phase 3 | STATS-01 through STATS-04 fulfilled |
| No reports screen | Three-section reports (ScrollPane+VBox) | Phase 3 | REPORT-01 through REPORT-03 fulfilled |

**Deprecated/outdated:**
- "N/A" hit rate display: Replaced by real data from StatsService in Phase 3

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Jackson's default deserialization handles `Map<String, ThemeStats>` correctly when `ThemeStats` is an inner static class | Architecture Patterns | Low — standard Jackson behavior for POJOs with no-arg ctors and getters/setters |
| A2 | Using question text as a HashMap key is stable enough for a single-user app | Common Pitfalls | Low — edge case of identical text causing merged stats is acceptable |
| A3 | Wrapping VBox in ScrollPane with `setFitToWidth(true)` provides adequate reports UI | Code Examples | Low — well-documented JavaFX pattern (confirmed via openjfx.io and StackOverflow) |

**If this table is empty:** All claims in this research were verified or cited — no user confirmation needed.

## Open Questions

1. **Should StatsService record partial rounds on exit?**
   - What we know: StudyRoundController.handleExit() currently drops all answers.
   - What's unclear: Should answers accumulated before "Sair" be saved?
   - Recommendation: **Yes, save them.** Any answer given is useful data. The exit handler should call `statsService.recordRound(results)` exactly like the completion path.

2. **How many top-error questions to display?**
   - What we know: The highest-error ranking should show the worst performers.
   - What's unclear: Fixed number (e.g., 5) or dynamic (all with >0 answers)?
   - Recommendation: **Show top 5** to keep the reports view compact. The controller sets the limit; view just renders what it receives. If the data set has fewer than 5, show all.

3. **Should Section 2 (per-theme) show all themes or only themes with stats?**
   - What we know: Themes without any answers have zero stats.
   - What's unclear: Should unreviewed themes appear with "N/A" or be hidden entirely?
   - Recommendation: **Show all discovered themes** with "N/A" for unanswered ones. This gives the user a complete picture of what they have (and haven't) studied.

## Environment Availability

> Skip this section if the phase has no external dependencies (code/config-only changes).

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 25 | Java compilation | ✓ | 25+36-3489 | — |
| Maven 3.9.16 | Build tool | ✓ | 3.9.16 | — |
| Jackson databind 2.17.2 | JSON serialization | ✓ (in pom.xml) | 2.17.2 | — |
| JavaFX 25 | UI framework | ✓ (in pom.xml) | 25 | — |

**Missing dependencies with no fallback:** none
**Missing dependencies with fallback:** none

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 (not yet configured — Wave 0 gap) |
| Config file | none |
| Quick run command | `mvn test` |
| Full suite command | `mvn verify` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| STATS-01 | Stats written/read to flashcard-stats.json | unit | — | ❌ Wave 0 |
| STATS-02 | Per-theme stats correct after recording | unit | — | ❌ Wave 0 |
| STATS-03 | Overall stats aggregated correctly | unit | — | ❌ Wave 0 |
| STATS-04 | Highest-error questions sorted correctly | unit | — | ❌ Wave 0 |
| REPORT-01/02/03 | View renders with data | manual | — | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** Run `mvn clean compile` to verify compilation
- **Per wave merge:** Full `mvn clean package` (compilation + packaging)
- **Phase gate:** `mvn clean package` green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `pom.xml` — add JUnit 5 dependency (if not already present)
- [ ] `src/test/java/org/IsmaelSS/service/StatsServiceTest.java` — unit tests for StatsService
- [ ] `src/test/java/org/IsmaelSS/model/StatsDataTest.java` — Jackson serialization round-trip test

*(JUnit 5 needs to be added to pom.xml; verify if any tests exist from Phases 1-2)*

## Security Domain

> `security_enforcement` is not explicitly set in config.json (absent = enabled by default), but this is a single-user desktop app with no network, no database, and no user input beyond clicking UI buttons.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | Single-user desktop app |
| V3 Session Management | no | No sessions |
| V4 Access Control | no | Single user |
| V5 Input Validation | no | Stats file read via Jackson (structured deserialization — no arbitrary user input) |
| V6 Cryptography | no | No sensitive data |
| V8 Data Protection | no | No PII or secrets stored |

**Justification:** The stats file contains only study performance data (question text, answer counts). No authentication, no network access, no PII. The only security concern is that `flashcard-stats.json` could be corrupted by external edits — handled by graceful degradation (start fresh on parse failure).

## Sources

### Primary (HIGH confidence)
- [VERIFIED: pom.xml] Jackson 2.17.2, JavaFX 25, Maven config — confirmed in project files
- [VERIFIED: codebase] ThemeLoader.java — Jackson read pattern already established
- [VERIFIED: codebase] StudyRoundController.java, RoundState.java — existing round engine integration points
- [VERIFIED: codebase] ScreenController.java, ThemeSelectionView.java — existing navigation/view patterns
- [CITED: openjfx.io/javadoc/23/javafx.controls/] ScrollPane, VBox, Separator official API docs
- [CITED: baeldung.com/java-json-pretty-print] Jackson pretty-print pattern

### Secondary (MEDIUM confidence)
- [CITED: stackoverflow.com/questions/34444408] Jackson Map<String, POJO> deserialization pattern
- [CITED: coderscratchpad.com/creating-scrollable-content-with-javafx-scrollpane] ScrollPane+VBox pattern

### Tertiary (LOW confidence)
- None — all key claims are verified against the codebase or official documentation

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — Jackson 2.17.2 and JavaFX 25 confirmed in pom.xml
- Architecture: HIGH — all patterns (MVC, ScreenController, programmatic views) confirmed in Phases 1-2
- Pitfalls: HIGH — based on well-known Jackson and JavaFX edge cases
- Environment: HIGH — JDK 25, Maven 3.9.16 verified via command output

**Research date:** 2026-07-07
**Valid until:** 2026-08-07 (30 days — stable stack, no fast-moving dependencies)

## RESEARCH COMPLETE

**Phase:** 3 — Performance Tracking & Reports
**Confidence:** HIGH

### Key Findings
1. **StatsService is the core new component** — handles load/save/recordRound/query operations using Jackson POJO serialization. No new dependencies needed.
2. **RoundState requires minor modification** — add `themeName` to internal `RoundQuestion` so StudyRoundController can build `RoundResult` records that include the originating theme.
3. **ReportsView uses VBox inside ScrollPane** — three sections (overall, per-theme, highest-error) styled with bold headers and Separators. Consistent with existing programmatic SceneGraph approach.
4. **Hit rate edges cases are safe** — "N/A" for 0-answered themes prevents division by zero; graceful degradation on corrupted/missing stats file.
5. **StatsService must be wired through Main.java** — created once, passed to ThemeSelectionController, then to StudyRoundController. Both exit and round-complete paths must call `recordRound()`.

### File Created
`.planning/phases/03-performance-tracking-reports/03-RESEARCH.md`

### Confidence Assessment
| Area | Level | Reason |
|------|-------|--------|
| Standard Stack | HIGH | All dependencies confirmed in pom.xml and project files |
| Architecture | HIGH | MVC pattern, programmatic views, ScreenController — all verified from Phases 1-2 code |
| Pitfalls | HIGH | Division by zero, file-not-found, corrupted JSON — well-known edge cases with standard mitigations |

### Open Questions
- [Answered above] Should partial rounds be saved on exit? **Yes.**
- [Answered above] How many top-error questions? **Top 5.**
- [Answered above] Should unreviewed themes appear in reports? **Yes, with "N/A".**

### Ready for Planning
Research complete. Planner can now create PLAN.md files. The implementation order is:
1. StatsData model + StatsService (load/save/record)
2. RoundState modification (theme tracking)
3. StudyRoundController integration (RoundResult collection)
4. ReportsView + ReportsController
5. ThemeSelectionView updates (hit rate + navigation)
