# Phase 10: Visual Theming & Color Identity - Research

**Researched:** 2026-07-13
**Domain:** JavaFX CSS theming, external stylesheet management
**Confidence:** HIGH

## Summary

This phase replaces all scattered inline `setStyle()` calls across three views (ThemeSelectionView, ReportsView, StudyRoundView) with a single external CSS stylesheet (`theme.css`) loaded centrally via `ScreenController.registerScreen()`. The mandatory color palette is: dark background `#020817`, orange accent `#fe9a00`, white/black text, with complementary surface, hover, pressed, and border colors.

**Critical technical constraint:** JavaFX CSS precedence rules dictate that **inline styles (`setStyle()`) override external CSS stylesheets**. [CITED: openjfx.io/javadoc/24/javafx.graphics/javafx/scene/doc-files/cssref.html] This means every `setStyle()` call in the codebase MUST be removed (or converted to class-based toggling) for the external theme to take effect. The sole exception is the root font-size set by the zoom feature (Phase 8), which must remain inline because it's dynamically updated per scroll event.

The project already has an empty `src/main/resources/` directory. No external packages need to be installed — this phase is pure CSS + Java code changes. There are 17 `setStyle()` calls across the codebase; 15 must be removed or refactored to CSS classes, 1 must be kept (zoom), and 1 is a no-op empty string.

**Primary recommendation:** Create `src/main/resources/styles/theme.css`, load it once in `ScreenController.registerScreen()`, replace all static inline styles with CSS classes, and convert dynamic inline styles (correct/wrong feedback) to CSS class toggling via `getStyleClass().add/remove()`.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Theme CSS file creation | Frontend (View) | — | CSS is a view-layer artifact |
| CSS loading into scenes | API/Backend (ScreenController) | Frontend (View) | Centralized loading in controller |
| Inline style removal | Frontend (View) | — | Views own their styling |
| Dynamic state styling (correct/wrong) | Frontend (View) | API/Backend (Controller) | View toggles classes, controller triggers state changes |
| Zoom compatibility | API/Backend (ScreenController) | Frontend (View) | Zoom sets root inline font-size; CSS must not conflict |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| JavaFX CSS | 25 | External stylesheet theming | Built-in to JavaFX, no external deps needed |
| Maven resources | — | CSS file packaging | Standard Maven layout: `src/main/resources/` |

No external packages required. This phase is pure JavaFX CSS + code refactoring.

### CSS Loading Pattern
```java
// Source: Official JavaFX CSS Reference + Stack Overflow consensus
// Load once in ScreenController.registerScreen():
String css = getClass().getResource("/styles/theme.css").toExternalForm();
scene.getStylesheets().add(css);
```

**Resource path:** `src/main/resources/styles/theme.css` → accessible as `/styles/theme.css` at runtime. [CITED: openjfx.io/javadoc/11 — "A relative URL is resolved against the base URL of the ClassLoader of the concrete Application class"]

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Single theme.css | Multiple CSS files per screen | Over-engineered — single file is simpler for 3 screens |
| Class-based selectors | ID selectors | Class selectors are reusable; IDs are one-off |
| getStyleClass().add/remove | setStyle() for dynamic states | Class-based is the only way to work with external CSS |

## Package Legitimacy Audit

No external packages are installed in this phase. This section is not applicable.

## Architecture Patterns

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────┐
│                    Main.java                         │
│  Creates ScreenController, loads initial view        │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              ScreenController                        │
│  registerScreen(name, scene)                         │
│    ├── scene.getStylesheets().add(theme.css)  ← NEW │
│    ├── installZoomFilter(scene)                      │
│    └── applyCurrentFontSize(scene)  (inline, KEPT)   │
│                                                      │
│  switchTo(name)                                      │
│    └── applyCurrentFontSize(scene)  (inline, KEPT)   │
└──────────────────────┬──────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
┌──────────────┐ ┌──────────┐ ┌──────────────┐
│ThemeSelection│ │StudyRound│ │  ReportsView  │
│    View      │ │   View   │ │              │
│ remove: 2    │ │ remove:10│ │  remove: 3   │
│ setStyle()   │ │ setStyle │ │  setStyle()  │
│ calls        │ │ + class  │ │  calls       │
│              │ │ toggle   │ │              │
└──────────────┘ └──────────┘ └──────────────┘

                    CSS File
              ┌──────────────────┐
              │  theme.css       │
              │  .background     │
              │  .surface        │
              │  .button-primary │
              │  .title          │
              │  .label          │
              │  .accent         │
              │  .correct        │
              │  .wrong          │
              │  :hover, :pressed│
              └──────────────────┘
```

### Recommended Project Structure
```
src/main/resources/
└── styles/
    └── theme.css          # Single centralized stylesheet
```

### Pattern 1: Centralized CSS Loading
**What:** Load the theme CSS once per scene in ScreenController.registerScreen()
**When to use:** When all scenes share the same visual theme
**Example:**
```java
// Source: Official JavaFX docs + project convention
public void registerScreen(String name, Scene scene) {
    screens.put(name, scene);
    loadTheme(scene);        // NEW: load CSS
    installZoomFilter(scene);
    applyCurrentFontSize(scene);
}

private void loadTheme(Scene scene) {
    String css = getClass().getResource("/styles/theme.css").toExternalForm();
    scene.getStylesheets().add(css);
}
```

### Pattern 2: CSS Class Toggling for Dynamic States
**What:** Replace setStyle() calls for correct/wrong feedback with CSS class add/remove
**When to use:** When visual state changes based on user interaction (answer correct/wrong)
**Example:**
```java
// Source: JavaFX CSS Reference — style class manipulation
// BEFORE (inline):
optionButtons[correctIndex].setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

// AFTER (class-based):
optionButtons[correctIndex].getStyleClass().removeAll("option-default");
optionButtons[correctIndex].getStyleClass().add("option-correct");
```

**CSS:**
```css
.option-default {
    -fx-background-color: #e0e0e0;
    -fx-text-fill: black;
    -fx-cursor: hand;
    -fx-padding: 8;
}
.option-correct {
    -fx-background-color: #27ae60;
    -fx-text-fill: white;
    -fx-cursor: hand;
    -fx-padding: 8;
}
.option-wrong {
    -fx-background-color: #e74c3c;
    -fx-text-fill: white;
    -fx-cursor: hand;
    -fx-padding: 8;
}
```

### Pattern 3: Zoom + CSS Coexistence
**What:** Root font-size stays as inline (zoom), CSS handles everything else
**When to use:** When dynamic zoom must coexist with static theme
**Example:**
```java
// ScreenController keeps this inline (zoom feature):
scene.getRoot().setStyle("-fx-font-size: " + baseFontSize + "px");

// CSS handles all other properties — no -fx-font-size rules for child nodes
// Children inherit font-size from root automatically
```

### Anti-Patterns to Avoid
- **Keeping setStyle() alongside external CSS:** Inline styles ALWAYS override external CSS in JavaFX [CITED: JavaFX CSS Reference — "Inline styles have highest precedence"]. If a setStyle() call sets `-fx-background-color`, the CSS rule for the same property on that node is ignored.
- **Using CSS variables for zoom:** JavaFX CSS does not support CSS custom properties (`var(--name)`). Use inline setStyle() for dynamic values.
- **Loading CSS in every view constructor:** Centralize in ScreenController to avoid duplicate loading and ensure consistency.
- **Setting -fx-font-size in CSS for children:** Would conflict with zoom inheritance. Let children inherit from root.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Color consistency | Hardcode colors in each view | CSS theme variables/classes | Single source of truth |
| Hover effects | PauseTransition + setFill | CSS `:hover` pseudo-class | Native, performant, no Java code |
| Button styling | setStyle() per button | CSS `.button-primary` class | Reusable, maintainable |
| Background theming | setStyle() on root | CSS `.background` class | Consistent across all screens |

**Key insight:** JavaFX CSS is a full styling system — use it. The only thing that MUST stay inline is the zoom font-size because it changes dynamically per scroll event.

## Common Pitfalls

### Pitfall 1: Inline Styles Override External CSS
**What goes wrong:** After adding theme.css, some elements still show old colors
**Why it happens:** `setStyle()` calls in view constructors set properties that CSS cannot override
**How to avoid:** Remove ALL `setStyle()` calls except the zoom root font-size. Grep for `setStyle` and verify each one is removed or converted to class-based.
**Warning signs:** Button still blue (#2196F3) after CSS defines orange; background still white after CSS defines dark

### Pitfall 2: CSS File Not Found (NullPointerException)
**What goes wrong:** `getClass().getResource("/styles/theme.css")` returns null
**Why it happens:** CSS file not in `src/main/resources/styles/` or wrong path in getResource()
**How to avoid:** Place CSS at `src/main/resources/styles/theme.css`. Use absolute path `/styles/theme.css` (leading slash = from classpath root). Verify file exists before loading.
**Warning signs:** NPE on startup, blank screen

### Pitfall 3: Zoom Broken After CSS Migration
**What goes wrong:** Ctrl+scroll no longer changes font size
**Why it happens:** CSS `-fx-font-size` rule on root conflicts with inline zoom
**How to avoid:** Do NOT add `-fx-font-size` rules to theme.css for root or any node. Zoom uses inline `root.setStyle("-fx-font-size: Xpx")` which has higher precedence than CSS — this is intentional.
**Warning signs:** Font size static regardless of scroll

### Pitfall 4: Dynamic State Styles Lost
**What goes wrong:** After answering a question, correct/wrong colors don't appear
**Why it happens:** Removed setStyle() for highlightCorrect/highlightWrong but didn't add CSS class toggling
**How to avoid:** Convert StudyRoundView's highlightCorrect(), highlightWrong(), and setQuestion() from setStyle() to getStyleClass().add/remove() with corresponding CSS classes
**Warning signs:** All buttons stay same color after answering

### Pitfall 5: Accordion/TitledPane Styling Incomplete
**What goes wrong:** ReportsView Accordion looks unstyled on dark background
**Why it happens:** Accordion, TitledPane, and ScrollPane have their own internal structure; CSS must target sub-nodes
**How to avoid:** Add CSS rules for `.titled-pane`, `.titled-pane .title`, `.titled-pane .content`, `.scroll-pane`, `.accordion` to ensure consistent dark theme on complex controls
**Warning signs:** White/gray sections inside Accordion on dark background

## Code Examples

### Inline Styles Inventory (complete list)

**ThemeSelectionView.java (2 calls to remove):**
```java
// Line 38 — REMOVE, replace with: title.getStyleClass().add("title");
title.setStyle("-fx-font-weight: bold;");

// Line 53 — REMOVE, replace with: feedbackLabel.getStyleClass().add("error-text");
feedbackLabel.setStyle("-fx-text-fill: red;");
```

**ReportsView.java (3 calls to remove):**
```java
// Line 25 — REMOVE, replace with: title.getStyleClass().add("title");
title.setStyle("-fx-font-weight: bold;");

// Line 29 — REMOVE, replace with: section1.getStyleClass().add("section-title");
section1.setStyle("-fx-font-weight: bold;");

// Line 37 — REMOVE, replace with: section2.getStyleClass().add("section-title");
section2.setStyle("-fx-font-weight: bold;");
```

**StudyRoundView.java (10 calls to remove/refactor):**
```java
// Line 36 — REMOVE (empty string, no-op)
progressLabel.setStyle("");

// Line 43 — REMOVE, replace with: questionLabel.getStyleClass().add("question-text");
questionLabel.setStyle("-fx-font-weight: bold;");

// Line 53 — REMOVE, replace with: btn.getStyleClass().add("option-default");
btn.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: black; -fx-cursor: hand; -fx-padding: 8;");

// Line 66 — REMOVE, replace with: exitButton.getStyleClass().add("button-secondary");
exitButton.setStyle("-fx-padding: 8 20 8 20;");

// Line 74 — REMOVE, replace with: proximaButton.getStyleClass().add("button-primary");
proximaButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 8 20 8 20; -fx-cursor: hand;");

// Line 93 — REMOVE, replace with: completionLabel.getStyleClass().add("title");
completionLabel.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");

// Line 96 — REMOVE, replace with: voltarButton.getStyleClass().add("button-secondary");
voltarButton.setStyle("-fx-padding: 8 20 8 20;");

// Line 119 — REMOVE, replace with: optionButtons[i].getStyleClass().setAll("option-default");
optionButtons[i].setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: black; -fx-cursor: hand; -fx-padding: 8;");

// Lines 128, 132-133 — CONVERT to class toggling (see Pattern 2 above)
optionButtons[correctIndex].setStyle("-fx-background-color: #4CAF50; ...");
optionButtons[wrongIndex].setStyle("-fx-background-color: #F44336; ...");
```

**ScreenController.java (1 call to KEEP):**
```java
// Line 55 — KEEP (zoom feature, dynamic inline value)
scene.getRoot().setStyle("-fx-font-size: " + baseFontSize + "px");
```

### Theme CSS Structure
```css
/* Source: Phase 10 CONTEXT.md color palette + JavaFX CSS Reference */

/* === Global === */
.root {
    -fx-background-color: #020817;
    -fx-font-family: "System";
}

/* === Layout === */
.background {
    -fx-background-color: #020817;
}
.surface {
    -fx-background-color: #1a1a2e;
}

/* === Typography === */
.title {
    -fx-font-weight: bold;
    -fx-text-fill: #ffffff;
    -fx-font-size: 1.4em;
}
.section-title {
    -fx-font-weight: bold;
    -fx-text-fill: #ffffff;
}
.label {
    -fx-text-fill: #e0e0e0;
}
.error-text {
    -fx-text-fill: #e74c3c;
}

/* === Buttons === */
.button-primary {
    -fx-background-color: #fe9a00;
    -fx-text-fill: #ffffff;
    -fx-cursor: hand;
    -fx-padding: 8 20 8 20;
    -fx-background-radius: 4;
}
.button-primary:hover {
    -fx-background-color: #ffb340;
}
.button-primary:pressed {
    -fx-background-color: #cc7c00;
}
.button-secondary {
    -fx-background-color: transparent;
    -fx-text-fill: #e0e0e0;
    -fx-border-color: #333355;
    -fx-border-radius: 4;
    -fx-padding: 8 20 8 20;
    -fx-cursor: hand;
}
.button-secondary:hover {
    -fx-background-color: #1a1a2e;
}

/* === Option Buttons (Study Round) === */
.option-default {
    -fx-background-color: #e0e0e0;
    -fx-text-fill: #000000;
    -fx-cursor: hand;
    -fx-padding: 8;
    -fx-background-radius: 4;
}
.option-correct {
    -fx-background-color: #27ae60;
    -fx-text-fill: #ffffff;
    -fx-cursor: hand;
    -fx-padding: 8;
    -fx-background-radius: 4;
}
.option-wrong {
    -fx-background-color: #e74c3c;
    -fx-text-fill: #ffffff;
    -fx-cursor: hand;
    -fx-padding: 8;
    -fx-background-radius: 4;
}

/* === Forms === */
.spinner {
    -fx-background-color: #1a1a2e;
}
.check-box {
    -fx-text-fill: #e0e0e0;
}

/* === Reports / Accordion === */
.accordion {
    -fx-background-color: transparent;
}
.titled-pane > .title {
    -fx-background-color: #1a1a2e;
    -fx-text-fill: #ffffff;
}
.titled-pane .content {
    -fx-background-color: #020817;
    -fx-border-color: #333355;
}

/* === Separator === */
.separator .line {
    -fx-border-color: #333355;
}

/* === ScrollPane === */
.scroll-pane {
    -fx-background-color: #020817;
}
.scroll-bar {
    -fx-background-color: #1a1a2e;
}
.scroll-bar .thumb {
    -fx-background-color: #333355;
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Inline setStyle() per element | External CSS stylesheet | This phase (10) | Centralized, maintainable theming |
| Hardcoded colors in Java code | CSS color variables/classes | This phase (10) | Single source of truth for colors |
| No hover/pressed effects | CSS pseudo-classes | This phase (10) | Better UX feedback |
| Blue "Proxima" button (#2196F3) | Orange accent (#fe9a00) | This phase (10) | Consistent brand identity |

**Deprecated/outdated:**
- All `setStyle()` calls in views: replaced by CSS class assignments
- Ad-hoc colors (#E0E0E0, #4CAF50, #F44336, #2196F3): replaced by theme palette

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | JavaFX CSS does not support CSS custom properties (var(--name)) | Zoom + CSS Coexistence | If it does, could use variables for dynamic values instead of inline |
| A2 | The `src/main/resources/` directory already exists and Maven picks it up | Resource Loading | If not, CSS file won't be in classpath — but we verified it exists |
| A3 | No module-info.java exists (non-modular project) | Resource Loading | If modular, would need `opens` directive for resources — but pom.xml shows no module config |

**If this table is empty:** Not applicable — 3 assumptions listed above.

## Open Questions

1. **How to handle Accordion/TitledPane internal styling?**
   - What we know: Accordion has sub-nodes (.titled-pane, .title, .content) that need explicit CSS rules
   - What's unclear: Exact sub-node structure may vary by JavaFX version
   - Recommendation: Use broad selectors and test visually; add `.accordion` and `.titled-pane` rules

2. **Should root node get a CSS class for background?**
   - What we know: Each view's root VBox needs dark background
   - What's unclear: Whether to add `.background` class to each root or style `.root` directly in CSS
   - Recommendation: Style `.root` in CSS for global background, add `.background` class only where needed for specificity

3. **How to handle the CheckBox and Spinner styling on dark background?**
   - What we know: JavaFX CheckBox and Spinner have default light styling from modena.css
   - What's unclear: Exact CSS selectors needed to restyle these controls on dark background
   - Recommendation: Target `.check-box` and `.spinner` with dark theme colors; test thoroughly

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit Jupiter 5.11.0 |
| Config file | none — standard Maven layout |
| Quick run command | `mvn test -q` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| VIZ-01 | External CSS loaded in all scenes | integration | `mvn test -q` (app launches without NPE) | ❌ Wave 0 |
| VIZ-02 | Dark background applied | visual | Manual — launch app, verify dark bg | ❌ Manual |
| VIZ-03 | Orange accent on buttons | visual | Manual — launch app, verify orange buttons | ❌ Manual |
| VIZ-04 | White/black text contrast | visual | Manual — launch app, verify text readability | ❌ Manual |
| VIZ-05 | Consistent padding/radius/hover | visual | Manual — launch app, verify consistency | ❌ Manual |

### Sampling Rate
- **Per task commit:** `mvn test -q`
- **Per wave merge:** `mvn test`
- **Phase gate:** Full suite green + manual visual verification

### Wave 0 Gaps
- [ ] No automated visual tests exist — all VIZ requirements are visual/manual
- [ ] Consider adding a smoke test that verifies CSS file loads without NPE
- [ ] Framework install: none needed (JUnit already configured)

## Security Domain

Not applicable — this phase involves CSS styling only. No authentication, session management, input validation, cryptography, or data handling changes.

## Sources

### Primary (HIGH confidence)
- [JavaFX CSS Reference Guide](https://openjfx.io/javadoc/24/javafx.graphics/javafx/scene/doc-files/cssref.html) — CSS precedence rules, stylesheet loading, naming conventions
- [JavaFX CSS Reference (JDK 10)](https://cr.openjdk.org/~ksrini/8193671/docs/api/javafx/scene/doc-files/cssref.html) — Inline vs external precedence confirmed

### Secondary (MEDIUM confidence)
- [Stack Overflow: JavaFX CSS loading patterns](https://stackoverflow.com/questions/61531317/how-do-i-determine-the correct-path-for-fxml-files-css-files-images-and-other) — Maven resource path conventions
- [w3resource: JavaFX CSS styling exercises](https://www.w3resource.com/java-exercises/javafx/javafx-styling-and-css-exercise-5.php) — scene.getStylesheets().add() pattern

### Tertiary (LOW confidence)
- [MoldStud: JavaFX CSS best practices](https://moldstud.com/articles/p-exploring-javafx-css-styling-practical-examples-and-tips-for-developers) — General theming advice (unverified survey claims)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — No external packages; pure JavaFX CSS (built-in)
- Architecture: HIGH — Pattern well-documented in official JavaFX docs
- Pitfalls: HIGH — Inline override behavior confirmed by multiple official sources

**Research date:** 2026-07-13
**Valid until:** 2026-08-13 (stable — JavaFX CSS API is mature and unlikely to change)
