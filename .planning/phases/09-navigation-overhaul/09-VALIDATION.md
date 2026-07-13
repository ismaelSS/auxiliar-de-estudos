---
phase: 9
slug: navigation-overhaul
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-10
---

# Phase 9 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5.11.0 (via Maven Surefire 3.2.5) |
| **Config file** | pom.xml |
| **Quick run command** | `cd C:\prog\flashCardJava && mvn test -pl .` |
| **Full suite command** | `cd C:\prog\flashCardJava && mvn clean test` |
| **Estimated runtime** | ~5 seconds |

---

## Sampling Rate

- **After every task commit:** Run `cd C:\prog\flashCardJava && mvn test`
- **After every plan wave:** Run `cd C:\prog\flashCardJava && mvn clean test`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 10 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 09-01-01 | 01 | 1 | NAV-01 | T-09-01 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |
| 09-01-02 | 01 | 1 | NAV-02 | T-09-02 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |
| 09-01-03 | 01 | 1 | NAV-03 | T-09-03 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |
| 09-01-04 | 01 | 1 | NAV-04 | T-09-04 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |
| 09-01-05 | 01 | 1 | NAV-05 | T-09-05 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements (JUnit 5 + Surefire already configured).

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Back button visible on screens | NAV-01 | Visual UI element | Launch app, verify back button appears on study round and reports screens |
| Smooth transitions between screens | NAV-03 | Visual/animation quality | Launch app, navigate between screens, observe fade/slide transitions |
| Keyboard shortcut Escape goes back | NAV-04 | Keyboard interaction | Launch app, press Escape, verify navigation back |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 10s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
