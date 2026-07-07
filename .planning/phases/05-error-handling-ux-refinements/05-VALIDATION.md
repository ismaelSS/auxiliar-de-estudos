---
phase: 05
slug: error-handling-ux-refinements
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-07
---

# Phase 5 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5.11.0 |
| **Config file** | pom.xml (maven-surefire-plugin 3.2.5) |
| **Quick run command** | `mvn clean test` |
| **Full suite command** | `mvn clean test` |
| **Estimated runtime** | ~5 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn clean compile`
- **After every plan wave:** Run `mvn clean test`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 15 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 05-01-01 | 01 | 1 | UX-05 | — | External CSS file created | compile | `mvn clean compile -q` | ❌ W0 | ⬜ pending |
| 05-01-02 | 01 | 1 | UX-05 | — | All views load external CSS | compile | `mvn clean compile -q` | ❌ W0 | ⬜ pending |
| 05-01-03 | 01 | 1 | UX-06 | — | ErrorUtil helper exists | unit | `mvn clean test -Dtest=ErrorUtilTest` | ❌ W0 | ⬜ pending |
| 05-02-01 | 02 | 2 | ERR-01, ERR-02 | — | ThemeLoader handles malformed JSON | unit | `mvn clean test -Dtest=ThemeLoaderErrorHandlingTest` | ❌ W0 | ⬜ pending |
| 05-02-02 | 02 | 2 | ERR-03, ERR-04 | — | StatsService save/load errors handled | unit | `mvn clean test` | ⬜ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `src/test/java/org/IsmaelSS/service/ThemeLoaderErrorHandlingTest.java` — malformed JSON, empty file, missing fields
- [ ] `src/test/java/org/IsmaelSS/util/ErrorUtilTest.java` — verify utility methods exist (headless-safe subset)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| CSS stylesheet loads on all screens | UX-05 | Requires JavaFX toolkit with Scene/Stage | Launch app, verify theme selection, study round, and reports screens use consistent styling |
| Responsive layout on resize | UX-05 | Visual test requiring rendering | Resize window, verify content scrolls and buttons remain accessible |
| Error dialogs display correctly | ERR-01 | Alert requires JavaFX toolkit | Place malformed JSON in themes/, launch app, verify Alert appears with filename and error details |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 15s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
