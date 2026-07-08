# Phase 6: Scoring System & Question ID Rework — Plan Checker Report

**Verified:** 2026-07-08  
**Plans checked:** 3 (06-01, 06-02, 06-03)  
**Status:** ISSUES FOUND — 2 blocker(s), 3 warning(s)

---

## Verdict Summary

| Plan | Tasks | Files | Wave | Verdict |
|------|-------|-------|------|---------|
| 06-01 | 3 | 6 | 1 | **FLAG** (intermediate broken state acceptable, creates dependency shape for 06-02 breach) |
| 06-02 | 3 | 2 | 2 | **BLOCK** (removes method that RoundState still calls) |
| 06-03 | 3 | 7 | 3 | **FLAG** (inherits 06-02's compilation break; no fix for RoundState) |
| **Overall** | **9** | **15** | — | **BLOCK** — must resolve before execution |

---

## Dimension 1: Requirement Coverage ✅

| Requirement | Plans | Status |
|-------------|-------|--------|
| SCORE-01 (Question IDs) | 06-01 Tasks 1-3 | Covered |
| SCORE-02 (Negative scoring -3, floor -10) | 06-02 Tasks 1-2 | Covered |
| SCORE-03 (Positive scoring +2, cap +5) | 06-02 Tasks 1-2 | Covered |
| SCORE-04 (Aproveitamento in UI) | 06-03 Tasks 1-2 | Covered |
| SCORE-05 (Aproveitamento formula) | 06-03 Tasks 1, 3 | Covered |

All five ROADMAP.md requirements have covering plans. No requirements are orphaned.

---

## Dimension 2: Task Completeness ✅

All 9 tasks across 3 plans have complete fields:
- **Files:** Present with specific paths ✓
- **Action:** Detailed with before/after code snippets and line references ✓
- **Verify:** Runnable `mvn clean compile -q` or `mvn test -q` commands ✓
- **Done:** Measurable acceptance criteria with numbered items ✓

No empty or vague tasks. Each task has concrete code-change instructions.

---

## Dimension 3: Dependency Correctness ⚠️

**Formal dependency graph:**
```
06-01 (Wave 1, depends_on: [])  →  provides Question.id, RoundResult.questionId, ID-based matching
06-02 (Wave 2, depends_on: [06-01])  →  provides scoring engine, removes old methods
06-03 (Wave 3, depends_on: [06-02])  →  provides UI updates, rewritten tests
```

**No cycles, no missing references.** Formal flow is correct.

**However, there is a semantic dependency break:**

Plan **06-02 Task 3** removes `getHighestErrorQuestions(int limit)` from `StatsService.java`. But **RoundState.createReinforcementRound()** (modified in Plan 06-01 Task 3, never touched again) still calls `statsService.getHighestErrorQuestions(50)` on what was line 54.

After Plan 06-02 executes, `RoundState.java` will not compile because it references a deleted method. No plan task updates this call to `getLowestScoreQuestions()`.

**This is classified as a blocker (see Dimension 7 issues below).**

---

## Dimension 4: Key Links Planned ✅

All critical wiring between artifacts is specified with `from → to → via → pattern`:

| Plan | Link | Status |
|------|------|--------|
| 06-01 | RoundState → Question (`q.getId()` for matching) | ✅ |
| 06-01 | ThemeLoader → Question (duplicate ID validation) | ✅ |
| 06-01 | StudyRoundController → RoundResult (passes questionId) | ✅ |
| 06-02 | StatsService.recordRound() → RoundResult.questionId() | ✅ |
| 06-02 | StatsService.getAproveitamento() → QuestionScore.getScore() | ✅ |
| 06-02 | StatsService.getLowestScoreQuestions() → QuestionScore | ✅ |
| 06-03 | ThemeSelectionController → StatsService.getAproveitamento() | ✅ |
| 06-03 | ReportsController → StatsService.getLowestScoreQuestions() | ✅ |

---

## Dimension 5: Scope Sanity ⚠️ (WARNING)

| Metric | Plan 06-01 | Plan 06-02 | Plan 06-03 | Threshold |
|--------|-----------|-----------|-----------|-----------|
| Tasks/plan | 3 ✅ | 3 ✅ | 3 ✅ | 2-3 (4=warn, 5+=block) |
| Files modified | 6 ✅ | 2 ✅ | 7 ⚠️ | 5-8 (10=warn, 15+=block) |
| Lines of new code (est.) | ~370 (JSON IDs) + ~50 (Java) | ~120 (Java) | ~500 (tests) + ~50 (Java) | — |

**Warning:** Plan 06-03 handles 7 files across 3 tasks, with Task 3 alone requiring rewriting 3 substantial test files (~500+ total lines of test code provided inline). Task 3's `RoundStateReinforcementTest.java` rewrite is 196 lines of inline code plus complex test logic. This represents the highest risk task in the phase and could exhaust context budget. Consider whether the test rewrites should be a Plan 06-04.

---

## Dimension 6: Verification Derivation ✅

**Truths** in all three plans are user-observable:
- "Question.java has a String `id` field with getter/setter" ✓
- "StatsData has a QuestionScore inner class with score field" ✓
- "ThemeSelectionView shows 'Pontuação: N/A' instead of 'Hit rate: N/A'" ✓

No implementation-focused truths (e.g., "bcrypt installed"). All truths map to verifiable outcomes. ✓

**Artifacts** list expected `min_lines` and `contains` markers — sound. **Key links** connect the artifacts.

---

## Dimension 7: Context Compliance ✅

CONTEXT.md has no locked decisions in the "Decisions" section — all items are specified as requirements. No deferred ideas are included in the plans. All discretion areas (ID type, migration strategy, method naming, UI label text) are handled appropriately.

No contradictions found.

---

## Dimension 7b: Scope Reduction Detection ✅

No scope reduction language found (`"v1"`, `"simplified"`, `"static for now"`, `"placeholder"`, `"skip"`, `"defer"` used only for legitimate out-of-scope concerns like `ThemeLoader`-based text lookup for reports — properly flagged as deferrable polish).

The one instance of labeling a `future enhancement` (ID→text mapping in reports) is correctly identified as future work per the plan's design scope.

---

## Dimension 7c: Architectural Tier Compliance ✅

Responsibility Map from RESEARCH.md:

| Capability | Tier | Plan Assignment | Correct? |
|------------|------|----------------|----------|
| Question identity (`id`) | Data model + JSON | 06-01: Question.java + JSON | ✅ |
| Score accumulation | Service | 06-02: StatsService.recordRound() | ✅ |
| Score bounds enforcement | Service | 06-02: QuestionScore.recordCorrect/Wrong | ✅ |
| Aproveitamento formula | Service | 06-02: StatsService.getAproveitamento() | ✅ |
| Reinforcement selection | Model → Service | 06-01/06-02: RoundState calls StatsService | ✅ (gap: see Blockers) |
| Migration of old stats | Service | 06-02: StatsService.load() | ✅ |
| UI display of scores | View → Controller | 06-03: ThemeSelectionView + ReportsView | ✅ |

No tier mismatches. All capabilities assigned to appropriate architectural tiers.

---

## Dimension 8: Nyquist Compliance ❌ (BLOCKER)

### Check 8e — VALIDATION.md Existence: ❌ **BLOCKING FAIL**

`nyquist_validation` is `true` in `.planning/config.json` (line 11). RESEARCH.md has a "Validation Architecture" section (lines 554-584). Both conditions require Nyquist checks.

**No VALIDATION.md found** in `.planning/phases/06-scoring-system-question-id-rework/`. Missing file means the gate check fails.

**Fix:** Run `/gsd-plan-phase 06 --research` to regenerate VALIDATION.md.

---

## Dimension 9: Cross-Plan Data Contracts ✅

The shared data pipeline flows:
```
Theme JSON → Question (id) → RoundResult (questionId) → StatsService (questionId key) → QuestionScore → StatsData
```

No conflicting transforms between plans:
- 06-01 creates `questionId` field → 06-02 consumes it → 06-03 displays computed scores
- All plans agree on `questionId` as the map key for stats
- Migration in 06-02 detects old format (text keys, long strings) and resets to empty question scores

No incompatible transforms detected.

---

## Dimension 10: AGENTS.md Compliance ✅ (SKIPPED)

No `AGENTS.md` found in project root. Dimension skipped.

---

## Dimension 11: Research Resolution ✅

RESEARCH.md has no `## Open Questions` section heading that requires resolution. The "Open Questions" section at line 529 contains recommendations (not open/unresolved items). All analysis questions are resolved within the document. PASS.

---

## Dimension 12: Pattern Compliance ✅

PATTERNS.md exists with 15/15 files classified. Every plan references the correct analogs (itself for modification files). All shared patterns (constructor injection, view scene exposure, shuffled options, file JSON persistence) are respected.

---

# 🔴 BLOCKERS (must fix before execution)

## Blocker 1: RoundState.createReinforcementRound() will not compile after Plan 06-02

| Field | Value |
|-------|-------|
| **Dimension** | Dependency Correctness |
| **Plans affected** | 06-01 (Task 3), 06-02 (Task 3), 06-03 |
| **Severity** | BLOCKER |
| **Description** | Plan 06-02 Task 3 removes `StatsService.getHighestErrorQuestions(int limit)` entirely. But `RoundState.createReinforcementRound()` (modified in 06-01 Task 3 at what was line 54) still calls `statsService.getHighestErrorQuestions(50)`. This call is never updated to `statsService.getLowestScoreQuestions(50)` in any plan. After 06-02 executes, RoundState.java will fail to compile. Additionally, the return type changes from `Map.Entry<String, Double>` to `Map.Entry<String, Integer>`, meaning the local `errorMap` variable and sorting comparator also need updating. |
| **Root cause** | Plan 06-01 Task 3 updated `createReinforcementRound()` to use `q.getId()` for matching/sorting, but kept the old `getHighestErrorQuestions()` call. Plan 06-02 removes that method but has no task to update the call site. Plan 06-03 doesn't touch RoundState.java. The call is orphaned. |
| **Fix hint** | Add a task (either to Plan 06-02 or a new Plan 06-04) that updates `RoundState.createReinforcementRound()` to call `statsService.getLowestScoreQuestions(50)` instead of `getHighestErrorQuestions(50)`, changes errorMap to `Map<String, Integer>` with Integer-based sorting, and changes the `errorTexts` set to use String (ID) keys. This must happen _after_ `getLowestScoreQuestions` is added (06-02 Task 3) but _before_ the tests are rewritten (06-03 Task 3 — already expects the correct signature). |

**Affected code in RoundState.createReinforcementRound() (after Plan 06-01 modifications):**
```java
// Line 53-62 currently reads:
List<Map.Entry<String, Double>> topErrors = statsService.getHighestErrorQuestions(50);  // ← WILL BREAK
Set<String> errorTexts = topErrors.stream()
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
Map<String, Double> errorMap = new HashMap<>();  // ← Needs to become Map<String, Integer>
```

**Must be changed to:**
```java
List<Map.Entry<String, Integer>> lowestScores = statsService.getLowestScoreQuestions(50);
Set<String> lowScoreIds = lowestScores.stream()
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
Map<String, Integer> scoreMap = new HashMap<>();
for (Map.Entry<String, Integer> e : lowestScores) {
    scoreMap.put(e.getKey(), e.getValue());
}
```

The matching predicate and sorting comparator then use `q.getId()` (correctly set up in 06-01) against the new integer-scored map.

---

## Blocker 2: VALIDATION.md Missing (Nyquist gate check)

| Field | Value |
|-------|-------|
| **Dimension** | Nyquist Compliance (8e) |
| **Severity** | BLOCKER |
| **Description** | No VALIDATION.md exists in the phase directory. `nyquist_validation` is `true` in config.json. RESEARCH.md has a "Validation Architecture" section. Nyquist validation is required but the gate file is absent. |
| **Fix hint** | Run `/gsd-plan-phase 06 --research` to regenerate VALIDATION.md, or create it manually mapping each requirement to test commands as outlined in RESEARCH.md's Validation Architecture table. |

---

# 🟡 WARNINGS (should fix, execution may proceed if blockers resolved)

## Warning 1: Plan 06-03 scope is high (task 3 test rewrites)

| Field | Value |
|-------|-------|
| **Dimension** | Scope Sanity |
| **Severity** | WARNING |
| **Description** | Plan 06-03 Task 3 requires rewriting 3 test files (~500+ lines of inline code) in a single task. `RoundStateReinforcementTest.java` (199 lines existing, full rewrite inline at ~196 lines), `StatsServiceTest.java` (177 lines existing, full rewrite inline at ~483 lines), and `StatsDataTest.java` (66 lines existing, full rewrite inline at ~90 lines). Total: ~770 lines of new test code in one task. This is likely to exceed context budget for a single execution step. |
| **Fix hint** | Consider splitting: move test rewrites to a Plan 06-04. Or split Task 3 into three sub-tasks within the plan. |

## Warning 2: Reports UI shows question IDs instead of question text

| Field | Value |
|-------|-------|
| **Dimension** | Verification Derivation |
| **Severity** | WARNING |
| **Description** | Plan 06-03 Task 2 replaces the error section to show `entry.getKey()` (the question ID slug like `"record-primary-purpose"`) instead of the actual question text. The plan notes this explicitly and says ID→text lookup is deferred to future polish. This is a minor UX regression — users will see `"record-primary-purpose — Pontuação: 5"` instead of `"Qual é o principal objetivo de um record em Java? — Taxa de erro: 0%"`. While IDs are descriptive, the current behavior shows full question text. |
| **Fix hint** | Either: (a) Accept as-is (noted in plan as acceptable), (b) add a simple `Map<String, String>` ID→text lookup before this step, or (c) split the ID→text mapping into a follow-up. If accepted, this is purely informational for reviewers. |

## Warning 3: Intermediate broken state between Plan 06-01 and 06-02

| Field | Value |
|-------|-------|
| **Dimension** | Dependency Correctness |
| **Severity** | WARNING |
| **Description** | Between Plan 06-01 (Wave 1) and Plan 06-02 (Wave 2), `createReinforcementRound()` will match question IDs against the old `getHighestErrorQuestions()` return values (which are question-text-keyed). Since IDs like `"record-primary-purpose"` will never match question texts like `"Qual é o principal objetivo de um record em Java?"`, reinforcement mode will select zero error questions. This means reinforcement mode is functionally broken in the Wave 1→Wave 2 window. The plan acknowledges this in the Task 3 Note. This is acceptable if waves are executed atomically in sequence, but flagging for awareness. |
| **Fix hint** | Ensure Plans 06-01 and 06-02 are executed in immediate sequence without intermediate testing of reinforcement mode. Or consolidate 06-01 Task 3's createReinforcementRound changes into 06-02. |

---

# ✅ PASS DIMENSIONS

| Dimension | Status |
|-----------|--------|
| D1 — Requirement Coverage | ✅ PASS |
| D2 — Task Completeness | ✅ PASS |
| D3 — Dependency Correctness (formal) | ✅ PASS (semantic: ⚠️ see Blocker 1) |
| D4 — Key Links Planned | ✅ PASS |
| D5 — Scope Sanity | ⚠️ PASS (with Warning 1) |
| D6 — Verification Derivation | ✅ PASS |
| D7 — Context Compliance | ✅ PASS |
| D7b — Scope Reduction | ✅ PASS |
| D7c — Architectural Tier Compliance | ✅ PASS |
| D8 — Nyquist Compliance | ❌ FAIL (Blocker 2) |
| D9 — Cross-Plan Data Contracts | ✅ PASS |
| D10 — AGENTS.md Compliance | ⏭️ SKIPPED |
| D11 — Research Resolution | ✅ PASS |
| D12 — Pattern Compliance | ✅ PASS |

---

## Recommendation

**Return to planner for revision.** Two blockers require resolution:

1. **Fix the orphaned `getHighestErrorQuestions()` call** in `RoundState.createReinforcementRound()`. Add a task to update it to call `statsService.getLowestScoreQuestions(50)` with `Map<String, Integer>` types. This can go in Plan 06-02 (after Task 3 adds `getLowestScoreQuestions`, add a Task 4 to update RoundState) or in Plan 06-01 (by keeping the old method call and adding a note that it will be updated when the new method exists).

2. **Generate VALIDATION.md** by running `/gsd-plan-phase 06 --research`.

Consider also splitting Plan 06-03's test rewrites into a Plan 06-04 if context budget is a concern (the 3 test file rewrites total ~770 lines of inline code in a single task).
