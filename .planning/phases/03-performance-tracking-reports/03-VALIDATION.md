---
phase: 3
slug: performance-tracking-reports
status: verified
nyquist_compliant: true
wave_0_complete: true
created: 2026-07-07
audited: 2026-07-07
---

# Phase 3 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5.11.0 + Maven Surefire 3.2.5 |
| **Config file** | pom.xml (compiler + surefire + resources plugins) |
| **Quick run command** | `mvn clean compile` |
| **Test command** | `mvn clean test` |
| **Full suite command** | `mvn clean package` |
| **Estimated runtime** | ~8 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn clean compile`
- **After every plan wave:** Run `mvn clean package`
- **Before phase gate:** `mvn clean package` must be green
- **Max feedback latency:** 10 seconds

---

## Per-Task Verification Map

| Task ID | Wave | Requirement | Test Type | Automated Command | Status |
|---------|------|-------------|-----------|-------------------|--------|
| 3-1-01 | 1 | STATS-01, STATS-02, STATS-03 | model | `mvn clean test` (StatsDataTest) | ✅ covered |
| 3-1-02 | 1 | STATS-01, STATS-02, STATS-03, STATS-04 | service | `mvn clean test` (StatsServiceTest) | ✅ covered |
| 3-2-01 | 2 | — | model mod | `mvn clean compile` | ✅ covered |
| 3-2-02 | 2 | — | controller | `mvn clean compile` | ⚠ manual-only |
| 3-3-01 | 3 | REPORT-01, REPORT-02, REPORT-03 | view | `mvn clean compile` | ⚠ manual-only |
| 3-3-02 | 3 | THEME-04 | view mod | `mvn clean compile` | ⚠ manual-only |
| 3-3-03 | 3 | — | controller | `mvn clean compile` | ⚠ manual-only |
| 3-4-01 | 4 | — | integration | `mvn clean compile` | ✅ covered |
| 3-4-02 | 4 | — | compile | `mvn clean compile` | ✅ covered |
| 3-4-03 | 4 | — | package | `mvn clean package` | ✅ covered |

---

## Wave 0 Requirements

- [x] Add JUnit 5 dependency to `pom.xml` (junit-jupiter 5.11.0, scope: test)
- [x] Add Maven Surefire Plugin 3.2.5 to `pom.xml`
- [x] `src/test/java/org/IsmaelSS/model/StatsDataTest.java` — 4 tests (serialization round-trip, empty state, zero-answers, question increments)
- [x] `src/test/java/org/IsmaelSS/service/StatsServiceTest.java` — 13 tests (constructor, recordRound, hit rate, error ranking, limits, empty lists, theme queries, persistence across instances, multi-round accumulation)

*All 17 tests pass with `mvn clean test`.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Reports screen displays correct stats | REPORT-01/02/03 | JavaFX GUI requires visual inspection | Run `mvn javafx:run`, start a round, answer questions, exit, click "Relatórios", verify numbers match |
| Hit rate updates on theme selection | THEME-04 | JavaFX GUI requires visual inspection | After round, verify theme selection shows correct hit rate instead of "N/A" |
| StudyRoundController round-trip | — | JavaFX controller needs toolkit | Manual inspection of answer feedback, round completion, and exit behavior |

---

## Validation Sign-Off

- [x] All tasks have compile/package verification
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 complete: JUnit 5 + 2 test files (17 tests) added
- [x] No watch-mode flags
- [x] Feedback latency < 10s
- [x] `nyquist_compliant: true` set in frontmatter

## Validation Audit 2026-07-07

| Metric | Count |
|--------|-------|
| Gaps found | 3 |
| Resolved (automated tests) | 3 |
| Escalated (JavaFX manual-only) | 3 |

**Approval:** verified ✓
