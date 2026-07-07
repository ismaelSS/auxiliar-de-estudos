# Phase 1 Context: Project Scaffold & Theme Loading

## Phase Goal
Set up JavaFX + Maven project, create data models (Question + Theme), implement theme JSON loading with auto-detection, and build the theme selection screen with multi-select and question count controls.

## Requirements Covered
- **UI-01**: JavaFX window with navigation between screens (Phase 1 → initial window + theme selection)
- **THEME-01**: Questions stored as JSON files with 5 multiple-choice options in `themes/`
- **THEME-02**: Themes auto-detected from JSON filenames (`themes/*.json`)
- **THEME-03**: Hot-pluggable JSON (added/modified post-compilation)
- **THEME-04**: Hit rate displayed per theme (stub "N/A" until Phase 3 provides StatsService)
- **THEME-05**: User can select one or multiple themes for a round
- **THEME-06**: User can choose how many questions per round
- **DIST-01**: Compilable with `mvn clean package`
- **DIST-02**: Runnable via `mvn javafx:run`

## Design Decisions

### Architecture: MVC with Programmatic Views
- **No FXML** — programmatic SceneGraph construction via JavaFX API (simpler for this scope, no additional tooling)
- **Package structure**: `model/`, `service/`, `view/`, `controller/` under `org.IsmaelSS`
- **ScreenController** manages scene switching via `Stage.setScene()`

### JSON Question Schema
```json
{
  "question": "Texto da pergunta?",
  "options": ["Opção A", "Opção B", "Opção C", "Opção D", "Opção E"],
  "correct": 0  // zero-based index of correct option
}
```
- Jackson `ObjectMapper` with `@JsonProperty` annotations on Question POJO
- Each JSON file in `themes/` contains an **array** of question objects

### Hit Rate Stub
- `ThemeLoader` will have a setter/optional field for hit rate
- Display `"N/A"` until Phase 3 wires real stats

### Theme Model
- `Theme` wraps: name (derived from filename), list of questions, computed question count
- No stats data in Phase 1 (no stats file exists yet)

### ThemeLoader Design
- Scans `themes/` directory relative to `user.dir` at app startup
- Filters for `.json` extension
- Parses each file with Jackson
- Validates: file exists, valid JSON, non-empty question array, correct field has 5 options
- Skips invalid files with logged warning (Phase 5 will add user-facing error handling)

### Screen Navigation (Phase 1 minimal)
- Only ThemeSelectionView exists as a full screen
- ScreenController prepares navigation infrastructure (map of screen name → Scene)
- Future screens (StudyRoundView, ReportsView) are stubs/placeholder

## Open Questions (Resolved)
- **FXML vs programmatic?** → Programmatic (simpler, no extra tooling, full control)
- **Theme directory path?** → Relative `themes/` from working directory (`System.getProperty("user.dir")`)
- **Sample questions?** → Yes, create `matematica.json` (5 questions) and `historia.json` (5 questions) for testing
