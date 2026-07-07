# Phase 5: Error Handling & UX Refinements - Research

**Researched:** 2026-07-07
**Domain:** Error handling, user-facing diagnostics, UI consistency, responsive layout
**Confidence:** HIGH

## Summary

This phase adds graceful error handling for edge cases (malformed JSON, empty themes, file I/O failures) and applies consistent styling + responsive layout across all screens. The codebase already has basic error resilience (try-catch blocks in ThemeLoader, StatsService) but surfaces all errors only via `java.util.logging.Logger` — invisible to the user. No CSS stylesheet exists; all styling is inline and inconsistent between screens. Window sizing is fixed with no responsive behavior.

**Primary recommendation:** Add a centralized error-dialog utility using JavaFX's built-in `Alert` API, extract all inline styles into an external `application.css` stylesheet loaded by every scene, wrap all I/O boundaries in try-catch blocks that show user-facing error alerts, and apply `VBox.setVgrow` / `AnchorPane` constraints for responsive resizing.

**Key changes needed:**
1. Create `ErrorUtil` helper class for consistent Alert dialogs (ERROR, WARNING, INFORMATION)
2. Create `application.css` with root-level variables and per-screen style classes
3. Add try-catch around `themeLoader.loadAllThemes()` in `ThemeSelectionController.initialize()`
4. Wrap `statsService.recordRound()` calls in StudyRoundController with error handling
5. Add malformed-JSON detection in ThemeLoader with user-visible warning
6. Apply `VBox.setVgrow(content, Priority.ALWAYS)` and `AnchorPane` constraints for window resizing
7. Ensure empty themes folder shows Portuguese message in a styled container

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| (none) | Malformed JSON → graceful error + skip | Jackson throws `JsonParseException` ← catch per-file in ThemeLoader, show `Alert(ERROR)` summary with file name, continue loading remaining files |
| (none) | Empty themes folder → appropriate message | Already handled in `ThemeSelectionView.setThemes()` with text label; needs styling consistency pass |
| (none) | File I/O errors → no crashes | Wrap all I/O calls in try-catch with user-facing `Alert(ERROR)`; `StatsService.save()` and `ThemeLoader.loadAllThemes()` are the primary risk points |
| (none) | Consistent styling + responsive layout | Apply single `application.css` to all 3 scenes via `scene.getStylesheets().add()`; use `VBox.setVgrow` + `AnchorPane` for resize behavior; define root-level `-fx-*` properties for color/font consistency |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Error dialog display | View (helper) | — | `Alert` is a UI component; encapsulated in a static utility class `ErrorUtil` |
| Malformed JSON detection | Service (ThemeLoader) | — | Jackson parsing happens in `ThemeLoader.loadTheme()`; the catch + user notification is the new behavior |
| File I/O error recovery | Service (ThemeLoader, StatsService) | Controller | Services catch I/O exceptions and recover gracefully; controllers display results |
| Consistent CSS styling | View (all) | — | Each View class loads the shared stylesheet; no controller involvement |
| Responsive window layout | View (all) | — | Layout constraints applied in View constructors via `VBox.setVgrow()` etc. |
| Empty state messaging | View (ThemeSelectionView) | Controller | View already shows "Nenhum tema encontrado" when `themes` list is empty |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| JavaFX | 25 (bundled) | Alert dialogs, CSS stylesheets, layout | Already the UI framework; `Alert` is built into `javafx.controls` |
| Jackson | 2.17.2 (pom.xml) | JSON parsing with exception granularity | Already the JSON library; `JsonParseException` vs `JsonMappingException` provide specificity |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `java.util.logging.Logger` | (JDK) | Backend error logging | Already used; continue for log files while adding Alert for users |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| JavaFX `Alert` API | ControlsFX `Dialogs` | ControlsFX adds a dependency; `Alert` is standard since JavaFX 8u40, sufficient for this app |
| External `.css` file | Inline styles (current) | Inline styles duplicate across views, harder to maintain; external CSS enables single-source-of-truth styling |

**Installation:**
```bash
# No new dependencies — uses JavaFX Alert (built-in) and Jackson (already in pom.xml)
```

**Version verification:** No new packages to verify. JavaFX `Alert` class is confirmed present in `javafx.controls` module since JavaFX 8u40 [VERIFIED: openjfx.io javadoc 25]. Jackson 2.17.2 is already in pom.xml and the project compiles.

## Package Legitimacy Audit

No external packages are being added in this phase. All work uses:
- JavaFX 25 built-in `Alert` / `Dialog` classes — part of `javafx.controls` module already declared in pom.xml
- Jackson 2.17.2 — already declared in pom.xml
- `java.util.logging.Logger` — JDK standard

No package legitimacy checks needed.

## Architecture Patterns

### System Architecture Diagram

```
User Action (click, resize)          Error Condition (bad JSON, I/O fail)
        |                                      |
        v                                      v
  Controller (processes              Service (ThemeLoader / StatsService)
  user intent)                            throws / catches exception
        |                                      |
        v                                      v
  View (displays data)              ErrorUtil.showError(Alert)
        |                                      |
        v                                      v
  application.css                   Modal Alert dialog visible to user
  (consistent styling)              (service recovers gracefully)
        |
        v
  Responsive layout
  (VBox.setVgrow, AnchorPane)
```

**Data flow for error scenario:**
1. `ThemeLoader.loadTheme(file)` → Jackson `ObjectMapper.readValue()` throws `JsonParseException`
2. `ThemeLoader.loadAllThemes()` catches exception, logs via `Logger`
3. **NEW:** Controller calls `ErrorUtil.showWarning(themeName + " skipped: malformed JSON")`
4. Controller continues loading remaining files
5. Load completes with partial results; empty state shown if all files failed

### Recommended Project Structure
```
src/main/java/org/IsmaelSS/
├── Main.java                    # Add global exception handler
├── controller/
│   ├── ScreenController.java    # No changes needed
│   ├── ThemeSelectionController.java  # Add try-catch, error alerts
│   ├── StudyRoundController.java      # Add try-catch for recordRound
│   └── ReportsController.java         # No changes needed
├── view/
│   ├── ThemeSelectionView.java  # Add CSS loading; apply style classes
│   ├── StudyRoundView.java      # Add CSS loading; responsive layout
│   └── ReportsView.java         # Add CSS loading; responsive layout
├── service/
│   ├── ThemeLoader.java         # Surface per-file error details for user display
│   └── StatsService.java        # Return success/failure flag from save()
├── model/
│   └── (no changes needed)
└── util/
    └── ErrorUtil.java           # NEW — static Alert helper methods
src/main/resources/
└── org/IsmaelSS/
    └── application.css          # NEW — application-wide stylesheet
```

### Pattern 1: Centralized Error Utility (ErrorUtil)
**What:** A static utility class that creates and shows JavaFX Alert dialogs with consistent formatting. Every user-facing error goes through this class.

**When to use:** All places where an error, warning, or informational message needs to be shown to the user.

**Example:**
```java
// Source: openjfx.io/javadoc/25/javafx.controls/javafx/scene/control/Alert.html
public final class ErrorUtil {
    private ErrorUtil() {}

    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showException(String title, String header, String content, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Create expandable exception section
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(new Label("Detalhes do erro:"), 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }
}
```

### Pattern 2: External CSS Stylesheet (application.css)
**What:** A single CSS file loaded by every scene, defining root-level theme variables and per-component style classes.

**When to use:** All View classes add the stylesheet to their Scene. No inline styles for colors/fonts/spacing after migration.

**Example CSS:**
```css
/* Source: docs.oracle.com/javafx/2/css_tutorial/jfxpub-css_tutorial.htm */
.root {
    -fx-font-family: "Segoe UI", "Arial", sans-serif;
    -fx-font-size: 14px;
    -fx-background-color: #f5f5f5;
    -fx-primary-color: #4CAF50;
    -fx-error-color: #F44336;
    -fx-warning-color: #FF9800;
    -fx-text-dark: #333333;
    -fx-text-light: #ffffff;
}

.label {
    -fx-text-fill: -fx-text-dark;
}

.title-label {
    -fx-font-size: 20px;
    -fx-font-weight: bold;
    -fx-padding: 0 0 10px 0;
}

.section-label {
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-padding: 10px 0 5px 0;
}

.option-button {
    -fx-background-color: #E0E0E0;
    -fx-text-fill: -fx-text-dark;
    -fx-cursor: hand;
    -fx-padding: 10px;
    -fx-background-radius: 5px;
}

.option-button:hover {
    -fx-background-color: #D0D0D0;
}

.correct-button {
    -fx-background-color: -fx-primary-color;
    -fx-text-fill: -fx-text-light;
}

.wrong-button {
    -fx-background-color: -fx-error-color;
    -fx-text-fill: -fx-text-light;
}

.feedback-label {
    -fx-text-fill: -fx-error-color;
    -fx-font-weight: bold;
}
```

**Example Java loading:**
```java
scene.getStylesheets().add(
    getClass().getResource("/org/IsmaelSS/application.css").toExternalForm()
);
```

### Pattern 3: Responsive Layout via VBox Growth Priority
**What:** Use `VBox.setVgrow(child, Priority.ALWAYS)` to make content regions fill available vertical space when the window is resized.

**When to use:** Any view with a scrollable or expandable content area (theme list in ThemeSelectionView, stats sections in ReportsView, question area in StudyRoundView).

**Example:**
```java
// In ReportsView constructor — make content area grow
ScrollPane scrollPane = new ScrollPane(content);
scrollPane.setFitToWidth(true);
VBox.setVgrow(scrollPane, Priority.ALWAYS);  // ← expand when window grows

root.getChildren().addAll(title, scrollPane, voltarButton);
// No VBox.setVgrow on voltarButton → stays at bottom
```

### Anti-Patterns to Avoid
- **Silent failures:** Currently `StatsService.save()` logs SEVERE but never tells the user. After phase, all persistence failures show an Alert.
- **Using `printStackTrace()` in production:** Always log properly and show user-friendly messages. Use `ErrorUtil.showException()` for technical details behind expandable content.
- **Hardcoded colors in Java code:** Inline `setStyle()` calls with specific hex values make theme changes impossible. Extract all style constants to `application.css`.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Error dialog boxes | Custom Stage/Scene | JavaFX `Alert` API | `Alert` handles modality, icon, button types, expandable content, and cross-platform look. Custom dialogs are error-prone and inconsistent. |
| CSS system | Inline styles everywhere | One `application.css` file | External CSS enables single-point changes, hover states via `:hover` pseudo-class, and inheritance via root variables. |
| Responsive layout | Manual bounds calculation | `VBox.setVgrow()` / `AnchorPane` constraints | JavaFX layout panes handle resize automatically; manual bounds calculations break on different screen sizes. |

**Key insight:** JavaFX's `Alert` class (available since 8u40) and CSS support are mature, well-documented, and eliminate 90% of the boilerplate error-handling and styling code developers typically hand-write. The project already uses programmatic views — adding CSS doesn't require FXML or SceneBuilder; `scene.getStylesheets().add()` works with pure-code views.

## Common Pitfalls

### Pitfall 1: Alert called from non-JavaFX thread
**What goes wrong:** `Alert.showAndWait()` throws `IllegalStateException` if called from a background thread.
**Why it happens:** `Dialog.showAndWait()` requires the JavaFX Application Thread.
**How to avoid:** All current code runs on the FX thread (event handlers, `Platform.runLater` callbacks). If adding background threads (not planned for this phase), wrap Alert calls in `Platform.runLater(() -> errorAlert.showAndWait())`.
**Warning signs:** `java.lang.IllegalStateException: This method must be called on the JavaFX Application thread`

### Pitfall 2: Same CSS file path fails when running from JAR
**What goes wrong:** `new File("src/main/resources/application.css")` works during development but fails in production JAR.
**Why it happens:** Resources in a JAR are not files on the filesystem.
**How to avoid:** Always load CSS via `getClass().getResource("/path/in/jar/application.css").toExternalForm()`.
**Warning signs:** `NullPointerException` from `getResource()` when running `java -jar target/flashcard*.jar`

### Pitfall 3: VBox layout collapse when content is too large
**What goes wrong:** VBox grows beyond window bounds and content is clipped.
**Why it happens:** VBox does not clip by default.
**How to avoid:** Wrap overflow content in `ScrollPane` (already done in ReportsView) or set `VBox.clipChildren(true)` (JavaFX 8+).
**Warning signs:** Buttons or labels disappear off bottom of window.

### Pitfall 4: Jackson JsonParseException vs IOException confusion
**What goes wrong:** Catching generic `IOException` misses `JsonParseException` (which is a subclass) but the catch logic may not distinguish between "file not found" and "malformed JSON".
**Why it happens:** `JsonParseException extends JsonProcessingException extends IOException` — they're all `IOException` subclasses.
**How to avoid:** Check exception type in catch block or catch `JsonParseException` first, then `IOException`. Show different messages for corrupt file vs missing file.
**Warning signs:** User sees "Arquivo não encontrado" when the file exists but has syntax errors.

## Code Examples

### Centralized Error Dialog
```java
// Source: openjfx.io/javadoc/25/javafx.controls/javafx/scene/control/Alert.html
// Pattern: ErrorUtil.showWarning shown from ThemeSelectionController
private void initialize() {
    try {
        themes = themeLoader.loadAllThemes();
        // ... rest of init
    } catch (Exception e) {
        LOG.severe("Failed to load themes: " + e.getMessage());
        ErrorUtil.showError("Erro de Carregamento",
            "Nao foi possivel carregar os temas.",
            "Verifique se a pasta themes/ existe e contem arquivos JSON validos.\n\n" +
            "Detalhes: " + e.getMessage());
        themes = new ArrayList<>();
    }
    view.setThemes(themes);
    // continue with empty list if all failed
}
```

### External CSS Loading
```java
// Source: docs.oracle.com/javafx/2/css_tutorial/jfxpub-css_tutorial.htm
// Every View constructor loads the shared stylesheet
public ThemeSelectionView() {
    // ... build root, layout ...

    scene = new Scene(root, 600, 400);
    String css = getClass().getResource("/org/IsmaelSS/application.css").toExternalForm();
    scene.getStylesheets().add(css);
}
```

### Responsive Layout Pattern
```java
// Source: openjfx.io/javadoc/25/javafx.graphics/javafx/scene/layout/VBox.html
// In StudyRoundView constructor — make question content area growable
VBox root = new VBox(10);
root.setPadding(new Insets(20));

// Content area that should expand when window is resized
VBox questionContent = new VBox(10);
questionContent.setMaxHeight(Double.MAX_VALUE);  // allow growth
VBox.setVgrow(questionContent, Priority.ALWAYS); // fill extra space

// Buttons stay at bottom (no vgrow set)
Button exitButton = new Button("Sair");

root.getChildren().addAll(questionContent, exitButton);
scene = new Scene(root, 600, 400);
```

### Malformed JSON Handling with User Feedback
```java
// Source: fasterxml.github.io/jackson-core/javadoc/2.14/
// Pattern: catch specific Jackson exception for precise user message
private Theme loadTheme(File file) throws IOException {
    String name = file.getName().replaceAll("\\.json$", "");

    List<Question> questions;
    try {
        questions = mapper.readValue(file, new TypeReference<List<Question>>() {});
    } catch (JsonParseException e) {
        LOG.warning("Malformed JSON in " + file.getName() + ": " + e.getMessage());
        throw new IOException("Arquivo \"" + file.getName()
            + "\" contem JSON mal formatado. Verifique a sintaxe.", e);
    } catch (JsonMappingException e) {
        LOG.warning("Invalid structure in " + file.getName() + ": " + e.getMessage());
        throw new IOException("Arquivo \"" + file.getName()
            + "\" tem estrutura invalida. Deve ser uma lista de objetos com "
            + "question, options (5 itens) e correct.", e);
    }
    // ... rest of validation ...
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Inline `setStyle()` on every Node | External `application.css` with style classes | This phase | Single-source theming; hover states; root variables |
| Logger-only error reporting | Logger + user-facing Alert dialog | This phase | User sees errors instead of silent failures |
| Fixed-size views | `VBox.setVgrow()` + responsive layout | This phase | Window resizing works gracefully |

**Deprecated/outdated:**
- **ControlsFX Dialogs library** (pre-2014): No longer needed — `Alert` is built into JavaFX since 8u40.
- **JFXPanel embedded error handling**: Not applicable — this is pure JavaFX desktop app.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | All current code runs on the JavaFX Application Thread | Common Pitfalls | If any code path runs on a background thread, `Alert.showAndWait()` will throw IllegalStateException. Low risk — this app is single-threaded (no Task/Service usage). |
| A2 | CSS resource path `/org/IsmaelSS/application.css` resolves correctly from both IDE run and JAR | Code Examples | If the CSS file isn't included in the JAR by Maven, loading fails silently (Scene renders with default style). Mitigation: verify CSS in JAR via `jar tf target/*.jar \| grep css`. |

**If this table is empty:** All claims in this research were verified or cited — no user confirmation needed.

## Open Questions

1. **Should per-question validation failures skip only the bad question, not the whole theme?**
   - What we know: Current code returns `null` from `loadTheme()` if any question is invalid, skipping the entire theme file.
   - What's unclear: Whether users would prefer partial loading (valid questions kept, bad ones reported) or whole-file rejection.
   - Recommendation: Keep whole-file rejection (simpler, matches user expectation that a theme file = one theme). Document in error message how many questions were valid before the invalid one was found.

2. **Should StatsService.save() signal failure to callers?**
   - What we know: Current `save()` returns void; failures are logged but invisible.
   - What's unclear: Changing the return type requires updating all call sites (StudyRoundController handlesRound, handleExit).
   - Recommendation: Return `boolean` from `save()` (true = success, false = failure). Controllers show error Alert on false. This is a 15-minute change with high user value.

## Validation Architecture

> Nyquist validation is enabled (workflow.nyquist_validation: true in config.json).

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5.11.0 |
| Config file | none (Maven Surefire plugin configured in pom.xml) |
| Quick run command | `mvn test` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| (none) | ThemeLoader skips malformed JSON without crashing | unit | `mvn test -pl . -Dtest=ThemeLoaderErrorHandlingTest` | ❌ Wave 0 |
| (none) | ThemeLoader shows appropriate message for empty themes dir | unit | (part of ThemeLoaderErrorHandlingTest) | ❌ Wave 0 |
| (none) | StatsService handles corrupted stats file gracefully | unit | `mvn test -pl . -Dtest=StatsServiceTest` | ✅ (existing test covers missing file) |
| (none) | Application window responsive when resized | manual | N/A | N/A (visual test) |
| (none) | CSS stylesheet loads on all three screens | manual | N/A | N/A (visual test) |

### Sampling Rate
- **Per task commit:** `mvn test`
- **Phase gate:** All tests green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `src/test/java/org/IsmaelSS/service/ThemeLoaderErrorHandlingTest.java` — tests for malformed JSON, empty file, missing fields
- [ ] `src/test/java/org/IsmaelSS/util/ErrorUtilTest.java` — verify utility class methods exist (can't fully test Alert in headless test environment)

*(No gaps for existing StatsService tests — they already verify corrupted stats file behavior)*

## Security Domain

> Security enforcement not applicable to this phase. No authentication, authorization, input validation (beyond JSON shape validation already in ThemeLoader), or cryptography concerns. The app is single-user local desktop with no network exposure.

**security_enforcement:** false (implicitly — no authentication, user data, or sensitive operations affected by this phase).

## Sources

### Primary (HIGH confidence)
- [openjfx.io/javadoc/25/javafx.controls/javafx/scene/control/Alert.html] — Alert class API, AlertType enum, showAndWait()
- [openjfx.io/javadoc/25/javafx.graphics/javafx/scene/layout/VBox.html] — VBox layout, setVgrow, Priority.ALWAYS
- [docs.oracle.com/javafx/2/css_tutorial/jfxpub-css_tutorial.htm] — Adding CSS stylesheets to Scene, style class selectors
- [fasterxml.github.io/jackson-core/javadoc/2.14/] — JsonParseException, JsonMappingException hierarchy
- [code.makery.ch/blog/javafx-dialogs-official] — Verified Alert patterns with expandable exception content

### Secondary (MEDIUM confidence)
- [stackoverflow.com/questions/25145956/] — JavaFX global exception handler with Platform.runLater
- [docs.oracle.com/javase/8/javafx/api/javafx/scene/doc-files/cssref.html] — JavaFX CSS reference, -fx- vendor prefix properties
- [open-elements.com/posts/2016-02-07-javafx-and-css] — JavaFX CSS best practices, stylesheet loading patterns

### Tertiary (LOW confidence)
- [dev.java/learn/javafx/layout] — Additional layout confirmation

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - JavaFX Alert and CSS are built-in; Jackson is already in the project
- Architecture: HIGH - MVC pattern is established; ErrorUtil is a standard pattern
- Pitfalls: HIGH - All listed pitfalls are documented in official JavaFX docs and Stack Overflow
- CSS patterns: MEDIUM - External CSS loading is documented; exact selectors depend on View refactoring choices

**Research date:** 2026-07-07
**Valid until:** 2027-01-07 (stable JavaFX API, no major version changes expected)
