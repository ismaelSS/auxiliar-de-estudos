---
phase: quick-01
plan: 01
subsystem: model
tags: [question-id, int, migration]
requires: []
provides:
  - "Question.id changed from String to int"
  - "RoundResult.questionId changed from String to int"
  - "JSON theme files use integer IDs 0..N"
affects: []

tech-stack:
  added: []
  patterns:
    - "Question IDs are sequential integers instead of arbitrary strings"
    - "Stats map keys remain String (via String.valueOf()) for JSON serialization compatibility"

key-files:
  created: []
  modified:
    - src/main/java/org/IsmaelSS/model/Question.java
    - src/main/java/org/IsmaelSS/model/RoundResult.java
    - src/main/java/org/IsmaelSS/model/RoundState.java
    - src/main/java/org/IsmaelSS/service/ThemeLoader.java
    - src/main/java/org/IsmaelSS/service/StatsService.java
    - themes/java-record.json
    - themes/java-modificadores.json
    - src/test/java/org/IsmaelSS/model/RoundStateReinforcementTest.java
    - src/test/java/org/IsmaelSS/service/StatsServiceTest.java

key-decisions:
  - "StatsData.questions remains Map<String, QuestionScore> — int IDs converted to String via String.valueOf() for JSON key compatibility"
  - "Reinforcement code in RoundState.convert int IDs to String when matching against String-keyed errorId sets"

requirements-completed: []

duration: 12min
completed: 2026-07-08
---

# Quick Task: Question ID should be an int (incremental number)

**Changed Question.id from String to int across model classes, JSON theme files, and all consumers — all 30 tests pass.**

## Performance

- **Duration:** 12 min
- **Started:** 2026-07-08T13:25:00Z
- **Completed:** 2026-07-08T13:37:00Z
- **Tasks:** 3
- **Files modified:** 9

## Accomplishments

- `Question.id` field changed from `String` to `int` with Jackson `@JsonProperty` compatibility
- `RoundResult.questionId` changed from `String` to `int`
- `RoundState` reinforcement matching uses `String.valueOf(q.getId())` against String-keyed error/scores maps
- `ThemeLoader` validation updated: `q.getId() < 0` instead of null/empty check; `Set<Integer>` for duplicate detection
- `StatsService` converts int `questionId()` to `String` via `String.valueOf()` for stats map key
- JSON theme files: `java-record.json` IDs 0-34, `java-modificadores.json` IDs 0-29
- All 7 RoundStateReinforcement tests, 7 StatsData tests, 16 StatsService tests pass

## Task Commits

Each task was committed atomically:

1. **Task 1: Change Question.id to int and update all consumers** - `c5bf687` (feat)
2. **Task 2: Update JSON theme files — replace string IDs with sequential integers** - `da8120a` (feat)
3. **Task 3: Update tests to use int IDs** - `3e202f0` (test)

## Files Created/Modified

- `src/main/java/org/IsmaelSS/model/Question.java` — `id` field from `String` to `int`
- `src/main/java/org/IsmaelSS/model/RoundResult.java` — `questionId` from `String` to `int`
- `src/main/java/org/IsmaelSS/model/RoundState.java` — `String.valueOf(q.getId())` in reinforcement matching
- `src/main/java/org/IsmaelSS/service/ThemeLoader.java` — `int` validation (`< 0`), `Set<Integer>`
- `src/main/java/org/IsmaelSS/service/StatsService.java` — `String.valueOf(result.questionId())`
- `themes/java-record.json` — integer IDs 0..34
- `themes/java-modificadores.json` — integer IDs 0..29
- `src/test/java/org/IsmaelSS/model/RoundStateReinforcementTest.java` — int IDs, `List<Integer>`
- `src/test/java/org/IsmaelSS/service/StatsServiceTest.java` — int questionId in RoundResult calls

## Decisions Made

- **Stats map keys stay String:** `StatsData.questions` remains `Map<String, QuestionScore>` because JSON object keys must be strings. `String.valueOf(int)` converts the int ID when used as a key.
- **Reinforcement matching bridges types:** `errorIds` and `scoreMap` in `RoundState` remain `Set<String>` / `Map<String, Integer>` to match the stats storage format. `String.valueOf(q.getId())` bridges the int ID.

## Deviations from Plan

None — plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None — no external service configuration required.

## Self-Check: PASSED

- [x] All 9 modified files exist
- [x] All 3 commits verified (c5bf687, da8120a, 3e202f0)
- [x] No accidental file deletions
- [x] `mvn compile` passes
- [x] `mvn test` — all 30 tests pass (0 failures, 0 errors)
- [x] `java-record.json` — 35 sequential integer IDs 0..34
- [x] `java-modificadores.json` — 30 sequential integer IDs 0..29
- [x] No remaining `String` question ID references in Java source

---

*Quick task: 260708-ij0*
*Completed: 2026-07-08*
