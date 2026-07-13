---
phase: 11-question-file-manager
plan: 02
subsystem: ui
tags: [javafx, crud, file-manager, question-editor, auto-save]

requires:
  - phase: 11-question-file-manager
    provides: TabPane-based home screen with Gerenciar tab placeholder
provides:
  - QuestionFileManagerView with file list and question editor UI
  - QuestionFileManagerController with file/question CRUD and auto-save
  - ThemeLoader.saveTheme() and deleteTheme() for file persistence
  - ThemeLoader.listThemeFiles() and loadThemeQuestions() for file listing
affects: [question-file-manager]

tech-stack:
  added: []
  patterns: [auto-save-on-focus-lost, sanitize-title-to-filename, embedded-tab-crud]

key-files:
  created:
    - src/main/java/org/IsmaelSS/view/QuestionFileManagerView.java
    - src/main/java/org/IsmaelSS/controller/QuestionFileManagerController.java
  modified:
    - src/main/java/org/IsmaelSS/service/ThemeLoader.java
    - src/main/java/org/IsmaelSS/controller/ThemeSelectionController.java

key-decisions:
  - "ThemeLoader.loadAllThemes() skips empty files — added listThemeFiles() for file manager"
  - "refreshScores() now reloads themes from disk to reflect file create/delete"
  - "Auto-save on focus-lost rather than explicit save button"

patterns-established:
  - "File manager pattern: listThemeFiles() + loadThemeQuestions() for CRUD views"
  - "Embedded tab content: View returns VBox root, Controller handles lazy-init"
  - "Auto-save: focus-lost listeners on TextField, valueProperty listener on ComboBox"

requirements-completed: [QMGMT-02, QMGMT-03, QMGMT-04, QMGMT-05, QMGMT-06, QMGMT-07]

duration: 7min
completed: 2026-07-13
---

# Phase 11 Plan 02: File + Question CRUD Summary

**QuestionFileManagerView with file list/create/delete, question editor with add/edit/delete, and auto-save via ThemeLoader persistence**

## Performance

- **Duration:** 7 min
- **Started:** 2026-07-13T12:43:19Z
- **Completed:** 2026-07-13T12:51:11Z
- **Tasks:** 5
- **Files modified:** 4

## Accomplishments

- ThemeLoader gained saveTheme(), deleteTheme(), listThemeFiles(), loadThemeQuestions()
- QuestionFileManagerView: file list with create/delete, question editor with text fields, option fields, correct answer ComboBox
- QuestionFileManagerController: full CRUD with auto-save on focus-lost, confirmation dialogs, ID re-indexing
- ThemeSelectionController wired to lazy-init QuestionFileManagerView on Gerenciar tab
- All 38 existing tests pass, compiles clean

## Task Commits

Each task was committed atomically:

1. **Task 1: ThemeLoader saveTheme/deleteTheme** - `dfa67ed` (feat)
2. **Task 2: QuestionFileManagerView** - `c54577d` (feat)
3. **Task 3: QuestionFileManagerController** - `7415564` (feat)
4. **Task 4: Wire into ThemeSelectionController** - `6106606` (feat)
5. **Task 5: Fix empty file listing (deviation)** - `c09d003` (fix)

## Files Created/Modified

- `src/main/java/org/IsmaelSS/view/QuestionFileManagerView.java` - VBox layout with file list section and scrollable question editor (172 lines)
- `src/main/java/org/IsmaelSS/controller/QuestionFileManagerController.java` - CRUD logic for files and questions with auto-save (217 lines)
- `src/main/java/org/IsmaelSS/service/ThemeLoader.java` - Added saveTheme(), deleteTheme(), listThemeFiles(), loadThemeQuestions()
- `src/main/java/org/IsmaelSS/controller/ThemeSelectionController.java` - Lazy-init QuestionFileManagerView on Gerenciar tab, refreshScores() reloads themes

## Decisions Made

- ThemeLoader.loadAllThemes() skips empty theme files — added listThemeFiles() to list all .json files regardless of content, and loadThemeQuestions() to load specific file content for the editor.
- refreshScores() now reloads themes from disk (not just updates scores) so Jogar tab reflects file create/delete.
- Auto-save uses focus-lost listeners on TextFields and valueProperty listeners on ComboBoxes — no explicit save button needed.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Empty theme files not listed in file manager**
- **Found during:** Task 4 (wiring controller)
- **Issue:** ThemeLoader.loadAllThemes() returns null for empty theme files ([]), so newly created files wouldn't appear in the file list
- **Fix:** Added listThemeFiles() to return all .json file names without parsing, and loadThemeQuestions() to load specific file content. Updated controller to use these methods.
- **Files modified:** ThemeLoader.java, QuestionFileManagerController.java
- **Verification:** Empty files now listed correctly, all 38 tests pass
- **Committed in:** c09d003 (fix commit)

**2. [Rule 1 - Bug] refreshScores() didn't update Jogar tab theme list**
- **Found during:** Task 4 (wiring controller)
- **Issue:** refreshScores() only updated score labels on existing themes — after file create/delete, the Jogar tab wouldn't show new/removed themes
- **Fix:** refreshScores() now reloads themes from disk and calls view.setThemes() before updating scores
- **Files modified:** ThemeSelectionController.java
- **Verification:** Jogar tab updates after file operations
- **Committed in:** 6106606 (part of Task 4 commit)

---

**Total deviations:** 2 auto-fixed (2 bugs)
**Impact on plan:** Both fixes necessary for correct behavior. No scope creep.

## Issues Encountered

None beyond the auto-fixed deviations.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Gerenciar tab fully functional with file CRUD and question editing
- Ready for any additional UI polish or testing phases
- Jogar tab auto-refreshes when files are created/deleted

---
*Phase: 11-question-file-manager*
*Completed: 2026-07-13*
