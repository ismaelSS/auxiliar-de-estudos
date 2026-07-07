# Plan: Phase 2 — Study Round Engine

## Goal
Implement the core study round: display questions with shuffled alternatives, handle answer clicks with green/red feedback, randomize question and alternative order per round, prevent repeats, and complete the round.

## Verification
- `mvn clean compile` passes
- `mvn package` passes
- Manual: `mvn javafx:run` — start round from theme selection, verify question display, click answer → feedback colors, advance through all questions, round completion screen appears
- Manual: verify no question repeats within a round
- Manual: verify alternatives appear in different order each round
- Manual: verify "Sair" button exits round back to theme selection

## Wave 1: Round State Model + Controller Logic

### Task 1.1 — Create `model/RoundState.java`
**File:** `src/main/java/org/IsmaelSS/model/RoundState.java`
- Fields: `List<Question> questions` (flattened from selected themes, shuffled), `Map<Question, List<String>> shuffledOptions`, `Map<Question, Integer> shuffledCorrectIndices`, `int currentIndex`, `Set<Integer> answeredIndices`, `int correctCount`
- Constructor: accepts `List<Theme>` and `int questionsPerTheme`; extracts X questions per theme, shuffles question list with `Collections.shuffle()`, then for each question shuffles options and computes new correct index
- Methods:
  - `isComplete()` — currentIndex >= questions.size()
  - `getCurrentQuestion()` — questions.get(currentIndex)
  - `getCurrentOptions()` — shuffledOptions.get(currentQuestion)
  - `getCurrentCorrectIndex()` — shuffledCorrectIndices.get(currentQuestion)
  - `checkAnswer(int selectedIndex)` — records answer, increments correctCount if match, returns boolean
  - `advanceToNext()` — increments currentIndex
  - `getProgress()` — "X / Y"
  - `getCorrectCount()` / `getTotalAnswered()`
  - `getSelectedQuestionsCount()` — total questions in round

### Task 1.2 — Create `controller/StudyRoundController.java`
**File:** `src/main/java/org/IsmaelSS/controller/StudyRoundController.java`
- Constructor args: `RoundState`, `StudyRoundView`, `ScreenController`
- `initialize()` — registers scene in ScreenController, shows first question
- `showCurrentQuestion()` — calls view.setQuestion() with current question data
- `handleOptionClick(int index)` — calls RoundState.checkAnswer(), updates view with highlightFeedback(), starts PauseTransition(1s) → advanceOrFinish()
- `handleExit()` — switches to themeSelection screen
- `advanceOrFinish()` — if RoundState.isComplete() → showRoundComplete() else advanceToNext() + showCurrentQuestion()
- `showRoundComplete()` — displays completion view with score, "Voltar" button to theme selection

## Wave 2: Study Round View

### Task 2.1 — Create `view/StudyRoundView.java`
**File:** `src/main/java/org/IsmaelSS/view/StudyRoundView.java`
- Programmatic Scene in a VBox layout
- Components: question Label (bold, wrapped), 5 option Buttons (full width, styled), progress Label ("Questão X / Y"), exit Button ("Sair"), roundComplete VBox (overlay or separate content)
- Button styling: normal state `-fx-background-color: #E0E0E0; -fx-text-fill: black;`, hover `-fx-cursor: hand;`
- `setQuestion(String question, List<String> options)` — updates text and option buttons
- `highlightCorrect(int correctIndex)` — sets correct button to green `#4CAF50`
- `highlightWrong(int wrongIndex, int correctIndex)` — sets wrong button to red `#F44336`, correct button to green
- `clearHighlights()` — resets all buttons to default color
- `disableOptions(boolean)` — enable/disable click interaction
- `updateProgress(String text)` — updates progress label
- `showRoundComplete(int correct, int total)` — replaces question content with completion message and "Voltar" button
- `setOnOptionClick(Consumer<Integer> handler)` — attach click handler
- `setOnExit(Runnable handler)` — attach exit handler
- `setOnVoltar(Runnable handler)` — attach volta (round complete) handler

## Wave 3: Integration

### Task 3.1 — Update `controller/ThemeSelectionController.java`
- In `handleStart()`: remove placeholder message, construct RoundState from selected themes + question count, construct StudyRoundView + StudyRoundController, register scene in ScreenController, switch to "studyRound" scene
- Import new classes

### Task 3.2 — Update `Main.java`
- No significant changes needed if ThemeSelectionController handles all wiring
- If ScreenController or other wiring needed, add minimal initialization

### Task 3.3 — Verify ScreenController compatibility
- ScreenController already has generic `registerScreen()` / `switchTo()` — no changes needed
- Ensure "studyRound" scene key is used consistently

## Wave 4: Verification

### Task 4.1 — Compile verification
- `mvn clean compile` — must pass with zero errors

### Task 4.2 — Package verification  
- `mvn package` — must produce executable JAR

### Task 4.3 — Manual runtime verification
- Run `mvn javafx:run`
- Verify: theme selection screen loads, select themes, click "Iniciar"
- Verify: question appears with 5 buttons
- Verify: clicking correct answer → green highlight
- Verify: clicking wrong answer → red (selected) + green (correct)
- Verify: after ~1s delay, next question appears
- Verify: no question repeats
- Verify: "Sair" button returns to theme selection
- Verify: after last question, round complete screen with score
- Verify: "Voltar" on round complete returns to theme selection
