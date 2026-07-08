---
phase: 06-scoring-system-question-id-rework
plan: 03
subsystem: views-controllers-tests
tags: aproveitamento, pontuação, java, javafx, junit
requires:
  - phase: 06-02
    provides: StatsService with getAproveitamento(), getLowestScoreQuestions(), QuestionScore
provides:
  - ThemeSelectionView with updateAproveitamento() replacing updateHitRate()
  - ThemeSelectionController.refreshScores() replacing refreshHitRates()
  - ReportsView section renamed to "Questões com Menor Pontuação"
  - ReportsController using getLowestScoreQuestions() and getAproveitamento()
  - Rewritten StatsServiceTest (15 tests), StatsDataTest (7 tests), RoundStateReinforcementTest (8 tests)
  - Bug fix: QuestionScore @JsonIgnoreProperties for old-format migration
  - Bug fix: createReinforcementRound sort order (ascending for lowest scores first)
affects: none (UI-layer only, all downstream phases complete)

tech-stack:
  added: []
  patterns: []

key-files:
  created: []
  modified:
    - src/main/java/org/IsmaelSS/view/ThemeSelectionView.java
    - src/main/java/org/IsmaelSS/controller/ThemeSelectionController.java
    - src/main/java/org/IsmaelSS/view/ReportsView.java
    - src/main/java/org/IsmaelSS/controller/ReportsController.java
    - src/main/java/org/IsmaelSS/model/StatsData.java
    - src/main/java/org/IsmaelSS/model/RoundState.java
    - src/test/java/org/IsmaelSS/service/StatsServiceTest.java
    - src/test/java/org/IsmaelSS/model/StatsDataTest.java
    - src/test/java/org/IsmaelSS/model/RoundStateReinforcementTest.java

key-decisions:
  - "Question IDs shown in reports (descriptive slugs like 'record-primary-purpose') — acceptable per T-06-08 accept disposition"
  - "errorBox field name kept as-is for backward compatibility — comment added"

patterns-established:
  - "Methods renamed from hit-rate to score semantics (updateHitRate → updateAproveitamento, refreshHitRates → refreshScores)"

requirements-completed:
  - SCORE-04
  - SCORE-05

duration: 5min
completed: 2026-07-08
---

# Phase 6 Plan 03: View/Controller/Test Updates for Score-Based Model Summary

**Updated ThemeSelectionView/Controller and ReportsView/Controller to use score-based semantics instead of hit rates. Rewrote all three test files for the new QuestionScore model. Fixed two bugs discovered during test execution.**

## Performance

- **Duration:** 5 min
- **Started:** 2026-07-08T13:10:00Z
- **Completed:** 2026-07-08T13:14:48Z
- **Tasks:** 3
- **Files modified:** 9

## Accomplishments

- ThemeSelectionView now shows "Pontuação: N/A" instead of "Hit rate: N/A" on initial load
- `updateAproveitamento()` replaces `updateHitRate()` with score display
- `ThemeSelectionController.refreshScores()` replaces `refreshHitRates()`, calls `statsService.getAproveitamento()`
- ReportsView section header updated to "Questões com Menor Pontuação"
- ReportsController uses `getLowestScoreQuestions()` showing score values and per-theme aproveitamento
- ReportsController no longer calls removed `getOverallHitRate()` — section removed
- All 30 tests pass (15 StatsService, 7 StatsData, 8 RoundStateReinforcement)

## Task Commits

1. **Task 1: Update ThemeSelectionView/Controller** — `1b6a250` (feat)
2. **Task 2: Update ReportsView/Controller** — `b9e3a1f` (feat)
3. **Task 3: Rewrite tests + bug fixes** — `82e1721` (feat)

## Files Modified

- `src/main/java/org/IsmaelSS/view/ThemeSelectionView.java` — `updateAproveitamento()` replaces `updateHitRate()`, label text to "Pontuação: N/A"
- `src/main/java/org/IsmaelSS/controller/ThemeSelectionController.java` — `refreshScores()` replaces `refreshHitRates()`
- `src/main/java/org/IsmaelSS/view/ReportsView.java` — Section label to "Questões com Menor Pontuação", comment above errorBox
- `src/main/java/org/IsmaelSS/controller/ReportsController.java` — Uses `getAproveitamento()` and `getLowestScoreQuestions()`, removed overall hit rate line
- `src/main/java/org/IsmaelSS/model/StatsData.java` — Added `@JsonIgnoreProperties(ignoreUnknown = true)` to QuestionScore
- `src/main/java/org/IsmaelSS/model/RoundState.java` — Fixed sort order in `createReinforcementRound()` to ascending
- `src/test/java/org/IsmaelSS/service/StatsServiceTest.java` — 15 tests for score model
- `src/test/java/org/IsmaelSS/model/StatsDataTest.java` — 7 tests for QuestionScore
- `src/test/java/org/IsmaelSS/model/RoundStateReinforcementTest.java` — 8 tests with score-based selection

## Decisions Made

- **Question IDs in reports:** Descriptive slug IDs shown (not question text) — acceptable per T-06-08 accept disposition
- **errorBox field name preserved:** Internal field name kept as `errorBox` with clarifying comment, avoiding unnecessary renames that would break no functionality

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] QuestionScore lacks @JsonIgnoreProperties, blocking old-format migration**
- **Found during:** Task 3 (StatsServiceTest.migrationFromOldFormatPreservesThemeTotals)
- **Issue:** Old-format JSON has `answered`/`correct` fields which QuestionScore cannot deserialize, causing IOException and data loss
- **Fix:** Added `@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)` to QuestionScore class
- **Files modified:** src/main/java/org/IsmaelSS/model/StatsData.java
- **Verification:** Migration test now passes — old format loads, theme totals preserved, per-question data reset
- **Committed in:** 82e1721 (Task 3 commit)

**2. [Rule 1 - Bug] createReinforcementRound sorts descending, should be ascending**
- **Found during:** Task 3 (RoundStateReinforcementTest.createReinforcementRoundPrioritizesLowestScoreQuestions)
- **Issue:** Sort comparator was `b.score - a.score` (descending, highest scores first), but reinforcement should pick lowest scores first
- **Fix:** Changed to `a.score - b.score` (ascending, lowest scores first)
- **Files modified:** src/main/java/org/IsmaelSS/model/RoundState.java
- **Verification:** Reinforcement priority test now passes — lowest-scoring questions selected first
- **Committed in:** 82e1721 (Task 3 commit)

---

**Total deviations:** 2 auto-fixed (2 Rule 1 - Bug)
**Impact on plan:** Both fixes essential for correctness. No scope creep.

## Issues Encountered

None — all issues were auto-fixed via deviation rules.

## User Setup Required

None — no external service configuration required.

## Next Phase Readiness

- All views and controllers updated for score-based model
- All tests rewritten and passing
- Phase 6 complete — ready for next phase or milestone completion

---

*Phase: 06-scoring-system-question-id-rework*
*Completed: 2026-07-08*
