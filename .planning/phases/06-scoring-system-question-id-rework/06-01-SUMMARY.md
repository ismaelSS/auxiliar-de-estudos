---
phase: 06-scoring-system-question-id-rework
plan: 01
subsystem: model
tags: question-id, json-annotation, model-rework, id-based-matching
requires:
  - phase: 04-reinforcement-mode-navigation-polish
    provides: RoundState.createReinforcementRound(), StudyRoundController
provides:
  - Question model with String id field (getter/setter/constructor)
  - 65 unique question IDs across both theme JSON files
  - RoundResult record with questionId field
  - ThemeLoader duplicate/missing ID validation
  - ID-based matching in RoundState.createReinforcementRound()
affects:
  - 06-02 (Scoring Model & StatsService)
  - 06-03 (Aproveitamento Display)
tech-stack:
  added: []
  patterns:
    - Question identity via stable string ID (not question text)
    - ID validation at theme load time (Set-based duplicate check)
key-files:
  created: []
  modified:
    - src/main/java/org/IsmaelSS/model/Question.java
    - src/main/java/org/IsmaelSS/model/RoundResult.java
    - src/main/java/org/IsmaelSS/model/RoundState.java
    - src/main/java/org/IsmaelSS/service/ThemeLoader.java
    - src/main/java/org/IsmaelSS/controller/StudyRoundController.java
    - themes/java-record.json
    - themes/java-modificadores.json
key-decisions:
  - "Question IDs are descriptive English slugs (e.g., record-shallow-immutability) instead of numeric IDs — human-readable in JSON and safe for JSON key usage"
  - "questionText retained in RoundResult for backward compatibility with reports view; questionId added alongside it"
  - "ID validation at ThemeLoader level (fail-fast on load) rather than at usage time"
requirements-completed:
  - SCORE-01
duration: 183 min
completed: 2026-07-08
---

# Phase 6 Plan 1: Question Identity Layer Summary

**Stable question identity layer — every question gets a unique descriptive `id` string, carried end-to-end from JSON to model to matching logic**

## Performance

- **Duration:** 183 min
- **Started:** 2026-07-08T13:02:51Z
- **Completed:** 2026-07-08T16:05:42Z
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments

- Question.java now has a `String id` field with `@JsonProperty` constructor parameter, getter, and setter — ready for Jackson deserialization from JSON files
- All 35 entries in `themes/java-record.json` have unique descriptive `id` slugs
- All 30 entries in `themes/java-modificadores.json` have unique descriptive `id` slugs
- RoundResult record now includes `String questionId` as its third component for stats keying
- StudyRoundController.handleOptionClick() passes `roundState.getCurrentQuestion().getId()` in each RoundResult
- ThemeLoader.loadTheme() validates that every question has a non-null, non-empty, unique `id` — rejects files with missing or duplicate IDs
- RoundState.createReinforcementRound() matches and sorts error questions by `q.getId()` instead of `q.getQuestion()`

## Task Commits

Each task was committed atomically:

1. **Task 1: Add id field to Question model and annotate theme JSON files** - `bf8e572` (feat)
2. **Task 2: Add questionId to RoundResult and pass it in StudyRoundController** - `e60a614` (feat)
3. **Task 3: Add duplicate ID validation to ThemeLoader and switch RoundState to ID-based matching** - `50a62d2` (feat)

**Plan metadata:** _(to be committed via gsd-tools)_

## Files Created/Modified

- `src/main/java/org/IsmaelSS/model/Question.java` — Added `private String id` field, updated all-args constructor to accept `@JsonProperty("id") String id` as first parameter, added `getId()`/`setId()` methods
- `src/main/java/org/IsmaelSS/model/RoundResult.java` — Added `String questionId` as third component of the record
- `src/main/java/org/IsmaelSS/model/RoundState.java` — Changed `createReinforcementRound()` matching predicate from `q.getQuestion()` to `q.getId()`; changed sorting comparator from `getQuestion()` to `getId()` for both sides
- `src/main/java/org/IsmaelSS/service/ThemeLoader.java` — Added `HashSet<String> seenIds` before validation loop, null/empty ID check, and duplicate ID detection with early return null
- `src/main/java/org/IsmaelSS/controller/StudyRoundController.java` — Updated `handleOptionClick()` to pass `roundState.getCurrentQuestion().getId()` as the third argument to `new RoundResult(...)`
- `themes/java-record.json` — Added unique `"id"` field (e.g., `"record-primary-purpose"`) as first key in all 35 question objects
- `themes/java-modificadores.json` — Added unique `"id"` field (e.g., `"mod-public-access"`) as first key in all 30 question objects

## Decisions Made

- **Descriptive slug IDs over numeric IDs:** English hyphenated slugs (e.g., `"record-shallow-immutability"`) are human-readable in JSON, self-documenting the question topic, and safe for use as JSON keys in stats data
- **questionText retained in RoundResult:** Kept for backward compatibility with reports view display; the new `questionId` is used for stats keying only
- **ID validation at load time:** ThemeLoader performs fail-fast validation when themes are loaded — any missing, empty, or duplicate `id` causes the entire file to be rejected, preventing silent data corruption

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None

## User Setup Required

None - no external service configuration required.

## Threat Flags

No new security-relevant surface introduced beyond what is tracked in the plan's threat model (T-06-01 duplicates/missing ID validation, T-06-02 and T-06-03 accepted risk).

## Self-Check: PASSED

- [x] Question.java: String id field exists at line 7, getter at getId(), setter at setId(), constructor uses @JsonProperty("id")
- [x] java-record.json: 35 total entries, 35 unique IDs (no duplicates)
- [x] java-modificadores.json: 30 total entries, 30 unique IDs (no duplicates)
- [x] RoundResult record includes questionId: String as third component
- [x] StudyRoundController passes roundState.getCurrentQuestion().getId() in RoundResult
- [x] ThemeLoader.loadTheme() returns null for missing id, empty id, and duplicate id
- [x] RoundState.createReinforcementRound() uses q.getId() for matching and sorting lookup
- [x] `mvn clean compile` passes with zero errors

## Next Phase Readiness

Ready for 06-02 (Scoring Model & StatsService refactor) — the question ID layer is complete, enabling per-question score tracking in StatsData and the score-based `getLowestScoreQuestions()` method.

---

*Phase: 06-scoring-system-question-id-rework*
*Completed: 2026-07-08*
