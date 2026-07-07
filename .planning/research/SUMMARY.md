# Research Summary: FlashCard JavaFX

## Key Findings

**Stack:** Java 26 + JavaFX 26.0.1 + Maven + Jackson 2.17+ for JSON. MVC architecture with FXML or programmatic views.

**Table Stakes:** Question display with multiple choice, correct/incorrect feedback, score tracking, theme-based question sets.

**Differentiators:** Theme hit rate display, reinforcement mode, hot-pluggable JSON, full performance persistence, random alternatives, no-repeat questions.

**Architecture:** 3-layer design (UI/Views → Business Logic Services → Data Access). Theme auto-detection from `themes/*.json`. Performance stored in `flashcard-stats.json`.

**Watch Out For:** 
- JavaFX module path configuration with Maven (Phase 1)
- UI thread blocking from file I/O (use background tasks)
- Hardcoded file paths for themes directory
- Stats file corruption from concurrent writes
- Empty/malformed theme JSON files

**Build Order:**
1. Phase 1: Project scaffold + JavaFX/Maven setup + theme loading
2. Phase 2: Study round engine (shuffle, no-repeat, alternatives)
3. Phase 3: Performance tracking + stats persistence
4. Phase 4: Reinforcement mode + reports screen
5. Phase 5: Polish, error handling, UX refinements
