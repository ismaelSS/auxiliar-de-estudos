# Phase 4 Context: Reinforcement Mode & Navigation Polish

## Domain
Add a reinforcement mode that prioritizes questions the user has historically gotten wrong, improving weak-area targeting. Navigation between screens is already adequate from Phase 3.

## Requirements Covered
- **ROUND-04**: Reinforcement mode prioritizes questions with highest error rate from past sessions
- **UI-04**: Navigation polish — already satisfied by Phase 3 (Relatórios button on theme selection, Voltar on reports)

## Decisions

### Reinforcement Mode Activation
- A checkbox labeled "Modo Reforço" on the theme selection screen, placed near the question count spinner
- When checked, the round uses error-weighted question selection instead of the default random/sequential selection
- When unchecked, behavior is identical to Phase 2/3 (random shuffle from selected themes)

### Question Selection Algorithm (Error-Weighting)
- For each selected theme, pull questions with the highest error rate from `StatsService.getHighestErrorQuestions()`
- These error-weighted questions appear first in the round
- If the weighted pool is smaller than the requested count per theme, fill remaining slots with random questions from that theme
- If the weighted pool is larger, take only the top N (up to the requested question count per theme)
- Same total question count per theme as the spinner value — the toggle only changes *which* questions are selected, not *how many*

### Navigation Polish
- No changes needed. Phase 3 already implemented:
  - Relatórios button on theme selection → reports screen
  - Voltar button on reports → theme selection
  - Post-round callback returns to theme selection with refreshed hit rates
- UI-04 is considered satisfied by existing Phase 3 implementation

### Dependencies
- **Phase 3 provides**: `StatsService.getHighestErrorQuestions()`, `StatsData` with per-question error tracking, `RoundState` with theme-aware question construction
- **No changes needed**: ReportsView, ReportsController, StudyRoundView, StudyRoundController, ScreenController, Main.java

## Architecture

### RoundState Modification
- Add constructor overload or builder flag for "reinforcement mode"
- In reinforcement mode:
  1. Query StatsService for highest-error questions per theme
  2. Deduplicate by question text
  3. Separate into "error" and "fresh" pools
  4. Take all error questions (up to questionsPerTheme), fill remaining with fresh
  5. Shuffle final pool (mix error + fresh together — user doesn't know which is which)

### ThemeSelectionView Changes
- Add `CheckBox reforcoCheckBox` field
- Add getter: `isReinforcementMode()`

### ThemeSelectionController Changes
- Read checkbox state in `handleStart()`
- Pass reinforcement flag to RoundState (or toggle selection algorithm)
- RoundState uses StatsService to select questions when reinforcement is on

## Canonical Refs
- `.planning/REQUIREMENTS.md` — ROUND-04, UI-04
- `.planning/ROADMAP.md` — Phase 4 goal and success criteria
- `src/main/java/org/IsmaelSS/service/StatsService.java` — `getHighestErrorQuestions()`
- `src/main/java/org/IsmaelSS/model/RoundState.java` — constructor to modify

## Code Context
- **StatsService.getHighestErrorQuestions(limit)**: Returns `List<Map.Entry<String, Double>>` sorted by error rate descending. Already used by ReportsController.
- **RoundState constructor**: Takes `List<Theme> themes, int questionsPerTheme`. Currently takes first N questions per theme, then shuffles. Needs alternate mode for error-weighting.
- **ThemeSelectionView**: Has theme checkboxes, question count spinner, start button, Relatórios button. Adding a checkbox.
- **ThemeSelectionController.handleStart()**: Creates RoundState then StudyRoundController. Currently passes selected themes + count. Will also pass StatsService + reinforcement flag.
