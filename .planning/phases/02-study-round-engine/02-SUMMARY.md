# Phase 2 Summary: Study Round Engine

**Completed:** 2026-07-07
**Verification:** `mvn clean compile` ✓ | `mvn package` ✓

## What was built

### New files
| File | Purpose |
|------|---------|
| `src/main/java/org/IsmaelSS/model/RoundState.java` | Round state model — shuffles questions and options, tracks correct answers, manages round lifecycle |
| `src/main/java/org/IsmaelSS/view/StudyRoundView.java` | Question display with 5 clickable option buttons, correct/wrong highlighting, round-complete screen |
| `src/main/java/org/IsmaelSS/controller/StudyRoundController.java` | Coordinates round lifecycle — displays questions, handles answer clicks with PauseTransition feedback, advances through questions |

### Modified files
| File | Change |
|------|--------|
| `ThemeSelectionController.java` | Start button now creates RoundState + StudyRoundController and switches to study round scene |

### Requirements delivered
- **UI-02** ✓ — Question screen with 5 clickable alternatives
- **UI-03** ✓ — Correct (green) / wrong (red) highlighting
- **ROUND-01** ✓ — Questions shuffled via `Collections.shuffle()`
- **ROUND-02** ✓ — No repeats (each question appears once, sequential pointer)
- **ROUND-03** ✓ — Alternatives shuffled per question, correct index recalculated
- **ROUND-05** ✓ — Round ends when all questions answered; exit button returns to theme selection

## Key details
- RoundState extracts up to `questionsPerTheme` from each selected theme, flattens, shuffles, then shuffles options per question
- Correct answer tracked by storing original correct text, finding its index after shuffle
- `PauseTransition` (1s) provides non-blocking delay between questions
- "Sair" button available at all times; round-complete screen shows score + "Voltar" button
- In-memory scoring only — no stats persistence (Phase 3)
- UI-04 (back navigation from reports) deferred to Phase 4
