---
phase: 12
slug: spaced-repetition-visual-review-dashboard
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-21
---

# Phase 12 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5.11 |
| **Config file** | pom.xml (surefire 3.2.5) |
| **Quick run command** | `mvn test -Dtest=StatsServiceTest` |
| **Full suite command** | `mvn test` |
| **Estimated runtime** | ~30 seconds |

---

## Sampling Rate

- **After every task commit:** Run quick test for affected module
- **After every plan wave:** Run `mvn test`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | Status |
|---------|------|------|-------------|-----------|-------------------|--------|
| TBD | 01 | 1 | V2-01 | unit | `mvn test -Dtest=StatsServiceTest` | ⬜ pending |
| TBD | 02 | 1 | SR-01, SR-02 | unit | `mvn test` | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements (JUnit 5 + Maven Surefire already configured).

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Dashboard visual layout | SR-01, SR-02 | JavaFX visual rendering not testable via JUnit | Launch app, verify dashboard replaces "Jogar" tab with theme cards |
| Timeline visual display | SR-07 | JavaFX visual rendering | Launch app, navigate to dashboard, verify timeline shows study history |
| Priority colors | SR-04 | Visual rendering | Verify overdue themes show red, today orange, no-review green |
| Search filtering | SR-03 | UI interaction | Type in search bar, verify theme list filters in real-time |
| "Marcar como feita" button | SR-06 | UI interaction | Click button, verify overdue count decreases and SM-2 fields updated |

---

## Validation Sign-Off

- [ ] All tasks have automated verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 60s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
