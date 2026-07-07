# FlashCard Java

## What This Is

A desktop flashcard study application built with JavaFX. Students can load question banks from JSON files organized by theme (topic), run study rounds with configurable theme selection and question count, track their performance metrics per theme, and get reinforcement on their weakest questions. Questions are multiple-choice with 5 options.

## Core Value

Students can study any topic by loading a themed JSON question bank and immediately start quizzing themselves with performance tracking and weak-area reinforcement.

## Requirements

### Validated

- [x] **UI-01**: JavaFX desktop window with navigation between screens (theme selection, study round, reports) — Phase 1
- [x] **THEME-01**: Questions stored as JSON files with 5 multiple-choice options, placed in a dedicated `themes/` folder — Phase 1
- [x] **THEME-02**: Theme auto-detection from JSON files in the themes folder; theme name = filename (minus extension) — Phase 1
- [x] **THEME-03**: New JSON files can be added post-compilation; existing files can be modified post-compilation — Phase 1
- [x] **THEME-05**: User can select one or multiple themes for a round — Phase 1
- [x] **THEME-06**: User can choose how many questions per round — Phase 1
- [x] **DIST-01**: Application compilable with `mvn clean package` — Phase 1
- [x] **DIST-02**: Application runnable via `mvn javafx:run` — Phase 1

### Active

- [ ] **THEME-04**: Theme hit rate displayed during theme selection
- [ ] **ROUND-01**: Before starting, user selects which themes to include and how many questions per round
- [ ] **ROUND-02**: No repeated questions within the same round
- [ ] **ROUND-03**: Alternative order randomized each round
- [ ] **ROUND-04**: "Reinforcement" mode that prioritizes questions with highest error rate
- [ ] **UI-02**: Question display with clickable alternatives; after answering, correct answer highlighted green, wrong selection (if any) highlighted red
- [ ] **STATS-01**: Performance metrics saved to a separate file (overall and per-theme: questions answered, hit rate, per-question stats)
- [ ] **STATS-02**: Report screen showing error/hit summary and highest-error questions

### Out of Scope

- Web/mobile version — Desktop-only (JavaFX)
- Multi-user or authentication — Single-user local app
- Cloud sync — No online features
- Spaced repetition algorithm (SM-2 etc.) — Simple reinforcement mode is sufficient
- Question editing UI — Questions edited by creating/modifying JSON files externally

## Context

- **Language:** Java 25
- **Build tool:** Maven
- **UI framework:** JavaFX (via Maven dependency)
- **Data format:** JSON for both questions and performance metrics
- **Persistence:** Local filesystem only

## Constraints

- **Tech Stack**: JavaFX is required (user mandate)
- **Data**: Questions must be editable post-compilation via JSON files
- **Persistence**: Performance data stored in JSON files alongside themes

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| JavaFX for UI | User requirement | ✓ Validated — Phase 1 |
| JSON for question storage | Human-readable, editable post-compilation | ✓ Validated — Phase 1 |
| Theme = JSON filename | Simple, automatic discovery | ✓ Validated — Phase 1 |
| JSON for performance data | Consistent with question format, no DB needed | — Pending |
| Programmatic views (no FXML) | Simpler, full control, no extra tooling | ✓ Validated — Phase 1 |
| JDK 25 | Environment constraint | ✓ Validated — Phase 1 |

---

*Last updated: 2026-07-07 after Phase 1 execution*
