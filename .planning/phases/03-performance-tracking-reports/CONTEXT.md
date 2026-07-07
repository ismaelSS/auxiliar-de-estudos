# Phase 3 Context: Performance Tracking & Reports

## Phase Goal
Implement performance persistence (`flashcard-stats.json`) and a reports screen showing per-theme and overall stats, highest-error question ranking, and navigation from theme selection to reports.

## Requirements Covered
- **STATS-01**: Performance metrics saved to `flashcard-stats.json` (Jackson, separate file)
- **STATS-02**: Per-theme stats: questions answered, hit rate, per-question answer history
- **STATS-03**: Overall stats aggregated across all themes
- **STATS-04**: Identification of questions with highest error rate
- **REPORT-01**: Report screen showing error/hit summary
- **REPORT-02**: Report screen showing highest-error questions ranking
- **REPORT-03**: Report screen showing per-theme performance breakdown
- **THEME-04** (indirect): Replace the "N/A" hit-rate stub on ThemeSelectionView with real data from StatsService

## Architecture & Design Decisions

### Stats JSON Schema (`flashcard-stats.json`)
```json
{
  "themes": {
    "matematica": {
      "totalAnswered": 10,
      "totalCorrect": 7,
      "questions": {
        "O que é 2+2?": { "answered": 2, "correct": 2 },
        "Qual a raiz quadrada de 9?": { "answered": 3, "correct": 1 }
      }
    }
  },
  "overall": {
    "totalAnswered": 20,
    "totalCorrect": 14
  }
}
```
- Questions keyed by question text (unique enough for a single-user flashcard app)
- If two questions have identical text, stats merge — acceptable for this scope
- Stats are incremental (append-only updates, never overwrite full history)

### Stats Service (`service/StatsService.java`)
- Singleton or instance passed via Main → controllers
- `load()` — reads `flashcard-stats.json` on app startup via Jackson, returns StatsData object (or empty if file doesn't exist)
- `save()` — writes StatsData back to `flashcard-stats.json` after each round
- `recordRound(List<RoundResult>)` — accepts per-question results (theme, question text, wasCorrect), updates in-memory StatsData, writes to disk
- `getThemeStats(String themeName)` — returns per-theme breakdown
- `getOverallStats()` — returns aggregated total/correct
- `getHighestErrorQuestions(int limit)` — returns questions sorted by error rate descending
- `getHitRate(String themeName)` — returns hit rate for ThemeSelectionView (replaces "N/A" stub)

### Round Integration
- RoundState or StudyRoundController needs to expose per-question results with theme info for StatsService
- **Approach**: Introduce a lightweight `RoundResult` record: `(String themeName, String questionText, boolean wasCorrect)`
- StudyRoundController collects `List<RoundResult>` during the round
- On round complete (or exit), passes results to `StatsService.recordRound()`
- RoundState needs `recordAttempt(String themeName, int index, boolean correct)` or similar

### Model — StatsData POJO
- `StatsData` — root: `Map<String, ThemeStats> themes`, `OverallStats overall`
- `ThemeStats` — `int totalAnswered, totalCorrect, Map<String, QuestionStats> questions`
- `QuestionStats` — `int answered, int correct`
- `OverallStats` — `int totalAnswered, int totalCorrect`
- Jackson `@JsonProperty` annotations or standard POJO with getters/setters

### Reports View (`view/ReportsView.java`)
- Programmatic JavaFX scene (no FXML, consistent with Phase 1-2)
- Three sections (VBox with titled sub-containers):
  1. **Overall Summary** — total answered, total correct, overall hit rate %
  2. **Per-Theme Breakdown** — list of themes with individual hit rates (HBox or Grid for each)
  3. **Highest-Error Questions** — ranked list (top N questions with worst hit rates)
- "Voltar" button to return to theme selection
- ScrollPane if content exceeds viewport

### Reports Controller (`controller/ReportsController.java`)
- Constructor: `StatsService`, `ReportsView`, `ScreenController`
- `initialize()` — loads stats from StatsService, populates ReportsView, registers scene in ScreenController
- Navigation: "Voltar" → themeSelection

### Theme Selection Updates
- ThemeSelectionView already shows `" — Hit rate: N/A"` per theme
- Phase 3 updates: instead of "N/A", display real hit rate via `StatsService.getHitRate(themeName)`
- ThemeSelectionController needs StatsService reference to populate hit rates
- ThemeSelectionView needs a "Relatórios" button to navigate to reports screen

### Main.java Updates
- Create `StatsService` instance and pass it to:
  - `ThemeSelectionController` (for hit rate display + reports navigation)
  - `StudyRoundController` (via ThemeSelectionController wiring) for recording results

### Data Flow Per Round
1. StudyRoundController initializes `List<RoundResult>` (empty)
2. Each answer → RoundState.checkAnswer() returns boolean → StudyRoundController appends `new RoundResult(themeName, questionText, wasCorrect)` to list
3. Round completes or user exits → StudyRoundController calls `statsService.recordRound(results)`
4. StatsService updates in-memory data + writes to flashcard-stats.json

### No Changes to ThemeLoader or Question/Theme Models
- Question model stays unchanged (no `id` field — questions identified by text)

## Dependencies
- **Phase 1 provides**: Jackson ObjectMapper (already in pom.xml), JSON file I/O patterns
- **Phase 2 provides**: StudyRoundController (needs to emit RoundResult data), RoundState (needs result tracking)
- **Phase 4 depends on**: Phase 3's StatsService for reinforcement mode (ROUND-04) and UI-04 navigation

## Open Questions
- (None resolved — all design decisions covered above)
