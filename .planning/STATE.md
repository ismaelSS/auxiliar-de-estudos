---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: milestone
status: Executing Phase 08
last_updated: "2026-07-13T14:20:53.103Z"
progress:
  total_phases: 10
  completed_phases: 5
  total_plans: 20
  completed_plans: 11
  percent: 50
---

# State: FlashCard JavaFX

## Project Reference

See: .planning/PROJECT.md (updated 2026-07-07)

**Core value:** Students can study any topic by loading a themed JSON question bank and immediately start quizzing themselves with performance tracking and weak-area reinforcement.
**Current focus:** Phase 08 — resizable-window-ctrl-scroll-zoom

## Phase Summary

| # | Phase | Status |
|---|-------|--------|
| 1 | Project Scaffold & Theme Loading | Complete ✓ |
| 2 | Study Round Engine | Complete ✓ |
| 3 | Performance Tracking & Reports | Complete ✓ |
| 4 | Reinforcement Mode & Navigation Polish | Complete ✓ |
| 5 | Error Handling & UX Refinements | Planned |
| 6 | Scoring System & Question ID Rework | Complete ✓ |
| 7 | Reports & AI-Assisted Review | Complete ✓ |
| 8 | Resizable Window & Ctrl+Scroll Zoom | Complete ✓ |

## Completed Phases

### Phase 1: Project Scaffold & Theme Loading ✓

**Outcome:** JavaFX app launches with theme selection screen. Themes auto-detected from `themes/` JSON files. Models, data access, and UI infrastructure built. Compiles and packages with Maven.

### Phase 2: Study Round Engine ✓

**Outcome:** Core study round implemented. Questions displayed with 5 shuffled alternatives. Correct (green) / wrong (red) feedback on click. Questions randomly ordered without repeats. Options shuffled per round. Exit button and round-complete summary screen. Compiles and packages with Maven.

### Phase 3: Performance Tracking & Reports ✓

**Outcome:** Performance persisted to `flashcard-stats.json` across sessions. Reports screen shows overall hit rate, per-theme breakdown, and top-5 highest-error questions. Stats survive app restart via `StatsService` (Jackson load/save). Hit rates update live on theme selection after each round. Reports accessible via "Relatórios" button with lazy-init plus refresh on subsequent visits. Compiles and packages with Maven.

### Phase 4: Reinforcement Mode & Navigation Polish ✓

**Outcome:** "Modo Reforço" checkbox added to theme selection screen. When enabled, `RoundState.createReinforcementRound()` factory selects highest-error questions first via `StatsService.getHighestErrorQuestions()`, fills remaining slots with fresh questions. `ThemeSelectionController` checks `view.isReinforcementMode()` and chooses the appropriate constructor. All 17 existing tests still pass.

### Phase 5: Error Handling & UX Refinements ○

**Outcome:** *Not yet executed.*

### Phase 6: Scoring System & Question ID Rework ✓

**Outcome:** Question IDs changed from descriptive strings to sequential integers. Theme stats schema updated with `lastAccessTime` and `sortByLastAccessTime`. Score tracking thresholds (3x review, 6x confidence, 10x mastery). `getDominio()` returns percentage score across all questions. Reports view shows "Domínio" per theme. All 30+ tests pass.

### Phase 7: Reports & AI-Assisted Review ✓

**Outcome:** Accordion-style ReportsView with per-theme TitledPane drawers showing ≤10 lowest-score questions. "Copiar prompt IA" button per drawer copies formatted AI review prompt with question text. StatsService.getLowestScoreQuestionsByTheme(). All 38 tests pass.

### Phase 8: Resizable Window & Ctrl+Scroll Zoom ✓

**Outcome:** Ctrl+scroll zoom (10-30px range, 1px steps) via ScreenController capture-phase event filter. Zoom applied as `-fx-font-size` on root node, inherited by all views. ThemeSelectionView and ReportsView scenes are now responsive (no hardcoded dimensions). StudyRoundView inline `-fx-font-size` removed. All 38 tests pass, app packages successfully.

---

## Current Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Horizontal Layers | Chosen by user — build layers bottom-up | Confirmed |
| MVC Architecture | Standard JavaFX pattern | Confirmed |
| Jackson for JSON | Industry standard for Java JSON processing | Confirmed |
| Programmatic Views (no FXML) | Simpler, full control, no extra tooling | Confirmed |
| Theme path: `themes/` relative to `user.dir` | Works from any run directory | Confirmed |
| Hit rate display as "N/A" in Phase 1 | Stats not available until Phase 3 | Confirmed |
| JDK 25 (not 26) | Environment constraint — only JDK 25 available | Confirmed |
| JavaFX 25 | Matches available JDK version | Confirmed |
| Round State Model class | Encapsulates shuffled questions, shuffled options, correct indices, answer tracking | Decided |
| Correct answer tracking after shuffle | Store original correct text, find its index in shuffled list | Decided |
| Answer feedback via PauseTransition 1s | Simple delay without threads | Decided |
| Round exits back to theme selection | "Sair" button + round-complete "Voltar" button | Decided |
| No stats file in Phase 2 | Round stats tracked in-memory only; persistence deferred to Phase 3 | Decided |
| Stats file schema: per-theme + per-question tracking | Matches stats/report requirements, simple flat JSON | Decided |
| Questions identified by text (no UUID) | Adequate for single-user local app | Decided |
| ReportsController lazy-init + refresh pattern | Avoids recreating scene each time; ensures fresh data | Decided |
| Round result collection via List<RoundResult> | Accumulates during round, flushed on completion or exit | Decided |
| Post-round callback refreshes hit rates | Ensures theme selection shows updated stats after round | Decided |
| getDominio uses score > 0 (not >= 0) | DOM-01 spec: zero = never answered/balanced, not positive | Decided |
| Dual-state label tracking for dominio | Separate HashMaps so updateAproveitamento and updateDominio don't overwrite each other | Decided |
| Accordion replaces flat VBoxes in ReportsView | Cleaner UX for per-theme exploration, collapsible drawers | Decided |
| AI prompt copies directly to clipboard | No preview dialog — auto-generated per plan D-01 | Decided |
| Zoom via root CSS -fx-font-size | Children inherit automatically, no per-label styles | Confirmed |
| ScreenController owns zoom state | Single source of truth across all screens | Confirmed |
| Session-only zoom persistence | No disk write per D-04 | Confirmed |
| Capture-phase event filter for Ctrl+scroll | Prevents ScrollPane double-scroll in ReportsView | Confirmed |

## Quick Tasks Completed

| # | Description | Date | Commit | Directory |
|---|-------------|------|--------|-----------|
| 260708-ij0 | Change question IDs from descriptive strings to sequential integers | 2026-07-08 | 3e202f0 | [260708-ij0-o-id-de-questoes-deve-ser-um-numero-incr](./quick/260708-ij0-o-id-de-questoes-deve-ser-um-numero-incr/) |

## Open Questions

- (None)

*Last updated: 2026-07-10 after Phase 8*

## Performance Metrics

| Phase | Plan | Duration | Notes |
|-------|------|----------|-------|
| Phase 07-reports-ai-assisted-review P01 | 2min | 2 tasks | 2 files |
| Phase 07-reports-ai-assisted-review P02 | 1min | 2 tasks | 2 files |
| Phase 07-reports-ai-assisted-review P03 | 2min | 2 tasks | 2 files |
| Phase 08 P01 | 6min | 3 tasks | 4 files |
