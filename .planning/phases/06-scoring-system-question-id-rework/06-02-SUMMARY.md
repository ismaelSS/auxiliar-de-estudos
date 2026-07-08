---
phase: 06-scoring-system-question-id-rework
plan: 02
subsystem: scoring
tags: [java, jackson, scoring, migration, question-id]
requires:
  - phase: 03-performance-tracking-reports
    provides: StatsData/StatsService foundation (QuestionStats, hit rates)
  - phase: 06-01-question-id
    provides: Question.id field, RoundResult.questionId()
provides:
  - QuestionScore inner class (bounded int score -10..+5)
  - Migration from old format (question-text keys) to new format (ID keys)
  - Score-delta recordRound() using questionId()
  - getAproveitamento() (weight sum formula)
  - getLowestScoreQuestions() (ascending score sort)
  - Updated createReinforcementRound() using score-based selection
affects:
  - 06-03-views-controllers-tests
tech-stack:
  added: []
  patterns: Score-based per-question tracking, old-format JSON migration, weight-sum aproveitamento
key-files:
  created: []
  modified:
    - src/main/java/org/IsmaelSS/model/StatsData.java
    - src/main/java/org/IsmaelSS/service/StatsService.java
    - src/main/java/org/IsmaelSS/model/RoundState.java
key-decisions:
  - "Migration heuristic: key length > 20 or contains space → old format"
  - "Old per-question answered/correct data is reset on migration (not convertible to bounded score)"
  - "getAproveitamento weight formula: negative → -3, zero → 0, positive → +2"
  - "getLowestScoreQuestions sorts ascending (lowest first), createReinforcementRound sorts descending internally for worst-first processing"
patterns-established:
  - "Per-question score bounded to [-10, +5] via Math.min/Math.max"
  - "Old-format migration preserves theme-level totalAnswered/totalCorrect"
requirements-completed: [SCORE-02, SCORE-03]
duration: 7 min
completed: 2026-07-08
---

# Phase 6 Plan 2: Scoring Engine — QuestionScore, migration, score-delta recordRound, aproveitamento, lowest-score query

**QuestionScore inner class with bounded -10..+5 scoring, old-format JSON migration, score-delta recordRound() keyed by question ID, aproveitamento weight-sum formula, and lowest-score question query; RoundState.createReinforcementRound() updated for score-based selection**

## Performance

- **Duration:** 7 min
- **Started:** 2026-07-08T16:02:03Z
- **Completed:** 2026-07-08T16:09:31Z
- **Tasks:** 4
- **Files modified:** 3

## Accomplishments

- Replaced `QuestionStats` (answered/correct counters) with `QuestionScore` (bounded int score -10..+5) with `recordCorrect()` (+2, cap +5) and `recordWrong()` (-3, floor -10)
- Updated `ThemeStats.questions` from `Map<String, QuestionStats>` to `Map<String, QuestionScore>` (keyed by question ID)
- Added old-format detection via `isOldFormat()` heuristic (key length > 20 or contains space) and `migrateOldFormat()` preserving theme-level totals
- Rewrote `recordRound()` to use `result.questionId()` for map key and `QuestionScore.recordCorrect()/recordWrong()` for score deltas
- Added `getAproveitamento(String themeName)` returning weight sum: negative scores → -3, zero → 0, positive → +2
- Added `getLowestScoreQuestions(int limit)` returning entries sorted ascending by score
- Removed `getHitRate()`, `getOverallHitRate()`, `getHighestErrorQuestions()` methods
- Updated `RoundState.createReinforcementRound()` to call `getLowestScoreQuestions(50)` with `Map<String, Integer>` and `Integer.compare()` sorting

## Task Commits

Each task was committed atomically:

1. **Task 1: Create QuestionScore, update ThemeStats, remove QuestionStats** — `ae1aaa6` (feat)
2. **Task 2: Add migration logic and rewrite recordRound() with score deltas** — `4e79955` (feat)
3. **Task 3: Add getAproveitamento(), getLowestScoreQuestions(), remove old methods** — `204eb56` (feat)
4. **Task 4: Update createReinforcementRound() to use getLowestScoreQuestions()** — `f0c8026` (feat)

## Files Modified

- `src/main/java/org/IsmaelSS/model/StatsData.java` — Added `QuestionScore` inner class, changed `ThemeStats.questions` to `Map<String, QuestionScore>`, removed `QuestionStats`
- `src/main/java/org/IsmaelSS/service/StatsService.java` — Added migration logic (`isOldFormat`, `migrateOldFormat`), rewritten `recordRound()`, added `getAproveitamento()`, `getLowestScoreQuestions()`, removed `getHitRate()`, `getOverallHitRate()`, `getHighestErrorQuestions()`
- `src/main/java/org/IsmaelSS/model/RoundState.java` — Updated `createReinforcementRound()` to use `getLowestScoreQuestions()` with `Map<String, Integer>` and `Integer.compare()`

## Decisions Made

- **Migration heuristic:** Old format detected by key length > 20 or containing space — low false-positive risk for a local app
- **Per-question data reset on migration:** Old answered/correct counters are not convertible to bounded score values, so per-question scores reset to empty on migration (theme totals preserved)
- **Aproveitamento weights:** Negative scores weigh -3, zero scores weigh 0, positive scores weigh +2 — per PRD specification
- **Sorting direction:** `getLowestScoreQuestions()` returns ascending (lowest first); `createReinforcementRound()` sorts descending internally to process worst-scored questions first
- **Scoring constants:** Declared as `private static final int` at class level (SCORE_CORRECT_DELTA, SCORE_MAX, SCORE_WRONG_DELTA, SCORE_MIN) for maintainability

## Deviations from Plan

### Expected downstream compilation gaps

**1. [Rule 3 - Blocking] ReportsController and ThemeSelectionController still reference removed methods**
- **Found during:** Task 2/3 compile verification
- **Issue:** `ReportsController.java` calls `getOverallHitRate()`, `getHitRate()`, `getHighestErrorQuestions()`; `ThemeSelectionController.java` calls `getHitRate()` — all were removed in Task 3
- **Fix:** These are expected — plan 06-03 explicitly handles updating these controllers. Not fixing here to respect plan boundaries.
- **Files affected:** `ReportsController.java`, `ThemeSelectionController.java`
- **Committed in:** N/A — deferred to plan 06-03

---

**Total deviations:** 1 expected (scope boundary — handled in dependent plan)
**Impact on plan:** No impact. The 3 files in scope compile and are fully functional. Downstream consumers will be updated in plan 06-03.

## Issues Encountered

- `ReportsController.java` and `ThemeSelectionController.java` still reference removed `getHitRate()`, `getOverallHitRate()`, `getHighestErrorQuestions()` methods — will be fixed in plan 06-03

## Next Phase Readiness

- Core scoring engine complete: `QuestionScore`, migration, score-delta recording, aproveitamento, lowest-score query
- Ready for plan 06-03: ThemeSelectionView/Controller, ReportsView/Controller updates, and test rewrites

---

## Self-Check: PASSED

- [x] `StatsData.java` — exists, contains QuestionScore, ThemeStats uses `Map<String, QuestionScore>`, QuestionStats removed
- [x] `StatsService.java` — exists, contains migration, new recordRound(), getAproveitamento(), getLowestScoreQuestions()
- [x] `RoundState.java` — exists, createReinforcementRound() uses getLowestScoreQuestions()
- [x] `06-02-SUMMARY.md` — exists with substantive content
- [x] 5 atomic commits (4 feat + 1 docs) all referencing 06-02

---

*Phase: 06-scoring-system-question-id-rework*
*Completed: 2026-07-08*
