# Features Research: FlashCard JavaFX

## Table Stakes (Must Have)

- Question display with multiple choice selection
- Correct/incorrect answer feedback
- Score tracking during a round
- Ability to define question sets (themes/topics)

## Differentiators

- **Theme-based hit rate display** before round starts
- **Reinforcement mode** prioritizing high-error questions
- **Hot-pluggable JSON questions** — add/edit files post-compilation
- **Full performance persistence** across sessions
- **Randomized alternatives** to prevent memorization of positions
- **No question repetition** within a round

## Feature Categories (for this project)

### Theme Management
- Auto-detect themes from JSON files in a dedicated folder
- Display hit rate per theme during selection
- Support any number of themes

### Study Round
- Configurable theme selection (one or multiple themes per round)
- Configurable question count per round
- Random question order, no repeats
- Random alternative order
- Reinforcement mode for weak questions

### Question Display & Feedback
- Show question text and 5 alternatives as clickable buttons
- After answering: highlight correct (green) and wrong selection (red)
- Immediate visual feedback

### Performance Tracking
- Per-theme stats: questions answered, hit rate, per-question history
- Overall stats aggregated across all themes
- Identify questions with highest error rate
- Persist to a separate JSON file

### Reports
- Error/hit summary screen
- Highest-error questions ranking
- Per-theme breakdown
