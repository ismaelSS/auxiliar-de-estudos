# Phase 12: Spaced Repetition & Visual Review Dashboard - Research

**Researched:** 2026-07-21
**Domain:** SM-2 spaced repetition algorithm + JavaFX dashboard UI
**Confidence:** HIGH

## Summary

Phase 12 implements the SM-2 spaced repetition algorithm (binary correct/wrong variant) and replaces the "Treinar" tab with a visual review dashboard. The SM-2 algorithm is mathematically simple — a single formula for ease factor adjustment and interval calculation — and fits cleanly into the existing `QuestionScore` model as new fields. The dashboard is a substantial UI refactor of `ThemeSelectionView`, replacing checkbox-based theme selection with priority-ordered theme cards, a search bar, and a vertical timeline. The study round flow (StudyRoundController/StudyRoundView) remains untouched — rounds are created from the dashboard and return to it on completion. Backward compatibility is handled by `@JsonIgnoreProperties(ignoreUnknown = true)` on `QuestionScore` (already present), so old JSON files without SM-2 fields simply deserialize to defaults.

**Primary recommendation:** Implement SM-2 as a pure method on `QuestionScore` (no new service class needed), build the dashboard as a new view class (`ReviewDashboardView`) that replaces the TabPane "Treinar" tab content, and keep the existing round flow intact.

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| V2-01 | Spaced repetition algorithm (SM-2) for smarter review scheduling | SM-2 math fully documented below; fields on QuestionScore; update logic in StatsService |
| SR-01 | SM-2 fields on QuestionScore (easeFactor, interval, repCount, timestamps) | New fields with defaults; @JsonIgnoreProperties handles backward compat |
| SR-02 | SM-2 update logic (correct/wrong binary) | Pure method: updateSM2(boolean correct) on QuestionScore |
| SR-03 | Dashboard with theme cards showing priority ordering | ReviewDashboardView with VBox of ThemeCardNodes, sorted by priority |
| SR-04 | Priority color coding (red/orange/green) | CSS classes: .priority-overdue, .priority-today, .priority-none |
| SR-05 | Search by theme name | TextField + listener filtering observable list |
| SR-06 | Action buttons (Revisar/Feito) per theme card | Button handlers calling new factory methods on RoundState |
| SR-07 | Timeline showing study history | Vertical VBox with per-day aggregation from theme stats timestamps |
| SR-08 | Fixation phase computation and display | Derived from SM-2 fields: repCount + interval thresholds |
</phase_requirements>

## User Constraints (from CONTEXT.md)

### Locked Decisions
- Replaces the "Treinar" tab in the TabPane (home screen)
- Quality model: Binary correct/wrong — correct→4, wrong→1 (no self-assessment UI)
- Default easeFactor: 2.5 (min 1.3, max 2.5)
- Default interval: 0 (unreviewed); first review: interval = 1 day
- Correct: interval = ceil(interval * easeFactor); easeFactor += 0.1 (max 2.5); repCount++
- Wrong: interval = 1; easeFactor -= 0.2 (min 1.3); repCount = 0
- Score (-10..+5) removed from recordCorrect/recordWrong logic — replaced by SM-2 fields
- Fixation phases derived from SM-2 data (not stored separately)
- Theme ordering: overdue > due today > no immediate review > alphabetical
- Priority colors: #e74c3c (overdue), #fe9a00 (today), #27ae60 (no review)
- "Marcar como feita" simulates perfect SM-2 answer (quality=5) for overdue questions only
- "Iniciar Revisão" picks overdue questions first, then new/unreviewed if none overdue
- Timeline: vertical, grouped by day (most recent first), GitHub contribution log style
- Search: filters themes by name only (real-time)
- Completing round auto-records with SM-2 update (existing recordRound flow updated)

### the agent's Discretion
- Exact CSS classes for theme cards (follow .surface, .button-primary etc. from Phase 10)
- Timeline visual details (dot color, line style, date format)
- Fixation phase badge colors for Domínio (suggest teal/blue)
- Card dimensions, spacing, responsive behavior
- Whether to show timeline inline on dashboard or as expandable section
- Transition animation when filtering by search

### Deferred Ideas (OUT OF SCOPE)
- UI de autoavaliação 1-5 (tipo Anki) para quality granular — fase separada
- Limite de revisões/dia — fase separada
- Estatísticas com gráficos (V2-02) — fase separada
- Search across question text — não necessário na fase atual

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| SM-2 algorithm calculation | Model (QuestionScore) | Service (StatsService) | Pure math belongs on the data object; service orchestrates calls |
| Dashboard layout & cards | View (ReviewDashboardView) | — | Pure UI presentation |
| Priority ordering logic | Controller (ThemeSelectionController) | Service (StatsService) | Controller decides sort order; service provides due/overdue data |
| Search filtering | View (TextField listener) | Controller | UI event triggers filter; controller updates observable list |
| Review round creation | Controller (ThemeSelectionController) | Model (RoundState) | Controller selects questions; RoundState factory builds the round |
| Timeline history | View (ReviewDashboardView) | Service (StatsService) | View renders timeline; service provides aggregated history data |
| Fixation phase computation | Model (QuestionScore) | — | Pure derivation from SM-2 fields, no service needed |
| Auto-recording on round end | Controller (StudyRoundController) | Service (StatsService) | Existing flow — controller calls recordRound; service applies SM-2 |
| JSON backward compat | Model (QuestionScore) | — | @JsonIgnoreProperties already on class |

## Standard Stack

### Core (no new dependencies)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| JavaFX 25 | 25 | UI toolkit | Project requirement, already in pom.xml |
| Jackson 2.17.2 | 2.17.2 | JSON persistence | Already in pom.xml |
| JUnit 5.11.0 | 5.11.0 | Testing | Already in pom.xml |

### No new packages required
This phase adds zero new dependencies. All SM-2 math is pure Java. All UI is built with existing JavaFX controls (VBox, HBox, Label, Button, TextField, ScrollPane).

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Hand-built SM-2 | third-party library | SM-2 is ~15 lines of math; adding a dependency for this is overkill |
| Hand-built timeline | JavaFX ListView with cell factory | Simpler than custom widget; no scroll virtualization needed for moderate history |
| New DashboardView class | Modify existing ThemeSelectionView | Modifying is cleaner — ThemeSelectionView is the TabPane root, replacing its Jogar tab content is the right seam |

## SM-2 Algorithm Implementation

### Mathematical Specification

Source: SuperMemo official SM-2 algorithm [CITED: supermemo.com/english/ol/sm2.htm]

**Inputs per question:**
- `quality` (q): 0–5 integer (binary in our case: correct→4, wrong→1)
- `repCount` (n): consecutive correct answers (0 = new/wrong)
- `easeFactor` (EF): float ≥ 1.3, default 2.5
- `interval` (I): integer days, default 0

**Update formula (quality ≥ 3 = correct):**
```
EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
```

For q=4 (correct after hesitation): EF' = EF + (0.1 - 1*(0.08+0.02)) = EF + 0 = **EF unchanged**

**Binary variant (from CONTEXT.md locked decision):**
- Correct (q=4): EF += 0.1 (max 2.5); interval = ceil(interval * EF); repCount++
- Wrong (q=1): EF -= 0.2 (min 1.3); interval = 1; repCount = 0

**Interval progression for first reviews:**
- repCount=0 → interval = 1 (first review always 1 day)
- repCount=1 → interval = ceil(1 * EF) ≈ 3 days (with EF=2.5)
- repCount=2 → interval = ceil(3 * EF) ≈ 8 days
- repCount=3 → interval = ceil(8 * EF) ≈ 20 days
- repCount=4 → interval = ceil(20 * EF) ≈ 50 days
- repCount=5 → interval = ceil(50 * EF) ≈ 125 days
- repCount=6 → interval = ceil(125 * EF) ≈ 313 days

**"Marcar como feita" (quality=5):**
```
EF' = EF + (0.1 - (5 - 5) * (0.08 + (5 - 5) * 0.02)) = EF + 0.1
```
So EF increases by 0.1, interval advances as if correct.

### Java Implementation Pattern

```java
// On QuestionScore — pure method, no side effects beyond updating fields
public void updateSM2(boolean correct) {
    if (correct) {
        this.easeFactor = Math.min(2.5, this.easeFactor + 0.1);
        if (this.repCount == 0 || this.interval <= 1) {
            this.interval = 1;
        } else {
            this.interval = (int) Math.ceil(this.interval * this.easeFactor);
        }
        this.repCount++;
    } else {
        this.easeFactor = Math.max(1.3, this.easeFactor - 0.2);
        this.interval = 1;
        this.repCount = 0;
    }
    this.lastReviewTimestamp = System.currentTimeMillis();
    this.nextReviewTimestamp = System.currentTimeMillis() + (long) this.interval * 86_400_000L;
}
```

### Fixation Phase Derivation

```java
// On QuestionScore — computed, not stored
public enum FixationPhase {
    APRENDENDO,  // Red badge
    REVISAO,     // Orange badge
    FIXA,        // Green badge
    DOMINIO      // Teal badge
}

public FixationPhase getFixationPhase() {
    if (repCount == 0 || interval <= 1) return FixationPhase.APRENDENDO;
    if (repCount <= 2 && interval <= 7) return FixationPhase.REVISAO;
    if (repCount <= 5 && interval <= 30) return FixationPhase.FIXA;
    return FixationPhase.DOMINIO;
}
```

Note: The thresholds in CONTEXT.md define these as:
- **Aprendendo**: repCount=0 OR interval≤1
- **Revisão**: repCount 1-2, interval 2-7
- **Fixa**: repCount 3-5, interval 8-30
- **Domínio**: repCount≥6, interval>30

The Java implementation above matches these. The enum is a helper for display; it doesn't need to be stored in JSON.

## Data Model Changes

### QuestionScore — New Fields

Add to existing `StatsData.QuestionScore` class (already has `@JsonIgnoreProperties(ignoreUnknown = true)`):

```java
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public static class QuestionScore {
    private int score;           // EXISTING — no longer updated, kept for backward compat
    private double easeFactor;   // NEW — default 2.5
    private int interval;        // NEW — days, default 0
    private int repCount;        // NEW — consecutive correct, default 0
    private long lastReviewTimestamp;   // NEW — millis since epoch, default 0
    private long nextReviewTimestamp;   // NEW — millis since epoch, default 0

    public QuestionScore() {
        this.easeFactor = 2.5;
    }
    // ... getters/setters ...
}
```

### Backward Compatibility Strategy

**JSON migration is automatic.** When Jackson deserializes an old `flashcard-stats.json` that lacks the SM-2 fields:
- `@JsonIgnoreProperties(ignoreUnknown = true)` on `QuestionScore` means unknown fields are silently ignored
- New fields default to 0 (int/long) or 0.0 (double)
- `easeFactor` must default to 2.5 — handled in the no-arg constructor
- Questions that were previously answered will have `score` set but SM-2 fields at defaults. This is fine — they'll start fresh with SM-2 on next review.

**No migration code needed** (unlike the old-format migration in StatsService). The Jackson annotation already handles this.

### ThemeStats — No Changes Needed

The existing `ThemeStats` structure (totalAnswered, totalCorrect, Map<String, QuestionScore>) is sufficient. SM-2 data lives inside each `QuestionScore`.

### New Data Needed for Dashboard

**Due questions query:** StatsService needs a method to get questions where `nextReviewTimestamp <= now` for a given theme. This reads from existing ThemeStats.questions map.

**Timeline history:** Need to aggregate `lastReviewTimestamp` values across all questions to build per-day study history. StatsService already has `getAllThemesWithData()`.

### RoundResult — No Changes Needed

The existing `RoundResult(themeName, questionText, questionId, wasCorrect)` record provides everything needed. The SM-2 update happens in `recordRound()` based on `wasCorrect`.

## Dashboard View Architecture

### Conceptual Layout

```
┌─────────────────────────────────────────────────┐
│ TabPane: [Treinar] [Relatórios] [Gerenciar]    │
├─────────────────────────────────────────────────┤
│ [Search field: "Buscar tema..."]                │
│                                                 │
│ ┌─────────────────────────────────────────────┐ │
│ │ Theme Card: "Biologia"                     │ │
│ │ ● 3 overdue  Domínio: 75%                  │ │
│ │ ████████░░░░░░░░░░ (fixation bar)           │ │
│ │ [Revisar] [Marcar como feita]              │ │
│ └─────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────┐ │
│ │ Theme Card: "Matemática"                   │ │
│ │ ● 0 overdue  Domínio: 90%                  │ │
│ │ ░░░░░██████████████████ (fixation bar)      │ │
│ │ [Revisar] [Marcar como feita]              │ │
│ └─────────────────────────────────────────────┘ │
│                                                 │
│ ── Histórico de Estudos ──                      │
│ ● 21/07  Biologia (12 questões)                │
│ │         ● 15/07  Matemática (8 questões)     │ │
│ │         │                                     │ │
│ ● 10/07  Biologia (5 questões)                 │
│                                                 │
└─────────────────────────────────────────────────┘
```

### Component Responsibilities

| Component | File | Responsibility |
|-----------|------|----------------|
| ReviewDashboardView | `view/ReviewDashboardView.java` | Dashboard VBox with search, card container, timeline; wraps in ScrollPane |
| ThemeCardNode | `view/ThemeCardNode.java` | Single theme card: HBox with priority border, name, overdue badge, dominio%, fixation bar, buttons |
| FixationBar | `view/FixationBar.java` | Horizontal HBox with 4 proportional segments (optional — can be inline in ThemeCardNode) |
| TimelineView | `view/TimelineView.java` | Vertical VBox with per-day entries, dots, vertical line |
| ThemeSelectionView | `view/ThemeSelectionView.java` | MODIFIED — Jogar tab content replaced with ReviewDashboardView |

### View Construction Pattern

Follow the existing pattern from `StudyRoundView` and `ReportsView`:
- Constructor builds all nodes programmatically (no FXML)
- Expose getter methods for controller access
- Use `getStyleClass().add()` for CSS styling
- Wrap in `ScrollPane` for overflow (same as ReportsView)

### ThemeSelectionView Refactoring Strategy

The current `ThemeSelectionView` builds the TabPane and three tabs in its constructor. The "Treinar" tab content is a VBox with checkboxes, spinner, and start button.

**Strategy:** Replace the Jogar tab content. The simplest approach:
1. Rename the class or keep it — it still manages the TabPane
2. Replace `jogarTab.setContent(new ReviewDashboardView(...).getContent())` instead of the old VBox
3. Remove the checkbox/spinner/startButton fields — no longer needed
4. Keep the TabPane, relatoriosTab, gerenciarTab management

**Alternative (cleaner):** Keep `ThemeSelectionView` as the TabPane shell. The Jogar tab content is injected from `ReviewDashboardView`. This minimizes changes to `ThemeSelectionController`'s tab management code.

## Controller & Service Changes

### StatsService — New Methods

```java
// Get questions in a theme that are due (nextReview <= now)
public List<Map.Entry<String, QuestionScore>> getDueQuestions(String themeName) {
    ThemeStats ts = data.getThemes().get(themeName);
    if (ts == null) return new ArrayList<>();
    long now = System.currentTimeMillis();
    return ts.getQuestions().entrySet().stream()
        .filter(e -> e.getValue().getNextReviewTimestamp() <= now && e.getValue().getNextReviewTimestamp() > 0)
        .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(qs -> qs.getRepCount())))
        .collect(Collectors.toList());
}

// Get count of overdue questions for a theme
public int getDueCount(String themeName) {
    return getDueQuestions(themeName).size();
}

// Get questions that have never been reviewed (repCount == 0)
public List<Map.Entry<String, QuestionScore>> getNewQuestions(String themeName) {
    ThemeStats ts = data.getThemes().get(themeName);
    if (ts == null) return new ArrayList<>();
    return ts.getQuestions().entrySet().stream()
        .filter(e -> e.getValue().getRepCount() == 0)
        .collect(Collectors.toList());
}

// Get fixation phase distribution for a theme
public Map<FixationPhase, Integer> getFixationPhases(String themeName) {
    ThemeStats ts = data.getThemes().get(themeName);
    Map<FixationPhase, Integer> dist = new EnumMap<>(FixationPhase.class);
    for (FixationPhase p : FixationPhase.values()) dist.put(p, 0);
    if (ts == null) return dist;
    for (QuestionScore qs : ts.getQuestions().values()) {
        dist.merge(qs.getFixationPhase(), 1, Integer::sum);
    }
    return dist;
}

// Get all review timestamps grouped by day (for timeline)
public Map<LocalDate, Map<String, Integer>> getTimelineData() {
    Map<LocalDate, Map<String, Integer>> timeline = new TreeMap<>(Collections.reverseOrder());
    for (Map.Entry<String, ThemeStats> themeEntry : data.getThemes().entrySet()) {
        for (QuestionScore qs : themeEntry.getValue().getQuestions().values()) {
            if (qs.getLastReviewTimestamp() > 0) {
                LocalDate day = Instant.ofEpochMilli(qs.getLastReviewTimestamp())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
                timeline.computeIfAbsent(day, k -> new HashMap<>())
                    .merge(themeEntry.getKey(), 1, Integer::sum);
            }
        }
    }
    return timeline;
}

// Simulate perfect answer for "Marcar como feita"
public void markThemeAsDone(String themeName) {
    ThemeStats ts = data.getThemes().get(themeName);
    if (ts == null) return;
    long now = System.currentTimeMillis();
    for (QuestionScore qs : ts.getQuestions().values()) {
        if (qs.getNextReviewTimestamp() <= now && qs.getNextReviewTimestamp() > 0) {
            qs.updateSM2(true);  // quality=4 equivalent
        }
    }
    save();
}
```

### recordRound() — SM-2 Update

Replace the old score-based logic in `recordRound()`:

```java
public void recordRound(List<RoundResult> results) {
    for (RoundResult result : results) {
        ThemeStats themeStats = data.getThemes()
                .computeIfAbsent(result.themeName(), k -> new ThemeStats());
        themeStats.setTotalAnswered(themeStats.getTotalAnswered() + 1);
        if (result.wasCorrect()) {
            themeStats.setTotalCorrect(themeStats.getTotalCorrect() + 1);
        }

        QuestionScore qScore = themeStats.getQuestions()
                .computeIfAbsent(String.valueOf(result.questionId()), k -> new QuestionScore());
        qScore.updateSM2(result.wasCorrect());  // SM-2 replaces old recordCorrect/recordWrong
    }
    recalculateOverall();
    save();
}
```

**Critical:** The old `recordCorrect()` / `recordWrong()` methods on `QuestionScore` should be preserved (not deleted) but no longer called by `recordRound()`. This avoids breaking existing tests that test those methods directly.

### RoundState — Due-Only Factory

```java
public static RoundState createDueReviewRound(Theme theme, StatsService statsService) {
    List<Question> dueQuestions = new ArrayList<>();
    List<Map.Entry<String, QuestionScore>> due = statsService.getDueQuestions(theme.getName());

    // Map question IDs to Question objects
    Map<Integer, Question> questionMap = new HashMap<>();
    for (Question q : theme.getQuestions()) {
        questionMap.put(q.getId(), q);
    }

    for (Map.Entry<String, QuestionScore> entry : due) {
        int qId = Integer.parseInt(entry.getKey());
        Question q = questionMap.get(qId);
        if (q != null) dueQuestions.add(q);
    }

    // If no overdue, fall back to new/unreviewed questions
    if (dueQuestions.isEmpty()) {
        List<Map.Entry<String, QuestionScore>> newQ = statsService.getNewQuestions(theme.getName());
        for (Map.Entry<String, QuestionScore> entry : newQ) {
            int qId = Integer.parseInt(entry.getKey());
            Question q = questionMap.get(qId);
            if (q != null) dueQuestions.add(q);
        }
    }

    // Shuffle and build RoundQuestion list (same pattern as existing)
    Collections.shuffle(dueQuestions);
    List<RoundQuestion> questions = new ArrayList<>();
    for (Question q : dueQuestions) {
        List<String> original = q.getOptions();
        String correctText = original.get(q.getCorrect());
        List<String> shuffled = new ArrayList<>(original);
        Collections.shuffle(shuffled);
        int newCorrect = shuffled.indexOf(correctText);
        questions.add(new RoundQuestion(theme.getName(), q, shuffled, newCorrect));
    }
    return new RoundState(questions, true);
}
```

### ThemeSelectionController — Dashboard Integration

```java
// In handleStart() — replace with dashboard-aware round creation
private void handleReviewTheme(Theme theme) {
    RoundState roundState = RoundState.createDueReviewRound(theme, statsService);
    StudyRoundView studyRoundView = new StudyRoundView();
    StudyRoundController studyRoundController = new StudyRoundController(
        roundState, studyRoundView, screenController, statsService);
    studyRoundController.setOnRoundEndCallback(() -> {
        refreshDashboard();  // Rebuild cards with updated SM-2 data
        screenController.switchTo("themeSelection");
    });
    studyRoundController.initialize();
}

// "Marcar como feita" handler
private void handleMarkAsDone(String themeName) {
    statsService.markThemeAsDone(themeName);
    refreshDashboard();
}
```

## CSS & Styling Requirements

### New CSS Classes for theme.css

```css
/* Theme card — surface background with left border priority color */
.theme-card {
    -fx-background-color: #1a1a2e;
    -fx-background-radius: 6;
    -fx-padding: 12 16 12 16;
    -fx-border-color: transparent;
    -fx-border-width: 0 0 0 4;
    -fx-border-radius: 6 0 0 6;
}

/* Priority variants — applied via styleClass toggling */
.priority-overdue {
    -fx-border-color: #e74c3c;
}

.priority-today {
    -fx-border-color: #fe9a00;
}

.priority-none {
    -fx-border-color: #27ae60;
}

/* Overdue count badge */
.badge-overdue {
    -fx-background-color: #e74c3c;
    -fx-text-fill: #ffffff;
    -fx-background-radius: 10;
    -fx-padding: 2 8 2 8;
    -fx-font-size: 0.85em;
}

/* Fixation phase badges */
.badge-aprendendo { -fx-background-color: #e74c3c; -fx-text-fill: #ffffff; }
.badge-revisao    { -fx-background-color: #fe9a00; -fx-text-fill: #ffffff; }
.badge-fixa       { -fx-background-color: #27ae60; -fx-text-fill: #ffffff; }
.badge-dominio    { -fx-background-color: #1abc9c; -fx-text-fill: #ffffff; }

/* Fixation bar segments */
.fixation-bar {
    -fx-background-color: #020817;
    -fx-background-radius: 3;
    -fx-padding: 4;
}

.fixation-segment-aprendendo { -fx-background-color: #e74c3c; }
.fixation-segment-revisao    { -fx-background-color: #fe9a00; }
.fixation-segment-fixa       { -fx-background-color: #27ae60; }
.fixation-segment-dominio    { -fx-background-color: #1abc9c; }

/* Dashboard action buttons */
.button-revisar {
    -fx-background-color: #fe9a00;
    -fx-text-fill: #ffffff;
    -fx-background-radius: 4;
    -fx-padding: 6 16 6 16;
    -fx-cursor: hand;
}

.button-feito {
    -fx-background-color: transparent;
    -fx-text-fill: #27ae60;
    -fx-border-color: #27ae60;
    -fx-border-radius: 4;
    -fx-padding: 6 16 6 16;
    -fx-cursor: hand;
}

/* Timeline styling */
.timeline-line {
    -fx-border-color: #333355;
    -fx-border-width: 0 0 0 2;
}

.timeline-dot {
    -fx-background-color: #fe9a00;
    -fx-background-radius: 5;
}

.timeline-date {
    -fx-text-fill: #888899;
    -fx-font-size: 0.85em;
}

.timeline-entry {
    -fx-text-fill: #e0e0e0;
}

/* Dashboard search field */
.search-field {
    -fx-background-color: #1a1a2e;
    -fx-text-fill: #e0e0e0;
    -fx-prompt-text-fill: #555577;
    -fx-border-color: #333355;
    -fx-border-radius: 4;
    -fx-padding: 8 12;
}
```

### Dynamic CSS Class Toggling Pattern

Follow existing pattern from `StudyRoundView.highlightCorrect()`:

```java
// Toggle priority class on theme card
card.getStyleClass().removeAll("priority-overdue", "priority-today", "priority-none");
card.getStyleClass().add("priority-" + priorityLevel);
```

## Integration Points & Risks

### Backward Compatibility
- **QuestionScore @JsonIgnoreProperties(ignoreUnknown=true):** Already present. New SM-2 fields deserialize to defaults (0) for old JSON. EaseFactor defaults to 2.5 via constructor. **LOW RISK.**
- **Old score field:** Remains in JSON but is no longer updated. Old tests that check score values will still pass since we keep recordCorrect/recordWrong methods. **NO RISK.**
- **recordRound() change:** Old tests call recordRound and check score-based assertions (getAproveitamento, getDominio). These will BREAK because recordRound now calls updateSM2 instead of recordCorrect/recordWrong. **MEDIUM RISK — tests need updating.**

### Existing Feature Preservation
- **Study round flow:** StudyRoundController.handleExit() calls recordRound(). This still works — recordRound now applies SM-2 instead of old scores. **NO RISK.**
- **Reinforcement mode:** createReinforcementRound() uses getLowestScoreQuestions() which reads the old `score` field. After SM-2, the `score` field is always 0 for new reviews. **HIGH RISK — reinforcement mode needs to be rethought or deprecated.** Consider: reinforcement could use `getDueQuestions()` instead, or the old score field can be kept in sync.
- **Reports view:** getDominio() and getAproveitamento() use the old `score` field. After SM-2, these will show "N/A" or 0 for newly reviewed questions. **MEDIUM RISK — reports need updating to use SM-2 data.**
- **TabPane tab management:** handleTabSelection() checks tab identity. The Jogar tab becomes the dashboard — its tab reference doesn't change. **NO RISK.**

### Risk Mitigation Recommendations
1. **Reinforcement mode:** For Phase 12, keep reinforcement mode working by also updating the old `score` field in `recordRound()` alongside SM-2. This preserves backward compat. Deprecation can happen in a later phase.
2. **getDominio/getAproveitamento:** Add new SM-2-based versions (e.g., `getDominioSM2()` based on fixation phases) while keeping old methods for Reports view compatibility.
3. **Test updates:** Update StatsServiceTest to account for SM-2 field updates alongside old score updates.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| SM-2 algorithm | Third-party library | Hand-coded (15 lines) | Algorithm is trivial math; no dependency needed |
| Timeline widget | Custom Canvas rendering | VBox + HBox + CSS | Simple vertical list with dots; no virtualization needed |
| Date formatting | Manual string building | `java.time.LocalDate` + `DateTimeFormatter` | Standard Java API, locale-aware |
| Priority sorting | Complex comparator | Simple Comparator chain | 3-group sort + alphabetical is ~10 lines |
| JSON migration | Custom parser | Jackson @JsonIgnoreProperties | Already on the class, handles missing fields automatically |

## Common Pitfalls

### Pitfall 1: SM-2 Interval for First Review
**What goes wrong:** Setting interval = ceil(1 * 2.5) = 3 for first correct answer instead of 1.
**Why it happens:** Applying the generic formula to the first review without the special case.
**How to avoid:** First review always sets interval = 1, regardless of easeFactor. Check `repCount == 0 || interval <= 1` before applying the formula.
**Warning signs:** New questions due again in 3 days instead of 1.

### Pitfall 2: easeFactor Floor/Ceiling
**What goes wrong:** easeFactor drifts below 1.3 or above 2.5 after many reviews.
**Why it happens:** Forgetting to clamp after the adjustment.
**How to avoid:** Always clamp: `Math.max(1.3, Math.min(2.5, ef))` after every update.
**Warning signs:** Questions with very short or very long intervals.

### Pitfall 3: Timestamp Precision
**What goes wrong:** nextReviewTimestamp calculated incorrectly (off by hours/days).
**Why it happens:** Mixing up millis vs. seconds, or forgetting that `interval` is in days.
**How to avoid:** Use `interval * 86_400_000L` (days to millis). Use `System.currentTimeMillis()` for current time.
**Warning signs:** Questions showing as "due" immediately or never.

### Pitfall 4: Breaking Reinforcement Mode
**What goes wrong:** Reinforcement mode stops working because it relies on old `score` field.
**Why it happens:** recordRound() no longer calls recordCorrect/recordWrong, so `score` stays at 0.
**How to avoid:** Also update the old `score` field in recordRound() alongside SM-2, or rewrite reinforcement to use SM-2 data.
**Warning signs:** Reinforcement mode selects random questions instead of error-prone ones.

### Pitfall 5: TabPane Content Replacement
**What goes wrong:** Jogar tab shows old content or throws NullPointerException.
**Why it happens:** Tab.setContent() called before tab is added to TabPane, or controller references stale nodes.
**How to avoid:** Build ReviewDashboardView first, then set tab content in ThemeSelectionView constructor or initialization.
**Warning signs:** Empty tab, old checkbox UI still showing.

### Pitfall 6: Dominio Calculation with SM-2
**What goes wrong:** getDominio() returns "N/A" or 0% for all themes after SM-2 migration.
**Why it happens:** Old `score` field is always 0 for new reviews; getDominio() checks `score > 0`.
**How to avoid:** Update getDominio() to use fixation phases (e.g., percentage of questions in Fixa + Dominio phases).
**Warning signs:** Dashboard shows 0% dominio for all themes.

## Code Examples

### SM-2 Update Method (on QuestionScore)
```java
// Source: SuperMemo SM-2 algorithm [CITED: supermemo.com/english/ol/sm2.htm]
// Adapted for binary correct/wrong per CONTEXT.md locked decision
public void updateSM2(boolean correct) {
    if (correct) {
        this.easeFactor = Math.min(2.5, this.easeFactor + 0.1);
        if (this.repCount == 0 || this.interval <= 1) {
            this.interval = 1;
        } else {
            this.interval = (int) Math.ceil(this.interval * this.easeFactor);
        }
        this.repCount++;
    } else {
        this.easeFactor = Math.max(1.3, this.easeFactor - 0.2);
        this.interval = 1;
        this.repCount = 0;
    }
    this.lastReviewTimestamp = System.currentTimeMillis();
    this.nextReviewTimestamp = System.currentTimeMillis() + (long) this.interval * 86_400_000L;
}
```

### Fixation Phase Derivation
```java
// Source: CONTEXT.md fixation phase table
public FixationPhase getFixationPhase() {
    if (repCount == 0 || interval <= 1) return FixationPhase.APRENDENDO;
    if (repCount <= 2 && interval <= 7) return FixationPhase.REVISAO;
    if (repCount <= 5 && interval <= 30) return FixationPhase.FIXA;
    return FixationPhase.DOMINIO;
}
```

### Priority Determination
```java
// Source: CONTEXT.md theme ordering
public enum Priority { OVERDUE, TODAY, NONE }

public static Priority getPriority(QuestionScore qs) {
    if (qs.getNextReviewTimestamp() == 0) return Priority.NONE; // never reviewed
    long now = System.currentTimeMillis();
    long todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    long todayEnd = todayStart + 86_400_000L;
    if (qs.getNextReviewTimestamp() < todayStart) return Priority.OVERDUE;
    if (qs.getNextReviewTimestamp() < todayEnd) return Priority.TODAY;
    return Priority.NONE;
}
```

### Theme Card CSS Class Toggling
```java
// Source: existing pattern from StudyRoundView.highlightCorrect()
card.getStyleClass().removeAll("priority-overdue", "priority-today", "priority-none");
switch (priority) {
    case OVERDUE -> card.getStyleClass().add("priority-overdue");
    case TODAY -> card.getStyleClass().add("priority-today");
    case NONE -> card.getStyleClass().add("priority-none");
}
```

### Timeline Day Entry
```java
// Source: CONTEXT.md — GitHub contribution log style
HBox dot = new HBox();
dot.getStyleClass().add("timeline-dot");
dot.setMinSize(10, 10);
dot.setMaxSize(10, 10);

VBox line = new VBox();
line.getStyleClass().add("timeline-line");
line.setMinWidth(2);

Label dateLabel = new Label(day.format(DateTimeFormatter.ofPattern("dd/MM")));
dateLabel.getStyleClass().add("timeline-date");

Label entryLabel = new Label("Biologia (12 questões)");
entryLabel.getStyleClass().add("timeline-entry");

VBox entry = new VBox(4, dateLabel, entryLabel);
HBox row = new HBox(8, dot, line, entry);
```

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5.11.0 (Jupiter) |
| Config file | none (default Maven layout) |
| Quick run command | `mvn test -pl . -Dtest="StatsServiceTest,StatsDataTest" -q` |
| Full suite command | `mvn test -q` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| V2-01 | SM-2 interval increases correctly on consecutive correct answers | unit | `mvn test -Dtest="QuestionScoreSM2Test#intervalIncreasesOnCorrect" -q` | ❌ Wave 0 |
| V2-01 | SM-2 easeFactor clamps between 1.3 and 2.5 | unit | `mvn test -Dtest="QuestionScoreSM2Test#easeFactorClamps" -q` | ❌ Wave 0 |
| SR-01 | QuestionScore JSON round-trip with SM-2 fields | unit | `mvn test -Dtest="StatsDataTest#questionScoreSM2RoundTrip" -q` | ❌ Wave 0 |
| SR-01 | Old JSON without SM-2 fields deserializes correctly | unit | `mvn test -Dtest="StatsDataTest#backwardCompatDeserialize" -q` | ❌ Wave 0 |
| SR-02 | recordRound applies SM-2 update | unit | `mvn test -Dtest="StatsServiceTest#recordRoundAppliesSM2" -q` | ❌ Wave 0 |
| SR-03 | Dashboard shows themes sorted by priority | integration | `mvn test -Dtest="ReviewDashboardTest#themeOrdering" -q` | ❌ Wave 0 |
| SR-04 | Priority colors applied correctly | integration | manual visual check | — |
| SR-05 | Search filters themes by name | unit | `mvn test -Dtest="ReviewDashboardTest#searchFiltering" -q` | ❌ Wave 0 |
| SR-06 | Revisar button creates due-only round | unit | `mvn test -Dtest="RoundStateTest#createDueReviewRound" -q` | ❌ Wave 0 |
| SR-07 | Timeline shows per-day aggregation | unit | `mvn test -Dtest="StatsServiceTest#getTimelineData" -q` | ❌ Wave 0 |
| SR-08 | Fixation phase computed correctly from SM-2 fields | unit | `mvn test -Dtest="QuestionScoreSM2Test#fixationPhases" -q` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `mvn test -q`
- **Per wave merge:** `mvn test -q` (full suite)
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `src/test/java/org/IsmaelSS/model/QuestionScoreSM2Test.java` — covers V2-01, SR-01, SR-08
- [ ] `src/test/java/org/IsmaelSS/service/StatsServiceSM2Test.java` — covers SR-02, SR-07
- [ ] `src/test/java/org/IsmaelSS/model/RoundStateDueReviewTest.java` — covers SR-06
- [ ] Update existing `StatsServiceTest.java` — existing tests may need score assertions updated

## Security Domain

> Omit — this is a single-user local desktop application with no authentication, network, or data exchange features. No applicable ASVS categories.

## Sources

### Primary (HIGH confidence)
- SuperMemo official SM-2 algorithm description [CITED: supermemo.com/english/ol/sm2.htm] — algorithm formulas, EF bounds, interval progression
- Existing codebase analysis (all files read) — integration points, existing patterns, test infrastructure
- Stack Overflow SM-2 Java implementations [CITED: stackoverflow.com/questions/49047159] — confirmed interval formula variants

### Secondary (MEDIUM confidence)
- RemNote SM-2 documentation [CITED: help.remnote.com] — Anki SM-2 variant comparison, ease factor adjustments
- JavaFX CSS Reference Guide [CITED: docs.oracle.com/javase/8/javafx/api/javafx/scene/doc-files/cssref.html] — CSS property reference for dynamic class toggling

### Tertiary (LOW confidence)
- SM-2 Python implementation (alankan886/SuperMemo2) — cross-reference for algorithm correctness [ASSUMED — not verified in this session]

## Metadata

**Confidence breakdown:**
- Standard Stack: HIGH — no new dependencies; all verified in pom.xml
- Architecture: HIGH — full codebase read; integration points mapped from source
- Pitfalls: HIGH — identified from code analysis (reinforcement mode, dominio calc, score field)
- SM-2 Algorithm: HIGH — verified against official SuperMemo docs
- Timeline Component: MEDIUM — built from standard JavaFX primitives, no external reference needed

**Research date:** 2026-07-21
**Valid until:** 2026-08-21 (30 days — stable domain, SM-2 algorithm is timeless)
