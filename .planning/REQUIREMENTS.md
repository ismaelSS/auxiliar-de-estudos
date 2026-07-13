# Requirements: FlashCard JavaFX

**Defined:** 2026-07-07
**Core Value:** Students can study any topic by loading a themed JSON question bank and immediately start quizzing themselves with performance tracking and weak-area reinforcement.

## v1 Requirements

### UI & Navigation

- [ ] **UI-01**: Application opens a JavaFX window with navigation between screens (theme selection, study round, reports)
- [x] **UI-02**: Question screen displays question text and 5 alternatives as clickable elements
- [x] **UI-03**: After answering, correct alternative highlighted green, wrong selection (if any) highlighted red
- [ ] **UI-04**: User can navigate back from reports to theme selection

### Theme Management

- [ ] **THEME-01**: Questions stored as JSON files with 5 multiple-choice options in a dedicated `themes/` folder
- [ ] **THEME-02**: Themes auto-detected from JSON files in `themes/` folder; theme name = filename (minus .json)
- [ ] **THEME-03**: New JSON files can be added post-compilation; existing files can be modified post-compilation
- [ ] **THEME-04**: During theme selection, hit rate displayed per theme
- [ ] **THEME-05**: User can select one or multiple themes for a round
- [ ] **THEME-06**: User can choose how many questions per round

### Study Round

- [x] **ROUND-01**: Questions served in random order within a round
- [x] **ROUND-02**: No question repeats within the same round
- [x] **ROUND-03**: Alternative order randomized each round
- [ ] **ROUND-04**: Reinforcement mode prioritizes questions with highest error rate from past sessions
- [x] **ROUND-05**: Round ends when all selected questions have been answered or user exits

### Performance Tracking

- [ ] **STATS-01**: Performance metrics saved to a separate JSON file (`flashcard-stats.json`)
- [ ] **STATS-02**: Per-theme stats: questions answered, hit rate, per-question answer history
- [ ] **STATS-03**: Overall stats aggregated across all themes
- [ ] **STATS-04**: Identification of questions with highest error rate

### Reports

- [ ] **REPORT-01**: Report screen showing error/hit summary
- [ ] **REPORT-02**: Report screen showing highest-error questions ranking
- [ ] **REPORT-03**: Report screen showing per-theme performance breakdown

### Distribution

- [ ] **DIST-01**: Application compilable with `mvn clean package`
- [ ] **DIST-02**: Application runnable via `mvn javafx:run`

### Question File Management

- [ ] **QMGMT-01**: New "Gerenciar" tab accessible from theme selection screen
- [ ] **QMGMT-02**: Lists all .json files in `themes/` folder with titles
- [ ] **QMGMT-03**: User can create new .json file with title (auto-generate ID, structure with empty questions array)
- [ ] **QMGMT-04**: User can add questions (question text + 5 alternatives + correct index) to an existing file
- [ ] **QMGMT-05**: User can delete questions from an existing file
- [ ] **QMGMT-06**: User can delete entire .json files (with confirmation dialog)
- [ ] **QMGMT-07**: Changes persist to disk and reflect immediately in theme selection screen

## v2 Requirements

### Advanced Features

- **V2-01**: Spaced repetition algorithm (SM-2) for smarter review scheduling
- **V2-02**: Statistics charts/visualizations (charts with JavaFX Charts API)
- **V2-03**: True/False question type support alongside multiple choice
- **V2-04**: Export/import performance data

## Out of Scope

| Feature | Reason |
|---------|--------|
| Multi-user / Authentication | Single-user local app |
| Web or mobile version | Desktop-only with JavaFX |
| Cloud sync | No online features |
| Question editing UI | Questions edited by modifying JSON files externally |
| Image/media in questions | Text-only questions for v1 |
| Timed quizzes | Not requested; focus on accuracy |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| UI-01 | Phase 1 | Complete ✓ |
| THEME-01 | Phase 1 | Complete ✓ |
| THEME-02 | Phase 1 | Complete ✓ |
| THEME-03 | Phase 1 | Complete ✓ |
| THEME-04 | Phase 1 | Stub (needs Phase 3) |
| THEME-05 | Phase 1 | Complete ✓ |
| THEME-06 | Phase 1 | Complete ✓ |
| UI-02 | Phase 2 | Complete ✓ |
| UI-03 | Phase 2 | Complete ✓ |
| ROUND-01 | Phase 2 | Complete ✓ |
| ROUND-02 | Phase 2 | Complete ✓ |
| ROUND-03 | Phase 2 | Complete ✓ |
| ROUND-05 | Phase 2 | Complete ✓ |
| STATS-01 | Phase 3 | Pending |
| STATS-02 | Phase 3 | Pending |
| STATS-03 | Phase 3 | Pending |
| STATS-04 | Phase 3 | Pending |
| REPORT-01 | Phase 3 | Pending |
| REPORT-02 | Phase 3 | Pending |
| REPORT-03 | Phase 3 | Pending |
| ROUND-04 | Phase 4 | Pending |
| UI-04 | Phase 4 | Pending |
| DIST-01 | Phase 1 | Complete ✓ |
| DIST-02 | Phase 1 | Complete ✓ |

**Coverage:**
- v1 requirements: 23 total
- Mapped to phases: 23
- Unmapped: 0 ✓

---
*Requirements defined: 2026-07-07*
*Last updated: 2026-07-07 after initial definition*
