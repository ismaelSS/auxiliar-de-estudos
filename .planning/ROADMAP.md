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
**Requirements:** UX-05, UX-06, ERR-01, ERR-02, ERR-03, ERR-04
**Plans:** 2 plans (Wave 1 → Wave 2)
**Success Criteria:**

1. Malformed JSON files show graceful error messages and are skipped
2. Empty themes folder shows appropriate message
3. File I/O errors handled without crashes
4. All screens have consistent styling and responsive layout

Plans:

- [ ] 05-01-PLAN.md — External CSS styling + ErrorUtil helper class (Wave 1)
- [ ] 05-02-PLAN.md — Error handling integration for ThemeLoader, StatsService, Main (Wave 2)

## Phase 6: Scoring System & Question ID Rework ✓ [2026-07-08]

**Goal:** Replace current reinforcement scoring with a robust per-question scoring system. Questions get IDs, wrong answers score negatively (-3 per miss, floor -10), right answers score positively (+2 per hit, cap +5). Theme selection screen replaces hit rate with aproveitamento score (negative -3 weight, neutral 0, positive +2 weight).
**Requirements:** SCORE-01, SCORE-02, SCORE-03, SCORE-04, SCORE-05
**Plans:** 3 plans (Wave 1 → Wave 3)
**Success Criteria:**

1. ✓ All questions in theme JSON files have a unique `id` field
2. ✓ Wrong answer reduces question score by -3 (min -10)
3. ✓ Correct answer increases question score by +2 (max +5)
4. ✓ Theme selection shows "aproveitamento" score instead of hit rate percentage
5. ✓ Aproveitamento formula: negative questions × -3, neutral × 0, positive × 2

Plans:

- [x] 06-01-PLAN.md — Question identity layer (Question id, RoundResult questionId, JSON IDs, ID validation, ID matching in RoundState)
- [x] 06-02-PLAN.md — StatsData/StatsService rewrite (QuestionScore, score deltas, migration, aproveitamento, lowest-score query, RoundState update)
- [x] 06-03-PLAN.md — Views, controllers, tests (aproveitamento display, reports update, all 3 test files rewritten)

## Phase 7: Reports & AI-Assisted Review

**Goal:** Reformular relatórios de desempenho e tela de seleção de temas: exibir "domínio" (porcentagem de questões com pontuação positiva) na seleção de temas; refocar relatório nas questões de menor pontuação por tema com seções em gaveta (limite 10); adicionar botão de prompt IA por tema.
**Requirements:** DOM-01, DOM-02, DOM-03, DOM-04, DOM-05
**Depends on:** Phase 6
**Success Criteria:**

1. Theme selection screen shows "Domínio: X%" (percentage of questions with positive score)
2. Reports tab focused on lowest-scoring questions per theme
3. Each theme in reports has drawer-style expandable section with up to 10 lowest-scoring questions
4. Each theme tab has a button that copies an AI prompt about difficult questions to clipboard
5. All existing tests still pass

Plans:

- [x] 07-01-PLAN.md — StatsService extension: getDominio() + getLowestScoreQuestionsByTheme() + tests (Wave 1)
- [x] 07-02-PLAN.md — ThemeSelectionView dominio display + controller wiring (Wave 2)
- [ ] 07-03-PLAN.md — ReportsView Accordion drawers + ReportsController AI prompt copy (Wave 2)
