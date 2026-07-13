# Phase 11 Context: Question File Manager

## Goal
Add a "Gerenciar" tab to the home screen for managing question files — CRUD of `.json` files and questions within them.

## Decisions

### UI Placement (Tab system)
- Home screen (`ThemeSelectionView`) refactored to use `TabPane` as root instead of `VBox`
- 3 tabs: **Jogar** (existing theme selection), **Relatórios** (embedded ReportsView), **Gerenciar** (new file manager)
- Study rounds remain separate screens via `ScreenController`
- ReportsView embedded as a Node — expose `getContent()` returning the VBox content tree (ReportsController still manages data refresh)
- Lazy init: Relatórios and Gerenciar tab content created on first click of that tab

### New File Creation
- User types a display title → system sanitizes to `title-sanitized.json`
- Template content: empty `[]`
- After create/delete, auto-refresh the theme list in the Jogar tab via `themeLoader.loadAllThemes()`

### Save Behavior
- Auto-save on each edit (question text, options, correct answer, add/remove question)
- Confirmation dialog before deleting files or questions

## Codebase Patterns (applicable)

### Existing patterns to follow
- **MVC:** View extends nothing (wraps JavaFX nodes), Controller handles logic, Service for I/O
- **Scene registration:** `ScreenController.registerScreen(name, scene)` for new screens
- **CSS classes:** dark theme from Phase 10 (`theme.css`), use existing classes (`.title`, `.button-primary`, `.button-secondary`, `.background`, `.section-title`, `.separator`, `.scroll-pane`)
- **JSON I/O:** Jackson `ObjectMapper` (shared from `ThemeLoader` or new service)
- **File location:** `themes/` directory, same as existing theme files

### Files to create/modify
| File | Change |
|---|---|
| `ThemeSelectionView.java` | Replace VBox root with TabPane; move existing content to "Jogar" tab; add "Relatórios" and "Gerenciar" tabs |
| `ThemeSelectionController.java` | Manage tab switching; lazy-init reports/file-manager on tab selection |
| `ReportsView.java` | Add `getContent()` method returning embeddable VBox node |
| `ReportsController.java` | May need minor adjustments for embedded mode (no scene switch) |
| `QuestionFileManagerView.java` | **New** — VBox layout with file list + question editor |
| `QuestionFileManagerController.java` | **New** — handles file CRUD, question editing, auto-save |
| `Main.java` | May need minor wiring (ThemeSelectionController handles everything internally) |

### Files NOT to modify
- `ScreenController.java` — zoom + CSS loading stays intact
- `StudyRoundView.java`, `StudyRoundController.java` — no changes needed
- `ThemeLoader.java` — may need a `saveTheme()` or `deleteTheme()` method, or a new service

## Sequence (expected flow)
1. App starts → TabPane with "Jogar" selected (existing theme selection content)
2. User switches to "Gerenciar" → lazy-init: lists `.json` files in `themes/`
3. User creates file → types title → auto-saves `title.json` with `[]` → Jogar tab refreshes
4. User selects a file → question list appears
5. User adds/edits/deletes questions → auto-save on each change
6. User deletes file → confirmation → delete + Jogar refreshes

## Refs
- `.planning/ROADMAP.md` — Phase 11 entry
- `.planning/REQUIREMENTS.md` — QMGMT-01 through QMGMT-07
- `src/main/resources/styles/theme.css` — CSS palette
- `src/main/java/org/IsmaelSS/service/ThemeLoader.java` — existing JSON I/O pattern
- `src/main/java/org/IsmaelSS/model/Question.java` — question model
- `src/main/java/org/IsmaelSS/model/Theme.java` — theme model
