# Phase 6: Scoring System & Question ID Rework

## PRD (from user)

Redesign of the reinforcement/scoring system:

1. **Question IDs** — All questions inside theme JSON files must have a unique `id` field to be referenced in scoring
2. **Negative scoring on wrong answer** — Each wrong answer deducts -3 points from the question, floor at -10 minimum
3. **Positive scoring on correct answer** — Each correct answer adds +2 points to the question, cap at +5 maximum
4. **Replace hit rate with aproveitamento** — Theme selection screen must show aproveitamento score instead of hit rate percentage
5. **Aproveitamento formula** — Questions with negative score → weight -3, questions with zero/neutral score → weight 0, questions with positive score → weight +2

## Requirements from user

- Questions need an `id` field inside the theme JSON model
- Wrong answer: -3 per miss, floor at -10 per question
- Right answer: +2 per hit, cap at +5 per question
- Theme selection screen: show "pontuação de aproveitamento" instead of hit rate
- Aproveitamento calculation: negative questions weight -3, zero-score questions weight 0, positive questions weight +2
- Portuguese description but summary in English

## Phase Goals

1. Add `id` field to Question model → update Theme JSON files (`themes/`) with IDs
2. Refactor StatsData/StatsService to track per-question score (int, -10..+5 range)
3. Replace error-rate-based logic with score-based logic in RoundState createReinforcementRound
4. Update ThemeSelectionView to show aproveitamento score instead of hit rate
5. Ensure all existing tests still pass; add new tests for scoring logic

## Specific changes needed

- `Question.java` — add `id` field (String or int)
- `Theme.java` — no change needed unless we need ID validation
- `StatsData.java` — replace `correctCount`/`wrongCount` per question with a `score` int field; remove `hitRate` concept
- `StatsService.java` — update load/save, replace hit rate calculations with score calculations; update `getHighestErrorQuestions` → `getLowestScoreQuestions`
- `RoundState.java` — update `createReinforcementRound` to use score-based selection
- `ThemeSelectionView.java` — replace hit rate display with aproveitamento score
- `ThemeSelectionController.java` — update refreshHitRates → refreshScores
- `StudyRoundController.java` — update round result to track score deltas
- `StudyRoundView.java` — no change needed (just shows question)
- `ReportsView.java` — may need score display instead of hit rate
- All theme JSON files — add `id` to every question

## Files affected

- `src/main/java/org/IsmaelSS/model/Question.java`
- `src/main/java/org/IsmaelSS/model/StatsData.java`
- `src/main/java/org/IsmaelSS/model/RoundState.java`
- `src/main/java/org/IsmaelSS/model/RoundResult.java`
- `src/main/java/org/IsmaelSS/service/StatsService.java`
- `src/main/java/org/IsmaelSS/view/ThemeSelectionView.java`
- `src/main/java/org/IsmaelSS/view/ReportsView.java`
- `src/main/java/org/IsmaelSS/controller/ThemeSelectionController.java`
- `src/main/java/org/IsmaelSS/controller/StudyRoundController.java`
- `themes/*.json` — all theme files need `id` fields
