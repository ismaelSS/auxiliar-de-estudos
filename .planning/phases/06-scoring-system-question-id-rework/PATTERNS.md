# Phase 6: Scoring System & Question ID Rework — Pattern Map

**Mapped:** 2026-07-08
**Files analyzed:** 15 new/modified (from CONTEXT.md)
**Analogs found:** 15 / 15

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `model/Question.java` | model | CRUD | `model/Theme.java` | exact |
| `model/StatsData.java` | model | CRUD | `model/StatsData.java` (itself) | exact |
| `model/RoundState.java` | model | CRUD | `model/RoundState.java` (itself) | exact |
| `model/RoundResult.java` | model | CRUD | `model/RoundResult.java` (itself) | exact |
| `service/StatsService.java` | service | CRUD | `service/StatsService.java` (itself) | exact |
| `view/ThemeSelectionView.java` | component | request-response | `view/ThemeSelectionView.java` (itself) | exact |
| `view/ReportsView.java` | component | request-response | `view/ReportsView.java` (itself) | exact |
| `controller/ThemeSelectionController.java` | controller | request-response | `controller/ThemeSelectionController.java` (itself) | exact |
| `controller/StudyRoundController.java` | controller | request-response | `controller/StudyRoundController.java` (itself) | exact |
| `themes/*.json` | config | file-I/O | `themes/java-record.json` | exact |

---

## Pattern Assignments

### `model/Question.java` — Add `id` field

**Analog:** `model/Question.java` (itself) + `model/Theme.java` (companion pattern)

**Current state** (lines 6-29):
```java
public class Question {
    private String question;
    private List<String> options;
    private int correct;

    public Question() {}

    public Question(@JsonProperty("question") String question,
                    @JsonProperty("options") List<String> options,
                    @JsonProperty("correct") int correct) {
        this.question = question;
        this.options = options;
        this.correct = correct;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public int getCorrect() { return correct; }
    public void setCorrect(int correct) { this.correct = correct; }
}
```

**Pattern to follow:**
- All fields are `private` with getter/setter pairs.
- No-arg constructor exists for Jackson deserialization.
- All-args constructor uses `@JsonProperty` on each parameter — **this is critical** because Jackson uses the constructor for deserialization when fields don't match JSON keys (see `Theme.java` lines 11-14: `Theme` has *no* `@JsonProperty` on its constructor because Jackson maps it by field name).
- New `id` field should be `private String id;` or `private int id;`.
- If `String` type, follow the same `@JsonProperty` pattern in the constructor.
- Add getter `public String getId()` and setter `public void setId(String id)`.

**Key insight about `@JsonProperty`:** `Question` uses it (line 13-15), `Theme` does not (lines 11-14 of `Theme.java`). Both work because:
- `Theme`'s constructor parameter names match JSON keys exactly (Java 25 records parameter names in bytecode).
- `Question` explicitly names them. Follow whichever pattern is consistent with the theme JSON files.

**Recommendation:** Add `id` field matching the convention in `Theme.java` (simple field + getter/setter, no `@JsonProperty` needed if param name matches).

---

### `model/StatsData.java` — Replace `correctCount`/`wrongCount` per question with `score` int

**Analog:** `model/StatsData.java` (itself, lines 1-55)

**Current nested structure pattern** (lines 17-42):
```java
public static class ThemeStats {
    private int totalAnswered;
    private int totalCorrect;
    private Map<String, QuestionStats> questions = new HashMap<>();

    public ThemeStats() {}

    public int getTotalAnswered() { return totalAnswered; }
    public void setTotalAnswered(int totalAnswered) { this.totalAnswered = totalAnswered; }
    public int getTotalCorrect() { return totalCorrect; }
    public void setTotalCorrect(int totalCorrect) { this.totalCorrect = totalCorrect; }
    public Map<String, QuestionStats> getQuestions() { return questions; }
    public void setQuestions(Map<String, QuestionStats> questions) { this.questions = questions; }
}

public static class QuestionStats {
    private int answered;
    private int correct;

    public QuestionStats() {}

    public int getAnswered() { return answered; }
    public void setAnswered(int answered) { this.answered = answered; }
    public int getCorrect() { return correct; }
    public void setCorrect(int correct) { this.correct = correct; }
}

public static class OverallStats {
    private int totalAnswered;
    private int totalCorrect;

    public OverallStats() {}

    public int getTotalAnswered() { return totalAnswered; }
    public void setTotalAnswered(int totalAnswered) { this.totalAnswered = totalAnswered; }
    public int getTotalCorrect() { return totalCorrect; }
    public void setTotalCorrect(int totalCorrect) { this.totalCorrect = totalCorrect; }
}
```

**Pattern to follow:**
- All nested static classes follow the same POJO + no-arg constructor + getter/setter pattern.
- `QuestionStats` currently uses `answered`/`correct` ints. Replace `correct` with `score` (int, default 0). The `answered` field remains useful for counting attempts but `correct` is being replaced by `score`.
- `OverallStats` may need a `totalScore` field analogous to `totalCorrect`.
- `ThemeStats` — `totalCorrect` may be replaced with `totalScore` or kept alongside. Follow the existing naming convention (camelCase, simple types).
- The `Map<String, QuestionStats>` key will change from question text to question `id` — or stay as text but the scoring becomes `score`-based.

---

### `model/RoundState.java` — Update `createReinforcementRound` to use score-based selection

**Analog:** `model/RoundState.java` (itself, lines 53-105)

**Current reinforcement pattern** (lines 53-105):
```java
public static RoundState createReinforcementRound(List<Theme> themes, int questionsPerTheme, StatsService statsService) {
    List<Map.Entry<String, Double>> topErrors = statsService.getHighestErrorQuestions(50);
    Set<String> errorTexts = topErrors.stream()
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    Map<String, Double> errorMap = new HashMap<>();
    for (Map.Entry<String, Double> e : topErrors) {
        errorMap.put(e.getKey(), e.getValue());
    }

    List<RoundQuestion> questions = new ArrayList<>();
    for (Theme theme : themes) {
        // ... splits questions into errorQuestions vs freshQuestions
        // ... sorts errorQuestions by error rate (descending)
        // ... fills remaining with fresh/random questions
    }
}
```

**Key observation:** The current code uses question *text* as the key to match stats data (`errorTexts.contains(q.getQuestion())`). With the new `id` field, this should match by `q.getId()` instead. The `getHighestErrorQuestions` will be renamed to `getLowestScoreQuestions` and return `Map.Entry<String, Integer>` instead of `Map.Entry<String, Double>`.

**Pattern to follow:**
- The internal `RoundQuestion` record (lines 154-165) stays the same — it wraps theme name, question, shuffled options, and correct index.
- The `checkAnswer` method (lines 123-128) returns boolean and increments `correctCount`/`totalAnswered`. This will need to return or communicate a *score delta* (+2 or -3) instead of just boolean.

---

### `model/RoundResult.java` — Track score deltas

**Analog:** `model/RoundResult.java` (itself, line 3)

**Current state:**
```java
public record RoundResult(String themeName, String questionText, boolean wasCorrect) {}
```

**Pattern to follow:**
- This is a Java `record` — the only one in the codebase.
- Records are used for simple immutable data carriers. Add a `int scoreDelta` field: `public record RoundResult(String themeName, String questionText, boolean wasCorrect, int scoreDelta) {}`.
- The `questionText` field may be replaced by `questionId` (String) to match the new `Question.id` field. This enables StatsData to key by ID instead of question text.

---

### `service/StatsService.java` — Score-based calculations

**Analog:** `service/StatsService.java` (itself, lines 1-124)

**Current patterns in the service:**

**Logger pattern** (line 21):
```java
private static final Logger LOG = Logger.getLogger(StatsService.class.getName());
```

**File path pattern** (lines 22-23):
```java
private static final String STATS_FILE = System.getProperty("user.dir")
        + File.separator + "flashcard-stats.json";
```

**Constructor pattern** (lines 28-32):
```java
public StatsService() {
    this.mapper = new ObjectMapper();
    this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    this.data = load();
}
```

**Load pattern** (lines 34-45):
```java
private StatsData load() {
    File file = new File(STATS_FILE);
    if (!file.exists()) {
        return new StatsData();
    }
    try {
        return mapper.readValue(file, StatsData.class);
    } catch (IOException e) {
        LOG.warning("Could not read stats file, starting fresh: " + e.getMessage());
        return new StatsData();
    }
}
```

**Save pattern** (lines 47-53):
```java
private void save() {
    try {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(STATS_FILE), data);
    } catch (IOException e) {
        LOG.severe("Failed to save stats: " + e.getMessage());
    }
}
```

**recordRound pattern** (lines 55-72) — This is the core pattern to modify:
```java
public void recordRound(List<RoundResult> results) {
    for (RoundResult result : results) {
        ThemeStats themeStats = data.getThemes()
                .computeIfAbsent(result.themeName(), k -> new ThemeStats());
        themeStats.setTotalAnswered(themeStats.getTotalAnswered() + 1);
        if (result.wasCorrect()) {
            themeStats.setTotalCorrect(themeStats.getTotalCorrect() + 1);
        }
        QuestionStats qStats = themeStats.getQuestions()
                .computeIfAbsent(result.questionText(), k -> new QuestionStats());
        qStats.setAnswered(qStats.getAnswered() + 1);
        if (result.wasCorrect()) {
            qStats.setCorrect(qStats.getCorrect() + 1);
        }
    }
    recalculateOverall();
    save();
}
```

**Get highest error questions** (lines 107-119) — Will be renamed to `getLowestScoreQuestions`:
```java
public List<Map.Entry<String, Double>> getHighestErrorQuestions(int limit) {
    List<Map.Entry<String, Double>> entries = new ArrayList<>();
    for (Map.Entry<String, ThemeStats> themeEntry : data.getThemes().entrySet()) {
        for (Map.Entry<String, QuestionStats> questionEntry : themeEntry.getValue().getQuestions().entrySet()) {
            QuestionStats qs = questionEntry.getValue();
            if (qs.getAnswered() == 0) continue;
            double errorRate = 1.0 - ((double) qs.getCorrect() / qs.getAnswered());
            entries.add(Map.entry(questionEntry.getKey(), errorRate));
        }
    }
    entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
    return entries.subList(0, Math.min(limit, entries.size()));
}
```

**Changes needed:**
- `recordRound` will need to apply score deltas (+2 or -3) per question, clamp to [-10, +5].
- `getHighestErrorQuestions` → `getLowestScoreQuestions`, key type `String` (question ID), value type `Integer` (score).
- `getHitRate` / `getOverallHitRate` → replaced with `getAproveitamento` methods that compute weighted scores:
  - Negative score → weight -3
  - Zero score → weight 0
  - Positive score → weight +2

**Import conventions** (lines 1-18):
```java
// 1. Package declaration
package org.IsmaelSS.service;
// 2. Third-party imports (com.fasterxml.*, java.io.*)
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
// 3. Same-project imports (org.IsmaelSS.model.*)
import org.IsmaelSS.model.RoundResult;
// 4. Java standard library (java.io.*, java.util.*)
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
// 5. java.util.logging last
import java.util.logging.Logger;
```

---

### `view/ThemeSelectionView.java` — Replace hit rate with aproveitamento score

**Analog:** `view/ThemeSelectionView.java` (itself, lines 121-129)

**Current hit rate update pattern:**
```java
public void updateHitRate(String themeName, String hitRate) {
    Label info = themeLabels.get(themeName);
    if (info != null) {
        Theme theme = themeMap.get(themeName);
        if (theme != null) {
            info.setText(theme.getName() + " (" + theme.getQuestionCount() + " perguntas) — Hit rate: " + hitRate);
        }
    }
}
```

**View initialization pattern** (lines 62-85):
```java
public void setThemes(List<Theme> themes) {
    themeListContainer.getChildren().clear();  // clear
    themeCheckboxes.clear();                   // clear maps
    themeLabels.clear();
    themeMap.clear();

    for (Theme theme : themes) {
        themeMap.put(theme.getName(), theme);
        CheckBox checkBox = new CheckBox();
        Label info = new Label(theme.getName() + " (" + theme.getQuestionCount() + " perguntas) — Hit rate: N/A");
        themeCheckboxes.put(theme.getName(), checkBox);
        themeLabels.put(theme.getName(), info);
        VBox row = new VBox(2, checkBox, info);
        row.setPadding(new Insets(5, 0, 5, 10));
        themeListContainer.getChildren().add(row);
    }
}
```

**Changes needed:**
- Replace `updateHitRate` with `updateAproveitamento(String themeName, String scoreText)`.
- Update the label text in `setThemes` from `"Hit rate: N/A"` to `"Pontuação: N/A"` or similar.

**UI Component naming pattern:**
- All components are `private final` fields.
- `Map<String, CheckBox>` for dynamic controls keyed by theme name.
- Scene created in constructor: `scene = new Scene(root, 600, 400)`.
- Getter for scene: `public Scene getScene() { return scene; }`.

---

### `view/ReportsView.java` — Score display instead of hit rate

**Analog:** `view/ReportsView.java` (itself, lines 11-74)

**Structure pattern:**
- Scene created in constructor with fixed size: `scene = new Scene(root, 600, 500)`.
- Uses `VBox` with `ScrollPane`.
- Containers for dynamic content: `overallBox`, `themeBox`, `errorBox` (all `VBox`).
- `clearContent()` method empties all dynamic containers.
- Button styling: `.setStyle("-fx-padding: 8 20 8 20;")`.

**Changes needed:**
- The `errorBox` section ("Questões com Maior Taxa de Erro") will need to be relabeled to "Questões com Menor Pontuação" or similar.
- Display score values instead of error percentages.

---

### `controller/ThemeSelectionController.java` — refreshHitRates → refreshScores

**Analog:** `controller/ThemeSelectionController.java` (itself, lines 48-53)

**Current refresh pattern:**
```java
public void refreshHitRates() {
    for (Theme theme : themes) {
        String hitRate = statsService.getHitRate(theme.getName());
        view.updateHitRate(theme.getName(), hitRate);
    }
}
```

**Initialize pattern** (lines 29-46):
```java
public void initialize() {
    themes = themeLoader.loadAllThemes();
    view.setThemes(themes);
    refreshHitRates();                  // ← will become refreshScores()

    int maxQuestions = themes.stream()
            .mapToInt(Theme::getQuestionCount)
            .max()
            .orElse(1);
    view.updateQuestionCountRange(maxQuestions);

    screenController.registerScreen("themeSelection", view.getScene());
    view.getStartButton().setOnAction(e -> handleStart());
    view.getRelatoriosButton().setOnAction(e -> handleRelatorios());
    screenController.switchTo("themeSelection");
}
```

**Constructor injection pattern** (lines 21-27):
```java
public ThemeSelectionController(ThemeLoader themeLoader, ThemeSelectionView view,
                                ScreenController screenController, StatsService statsService) {
    this.themeLoader = themeLoader;
    this.view = view;
    this.screenController = screenController;
    this.statsService = statsService;
}
```

**Dependencies listed in constructor** — all controllers follow this pattern (see `StudyRoundController` lines 21-27, `ReportsController` lines 17-21).

**Callback pattern** (lines 74-77 in `handleStart`):
```java
studyRoundController.setOnRoundEndCallback(() -> {
    refreshHitRates();      // ← will become refreshScores()
    screenController.switchTo("themeSelection");
});
```

---

### `controller/StudyRoundController.java` — Track score deltas

**Analog:** `controller/StudyRoundController.java` (itself, lines 1-99)

**Round tracking pattern** (lines 57-80):
```java
private void handleOptionClick(int index) {
    boolean correct = roundState.checkAnswer(index);

    if (correct) {
        view.highlightCorrect(index);
    } else {
        view.highlightWrong(index, roundState.getCurrentCorrectIndex());
    }

    results.add(new RoundResult(
            roundState.getCurrentThemeName(),
            roundState.getCurrentQuestion().getQuestion(),
            correct
    ));

    view.disableOptions(true);

    PauseTransition pause = new PauseTransition(Duration.seconds(1));
    pause.setOnFinished(e -> {
        roundState.advanceToNext();
        showCurrentQuestion();
    });
    pause.play();
}
```

**Changes needed:**
- Replace the `results.add(...)` call to use the new `RoundResult` constructor that includes `questionId` and `scoreDelta`.
- The score delta should come from `roundState.checkAnswer(index)` which will need to return or expose the score delta (+2 or -3).

---

### `themes/*.json` — Add `id` field to every question

**Analog:** `themes/java-record.json` (lines 1-387), `themes/java-modificadores.json` (lines 1-248)

**Current JSON structure:**
```json
[
  {
    "question": "Qual é o principal objetivo de um record em Java?",
    "options": [...],
    "correct": 1
  }
]
```

**Pattern to follow:**
- Each question object gets a new `"id"` field.
- Suggestions: `"id": "q1"`, `"id": "q2"`, etc., or more descriptive like `"java-record-001"`.
- This affects **both** theme files (`java-record.json` has 30 questions, `java-modificadores.json` has 25 questions).
- The `id` field must match the type declared in `Question.java` (String or int). Int IDs would be simpler.

---

## Shared Patterns

### Constructor Injection Pattern
**Source:** All controllers (`ThemeSelectionController` lines 21-27, `StudyRoundController` lines 21-27, `ReportsController` lines 17-21)

All controllers follow the same pattern:
```java
public class SomeController {
    private final ServiceType service;
    private final ViewType view;
    private final ScreenController screenController;

    public SomeController(ServiceType service, ViewType view, ScreenController screenController) {
        this.service = service;
        this.view = view;
        this.screenController = screenController;
    }
}
```

### Initialize → Register Screen → Set Handlers → Switch To
**Source:** `ThemeSelectionController` lines 29-46, `StudyRoundController` lines 33-42, `ReportsController` lines 23-27

Every controller follows this lifecycle:
```java
public void initialize() {
    // 1. Do any data loading / setup
    // 2. Register the scene with screenController
    screenController.registerScreen("screenName", view.getScene());
    // 3. Set event handlers on view
    view.getSomeButton().setOnAction(e -> handleSomething());
    // 4. Switch to the screen
    screenController.switchTo("screenName");
}
```

### View Scene Exposure Pattern
**Source:** `ThemeSelectionView` line 59, `StudyRoundView` lines 93-95, `ReportsView` line 63

Every view exposes its Scene for registration:
```java
private final Scene scene;
// ...
public Scene getScene() { return scene; }
```

### File-Based JSON Persistence Pattern
**Source:** `StatsService` lines 20-53, `ThemeLoader` lines 16-23

Both services use:
```java
private static final String FILE_PATH = System.getProperty("user.dir") + File.separator + "filename";
private static final Logger LOG = Logger.getLogger(ClassName.class.getName());
private final ObjectMapper mapper;
```

### Shuffled Options Pattern
**Source:** `RoundState` lines 40-46, 94-101

Questions always have their options shuffled when creating a round:
```java
List<String> original = q.getOptions();
String correctText = original.get(q.getCorrect());
List<String> shuffled = new ArrayList<>(original);
Collections.shuffle(shuffled);
int newCorrect = shuffled.indexOf(correctText);
questions.add(new RoundQuestion(theme.getName(), q, shuffled, newCorrect));
```

### JavaFX Pause Transition Pattern
**Source:** `StudyRoundController` lines 74-78

Delayed navigation after answering:
```java
PauseTransition pause = new PauseTransition(Duration.seconds(1));
pause.setOnFinished(e -> {
    roundState.advanceToNext();
    showCurrentQuestion();
});
pause.play();
```

---

## Testing Patterns

### Test File Naming
**Source:** `src/test/java/org/IsmaelSS/service/StatsServiceTest.java`, `src/test/java/org/IsmaelSS/model/StatsDataTest.java`, `src/test/java/org/IsmaelSS/model/RoundStateReinforcementTest.java`

- Test class name: `{ClassName}Test.java`
- Test class scope: package-private (no `public` modifier)
- Located in same package structure as the class under test

### Imports Pattern
```java
package org.IsmaelSS.service;   // same package as tested class

import org.IsmaelSS.model.RoundResult;
import org.IsmaelSS.model.StatsData;
import org.junit.jupiter.api.AfterEach;     // if cleanup needed
import org.junit.jupiter.api.BeforeEach;    // if cleanup needed
import org.junit.jupiter.api.Test;

import java.io.File;                         // for cleanup
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;  // static import all assertions
```

### Test Setup/Cleanup Pattern
**Source:** `StatsServiceTest` lines 20-28, `RoundStateReinforcementTest` lines 20-28

Tests that touch `flashcard-stats.json` must clean up before and after:
```java
private static final String STATS_FILE = System.getProperty("user.dir")
        + File.separator + "flashcard-stats.json";

@BeforeEach
void cleanUp() {
    new File(STATS_FILE).delete();
}

@AfterEach
void tearDown() {
    new File(STATS_FILE).delete();
}
```

### Test Method Naming
- camelCase describing the scenario: `constructorCreatesEmptyDataWhenNoFile`, `recordRoundSingleAnswer`, `highestErrorQuestionsRanking`
- Uses `@Test` annotation (JUnit 5)

### Assertion Style
- `assertEquals(expected, actual)` — primary assertion
- `assertTrue(condition)` / `assertFalse(condition)`
- `assertNotNull(obj)` / `assertNull(obj)`
- `assertTrue(condition)` with lambda: `assertTrue(texts.contains("t1q1"))`
- For doubles: `assertEquals(expected, actual, delta)` with delta `0.001`

### Test Helper Methods Pattern
**Source:** `RoundStateReinforcementTest` lines 30-45

```java
private Question createQ(String text, String... options) {
    return new Question(text, Arrays.asList(options), 0);
}

private Theme createTheme(String name, Question... questions) {
    return new Theme(name, Arrays.asList(questions));
}

private List<String> collectTexts(RoundState state) {
    List<String> texts = new ArrayList<>();
    while (!state.isComplete()) {
        texts.add(state.getCurrentQuestion().getQuestion());
        state.advanceToNext();
    }
    return texts;
}
```

### Serialization Test Pattern
**Source:** `StatsDataTest` lines 12-40

```java
private final ObjectMapper mapper = new ObjectMapper();

@Test
void roundTripSerialization() throws Exception {
    // Build
    StatsData original = new StatsData();
    // ... populate ...

    // Serialize + deserialize
    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(original);
    StatsData deserialized = mapper.readValue(json, StatsData.class);

    // Assert
    assertEquals(expected, deserialized.getSomething());
}
```

---

## No Analog Found

All files have direct analogs — they are all modifications to existing files. No purely new files are being created in this phase.

---

## Metadata

**Analog search scope:** `src/main/java/org/IsmaelSS/` (all 15 Java source files), `src/test/java/org/IsmaelSS/` (all 3 test files), `themes/` (2 JSON files)
**Files scanned:** 20
**Pattern extraction date:** 2026-07-08
