# State: FlashCard JavaFX

## Project Reference

See: .planning/PROJECT.md (updated 2026-07-07)

**Core value:** Students can study any topic by loading a themed JSON question bank and immediately start quizzing themselves with performance tracking and weak-area reinforcement.
**Current focus:** Phase 5 — Error Handling & UX Refinements (Planned — ready to execute)

## Phase Summary

| # | Phase | Status |
|---|-------|--------|
| 1 | Project Scaffold & Theme Loading | Complete ✓ |
| 2 | Study Round Engine | Complete ✓ |
| 3 | Performance Tracking & Reports | Complete ✓ |
| 4 | Reinforcement Mode & Navigation Polish | Complete ✓ |
| 5 | Error Handling & UX Refinements | Planned |

## Completed Phases

### Phase 1: Project Scaffold & Theme Loading ✓
**Outcome:** JavaFX app launches with theme selection screen. Themes auto-detected from `themes/` JSON files. Models, data access, and UI infrastructure built. Compiles and packages with Maven.

### Phase 2: Study Round Engine ✓
**Outcome:** Core study round implemented. Questions displayed with 5 shuffled alternatives. Correct (green) / wrong (red) feedback on click. Questions randomly ordered without repeats. Options shuffled per round. Exit button and round-complete summary screen. Compiles and packages with Maven.

### Phase 3: Performance Tracking & Reports ✓
**Outcome:** Performance persisted to `flashcard-stats.json` across sessions. Reports screen shows overall hit rate, per-theme breakdown, and top-5 highest-error questions. Stats survive app restart via `StatsService` (Jackson load/save). Hit rates update live on theme selection after each round. Reports accessible via "Relatórios" button with lazy-init plus refresh on subsequent visits. Compiles and packages with Maven.

### Phase 4: Reinforcement Mode & Navigation Polish ✓
**Outcome:** "Modo Reforço" checkbox added to theme selection screen. When enabled, `RoundState.createReinforcementRound()` factory selects highest-error questions first via `StatsService.getHighestErrorQuestions()`, fills remaining slots with fresh questions. `ThemeSelectionController` checks `view.isReinforcementMode()` and chooses the appropriate constructor. All 17 existing tests still pass.

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

## Open Questions

- (None)


*Last updated: 2026-07-07 after Phase 5 planning*
