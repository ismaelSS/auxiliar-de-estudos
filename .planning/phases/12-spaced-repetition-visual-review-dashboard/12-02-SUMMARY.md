---
phase: 12-spaced-repetition-visual-review-dashboard
plan: 02
type: execute
wave: 2
subsystem: dashboard-ui
status: complete
completed_date: 2026-07-21
requirements: [SR-03, SR-04, SR-05, SR-06, SR-07, SR-08]

key_files:
  created:
    - src/main/java/org/IsmaelSS/view/ThemeCardNode.java
    - src/main/java/org/IsmaelSS/view/TimelineView.java
    - src/main/java/org/IsmaelSS/view/ReviewDashboardView.java
  modified:
    - src/main/resources/styles/theme.css
    - src/main/java/org/IsmaelSS/view/ThemeSelectionView.java
    - src/main/java/org/IsmaelSS/controller/ThemeSelectionController.java
    - src/main/java/org/IsmaelSS/controller/QuestionFileManagerController.java

tech_stack:
  added: []
  patterns:
    - Priority enum (OVERDUE/TODAY/NONE) with CSS class toggling
    - Callback factories (Function<Theme, Runnable>) for decoupled dashboard→controller wiring
    - ScrollPane wrapping for vertical scroll overflow
    - TreeMap with reverseOrder for timeline grouping

decisions:
  - "Dashboard replaces Jogar tab entirely (no backward compat with old checkbox/spinner UI)"
  - "onReviewFactory passed as Function<Theme, Runnable> to decouple view from controller"
  - "onMarkDone passed as Consumer<String> (themeName) with confirmation dialog in controller"
  - "ThemeCardNode computes fixation distribution from ThemeStats via static helper"

metrics:
  total_tasks: 2
  completed_tasks: 2
  test_count: 51
  test_failures: 0
  full_suite: passes (exit code 0)

dependency_graph:
  provides:
    - "dashboard-ui: theme cards, priority ordering, search, timeline, action buttons"
  requires:
    - "12-01: SM-2 data layer (service methods, RoundState factory)"
  affects:
    - "ThemeSelectionView (Jogar tab replaced), ThemeSelectionController (wiring)"
---

# Phase 12 Plan 02: Visual Review Dashboard — Summary

Build the complete visual review dashboard: CSS styling, ThemeCardNode component, TimelineView component, ReviewDashboardView container, then refactor ThemeSelectionView and ThemeSelectionController to wire everything together.

## Commit History

| # | Commit | Message |
|---|--------|---------|
| 1 | `bb72560` | feat(12): implement visual review dashboard (Plan 12-02, Wave 2) |

## Files Created

- **`ThemeCardNode.java`** — VBox-based card component with priority border, overdue badge, dominio%, fixation bar (4 colored segments), Revisar/Feito buttons. Methods: `setPriority(Priority)`, `refresh(...)`, `computeFixationDist(ThemeStats)` static helper.

- **`TimelineView.java`** — VBox component rendering per-day study history from TreeMap with reverseOrder. Shows date labels, vertical lines, and per-theme entry counts.

- **`ReviewDashboardView.java`** — ScrollPane-based dashboard composing search, priority-sorted theme cards, and timeline. Accepts callback factories for decoupled controller wiring.

## Files Modified

### theme.css
Appended ~25 CSS classes for dashboard components: `.theme-card`, `.priority-*`, `.badge-overdue`, `.badge-aprendendo/revisao/fixa/dominio`, `.fixation-bar`, `.fixation-segment-*`, `.button-revisar`, `.button-feito`, `.timeline-*`, `.search-field`.

### ThemeSelectionView.java
Removed all old Jogar tab content (themeListContainer, questionCountSpinner, reforcoCheckBox, startButton, feedbackLabel, themeCheckboxes, themeLabels, themeMap, themeScores, themeDominioText). Removed methods: setThemes(), getSelectedThemes(), getQuestionCount(), isReinforcementMode(), getReforcoCheckBox(), getStartButton(), setFeedback(), updateAproveitamento(), updateDominio(), updateQuestionCountRange(). Added: `setDashboard(ReviewDashboardView)`, `getDashboard()`.

### ThemeSelectionController.java
Replaced refreshScores()/handleStart() with `refreshDashboard()`. Added `handleReviewTheme(Theme)` using RoundState.createDueReviewRound. Added `handleMarkAsDone(String)` with confirmation dialog and statsService.markThemeAsDone. Updated QuestionFileManagerController.java reference.

## Test Results

All 51 tests pass (13 new SM-2 tests + 38 existing). No new tests added in this plan — dashboard is view-layer only, tested via compilation and integration.

## Requirement Coverage

| Req ID | Description | Status | Covered By |
|--------|-------------|--------|------------|
| SR-03 | Theme cards with priority ordering | ✓ | ThemeCardNode + ReviewDashboardView |
| SR-04 | Overdue count badges | ✓ | ThemeCardNode.updateBadge() |
| SR-05 | Fixation phase bar | ✓ | ThemeCardNode.buildFixationBar() |
| SR-06 | Action buttons (Revisar/Feito) | ✓ | ThemeCardNode buttons + controller handlers |
| SR-07 | Timeline showing study history | ✓ | TimelineView + StatsService.getTimelineData() |
| SR-08 | Search filtering | ✓ | ReviewDashboardView.filterCards() |

## Known Stubs

None — all functionality is fully wired.

## Threat Flags

None — search field filters UI only, markThemeAsDone protected by confirmation dialog.
