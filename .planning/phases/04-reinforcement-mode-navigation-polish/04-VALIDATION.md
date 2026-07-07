---
phase: 04
slug: reinforcement-mode-navigation-polish
status: complete
nyquist_compliant: true
wave_0_complete: true
created: 2026-07-07
---

# Phase 4 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5.11.0 |
| **Config file** | pom.xml (maven-surefire-plugin 3.2.5) |
| **Quick run command** | `mvn clean test -Dtest=RoundStateReinforcementTest` |
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
| 04-01-01 | 01 | 1 | ROUND-04 | T-04-01 / T-04-02 | Checkbox added to view, compiles correctly | compile | `mvn clean compile -q` | ❌ W0 | ✅ green |
| 04-01-02 | 01 | 1 | ROUND-04 | T-04-01 / T-04-02 | Factory method uses error-weighting, handles empty/null stats, respects count | unit | `mvn clean test -Dtest=RoundStateReinforcementTest` | ✅ | ✅ green |
| 04-01-03 | 01 | 1 | ROUND-04 | T-04-01 / T-04-02 | Controller conditionally calls factory vs normal constructor | compile | `mvn clean compile -q` | ❌ W0 | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Navigation polish (UI-04) | UI-04 | No code changes in Phase 4; already satisfied by Phase 3 | Verify: "Relatórios" button on theme selection → reports screen, "Voltar" returns to theme selection |
| Checkbox visual appearance | ROUND-04 | JavaFX UI elements require JavaFX toolkit runtime, not available in headless test environment | Launch app, verify "Modo Reforço" checkbox appears between question count spinner and "Iniciar" button |
| Controller end-to-end flow | ROUND-04 | Full integration requires JavaFX Scene/Stage | Launch app, check checkbox, start round, verify questions include previously-missed ones |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 15s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved 2026-07-07

---

## Validation Audit 2026-07-07
| Metric | Count |
|--------|-------|
| Gaps found | 2 |
| Resolved | 1 (ROUND-04: 7 tests) |
| Escalated | 1 (UI-04: manual-only, no code changes in Phase 4) |
