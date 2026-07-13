# Phase 8: Resizable Window & Ctrl+Scroll Zoom - Research

**Researched:** 2026-07-10
**Domain:** JavaFX layout, CSS inheritance, scroll event handling
**Confidence:** HIGH

## Summary

This phase requires two coordinated changes: (1) making scenes responsive to stage resize by removing hardcoded Scene dimensions, and (2) implementing Ctrl+scroll wheel zoom that persists font size across screen switches.

**Current state:** Three views each create `Scene(root, 600, 400)` or `Scene(root, 600, 500)` with hardcoded dimensions. `ScreenController` manages screen switching but has no zoom awareness. No CSS files exist — all styling is inline via `setStyle()`.

**Key technical insight:** JavaFX CSS `-fx-font-size` set on the root node cascades to child nodes **only if those children don't have their own explicit font-size styles**. Since three views have hardcoded inline font sizes on title labels (18px, 20px, 16px), those titles won't respond to root-level zoom. The implementation must either remove hardcoded inline styles or use a programmatic approach to update font sizes on all controls.

**Primary recommendation:** Use `scene.addEventFilter(ScrollEvent.SCROLL, ...)` (capture phase) to intercept Ctrl+scroll before ScrollPane consumes it, track font size in `ScreenController`, and apply `-fx-font-size` to each scene's root on zoom and on screen switch.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Window resize handling | Stage / Scene | Layout panes (VBox/HBox) | Stage drives size; VBox fillWidth propagates to children |
| Ctrl+Scroll zoom event | Scene (event filter) | — | Filter on scene catches all scroll events before child nodes |
| Font size state | ScreenController | — | Centralized state survives screen switches |
| Font size application | View classes (root node) | — | Each view's root node receives -fx-font-size |
| ScrollPane zoom coexistence | ReportsView | Scene filter | ReportsView has ScrollPane; filter must consume Ctrl+scroll to prevent double-scroll |

## Standard Stack

### Core (no new dependencies required)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| javafx.scene.input.ScrollEvent | JavaFX 25 | Ctrl+scroll wheel detection | Built-in JavaFX event API |
| javafx.scene.layout.VBox | JavaFX 25 | Responsive root layout | Already used; supports fillWidth, vgrow |
| javafx.scene.layout.Region | JavaFX 25 | USE_COMPUTED_SIZE constant | Standard for removing hardcoded dimensions |

### Supporting (no installation needed)

| Component | Purpose | When to Use |
|-----------|---------|-------------|
| `scene.addEventFilter()` | Capture-phase scroll interception | Every screen — prevents ScrollPane from consuming Ctrl+scroll |
| `root.setStyle("-fx-font-size: Npx")` | Apply zoom to all inheriting children | On zoom change and screen switch |
| `stage.widthProperty().heightProperty()` | Stage resize listeners (optional) | If dynamic reflow needed on resize |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Inline `-fx-font-size` on root | External CSS stylesheet | Cleaner but adds complexity for a single property; current project uses no CSS files |
| Per-node font update | `root.lookupAll(".label")` CSS selector | Would miss buttons, checkboxes, spinners; root-level style is simpler |
| `setOnScroll` handler | `addEventFilter` (capture phase) | **Must use filter** — handler won't prevent ScrollPane from also scrolling |

**Installation:** None required — all APIs are built into JavaFX 25.

## Package Legitimacy Audit

No external packages are installed in this phase. This is a pure code/config change phase.

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Stage (resizable)                     │
│  ┌───────────────────────────────────────────────────┐  │
│  │              Scene (dynamic size)                  │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │  Event Filter: ScrollEvent.SCROLL           │  │  │
│  │  │  if (isControlDown()) → update fontSize     │  │  │
│  │  │  if (isControlDown()) → consume()           │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────┐  │  │
│  │  │  Root VBox (root.setStyle("-fx-font-size")) │  │  │
│  │  │  ├── Title Label                           │  │  │
│  │  │  ├── Content VBox                          │  │  │
│  │  │  │   ├── Controls...                       │  │  │
│  │  │  │   └── Buttons...                        │  │  │
│  │  │  └── (ScrollPane in ReportsView)            │  │  │
│  │  └─────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│           ScreenController (font size state)             │
│  - int baseFontSize = 14                                │
│  - void setFontSize(int size)                           │
│  - int getFontSize()                                    │
│  - void switchTo(name) → applies font to new scene root │
│  - void installZoomFilter(Stage stage)                  │
└─────────────────────────────────────────────────────────┘
```

### Pattern 1: Event Filter for Ctrl+Scroll Zoom

**What:** Intercept scroll events during the capture phase to detect Ctrl+scroll and apply zoom before the event reaches child nodes (like ScrollPane).

**When to use:** Every time you need Ctrl+scroll zoom to coexist with scrollable content.

**Example:**
```java
// Source: [CITED: docs.oracle.com/javase/8/javafx/api/javafx/scene/input/ScrollEvent.html]
// Source: [CITED: StackOverflow answer — addEventFilter prevents ScrollPane consumption]
// Installed on the Scene (not individual nodes)
scene.addEventFilter(ScrollEvent.SCROLL, event -> {
    if (event.isControlDown()) {
        double deltaY = event.getDeltaY();
        if (deltaY > 0) {
            fontSize = Math.min(MAX_FONT_SIZE, fontSize + 1);
        } else if (deltaY < 0) {
            fontSize = Math.max(MIN_FONT_SIZE, fontSize - 1);
        }
        applyFontSize(scene);
        event.consume(); // Critical: prevents ScrollPane from scrolling
    }
});
```

### Pattern 2: Font Size Application to Root

**What:** Apply `-fx-font-size` to the scene's root node so all children inherit the new size.

**When to use:** On zoom change and when switching screens.

**Example:**
```java
// Source: [CITED: openjfx.io/javadoc/24 — VBox CSS cascading]
private void applyFontSize(Scene scene) {
    if (scene != null && scene.getRoot() != null) {
        scene.getRoot().setStyle("-fx-font-size: " + fontSize + "px");
    }
}
```

**Critical nuance:** Children with explicit inline `-fx-font-size` will NOT inherit from root. Remove hardcoded font sizes from titles, or accept that titles stay at their original size.

### Pattern 3: Scene Without Hardcoded Dimensions

**What:** Create a Scene without width/height so it adapts to the stage's size.

**When to use:** For all views that should be resizable.

**Example:**
```java
// Source: [CITED: openjfx.io/javadoc/24 — VBox resizable range]
// Before: scene = new Scene(root, 600, 400);
// After:
scene = new Scene(root);  // No dimensions — uses stage size
```

### Anti-Patterns to Avoid

- **Using `setOnScroll` instead of `addEventFilter`:** Handler runs during bubbling phase. If a ScrollPane is present (ReportsView), it will process the scroll event first. The handler then cannot prevent the double-scroll behavior. [CITED: StackOverflow answer on ScrollEvent consumed inside ScrollPane]
- **Hardcoding scene dimensions:** `new Scene(root, 600, 400)` prevents the scene from resizing with the stage. Use `new Scene(root)` or `new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE)`. [CITED: VBox resizable range docs]
- **Not consuming the event after handling Ctrl+scroll:** If `event.consume()` is not called, the ScrollPane in ReportsView will also scroll vertically while zooming. [CITED: StackOverflow WebView ScrollEvent zoom + scroll fix]

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Ctrl+scroll detection | Custom KeyListener polling | `ScrollEvent.isControlDown()` | Built-in, reliable, no polling needed |
| Font inheritance through node tree | Recursive tree walk | `root.setStyle("-fx-font-size")` | JavaFX CSS inheritance handles this automatically |
| ScrollPane zoom conflict | Custom event re-dispatch | `addEventFilter` + `consume()` | Standard JavaFX event model handles capture/bubbling |
| Window resize handling | ResizeListener with manual relayout | VBox `fillWidth=true` (default) | Layout panes auto-resize children |

**Key insight:** JavaFX's CSS inheritance and event model already solve these problems — the main work is correctly wiring the existing APIs, not building custom infrastructure.

## Common Pitfalls

### Pitfall 1: ScrollPane Double-Scroll on Ctrl+Zoom
**What goes wrong:** When using Ctrl+scroll in ReportsView (which has a ScrollPane), both zoom and vertical scroll happen simultaneously.
**Why it happens:** `setOnScroll` is a bubbling-phase handler — the ScrollPane processes the event before the handler runs.
**How to avoid:** Use `scene.addEventFilter(ScrollEvent.SCROLL, ...)` which runs during capture phase, and call `event.consume()` when Ctrl is held.
**Warning signs:** ReportsView content scrolls up/down while font size changes.

### Pitfall 2: Hardcoded Inline Font Sizes Block Zoom
**What goes wrong:** Title labels (18px, 20px, 16px) don't change when zoom is applied via root `-fx-font-size`.
**Why it happens:** Inline styles have higher CSS specificity than inherited styles. JavaFX `-fx-font-size` on a parent does NOT override a child's explicit inline `-fx-font-size`.
**How to avoid:** Remove hardcoded `-fx-font-size` from inline styles on labels, or accept titles remain fixed and only body text zooms.
**Warning signs:** Font size changes on body text but titles stay the same size.

### Pitfall 3: New Views Created Without Zoom Applied
**What goes wrong:** When a new StudyRoundView is created (ThemeSelectionController.handleStart()), it doesn't receive the current zoom level.
**Why it happens:** New scenes are created after zoom was set on the previous scene's root.
**How to avoid:** After creating any new Scene and registering it with ScreenController, call `screenController.applyCurrentFontSize(scene)` or have ScreenController.auto-apply on `registerScreen()`.
**Warning signs:** StudyRoundView opens at default font size after zooming in ThemeSelection.

### Pitfall 4: Stage MinSize Conflict
**What goes wrong:** When zoomed to large font size, content overflows and becomes unreadable at the minimum window size.
**Why it happens:** 600×500 minimum may be too small for 30px font with wrapping text.
**How to avoid:** Either increase min size at large zoom levels, or let content scroll within the window. Since ReportsView already has a ScrollPane and ThemeSelection/StudyRound are simple VBoxes, natural scrolling is acceptable.
**Warning signs:** Text truncated or overlapping at minimum window size with large zoom.

### Pitfall 5: Font Size Not Persisted to Disk
**What goes wrong:** Zoom level resets on app restart.
**Why it happens:** Zoom state is stored in memory (ScreenController field).
**How to avoid:** This is by design per CONTEXT.md — zoom is session-scoped, not persisted. But document this behavior clearly.
**Warning signs:** User expects zoom to survive restart.

## Code Examples

### Complete Ctrl+Scroll Zoom Implementation

```java
// Source: [CITED: openjfx.io/javadoc/21/ScrollEvent — isControlDown, getDeltaY]
// Source: [CITED: docs.oracle.com/javafx/2/events/filters.htm — addEventFilter]

public class ScreenController {
    private final Stage stage;
    private final Map<String, Scene> screens = new HashMap<>();
    private int baseFontSize = 14;
    private static final int MIN_FONT_SIZE = 10;
    private static final int MAX_FONT_SIZE = 30;

    public ScreenController(Stage stage) {
        this.stage = stage;
        installZoomFilter();
    }

    private void installZoomFilter() {
        // Install filter on the stage's scene — we'll update it as scenes change
        // Alternative: install on each scene when registered
    }

    public void registerScreen(String name, Scene scene) {
        screens.put(name, scene);
        applyCurrentFontSize(scene); // Auto-apply zoom to new screens
        installZoomFilterOnScene(scene);
    }

    private void installZoomFilterOnScene(Scene scene) {
        scene.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                double deltaY = event.getDeltaY();
                if (deltaY > 0) {
                    baseFontSize = Math.min(MAX_FONT_SIZE, baseFontSize + 1);
                } else if (deltaY < 0) {
                    baseFontSize = Math.max(MIN_FONT_SIZE, baseFontSize - 1);
                }
                applyCurrentFontSize(event.getScene());
                event.consume();
            }
        });
    }

    private void applyCurrentFontSize(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().setStyle("-fx-font-size: " + baseFontSize + "px");
        }
    }

    public void switchTo(String name) {
        Scene scene = screens.get(name);
        if (scene != null) {
            applyCurrentFontSize(scene); // Ensure zoom is applied
            stage.setScene(scene);
            stage.show();
        }
    }

    public void setBaseFontSize(int size) { this.baseFontSize = size; }
    public int getBaseFontSize() { return baseFontSize; }
}
```

### ThemeSelectionView — Responsive Scene

```java
// Source: Codebase analysis — ThemeSelectionView.java line 57
// Before: scene = new Scene(root, 600, 400);
// After:
public ThemeSelectionView() {
    root = new VBox(10);
    root.setPadding(new Insets(20));
    // ... build children as before ...
    scene = new Scene(root); // No hardcoded dimensions
}
```

### ReportsView — Responsive Scene with ScrollPane

```java
// Source: Codebase analysis — ReportsView.java line 53
// Before: scene = new Scene(root, 600, 500);
// After:
public ReportsView() {
    // ... build content as before ...
    ScrollPane scrollPane = new ScrollPane(content);
    scrollPane.setFitToWidth(true);
    // ... existing scroll settings ...
    root = new VBox(scrollPane);
    scene = new Scene(root); // No hardcoded dimensions
}
```

### StudyRoundView — Already Responsive

```java
// Source: Codebase analysis — StudyRoundView.java line 106
// Already uses: scene = new Scene(root); — no changes needed
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Hardcoded `Scene(root, 600, 400)` | `Scene(root)` — dynamic sizing | This phase | Scenes resize with stage |
| No zoom functionality | Ctrl+scroll zoom via event filter | This phase | Font size adjustable across all screens |
| Inline font sizes (18px, 20px, 16px) | Root-level `-fx-font-size` cascading | This phase | Titles may need inline style removal |

**Deprecated/outdated:**
- Hardcoded Scene dimensions: Replace with dynamic sizing for resizable windows
- `setOnScroll` for zoom: Use `addEventFilter` to prevent ScrollPane conflicts

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Setting `-fx-font-size` on root node cascades to all children without explicit font-size | Common Pitfalls #2 | If children inherit differently, per-node updates needed — more code but same result |
| A2 | `Scene(root)` without dimensions uses the stage's current size | Standard Stack | If Scene creates with 0×0, may need `new Scene(root, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE)` instead |
| A3 | Zoom is session-scoped only (not persisted to disk) | Common Pitfalls #5 | User may expect persistence — would need preferences file |
| A4 | No new Maven dependencies needed | Standard Stack | Verified — all APIs are built into JavaFX 25 |
| A5 | Stage.setResizable(true) is default in JavaFX | Summary | If somehow disabled, must explicitly re-enable |

**If this table is empty:** Not applicable — 5 assumptions identified.

## Open Questions (RESOLVED)

1. **Should hardcoded inline font sizes be removed from titles?** (RESOLVED)
   - **Decision:** Remove all hardcoded `-fx-font-size` from titles per plan 08-01 Task 2. Titles will inherit root zoom level. Bold styling retained via `-fx-font-weight: bold`.
   - Rationale: Consistent with requirement ZOOM-02 (zoom applies to all text). Titles not zooming would be a confusing UX gap.

2. **What happens to StudyRoundView option buttons at very large zoom?** (RESOLVED)
   - **Decision:** Accept natural layout overflow; VBox pushes content down. No immediate mitigation — existing ScrollPane in ReportsView already handled via event.consume().
   - Rationale: 30px font size is an extreme case; typical usage at 14-22px range is well within bounds. Can add ScrollPane wrapper if users report issues.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JavaFX 25 | Core framework | ✓ | 25 | — |
| JDK 25 | Runtime | ✓ | 25 | — |
| Maven | Build | ✓ | — | — |

**Missing dependencies with no fallback:** None — all required tools are available.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 (assumed from existing tests) |
| Config file | None detected — see Wave 0 |
| Quick run command | `mvn test` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| ZOOM-01 | Window is resizable | smoke (manual) | Launch app, drag window corner | ❌ Manual only |
| ZOOM-02 | Ctrl+scroll changes font size | unit | `mvn test -pl . -Dtest=ZoomControllerTest` | ❌ Wave 0 |
| ZOOM-03 | Zoom persists across screen switches | integration | `mvn test -pl . -Dtest=ZoomPersistenceTest` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `mvn test`
- **Per wave merge:** `mvn test`
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `src/test/java/org/IsmaelSS/controller/ZoomControllerTest.java` — unit tests for font size tracking, min/max bounds
- [ ] `src/test/java/org/IsmaelSS/controller/ZoomPersistenceTest.java` — integration test for zoom applying to new scenes
- [ ] Manual smoke test: resize window, verify all content visible; Ctrl+scroll on each screen

## Security Domain

Not applicable — this phase involves no authentication, session management, access control, cryptography, or input validation. Pure UI/layout changes.

## Sources

### Primary (HIGH confidence)
- [openjfx.io/javadoc/24 — VBox] - Layout pane responsive behavior, fillWidth, resizable range
- [openjfx.io/javadoc/21 — ScrollEvent] - Event types, isControlDown(), getDeltaY(), capture vs bubbling
- [docs.oracle.com/javafx/2/events/filters.htm] - Event filter registration and capture phase behavior

### Secondary (MEDIUM confidence)
- [CITED: StackOverflow #13238507] - addEventFilter vs setOnScroll for ScrollPane zoom conflict
- [CITED: StackOverflow #51048312] - Consuming ScrollEvent in filter to prevent ScrollPane scrolling
- [CITED: jenkov.com/tutorials/javafx/vbox.html] - VBox fillWidth and vgrow documentation

### Tertiary (LOW confidence)
- None — all findings backed by official JavaFX docs or verified StackOverflow answers

## Metadata

**Confidence breakdown:**
- Standard Stack: HIGH — all APIs are built into JavaFX 25, no external packages needed
- Architecture: HIGH — ScreenController is the natural centralized point; event filter pattern is well-documented
- Pitfalls: HIGH — ScrollPane double-scroll and CSS specificity are well-known JavaFX gotchas with documented solutions

**Research date:** 2026-07-10
**Valid until:** 2026-08-10 (stable — JavaFX APIs are mature)
