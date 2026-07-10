# Phase 8: Resizable Window & Ctrl+Scroll Zoom — Context

**Gathered:** 2026-07-10
**Status:** Ready for planning
**Source:** User request

<domain>
## Phase Boundary

Make the entire application window resizable and add Ctrl+scroll wheel zoom functionality to adjust text size across all screens.

</domain>

<decisions>
## Implementation Decisions

### Window Resizing
- Stage is already resizable by default in JavaFX — ensure no fixed constraints prevent resizing
- Scene dimensions should adapt to stage size (currently hardcoded to 600x400 in ThemeSelectionView, ReportsView)
- Remove hardcoded scene dimensions: use `new Scene(root)` without width/height so scene fills the stage
- Min size: 600x500 (already set in Main.java)

### Ctrl+Scroll Zoom
- Ctrl+scroll up → increase font size
- Ctrl+scroll down → decrease font size
- Font size change persists across screen switches (ThemeSelection ↔ Reports ↔ StudyRound)
- Zoom applies to all text: labels, buttons, checkboxes, spinners — everything
- A shared font size tracker in ScreenController manages the current scale
- Use `ScrollEvent.SCROLL` event filter with `isControlDown()` check
- Use `event.consume()` when Ctrl is held to prevent ScrollPane double-scroll
- Zoom range: ~10px to ~30px base font, default ~14px
- Zoom step: 1px per scroll tick
- Use scene.addEventFilter (capture phase) not setOnScroll (bubbling phase)

### Font Inheritance
- Remove hardcoded inline `-fx-font-size` on title labels so they inherit root zoom
- Apply zoom via `root.setStyle("-fx-font-size: " + size + "px")` on each scene's root
- When switching screens, ScreenController applies the current zoom to the new scene

### the agent's Discretion
- Whether to save zoom preference to disk (session-only per current scope)

</decisions>

<canonical_refs>
## Canonical References

### Source files
- `src/main/java/org/IsmaelSS/Main.java` — Stage setup (min size: 600x500)
- `src/main/java/org/IsmaelSS/controller/ScreenController.java` — Scene registry, stage management
- `src/main/java/org/IsmaelSS/view/ThemeSelectionView.java` — Scene(600, 400) hardcoded
- `src/main/java/org/IsmaelSS/view/ReportsView.java` — Scene creation
- `src/main/java/org/IsmaelSS/view/StudyRoundView.java` — Scene creation (already responsive)

</canonical_refs>

<specifics>
## Specific Ideas
- JavaFX ScrollEvent: `scene.addEventFilter(ScrollEvent.SCROLL, e -> { if (e.isControlDown()) { ... e.consume(); } })`
- Font size change: `root.setStyle("-fx-font-size: " + size + "px")`
- Track font size in ScreenController: `private int baseFontSize = 14;`
- ScreenController method: `public void applyZoom(Scene scene)` applies current font size to the scene root
- Apply zoom in `switchTo()` after setting the new scene

</specifics>

<deferred>
## Deferred Ideas
- None — phase scope is well-defined

</deferred>

---

*Phase: 08-resizable-window-ctrl-scroll-zoom*
*Context gathered: 2026-07-10 via user request*
