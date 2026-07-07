# Phase 2 Context: Study Round Engine

## Phase Goal
Implement the core study round — question display with 5 randomized alternatives, answer feedback (correct green / wrong red), question-level randomization (shuffle order), alternative-level randomization (shuffle options), no-repeat logic within a round, and round-completion detection.

## Requirements Covered
- **UI-02**: Question screen displays question text and 5 alternatives as clickable elements
- **UI-03**: After answering, correct alternative highlighted green, wrong selection (if any) highlighted red
- **ROUND-01**: Questions served in random order within a round
- **ROUND-02**: No question repeats within the same round
- **ROUND-03**: Alternative order randomized each round
- **ROUND-05**: Round ends when all selected questions have been answered or user exits

## Architecture & Design Decisions

### Round State Model (`model/RoundState.java`)
- Encapsulates all mutable round state in one plain object
- Stores: flat list of selected questions, per-question shuffled options list, per-question correct answer index after shuffle, current question pointer, answered-index set
- Provides `isComplete()`, `getCurrentQuestion()`, `getCurrentOptions()`, `getCurrentCorrectIndex()`, `checkAnswer(int)`, `advanceToNext()`
- Questions are shuffled at construction time (ROUND-01), answered set prevents repeats (ROUND-02)
- Options are shuffled per-question at construction time (ROUND-03)
- **Correct answer tracking after shuffle**: store the original correct answer text, then find its index in the shuffled list

### StudyRoundController
- Creates RoundState from selected themes + question count
- Wires RoundState to StudyRoundView (bidirectional: view dispatches clicks, controller updates view)
- Handles: `startRound()`, `onOptionClicked(int)`, `advance()`, `finishRound()`
- On answer: highlights correct (green) and wrong selection (red) on view, after ~1s delay advances via `PauseTransition`

### StudyRoundView (`view/StudyRoundView.java`)
- Programmatic JavaFX scene (no FXML, consistent with Phase 1)
- Layout: VBox with question text Label at top, 5 option Button(s) below, optional progress indicator
- Methods: `setQuestion(String, List<String>)`, `highlightCorrect(int)`, `highlightWrong(int, int)`, `clearHighlights()`, `showRoundComplete()`
- Option buttons styled via inline `-fx-background-color` on hover/normal state
- Disables all option buttons after selection until advance

### Integration Points
- **ThemeSelectionController.handleStart()**: instead of placeholder feedback, creates StudyRoundController, registers its scene in ScreenController, switches to it
- **Main.java**: passes ScreenController to StudyRoundController wiring
- **No changes to ThemeLoader, Question, Theme, or ScreenController interface** (ScreenController already has generic `registerScreen`/`switchTo`)

### Answer Feedback UX
- Correct: selected button background → green (`#4CAF50`)
- Wrong: selected button background → red (`#F44336`); correct button (not selected) → green (`#4CAF50`)
- All buttons disabled during feedback
- `PauseTransition` (1 second) then advance to next question
- After last question: show "Round complete!" message, provide button to return to theme selection

### Round Termination (ROUND-05)
- Automatic: when `currentIndex >= questions.size()`
- User-initiated: "Sair" button available at all times during round → returns to theme selection (no stats saved yet — Phase 3)

### No Stats Persistence in Phase 2
- Answer correctness tracked in-memory only (RoundState holds `correctCount` / `totalAnswered`)
- Stats persistence and reports deferred to Phase 3
- No `flashcard-stats.json` reads or writes in Phase 2

## Dependencies
- **Phase 1 provides**: ScreenController (scene registry), ThemeLoader, Question/Theme models, theme selection UI
- **Phase 3 depends on**: Phase 2's answer correctness data (expanded RoundState or new service in Phase 3)

## Open Questions
- (None resolved — all design decisions covered above)
