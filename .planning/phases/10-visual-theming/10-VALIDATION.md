---
phase: 10
slug: visual-theming
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-13
---

# Phase 10 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5.11.0 (via Maven Surefire 3.2.5) |
| **Config file** | pom.xml |
| **Quick run command** | `cd C:\prog\flashCardJava && mvn test` |
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
| 10-01-01 | 01 | 1 | VIZ-01 | T-10-01 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |
| 10-01-02 | 01 | 1 | VIZ-02 | T-10-02 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |
| 10-01-03 | 01 | 1 | VIZ-03 | T-10-03 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |
| 10-01-04 | 01 | 1 | VIZ-04 | T-10-04 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |
| 10-01-05 | 01 | 1 | VIZ-05 | T-10-05 / — | N/A | unit | `mvn test` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements (JUnit 5 + Surefire already configured).

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Dark background (#020817) visible | VIZ-02 | Visual styling | Launch app, verify dark background on all screens |
| Orange accent (#fe9a00) on buttons | VIZ-03 | Visual styling | Launch app, verify buttons show orange accent |
| White text on dark backgrounds | VIZ-04 | Visual styling | Launch app, verify text is readable white on dark bg |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 10s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
