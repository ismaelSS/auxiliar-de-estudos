---
phase: 08
slug: resizable-window-ctrl-scroll-zoom
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-07-10
---

# Phase 08 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5.11.0 |
| **Config file** | pom.xml (surefire 3.2.5) |
| **Quick run command** | `mvn test -Dtest={TestClass} -q` |
| **Full suite command** | `mvn test` |
| **Estimated runtime** | ~5 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn compile -q`
- **After every plan wave:** Run `mvn test`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 10 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 08-01-01 | 01 | 1 | ZOOM-02, ZOOM-03 | T-08-01 | N/A | compile | `mvn compile -q` | ✅ | ⬜ pending |
| 08-01-02 | 01 | 1 | ZOOM-01 | T-08-02 | N/A | compile | `mvn compile -q` | ✅ | ⬜ pending |
| 08-01-03 | 01 | 1 | ZOOM-01, ZOOM-02, ZOOM-03 | — | N/A | suite | `mvn clean test` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- Existing infrastructure covers all phase requirements. Zoom/UI behavior is manual-verification only — JUnit cannot test JavaFX scene interactions (Ctrl+scroll events, font size inheritance).

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Window resizes freely | ZOOM-01 | JavaFX Stage behavior, not unit-testable | Launch app, drag window edges — content should fill available space |
| Ctrl+scroll up increases text | ZOOM-02 | Requires user input simulation | Launch app, hold Ctrl, scroll up — all text should increase in size |
| Ctrl+scroll down decreases text | ZOOM-02 | Requires user input simulation | Launch app, hold Ctrl, scroll down — all text should decrease in size |
| Zoom persists across screens | ZOOM-03 | Requires screen navigation | Zoom in ThemeSelection, navigate to Reports — text should remain zoomed |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 10s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
