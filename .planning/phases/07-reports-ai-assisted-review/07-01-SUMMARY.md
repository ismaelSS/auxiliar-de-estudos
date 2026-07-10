---
phase: 07-reports-ai-assisted-review
plan: 01
subsystem: service
tags: [stats, dominio, lowest-score, theme-filter]

# Dependency graph
requires:
  - phase: 06-scoring-question-id-rework
    provides: "StatsService with QuestionScore model and per-question tracking"
provides:
  - "getDominio(String) — percentage of questions with positive score per theme"
  - "getLowestScoreQuestionsByTheme(String, int) — sorted lowest-scoring questions for a single theme"
affects: [07-reports-ai-assisted-review]

# Tech tracking
tech-stack:
  added: []
  patterns: [stream-filter-count, theme-scoped-entry-filtering]

key-files:
  created: []
  modified:
    - src/main/java/org/IsmaelSS/service/StatsService.java
    - src/test/java/org/IsmaelSS/service/StatsServiceTest.java

key-decisions:
  - "getDominio uses score > 0 (not >= 0) per DOM-01 spec — zero means never answered or balanced"
  - "Integer division for percentage truncates (e.g. 66.6% → 66) — matches Portuguese 'dominio' convention"

patterns-established:
  - "Theme-scoped query pattern: lookup ThemeStats by name, return empty/default if null"

requirements-completed: [DOM-01, DOM-03, DOM-05]

# Metrics
duration: 2min
completed: 2026-07-10
---

# Phase 7 Plan 01: StatsService Query Methods Summary

**getDominio() returns percentage of positive-score questions per theme as integer string; getLowestScoreQuestionsByTheme() returns sorted lowest-scoring questions filtered to one theme**

## Performance

- **Duration:** 2 min
- **Started:** 2026-07-10T14:53:39Z
- **Completed:** 2026-07-10T14:56:18Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- getDominio(String themeName) implemented — returns "N/A" for empty/unknown themes, otherwise integer percentage string of questions with score > 0
- getLowestScoreQuestionsByTheme(String themeName, int limit) implemented — returns up to limit questions from one theme sorted by score ascending
- 8 new unit tests added covering N/A, 100%, 0%, mixed scores, theme isolation, limit, and empty-theme cases
- Full test suite green: 38 tests, 0 failures

## Task Commits

Each task was committed atomically:

1. **Task 1: Add getDominio() and getLowestScoreQuestionsByTheme() to StatsService** - `fe9c5cd` (feat)
2. **Task 2: Add unit tests for getDominio() and getLowestScoreQuestionsByTheme()** - `f475d7c` (test)

## Files Created/Modified
- `src/main/java/org/IsmaelSS/service/StatsService.java` - Added getDominio() and getLowestScoreQuestionsByTheme() methods after existing getLowestScoreQuestions()
- `src/test/java/org/IsmaelSS/service/StatsServiceTest.java` - Added 8 new test methods for the two new query methods

## Decisions Made
- getDominio uses `score > 0` (not `>= 0`) per DOM-01 specification — zero means never answered or balanced, not positive
- Integer division for percentage calculation: `(positiveCount * 100) / total` — truncates (e.g. 66.6% → "66")
- getLowestScoreQuestionsByTheme returns empty ArrayList (not null) for unknown themes — consistent with getLowestScoreQuestions pattern

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed flawed test assertion in lowestByThemeReturnsOnlyThemeQuestions**
- **Found during:** Task 2 (test verification)
- **Issue:** Original assertion `noneMatch(e -> getKey().equals("0") && getValue() == 2)` failed because both t1 and t2 have a question with id="0" and score=2 — the assertion caught t1's own question as a false positive
- **Fix:** Replaced with positive assertions verifying t1's question IDs (0 and 1) are present in the result, while the size assertion already proves theme isolation
- **Files modified:** src/test/java/org/IsmaelSS/service/StatsServiceTest.java
- **Verification:** All 24 StatsServiceTest tests pass, full suite 38/38 green
- **Committed in:** f475d7c (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (1 bug in test assertion)
**Impact on plan:** Minor test assertion fix. No scope creep — tests verify exactly what plan specified.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- StatsService query layer complete — ready for Wave 2 UI work (ThemeSelectionView dominio display, ReportsView per-theme drawers)
- Both methods follow existing patterns (getAproveitamento, getLowestScoreQuestions) — no architectural surprises

---
*Phase: 07-reports-ai-assisted-review*
*Completed: 2026-07-10*

## Self-Check: PASSED

- [x] StatsService.java exists on disk
- [x] StatsServiceTest.java exists on disk
- [x] 07-01-SUMMARY.md exists on disk
- [x] Commit fe9c5cd (feat) found in git log
- [x] Commit f475d7c (test) found in git log
- [x] All 24 StatsServiceTest pass (16 existing + 8 new)
- [x] Full suite 38/38 green
