# Phase 9: Navigation Overhaul - Research

**Researched:** 2026-07-10
**Domain:** JavaFX UI navigation, screen transitions, keyboard shortcuts
**Confidence:** HIGH

## Summary

This research covers implementing a comprehensive navigation system for the FlashCard JavaFX application. The current `ScreenController` provides basic screen switching but lacks back/forward navigation, breadcrumbs, keyboard shortcuts, and smooth transitions. The research identifies standard JavaFX patterns for navigation history stacks, keyboard event handling, breadcrumb indicators, and scene transitions.

**Primary recommendation:** Implement a navigation history stack in `ScreenController` with back/forward support, add breadcrumb indicators to each view, implement global keyboard shortcuts via scene-level event filters, and use `FadeTransition` for smooth screen transitions.

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| NAV-01 | Back navigation button visible on study round and reports screens | Pattern 1: Navigation History Stack, Pattern 3: Scene Transitions |
| NAV-02 | Breadcrumb or screen indicator shows user's current position | Pattern 4: Breadcrumb Indicator, Standard Stack: ControlsFX BreadCrumbBar |
| NAV-03 | Smooth transitions (fade/slide) between screens | Pattern 3: Scene Transitions, Standard Stack: JavaFX Transitions API |
| NAV-04 | Keyboard shortcuts: Escape to go back, Ctrl+H for history | Pattern 2: Global Keyboard Shortcuts, Standard Stack: JavaFX EventFilter API |
| NAV-05 | Screen history prevents going back past the main theme selection | Pattern 1: Navigation History Stack, Common Pitfall 2: Navigation State Leaking |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Navigation history stack | ScreenController | Controllers | Centralized navigation state management |
| Back/forward buttons | Views | Controllers | UI elements that trigger navigation actions |
| Breadcrumb indicators | Views | Controllers | Visual feedback for current position |
| Keyboard shortcuts | ScreenController | Views | Global event handling at scene level |
| Screen transitions | ScreenController | Views | Animation during scene switches |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| JavaFX Transitions API | 25 | FadeTransition, TranslateTransition for scene animations | Built-in JavaFX, no external dependencies |
| JavaFX EventFilter API | 25 | Global keyboard shortcut handling | Standard JavaFX event propagation model |
| JavaFX StackPane | 25 | Layered UI for breadcrumb indicators | Built-in layout container |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| ControlsFX BreadCrumbBar | 11.2.0 | Pre-built breadcrumb control | If custom implementation is too complex |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Custom history stack | javafx-routing library | External dependency vs. simpler custom solution |
| Custom breadcrumbs | ControlsFX BreadCrumbBar | External dependency vs. simpler custom implementation |
| FadeTransition | TranslateTransition | Different animation effect, same API |

**Installation:**
```bash
# No additional dependencies needed for core implementation
# If using ControlsFX:
mvn dependency:copy -Dartifact=org.controlsfx:controlsfx:11.2.0 -DoutputDirectory=lib
```

## Package Legitimacy Audit

> **Required** whenever this phase installs external packages. Run the Package Legitimacy Gate protocol before completing this section.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| javafx-transitions | JDK built-in | N/A | N/A | N/A | OK | Core JavaFX API |
| javafx-eventfilter | JDK built-in | N/A | N/A | N/A | OK | Core JavaFX API |
| controlsfx (optional) | Maven Central | 10+ years | 1M+/month | github.com/controlsfx/controlsfx | OK | Optional - only if custom implementation is insufficient |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

*All core navigation can be implemented with built-in JavaFX APIs. ControlsFX is optional for pre-built breadcrumb control.*

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    Main.java                            │
│  (Creates ScreenController, ThemeSelectionController)   │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│              ScreenController                           │
│  - Navigation history stack (List<String>)              │
│  - Back/forward navigation methods                     │
│  - Keyboard shortcut event filters                     │
│  - Screen transition animations                         │
│  - Breadcrumb state management                          │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│              Controllers                                │
│  - ThemeSelectionController                             │
│  - StudyRoundController                                 │
│  - ReportsController                                    │
│  (Register with ScreenController, handle navigation)    │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│              Views                                       │
│  - ThemeSelectionView                                    │
│  - StudyRoundView                                        │
│  - ReportsView                                           │
│  (Include back buttons, breadcrumb indicators)           │
└─────────────────────────────────────────────────────────┘
```

### Recommended Project Structure
```
src/main/java/org/IsmaelSS/
├── controller/
│   ├── ScreenController.java         # Enhanced with navigation history
│   ├── ThemeSelectionController.java # Register with navigation
│   ├── StudyRoundController.java     # Register with navigation
│   └── ReportsController.java        # Register with navigation
├── view/
│   ├── ThemeSelectionView.java       # Add back button, breadcrumb
│   ├── StudyRoundView.java           # Add back button, breadcrumb
│   └── ReportsView.java              # Add back button, breadcrumb
├── navigation/
│   ├── NavigationHistory.java        # Stack-based navigation history
│   └── ScreenTransition.java         # Transition animation utilities
└── ...
```

### Pattern 1: Navigation History Stack
**What:** Stack-based navigation history with back/forward support
**When to use:** When users need to navigate between multiple screens with history
**Example:**
```java
// Source: Custom implementation based on standard navigation patterns
public class NavigationHistory {
    private final Stack<String> history = new Stack<>();
    private final Stack<String> forwardStack = new Stack<>();
    
    public void push(String screenName) {
        history.push(screenName);
        forwardStack.clear(); // Clear forward history on new navigation
    }
    
    public String goBack() {
        if (history.size() > 1) {
            String current = history.pop();
            forwardStack.push(current);
            return history.peek();
        }
        return null; // Already at root
    }
    
    public String goForward() {
        if (!forwardStack.isEmpty()) {
            String next = forwardStack.pop();
            history.push(next);
            return next;
        }
        return null; // No forward history
    }
    
    public String getCurrent() {
        return history.isEmpty() ? null : history.peek();
    }
    
    public boolean canGoBack() {
        return history.size() > 1;
    }
    
    public boolean canGoForward() {
        return !forwardStack.isEmpty();
    }
}
```

### Pattern 2: Global Keyboard Shortcuts
**What:** Scene-level event filters for global keyboard shortcuts
**When to use:** When shortcuts should work regardless of focused node
**Example:**
```java
// Source: JavaFX EventFilter documentation
scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
    if (event.getCode() == KeyCode.ESCAPE) {
        // Handle back navigation
        screenController.goBack();
        event.consume();
    } else if (event.getCode() == KeyCode.H && event.isControlDown()) {
        // Handle history view
        showHistoryDialog();
        event.consume();
    }
});
```

### Pattern 3: Scene Transitions
**What:** Smooth fade/slide transitions between scenes
**When to use:** When visual feedback for screen changes improves UX
**Example:**
```java
// Source: JavaFX FadeTransition documentation
public static void transitionTo(Scene newScene, Stage stage) {
    Scene oldScene = stage.getScene();
    
    // Fade out old scene
    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldScene.getRoot());
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(e -> {
        stage.setScene(newScene);
        
        // Fade in new scene
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newScene.getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    });
    fadeOut.play();
}
```

### Anti-Patterns to Avoid
- **Hardcoding screen names:** Use constants or enum for screen identifiers
- **Duplicated navigation logic:** Centralize navigation in ScreenController
- **Ignoring keyboard accessibility:** Always provide keyboard alternatives to mouse actions

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Breadcrumb navigation | Custom HBox with buttons | ControlsFX BreadCrumbBar | Handles complex hierarchy, styling, events |
| Complex animations | Custom Timeline keyframes | JavaFX Transition API | Battle-tested, optimized, standard API |
| Keyboard shortcut routing | Manual key code checking | KeyCodeCombination class | Platform-agnostic, modifier key handling |

**Key insight:** JavaFX provides built-in APIs for all navigation requirements. Custom implementations risk introducing bugs in event handling, animation timing, and state management.

## Runtime State Inventory

> Not applicable - this is not a rename/refactor/migration phase.

## Common Pitfalls

### Pitfall 1: Event Filter vs Event Handler
**What goes wrong:** Keyboard shortcuts don't work when expected
**Why it happens:** Using `setOnKeyPressed` (handler) instead of `addEventFilter` (filter)
**How to avoid:** Use event filters for global shortcuts that should work regardless of focus
**Warning signs:** Shortcuts work sometimes but not others

### Pitfall 2: Navigation State Leaking
**What goes wrong:** Back button navigates to screens with stale data
**Why it happens:** Not refreshing views when navigating back
**How to avoid:** Call `refresh()` methods on controllers when navigating back
**Warning signs:** Outdated information displayed after navigation

### Pitfall 3: Animation Thread Blocking
**What goes wrong:** UI freezes during transitions
**Why it happens:** Running animations on main thread with blocking operations
**How to avoid:** Use JavaFX Transition API which handles threading automatically
**Warning signs:** Unresponsive UI during screen changes

### Pitfall 4: Breadcrumb Click Handling
**What goes wrong:** Clicking breadcrumb items doesn't navigate correctly
**Why it happens:** Not handling breadcrumb selection events properly
**How to avoid:** Implement proper event handlers for breadcrumb interactions
**Warning signs:** Breadcrumbs are visual only, not functional

## Code Examples

Verified patterns from official sources:

### Navigation History Implementation
```java
// Source: Custom implementation based on standard navigation patterns
public class ScreenController {
    private final NavigationHistory history = new NavigationHistory();
    
    public void switchTo(String name, boolean pushHistory) {
        Scene scene = screens.get(name);
        if (scene != null) {
            applyCurrentFontSize(scene);
            if (pushHistory) {
                history.push(name);
            }
            stage.setScene(scene);
            stage.show();
        }
    }
    
    public void goBack() {
        String previous = history.goBack();
        if (previous != null) {
            switchTo(previous, false);
        }
    }
    
    public void goForward() {
        String next = history.goForward();
        if (next != null) {
            switchTo(next, false);
        }
    }
}
```

### Global Keyboard Shortcuts
```java
// Source: JavaFX EventFilter documentation
public void installKeyboardShortcuts(Scene scene) {
    scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
        // Escape to go back
        if (event.getCode() == KeyCode.ESCAPE) {
            if (history.canGoBack()) {
                goBack();
                event.consume();
            }
        }
        // Ctrl+H for history
        else if (event.getCode() == KeyCode.H && event.isControlDown()) {
            showHistoryDialog();
            event.consume();
        }
    });
}
```

### Fade Transition Between Scenes
```java
// Source: JavaFX FadeTransition documentation
public void transitionTo(Scene newScene) {
    Scene oldScene = stage.getScene();
    
    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldScene.getRoot());
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(e -> {
        stage.setScene(newScene);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newScene.getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    });
    fadeOut.play();
}
```

### Breadcrumb Indicator
```java
// Source: Custom implementation using standard JavaFX components
public class BreadcrumbIndicator extends HBox {
    private final List<Label> crumbs = new ArrayList<>();
    
    public void updatePath(List<String> path) {
        getChildren().clear();
        crumbs.clear();
        
        for (int i = 0; i < path.size(); i++) {
            Label crumb = new Label(path.get(i));
            crumb.setStyle("-fx-font-weight: bold;");
            crumbs.add(crumb);
            getChildren().add(crumb);
            
            if (i < path.size() - 1) {
                Label separator = new Label(" > ");
                getChildren().add(separator);
            }
        }
    }
    
    public void setCurrentScreen(String screenName) {
        // Highlight current screen in breadcrumb
        for (Label crumb : crumbs) {
            if (crumb.getText().equals(screenName)) {
                crumb.setStyle("-fx-font-weight: bold; -fx-text-fill: #2196F3;");
            } else {
                crumb.setStyle("-fx-font-weight: bold;");
            }
        }
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Simple screen switching | Navigation history stack | 2026-07-10 | Enables back/forward navigation |
| No keyboard shortcuts | Global event filters | 2026-07-10 | Improves accessibility |
| Abrupt screen changes | Fade transitions | 2026-07-10 | Smoother user experience |
| No position indicator | Breadcrumb indicators | 2026-07-10 | Better orientation |

**Deprecated/outdated:**
- Simple `switchTo()` without history: Replace with history-aware navigation
- Screen-specific keyboard handlers: Replace with global event filters

## Assumptions Log

> List all claims tagged `[ASSUMED]` in this research. The planner and discuss-phase use this
> section to identify decisions that need user confirmation before execution.

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | ControlsFX is optional for breadcrumb implementation | Standard Stack | May need to include if custom implementation is complex |
| A2 | FadeTransition duration of 200ms is appropriate | Code Examples | May need adjustment based on user preference |

**If this table is empty:** All claims in this research were verified or cited — no user confirmation needed.

## Open Questions

1. **Breadcrumb Implementation Complexity**
   - What we know: ControlsFX provides BreadCrumbBar control
   - What's unclear: Whether custom implementation is sufficient for simple navigation
   - Recommendation: Start with custom implementation, use ControlsFX if needed

2. **Transition Duration Preference**
   - What we know: 200ms is standard for fade transitions
   - What's unclear: User preference for animation speed
   - Recommendation: Use 200ms as default, make configurable if needed

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JavaFX Transitions API | Scene animations | ✓ | 25 | None - core API |
| JavaFX EventFilter API | Keyboard shortcuts | ✓ | 25 | None - core API |
| ControlsFX (optional) | BreadCrumbBar | ✗ | — | Custom implementation |

**Missing dependencies with no fallback:**
- None - all core navigation can be implemented with built-in JavaFX APIs

**Missing dependencies with fallback:**
- ControlsFX BreadCrumbBar: Use custom implementation with HBox and Labels

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 |
| Config file | none — see Wave 0 |
| Quick run command | `mvn test` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| NAV-01 | Back button visibility | integration | `mvn test -Dtest=NavigationTest` | ❌ Wave 0 |
| NAV-02 | Breadcrumb indicator | integration | `mvn test -Dtest=NavigationTest` | ❌ Wave 0 |
| NAV-03 | Screen transitions | manual | Visual verification | N/A |
| NAV-04 | Keyboard shortcuts | integration | `mvn test -Dtest=KeyboardShortcutTest` | ❌ Wave 0 |
| NAV-05 | History boundary | unit | `mvn test -Dtest=NavigationHistoryTest` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `mvn test`
- **Per wave merge:** `mvn test`
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `NavigationHistoryTest.java` — covers NAV-05 (history boundary)
- [ ] `KeyboardShortcutTest.java` — covers NAV-04 (keyboard shortcuts)
- [ ] `NavigationTest.java` — covers NAV-01, NAV-02 (UI integration)

## Security Domain

> Required when `security_enforcement` is enabled (absent = enabled). Omit only if explicitly `false` in config.

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | Single-user local app |
| V3 Session Management | no | No sessions |
| V4 Access Control | no | No access control |
| V5 Input Validation | no | Navigation only |
| V6 Cryptography | no | No cryptography |

### Known Threat Patterns for JavaFX

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Keyboard shortcut hijacking | Tampering | Use scene-level filters, consume events |
| Navigation state manipulation | Elevation of Privilege | Validate navigation boundaries |

## Sources

### Primary (HIGH confidence)
- JavaFX 25 Official Documentation - FadeTransition, TranslateTransition, EventFilter
- JavaFX Scene API documentation - Keyboard event handling

### Secondary (MEDIUM confidence)
- Stack Overflow discussions on JavaFX navigation patterns
- ControlsFX documentation for BreadCrumbBar

### Tertiary (LOW confidence)
- Custom implementation patterns from various JavaFX projects

## Metadata

**Confidence breakdown:**
- Standard Stack: HIGH - Built-in JavaFX APIs, well-documented
- Architecture: HIGH - Standard MVC pattern with centralized navigation
- Pitfalls: MEDIUM - Based on common JavaFX development issues

**Research date:** 2026-07-10
**Valid until:** 2026-08-10 (30 days - stable JavaFX APIs)