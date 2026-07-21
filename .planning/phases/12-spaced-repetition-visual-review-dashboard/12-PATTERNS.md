# Phase 12: Spaced Repetition & Visual Review Dashboard - Pattern Map

**Mapped:** 2026-07-21
**Files analyzed:** 13 (4 new views, 4 modified, 1 new enum, 3 new tests, 1 modified CSS)
**Analogs found:** 12 / 13

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `view/ReviewDashboardView.java` | view (dashboard) | layout/composition | `view/ReportsView.java` | role-match |
| `view/ThemeCardNode.java` | view (component) | layout/composition | `view/StudyRoundView.java` | role-match |
| `view/TimelineView.java` | view (component) | layout/composition | `view/ReportsView.java` | partial |
| `view/ThemeSelectionView.java` | view (modified) | layout | self | exact (same file) |
| `model/StatsData.java` | model (modified) | CRUD | self | exact (same file) |
| `service/StatsService.java` | service (modified) | CRUD/query | self | exact (same file) |
| `model/RoundState.java` | model (modified) | factory/transform | self | exact (same file) |
| `controller/ThemeSelectionController.java` | controller (modified) | request-response | self | exact (same file) |
| `styles/theme.css` | config/styling | — | self | exact (same file) |
| `model/FixationPhase.java` | model (enum) | transform | none | no analog |
| `test/.../QuestionScoreSM2Test.java` | test | unit | `test/.../StatsDataTest.java` | role-match |
| `test/.../StatsServiceSM2Test.java` | test | unit | `test/.../StatsServiceTest.java` | role-match |
| `test/.../RoundStateDueReviewTest.java` | test | unit | `test/.../RoundStateReinforcementTest.java` | role-match |

## Pattern Assignments

### `view/ReviewDashboardView.java` (view, layout/composition) — NEW

**Analog:** `src/main/java/org/IsmaelSS/view/ReportsView.java`

**Why:** ReportsView is the only view that wraps content in a ScrollPane for overflow — exactly what the dashboard needs. It also shows the VBox-based content building pattern.

**Imports pattern** (lines 1-11):
```java
package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.IsmaelSS.model.Theme;
import org.IsmaelSS.service.StatsService;
```

**ScrollPane wrapping pattern** (ReportsView lines 42-48):
```java
ScrollPane scrollPane = new ScrollPane(content);
scrollPane.setFitToWidth(true);
scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

root = new VBox(scrollPane);
scene = new Scene(root);
```

**CSS class assignment pattern** (ReportsView lines 19-20):
```java
VBox content = new VBox(10);
content.getStyleClass().add("background");
content.setPadding(new Insets(20));
```

**Title pattern** (ReportsView lines 22-24):
```java
Label title = new Label("Relatórios de Desempenho");
title.getStyleClass().add("title");
content.getChildren().add(title);
```

**Separator pattern** (ReportsView lines 26-28):
```java
Label section1 = new Label("Resumo Geral");
section1.getStyleClass().add("section-title");
content.getChildren().addAll(section1, new Separator());
```

**Getter pattern** (ReportsView lines 51-54):
```java
public Scene getScene() { return scene; }
public VBox getContent() { return root; }
```

**Construction approach:** Build all nodes programmatically (no FXML). Constructor builds VBox → ScrollPane → Scene. Expose getter methods for controller access.

---

### `view/ThemeCardNode.java` (view/component, layout/composition) — NEW

**Analog:** `src/main/java/org/IsmaelSS/view/StudyRoundView.java`

**Why:** StudyRoundView demonstrates programmatic node construction with CSS class toggling — the exact pattern needed for theme cards with dynamic priority colors and fixation bars.

**Imports pattern** (StudyRoundView lines 1-12):
```java
package org.IsmaelSS.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
```

**Node construction with CSS classes** (StudyRoundView lines 31-44):
```java
root = new VBox(15);
root.setPadding(new Insets(20));
root.setAlignment(Pos.CENTER);

progressLabel = new Label();

questionContent = new VBox(10);
questionContent.setAlignment(Pos.CENTER);

questionLabel = new Label();
questionLabel.getStyleClass().add("title");
questionLabel.setWrapText(true);
```

**Button creation pattern** (StudyRoundView lines 64-73):
```java
exitButton = new Button("Sair");
exitButton.getStyleClass().add("button-secondary");
exitButton.setOnAction(e -> {
    if (onExit != null) onExit.run();
});

proximaButton = new Button("Próxima");
proximaButton.getStyleClass().add("button-primary");
```

**CSS class toggling pattern** (StudyRoundView lines 126-133):
```java
public void highlightCorrect(int correctIndex) {
    optionButtons[correctIndex].getStyleClass().setAll("option-correct");
}

public void highlightWrong(int wrongIndex, int correctIndex) {
    optionButtons[wrongIndex].getStyleClass().setAll("option-wrong");
    optionButtons[correctIndex].getStyleClass().setAll("option-correct");
}
```

**Callback registration pattern** (StudyRoundView lines 152-162):
```java
private Consumer<Integer> onOptionClick;
private Runnable onExit;
private Runnable onVoltar;

public void setOnOptionClick(Consumer<Integer> handler) {
    this.onOptionClick = handler;
}
```

**Construction approach:** ThemeCardNode is a VBox (not a Scene wrapper). Constructor takes theme data + StatsService, builds inner HBox/VBox nodes, assigns CSS classes. Expose button action setters for controller to wire handlers.

---

### `view/TimelineView.java` (view/component, layout/composition) — NEW

**Analog:** `src/main/java/org/IsmaelSS/view/ReportsView.java` (partial — for VBox content building)

**Why:** TimelineView is a VBox-based component (no Scene), similar to ReportsView's `overallBox` and `accordion` children. The closest pattern for building a list of items in a VBox.

**VBox content building pattern** (ReportsView lines 30-32):
```java
overallBox = new VBox(5);
overallBox.setPadding(new Insets(5, 0, 10, 10));
content.getChildren().add(overallBox);
```

**Clear and rebuild pattern** (ReportsView lines 56-59):
```java
public void clearContent() {
    overallBox.getChildren().clear();
    accordion.getPanes().clear();
}
```

**Construction approach:** TimelineView wraps a VBox. Has a `setData(Map<LocalDate, Map<String, Integer>>)` method that clears children and rebuilds per-day entries. Each entry is an HBox with dot + line + labels using CSS classes from theme.css.

---

### `view/ThemeSelectionView.java` (view, modified) — MODIFY

**Analog:** self (existing file)

**Current structure** (lines 37-71): Constructor builds TabPane with 3 tabs. Jogar tab has VBox with checkboxes, spinner, startButton.

**Modification pattern:** Replace the jogarContent VBox construction (lines 55-59) with:
```java
// Instead of old checkbox-based content:
ReviewDashboardView dashboardView = new ReviewDashboardView(statsService, themes);
jogarTab.setContent(dashboardView.getContent());
```

**Keep:** TabPane construction, relatoriosTab, gerenciarTab, scene creation, all getters.

**Remove:** themeCheckboxes, themeLabels, themeMap, themeScores, themeDominioText maps, questionCountSpinner, reforcoCheckBox, startButton, feedbackLabel, setThemes(), getSelectedThemes(), getQuestionCount(), isReinforcementMode(), updateAproveitamento(), updateDominio(), updateQuestionCountRange().

**New methods needed:** `refreshDashboard()` to call dashboardView.refresh().

---

### `model/StatsData.java` (model, modified — CRUD) — MODIFY

**Analog:** self (existing file)

**Current QuestionScore** (lines 33-58):
```java
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public static class QuestionScore {
    private int score;  // bounded -10..+5, defaults to 0

    public QuestionScore() {}

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int recordCorrect() {
        this.score = Math.min(5, this.score + 2);
        return this.score;
    }

    public int recordWrong() {
        this.score = Math.max(-10, this.score - 3);
        return this.score;
    }
}
```

**Modification pattern:** Add new SM-2 fields to QuestionScore class. Keep existing `score` field and `recordCorrect()`/`recordWrong()` methods for backward compat. Add new fields after `score`:

```java
private double easeFactor = 2.5;   // default 2.5
private int interval;              // days, default 0
private int repCount;              // consecutive correct, default 0
private long lastReviewTimestamp;  // millis since epoch, default 0
private long nextReviewTimestamp;  // millis since epoch, default 0
```

Add getters/setters and `updateSM2(boolean correct)` + `getFixationPhase()` methods.

**Jackson compat:** The existing `@JsonIgnoreProperties(ignoreUnknown = true)` annotation handles backward compat — old JSON without SM-2 fields will deserialize new fields to defaults. The `easeFactor` default of 2.5 is handled by the field initializer (no constructor change needed for Jackson — field defaults work).

---

### `service/StatsService.java` (service, modified — CRUD/query) — MODIFY

**Analog:** self (existing file)

**Current recordRound** (lines 89-108):
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

**Modification pattern:** Replace `qScore.recordCorrect()` / `qScore.recordWrong()` with `qScore.updateSM2(result.wasCorrect())`. Keep old score update for backward compat (also update `score` field alongside SM-2):

```java
if (result.wasCorrect()) {
    qScore.recordCorrect();  // keep old score for backward compat
}
qScore.updateSM2(result.wasCorrect());
```

**New methods to add** — follow the existing query method pattern (e.g., `getDominio()` lines 161-169):

```java
// getDueQuestions(themeName) — stream filter on nextReviewTimestamp
// getDueCount(themeName) — delegate to getDueQuestions().size()
// getNewQuestions(themeName) — stream filter on repCount == 0
// getFixationPhases(themeName) — EnumMap aggregation
// getTimelineData() — TreeMap<LocalDate, Map<String, Integer>> from lastReviewTimestamp
// markThemeAsDone(themeName) — iterate overdue questions, call updateSM2(true)
```

**Save pattern** (existing line 81-87):
```java
private void save() {
    try {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(STATS_FILE), data);
    } catch (IOException e) {
        LOG.severe("Failed to save stats: " + e.getMessage());
    }
}
```

---

### `model/RoundState.java` (model, modified — factory/transform) — MODIFY

**Analog:** self (existing file)

**Current createReinforcementRound** (lines 53-105):
```java
public static RoundState createReinforcementRound(List<Theme> themes, int questionsPerTheme, StatsService statsService) {
    List<Map.Entry<String, Integer>> lowestScores = statsService.getLowestScoreQuestions(50);
    Set<String> errorIds = lowestScores.stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    // ... selects error-prone questions, shuffles, builds RoundQuestion list
    Collections.shuffle(questions);
    return new RoundState(questions, true);
}
```

**New factory method pattern:** `createDueReviewRound(Theme theme, StatsService statsService)` — follows same structure:

1. Query due questions from StatsService
2. Map question IDs to Theme.getQuestions() objects
3. If no overdue, fall back to new/unreviewed questions
4. Shuffle and build RoundQuestion list (same shuffle pattern as lines 41-47)
5. Return `new RoundState(questions, true)`

**Key reuse:** The question shuffling logic (lines 41-47 of `buildQuestions`) is identical — extract or copy:
```java
List<String> original = q.getOptions();
String correctText = original.get(q.getCorrect());
List<String> shuffled = new ArrayList<>(original);
Collections.shuffle(shuffled);
int newCorrect = shuffled.indexOf(correctText);
questions.add(new RoundQuestion(theme.getName(), q, shuffled, newCorrect));
```

---

### `controller/ThemeSelectionController.java` (controller, modified — request-response) — MODIFY

**Analog:** self (existing file)

**Current handleStart** (lines 68-92):
```java
private void handleStart() {
    List<Theme> selectedThemes = view.getSelectedThemes();
    if (selectedThemes.isEmpty()) {
        view.setFeedback("Selecione pelo menos um tema para iniciar.");
        return;
    }
    // ... creates RoundState, StudyRoundView, StudyRoundController
    studyRoundController.setOnRoundEndCallback(() -> {
        refreshScores();
        screenController.switchTo("themeSelection");
    });
    studyRoundController.initialize();
}
```

**Modification pattern:** Replace `handleStart()` with two handlers:

1. `handleReviewTheme(Theme theme)` — creates due-only round:
```java
RoundState roundState = RoundState.createDueReviewRound(theme, statsService);
StudyRoundView studyRoundView = new StudyRoundView();
StudyRoundController studyRoundController = new StudyRoundController(
    roundState, studyRoundView, screenController, statsService);
studyRoundController.setOnRoundEndCallback(() -> {
    refreshDashboard();
    screenController.switchTo("themeSelection");
});
studyRoundController.initialize();
```

2. `handleMarkAsDone(String themeName)` — calls statsService.markThemeAsDone() then refreshDashboard().

**Initialization pattern** (existing lines 32-43):
```java
public void initialize() {
    refreshScores();
    screenController.registerScreen("themeSelection", view.getScene());
    view.getStartButton().setOnAction(e -> handleStart());
    view.getTabPane().getSelectionModel().selectedItemProperty().addListener(
            (obs, oldTab, newTab) -> handleTabSelection(newTab));
    screenController.switchTo("themeSelection");
}
```

**New initialize:** Wire dashboard callbacks instead of startButton.

---

### `styles/theme.css` (config/styling) — MODIFY

**Analog:** self (existing file)

**Current CSS class patterns:**
- `.background` → `#020817` (line 6-8)
- `.surface` → `#1a1a2e` (line 10-12)
- `.button-primary` → `#fe9a00` with `:hover` and `:pressed` (lines 33-47)
- `.button-secondary` → transparent with border (lines 49-60)
- `.title` → bold, white, 1.4em (lines 14-18)
- `.text-field` → dark background, focused border `#fe9a00` (lines 198-209)
- `.scroll-pane` → `#020817` background (lines 124-130)

**New classes to append** — follow existing naming conventions:
```css
/* Theme card — .surface background with left border priority color */
.theme-card { ... }
.priority-overdue { ... }
.priority-today { ... }
.priority-none { ... }

/* Badges */
.badge-overdue { ... }
.badge-aprendendo { ... }
.badge-revisao { ... }
.badge-fixa { ... }
.badge-dominio { ... }

/* Fixation bar */
.fixation-bar { ... }
.fixation-segment-* { ... }

/* Dashboard buttons */
.button-revisar { ... }  /* extends .button-primary pattern */
.button-feito { ... }     /* extends .button-secondary pattern */

/* Timeline */
.timeline-line { ... }
.timeline-dot { ... }
.timeline-date { ... }
.timeline-entry { ... }

/* Search field */
.search-field { ... }  /* extends .text-field pattern */
```

---

### `model/FixationPhase.java` (model/enum, transform) — NEW

**Analog:** None — first enum in the project.

**Pattern from RESEARCH.md** (lines 160-173):
```java
package org.IsmaelSS.model;

public enum FixationPhase {
    APRENDENDO,  // Red badge — new or last wrong
    REVISAO,     // Orange badge — 2-3 consecutive correct
    FIXA,        // Green badge — 4-6 consecutive correct
    DOMINIO      // Teal badge — 7+ consecutive correct
}
```

Pure data enum with no logic. Used by `QuestionScore.getFixationPhase()` and `StatsService.getFixationPhases()`.

---

### `test/.../QuestionScoreSM2Test.java` (test, unit) — NEW

**Analog:** `src/test/java/org/IsmaelSS/model/StatsDataTest.java`

**Test class pattern** (StatsDataTest lines 1-11):
```java
package org.IsmaelSS.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatsDataTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void roundTripSerialization() throws Exception {
        // ... setup, act, assert
    }
}
```

**Test method pattern:** Arrange → Act → Assert with `assertEquals`, `assertNotNull`, `assertTrue`.

---

### `test/.../StatsServiceSM2Test.java` (test, unit) — NEW

**Analog:** `src/test/java/org/IsmaelSS/service/StatsServiceTest.java`

Same JUnit 5 pattern. Tests SM-2 updates via StatsService.recordRound() and new query methods.

---

### `test/.../RoundStateDueReviewTest.java` (test, unit) — NEW

**Analog:** `src/test/java/org/IsmaelSS/model/RoundStateReinforcementTest.java`

Same JUnit 5 pattern. Tests `RoundState.createDueReviewRound()` factory method.

---

## Shared Patterns

### View Construction (All Views)
**Source:** `view/ReportsView.java` + `view/StudyRoundView.java`
**Apply to:** ReviewDashboardView, ThemeCardNode, TimelineView
```java
// Pattern 1: Scene-wrapping view (ReportsView)
VBox content = new VBox(10);
content.getStyleClass().add("background");
content.setPadding(new Insets(20));
ScrollPane scrollPane = new ScrollPane(content);
scrollPane.setFitToWidth(true);
root = new VBox(scrollPane);
scene = new Scene(root);

// Pattern 2: Component node (StudyRoundView)
VBox root = new VBox(15);
root.setPadding(new Insets(20));
root.setAlignment(Pos.CENTER);
```

### CSS Class Toggling
**Source:** `view/StudyRoundView.java` lines 126-133
**Apply to:** ThemeCardNode (priority colors), all dynamic styling
```java
card.getStyleClass().removeAll("priority-overdue", "priority-today", "priority-none");
card.getStyleClass().add("priority-" + priorityLevel);
```

### Callback Registration
**Source:** `view/StudyRoundView.java` lines 152-162
**Apply to:** ThemeCardNode (button actions), ReviewDashboardView (search/filter)
```java
private Runnable onReview;
private Runnable onMarkDone;

public void setOnReview(Runnable handler) { this.onReview = handler; }
public void setOnMarkDone(Runnable handler) { this.onMarkDone = handler; }
```

### Service Query Methods
**Source:** `service/StatsService.java` lines 161-169 (getDominio pattern)
**Apply to:** All new StatsService query methods
```java
public String getDominio(String themeName) {
    ThemeStats ts = data.getThemes().get(themeName);
    if (ts == null || ts.getQuestions().isEmpty()) return "N/A";
    // ... stream/filter/count logic
}
```

### Jackson Persistence
**Source:** `service/StatsService.java` lines 32-36, 81-87
**Apply to:** All model changes (backward compat via @JsonIgnoreProperties)
```java
private final ObjectMapper mapper = new ObjectMapper();
this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

private void save() {
    try {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(STATS_FILE), data);
    } catch (IOException e) {
        LOG.severe("Failed to save stats: " + e.getMessage());
    }
}
```

### Round Creation & Navigation
**Source:** `controller/ThemeSelectionController.java` lines 68-92
**Apply to:** ThemeSelectionController.handleReviewTheme()
```java
RoundState roundState = RoundState.createReinforcementRound(selectedThemes, questionsPerTheme, statsService);
StudyRoundView studyRoundView = new StudyRoundView();
StudyRoundController studyRoundController = new StudyRoundController(
    roundState, studyRoundView, screenController, statsService);
studyRoundController.setOnRoundEndCallback(() -> {
    refreshScores();
    screenController.switchTo("themeSelection");
});
studyRoundController.initialize();
```

### Test Structure
**Source:** `test/.../StatsDataTest.java`
**Apply to:** All new test files
```java
package org.IsmaelSS.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SomeTest {
    @Test
    void testSomething() {
        // Arrange → Act → Assert
    }
}
```

## No Analog Found

Files with no close match in the codebase (planner should use RESEARCH.md patterns instead):

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| `model/FixationPhase.java` | model (enum) | transform | First enum in project; no existing enum analog |

## Metadata

**Analog search scope:** `src/main/java/org/IsmaelSS/{view,controller,service,model}/`, `src/main/resources/styles/`, `src/test/java/org/IsmaelSS/`
**Files scanned:** 16 (4 views, 5 controllers, 2 services, 5 models, 1 CSS, 3 tests)
**Pattern extraction date:** 2026-07-21
