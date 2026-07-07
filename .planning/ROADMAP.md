# Roadmap: FlashCard JavaFX

## Phase 1: Project Scaffold & Theme Loading ✓ [2026-07-07]
**Goal:** Set up JavaFX + Maven project, create data models, implement theme JSON loading with auto-detection.
**Requirements:** UI-01, THEME-01, THEME-02, THEME-03, THEME-04 (stub), THEME-05, THEME-06, DIST-01, DIST-02
**Success Criteria:**
1. ✓ Application launches a JavaFX window with a theme selection screen
2. ✓ Themes auto-detected from JSON files in `themes/` folder
3. ✓ User can see theme list (hit rates as "N/A"), select themes and question count
4. ✓ Application compiles with `mvn clean compile` and packages with `mvn package`

## Phase 2: Study Round Engine ✓ [2026-07-07]
**Goal:** Implement the core study round — question display, answer feedback, randomization, no-repeat logic.
**Requirements:** UI-02, UI-03, ROUND-01, ROUND-02, ROUND-03, ROUND-05
**Success Criteria:**
1. ✓ Questions displayed with 5 randomized alternatives
2. ✓ Clicking an alternative shows correct (green) / wrong (red) highlight
3. ✓ No question repeats within a round
4. ✓ Round ends when all questions answered or user exits

## Phase 3: Performance Tracking & Reports ✓ [2026-07-07]
**Goal:** Implement performance persistence (`flashcard-stats.json`) and a reports screen showing per-theme and overall stats, highest-error question ranking, and navigation from theme selection to reports.
**Requirements:** STATS-01, STATS-02, STATS-03, STATS-04, REPORT-01, REPORT-02, REPORT-03
**Plans:** 4 plans (Wave 1 → Wave 3)
**Success Criteria:**
1. ✓ Performance data persisted to `flashcard-stats.json` across sessions
2. ✓ Per-theme and overall stats displayed on reports screen
3. ✓ Highest-error questions identified and shown
4. ✓ Stats survive app restart

Plans:
- [x] 03-01-PLAN.md — Stats Model + Service (Wave 1)
- [x] 03-02-PLAN.md — Round Integration (Wave 2)
- [x] 03-03-PLAN.md — Reports Screen (Wave 2)
- [x] 03-04-PLAN.md — Theme + Main Wiring (Wave 3)

## Phase 4: Reinforcement Mode & Navigation Polish
**Goal:** Implement reinforcement mode and polish navigation flow.
**Requirements:** ROUND-04, UI-04
**Success Criteria:**
1. Reinforcement mode gives preference to questions with highest error rate
2. Navigation between screens is smooth and intuitive
3. Reports accessible from theme selection and vice versa

Plans:
- [x] 04-01-PLAN.md — Reinforcement: checkbox + RoundState factory + controller wiring

## Phase 5: Error Handling & UX Refinements
**Goal:** Handle edge cases — malformed JSON, empty themes, file I/O errors, and general UX polish.
**Requirements:** (Derived from quality concerns across all phases)
**Success Criteria:**
1. Malformed JSON files show graceful error messages and are skipped
2. Empty themes folder shows appropriate message
3. File I/O errors handled without crashes
4. All screens have consistent styling and responsive layout
