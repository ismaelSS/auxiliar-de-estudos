# Phase 13: Study Mode Overhaul - Research

**Researched:** 2026-07-24
**Domain:** JavaFX UI overhaul — tab renaming, multi-select theme cards, question count selection, SM-2 round integration
**Confidence:** HIGH

## Summary

This phase transforms the existing "Treinar" (Train) tab into "Estudar" (Study), adding two new capabilities: (1) a question-count selector before each round starts, and (2) a custom study mode where users multi-select themes via checkboxes to create mixed-question rounds. The existing round infrastructure (`RoundState`, `StudyRoundController`, `StatsService.recordRound`) is well-designed and only needs a new factory method for multi-theme custom rounds. The main work is UI — adding a mode toggle to ReviewDashboardView, checkboxes to ThemeCardNode, a ComboBox for question count, and renaming labels.

**Primary recommendation:** Extend existing patterns — add `createCustomStudyRound` to RoundState, add a CheckBox to ThemeCardNode that's toggled by a mode flag, add a ComboBox-based question count selector, and rename two labels. No new classes needed beyond the RoundState factory method.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Tab renaming | View (ThemeSelectionView) | — | Pure UI label change |
| Question count selection | View + Controller | — | UI control + business logic to limit questions |
| Custom study mode toggle | View (ReviewDashboardView) | Controller | UI state + triggers round creation |
| Theme multi-select checkboxes | View (ThemeCardNode) | — | Pure UI component, controlled by parent |
| Mixed-theme round creation | Model (RoundState) | Service (StatsService) | Business logic for question selection |
| SM-2 recording of custom rounds | Service (StatsService) | Model (RoundResult) | Existing `recordRound` handles this — no changes needed |
| Round execution | Controller (StudyRoundController) | View (StudyRoundView) | Existing flow, reused as-is |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| JavaFX Controls | 25 (project) | UI framework — TabPane, ComboBox, CheckBox, Button | Already in use, project standard |
| JavaFX Layout | 25 | VBox, HBox, StackPane for card layout | Already in use |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| (none new) | — | — | This phase adds no new dependencies |

**Installation:** No new packages required — all UI components are standard JavaFX controls.

## Package Legitimacy Audit

> No external packages are installed in this phase. All changes are to existing Java source and CSS files.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| (none) | — | — | — | — | — | — |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### Current Flow Diagram

```
ThemeSelectionView (TabPane)
  └─ jogarTab ("Treinar" → will rename to "Estudar")
       └─ ReviewDashboardView
            └─ ThemeCardNode[] (per theme)
                 ├─ [Revisar] button → ThemeSelectionController.handleReviewTheme(theme)
                 │    ├─ RoundState.createDueReviewRound(theme, statsService)
                 │    ├─ StudyRoundView + StudyRoundController
                 │    └─ onRoundEnd → refreshDashboard()
                 └─ [Marcar como feita] button → handleMarkAsDone(themeName)

NEW FLOW (custom study):
ReviewDashboardView
  └─ [Modo Estudo toggle button] → sets customStudyMode=true
       └─ ThemeCardNode[] now show checkboxes
            └─ [Iniciar Estudo] button (new, appears when checkboxes checked)
                 ├─ Collect checked themes
                 ├─ ComboBox question-count selector (5/10/15/20/Todas)
                 ├─ RoundState.createCustomStudyRound(selectedThemes, count, statsService)
                 └─ Same StudyRoundController flow → onRoundEnd → refreshDashboard()
```

### Recommended Project Structure
No new files needed. Modifications to existing files:
```
src/main/java/org/IsmaelSS/
├── view/
│   ├── ThemeSelectionView.java    — rename tab label
│   ├── ThemeCardNode.java         — add CheckBox, mode toggle support
│   └── ReviewDashboardView.java   — add custom study mode section
├── model/
│   └── RoundState.java            — add createCustomStudyRound factory
├── controller/
│   └── ThemeSelectionController.java — add custom study round handling
└── resources/styles/
    └── theme.css                  — add checkbox-in-card styling
```

### Pattern 1: RoundState Factory Methods
**What:** Static factory methods on RoundState create different types of rounds
**When to use:** For the new custom study round
**Example:**
```java
// Existing pattern — createDueReviewRound (RoundState.java:113)
// New method follows same structure:
public static RoundState createCustomStudyRound(
        List<Theme> selectedThemes, int questionLimit, StatsService statsService) {
    List<RoundQuestion> questions = new ArrayList<>();
    for (Theme theme : selectedThemes) {
        List<Question> themeQuestions = new ArrayList<>(theme.getQuestions());
        Collections.shuffle(themeQuestions);
        int take = Math.min(questionLimit, themeQuestions.size());
        for (int i = 0; i < take; i++) {
            Question q = themeQuestions.get(i);
            List<String> original = q.getOptions();
            String correctText = original.get(q.getCorrect());
            List<String> shuffled = new ArrayList<>(original);
            Collections.shuffle(shuffled);
            int newCorrect = shuffled.indexOf(correctText);
            questions.add(new RoundQuestion(theme.getName(), q, shuffled, newCorrect));
        }
    }
    Collections.shuffle(questions);
    return new RoundState(questions, true);
}
```

### Pattern 2: Conditional UI Visibility
**What:** ThemeCardNode hides/shows the checkbox based on a mode flag set from the parent
**When to use:** Custom study mode toggle
**Example:**
```java
// ThemeCardNode — add checkbox field and toggle method
private final CheckBox studyCheckBox;

// In constructor — checkbox initially invisible
studyCheckBox = new CheckBox();
studyCheckBox.setVisible(false);
studyCheckBox.setManaged(false);

// New method to toggle visibility
public void setCustomStudyMode(boolean active) {
    studyCheckBox.setVisible(active);
    studyCheckBox.setManaged(active);
}

// Add to badgeRow: badgeRow.getChildren().addAll(studyCheckBox, nameLabel, dominioPrefix, dominioLabel);
```

### Pattern 3: ComboBox for Question Count
**What:** A dark-themed ComboBox for selecting question count
**When to use:** Before starting any round
**Example:**
```java
// ComboBox already styled in theme.css (lines 212-245)
ComboBox<String> countSelector = new ComboBox<>();
countSelector.getItems().addAll("5", "10", "15", "20", "Todas");
countSelector.setValue("Todas");
countSelector.getStyleClass().add("combo-box");
```

### Anti-Patterns to Avoid
- **Don't create a new Scene for custom study:** Reuse the existing screen navigation pattern via `screenController.registerScreen` and `switchTo`
- **Don't bypass RoundState:** The existing factory pattern ensures questions are properly shuffled and wrapped in RoundQuestion — follow the same structure
- **Don't create a new StatsService method for custom rounds:** `recordRound(List<RoundResult>)` already works for any theme name — just pass the correct theme name in each RoundResult

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| SM-2 scheduling | Custom spaced repetition logic | `QuestionScore.updateSM2()` via existing `recordRound` | SM-2 has specific interval/easiness formulas that are already implemented |
| Question shuffling | Custom randomization | `Collections.shuffle()` (already used) | Standard Java, already the pattern in RoundState |
| Multi-theme question pooling | Custom merge/sort logic | Follow `buildQuestions` pattern in RoundState:34-52 | Existing pattern handles per-theme selection + global shuffle |
| Dark checkbox styling | Write new CSS from scratch | Extend existing `.check-box` style (theme.css:86-88) | Base style already exists, just need active/checked variant |

**Key insight:** The entire round lifecycle (create → execute → record → refresh) is already robust. The only new thing is a different factory method for round creation and UI for mode toggling + question count selection.

## Common Pitfalls

### Pitfall 1: RoundState Accessibility
**What goes wrong:** The `RoundQuestion` inner class and `RoundState(List<RoundQuestion>, boolean)` constructor are `private`
**Why it happens:** They were designed for internal use only
**How to avoid:** Either add a new `public` factory method following the same pattern (preferred), or change visibility. The factory method approach is cleaner — just add `createCustomStudyRound` as a new static method.
**Warning signs:** Compilation error when trying to build RoundQuestion from outside RoundState

### Pitfall 2: ThemeCardNode Constructor Signature
**What goes wrong:** ThemeCardNode's constructor takes specific Runnable parameters — adding checkbox support requires either extending the constructor or adding a setter
**Why it happens:** The constructor was designed for a specific use case
**How to avoid:** Use the setter approach — add `setCustomStudyMode(boolean)` and `isSelected()` methods. The CheckBox can be added in the constructor but initially hidden. No constructor signature change needed.
**Warning signs:** Changes to `ReviewDashboardView.buildCards()` call site if constructor changes

### Pitfall 3: ThemeCardNode Not Accessible from ReviewDashboardView
**What goes wrong:** ReviewDashboardView creates ThemeCardNode instances in `buildCards()` but doesn't expose them for external mode toggling
**Why it happens:** Cards are created and added to `cardContainer` VBox
**How to avoid:** Add a method on ReviewDashboardView like `setCustomStudyMode(boolean)` that iterates `cardContainer.getChildren()` and toggles each card. Also add `getSelectedThemes()` that collects checked themes.
**Warning signs:** Having to make `cardContainer` public

### Pitfall 4: Question Count Applied Per-Theme vs Globally
**What goes wrong:** The question limit should be per-theme (same as `buildQuestions` pattern) not globally capped
**Why it happens:** RoundState.buildQuestions already applies `questionsPerTheme` per theme — custom study should follow the same pattern
**How to avoid:** Pass `questionLimit` as the per-theme parameter to the new factory method, consistent with the existing `RoundState(List<Theme>, int)` constructor pattern
**Warning signs:** User selects "10" and expects 10 questions total but gets 10 per theme

### Pitfall 5: Custom Study Round with No Themes Selected
**What goes wrong:** User clicks "Iniciar Estudo" without checking any themes
**Why it happens:** No validation before round creation
**How to avoid:** Disable the "Iniciar Estudo" button until at least one checkbox is checked. Use `BooleanBinding` or a listener on the selected count.
**Warning signs:** `IllegalArgumentException` or empty round behavior

### Pitfall 6: Existing "Revisar" Button Behavior in Custom Mode
**What goes wrong:** In custom study mode, clicking "Revisar" on a card still starts a single-theme due review round
**Why it happens:** The button handler is bound to `onReview` callback
**How to avoid:** This is actually correct behavior — the "Revisar" button always does single-theme due review. The custom study flow uses a separate "Iniciar Estudo" button. Document this clearly so the planner doesn't conflate the two.
**Warning signs:** Confusion about what "Revisar" does in custom mode

## Runtime State Inventory

> Not applicable — this is a feature-addition phase, not a rename/refactor/migration.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit Jupiter 5.11.0 |
| Config file | none — uses defaults |
| Quick run command | `mvn test -pl . -Dtest=RoundStateCustomStudyTest -q` |
| Full suite command | `mvn test -q` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| REQ-13.1 | Tab renamed to "Estudar" | manual-only | N/A — UI label | ❌ |
| REQ-13.2 | "Revisar" button renamed to "Estudar" | manual-only | N/A — UI label | ❌ |
| REQ-13.3 | Question count selector appears before round | manual-only | N/A — UI control | ❌ |
| REQ-13.4 | Custom study mode shows checkboxes | manual-only | N/A — UI control | ❌ |
| REQ-13.5 | Custom study creates mixed round from selected themes | unit | `mvn test -Dtest=RoundStateCustomStudyTest -q` | ❌ Wave 0 |
| REQ-13.6 | Custom study answers recorded via SM-2 | unit | `mvn test -Dtest=RoundStateCustomStudyTest -q` | ❌ Wave 0 |
| REQ-13.7 | Question count limits per-theme selection | unit | `mvn test -Dtest=RoundStateCustomStudyTest -q` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `mvn test -q`
- **Per wave merge:** `mvn test -q`
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `src/test/java/org/IsmaelSS/model/RoundStateCustomStudyTest.java` — covers REQ-13.5, REQ-13.6, REQ-13.7
- [ ] Framework install: None needed (JUnit 5 already configured in pom.xml)

## Security Domain

> Applicable — `security_enforcement` is absent (defaults to enabled).

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | N/A — desktop app, no auth |
| V3 Session Management | no | N/A — no sessions |
| V4 Access Control | no | N/A — single-user desktop |
| V5 Input Validation | no | N/A — all inputs are UI selections, free-text not used |
| V6 Cryptography | no | N/A — no crypto operations |

### Known Threat Patterns for JavaFX Desktop

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| (none significant) | — | Desktop app, local filesystem only |

## Code Examples

### Renaming Tab Label
```java
// Source: ThemeSelectionView.java:20 — change "Treinar" to "Estudar"
// Current:
jogarTab = new Tab("Treinar", new VBox());
// Change to:
jogarTab = new Tab("Estudar", new VBox());
```

### Renaming "Revisar" Button
```java
// Source: ThemeCardNode.java:68 — change "Revisar" to "Estudar"
// Current:
revisarBtn = new Button("Revisar");
// Change to:
revisarBtn = new Button("Estudar");
```

### Adding CheckBox to ThemeCardNode
```java
// Source: ThemeCardNode.java — add after badgeRow declaration (line 24)
private final CheckBox studyCheckBox;

// In constructor, after badgeRow setup:
studyCheckBox = new CheckBox();
studyCheckBox.setVisible(false);
studyCheckBox.setManaged(false);
studyCheckBox.getStyleClass().add("theme-card-checkbox");

// Add to badgeRow as first child:
badgeRow.getChildren().addAll(studyCheckBox, nameLabel, dominioPrefix, dominioLabel);

// New methods:
public void setCustomStudyMode(boolean active) {
    studyCheckBox.setVisible(active);
    studyCheckBox.setManaged(active);
}

public boolean isStudySelected() {
    return studyCheckBox.isSelected();
}

public void setSelected(boolean selected) {
    studyCheckBox.setSelected(selected);
}
```

### ReviewDashboardView Custom Study Mode Toggle
```java
// Source: ReviewDashboardView.java — add fields and methods
private boolean customStudyMode = false;
private Button startStudyBtn; // "Iniciar Estudo" button, initially hidden

// Add toggle method:
public void setCustomStudyMode(boolean active) {
    this.customStudyMode = active;
    for (Node node : cardContainer.getChildren()) {
        if (node instanceof ThemeCardNode card) {
            card.setCustomStudyMode(active);
        }
    }
    startStudyBtn.setVisible(active);
    startStudyBtn.setManaged(active);
}

// Add method to collect selected themes:
public List<String> getSelectedThemeNames() {
    List<String> selected = new ArrayList<>();
    for (Node node : cardContainer.getChildren()) {
        if (node instanceof ThemeCardNode card && card.isStudySelected()) {
            selected.add(card.getThemeName());
        }
    }
    return selected;
}
```

### Question Count ComboBox in ThemeSelectionController
```java
// Source: ThemeSelectionController.java — add before starting round
private int askQuestionCount(List<String> themeNames) {
    ComboBox<String> countBox = new ComboBox<>();
    countBox.getItems().addAll("5", "10", "15", "20", "Todas");
    countBox.setValue("Todas");
    countBox.getStyleClass().add("combo-box");

    Dialog<Integer> dialog = new Dialog<>();
    dialog.setTitle("Número de questões");
    dialog.setHeaderText("Quantas questões por tema?");
    dialog.getDialogPane().setContent(countBox);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    dialog.setResultConverter(btn -> {
        if (btn == ButtonType.OK) {
            return "Todas".equals(countBox.getValue()) ? Integer.MAX_VALUE : Integer.parseInt(countBox.getValue());
        }
        return null;
    });

    Optional<Integer> result = dialog.showAndWait();
    return result.orElse(0); // 0 = cancelled
}
```

### New RoundState Factory Method
```java
// Source: RoundState.java — add as new static method after createDueReviewRound (line 157)
/**
 * Creates a custom study round with questions from multiple selected themes.
 * Each theme contributes up to {@code questionsPerTheme} shuffled questions.
 * All questions from all themes are pooled and shuffled.
 */
public static RoundState createCustomStudyRound(
        List<Theme> selectedThemes, int questionsPerTheme, StatsService statsService) {
    List<RoundQuestion> questions = new ArrayList<>();
    for (Theme theme : selectedThemes) {
        List<Question> themeQuestions = new ArrayList<>(theme.getQuestions());
        Collections.shuffle(themeQuestions);
        int take = Math.min(questionsPerTheme, themeQuestions.size());
        for (int i = 0; i < take; i++) {
            Question q = themeQuestions.get(i);
            List<String> original = q.getOptions();
            String correctText = original.get(q.getCorrect());
            List<String> shuffled = new ArrayList<>(original);
            Collections.shuffle(shuffled);
            int newCorrect = shuffled.indexOf(correctText);
            questions.add(new RoundQuestion(theme.getName(), q, shuffled, newCorrect));
        }
    }
    Collections.shuffle(questions);
    return new RoundState(questions, true);
}
```

### Custom Study CSS for CheckBox in Card
```css
/* Source: theme.css — add after .theme-card block (line 255) */
/* Custom study checkbox inside theme card */
.theme-card-checkbox {
    -fx-padding: 0 4 0 0;
}
.theme-card-checkbox .box {
    -fx-background-color: #020817;
    -fx-border-color: #333355;
    -fx-border-radius: 3;
    -fx-background-radius: 3;
    -fx-padding: 3;
}
.theme-card-checkbox:selected .box {
    -fx-background-color: #fe9a00;
    -fx-border-color: #fe9a00;
}
.theme-card-checkbox:selected .mark {
    -fx-background-color: #ffffff;
}

/* Custom study section title */
.custom-study-header {
    -fx-padding: 8 0 0 0;
}

/* Start study button */
.button-start-study {
    -fx-background-color: #1abc9c;
    -fx-text-fill: #ffffff;
    -fx-background-radius: 4;
    -fx-padding: 8 24 8 24;
    -fx-cursor: hand;
    -fx-font-weight: bold;
}
.button-start-study:hover { -fx-background-color: #2dd4a8; }
.button-start-study:pressed { -fx-background-color: #16a085; }

/* Mode toggle button */
.button-mode-toggle {
    -fx-background-color: transparent;
    -fx-text-fill: #fe9a00;
    -fx-border-color: #fe9a00;
    -fx-border-radius: 4;
    -fx-padding: 6 16 6 16;
    -fx-cursor: hand;
}
.button-mode-toggle:hover { -fx-background-color: #1a1a2e; }
.button-mode-toggle-active {
    -fx-background-color: #fe9a00;
    -fx-text-fill: #ffffff;
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Single-theme due review only | + multi-theme custom study | This phase | New factory method, no existing behavior changed |

**Deprecated/outdated:**
- Label "Revisar" on button → replaced with "Estudar"
- Tab "Treinar" → replaced with "Estudar"

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `RoundQuestion` inner class visibility can be accessed from `createCustomStudyRound` since it's a static method inside RoundState | Common Pitfalls | Low — the factory method is inside the same class |
| A2 | JavaFX ComboBox styling is inherited from existing theme.css rules (lines 212-245) without needing a separate CSS file | Code Examples | Low — existing `.combo-box` rules are global |
| A3 | The question count limit is per-theme (consistent with `buildQuestions` pattern) not a global cap | Common Pitfalls | Medium — if planner interprets it as global, users get unexpected counts |
| A4 | The custom study mode does NOT filter by "due" status — it shows ALL questions from selected themes | Architecture | Medium — user may expect only due questions, but CONTEXT.md says "mixed round from all selected themes" without due filtering |
| A5 | No new Maven dependencies are needed | Standard Stack | Low — all components are in JavaFX Controls which is already a dependency |

## Open Questions

1. **Should the question count ComboBox appear in both single-theme review AND custom study?**
   - What we know: CONTEXT.md says "Before starting a study round, show a prompt/spinner to choose number of questions"
   - What's unclear: Does this apply to the existing "Estudar" (formerly "Revisar") button too, or only custom study?
   - Recommendation: Apply to both — add the count selector in `handleReviewTheme` AND in the custom study flow. This is the most consistent interpretation.

2. **Should the "Iniciar Estudo" button be in ReviewDashboardView or ThemeSelectionView?**
   - What we know: The custom study mode toggles checkboxes on cards
   - What's unclear: Where does the "start" button live?
   - Recommendation: Place it in ReviewDashboardView alongside the search field and mode toggle — keeps all dashboard interactions in one place.

3. **When custom study mode is active, should the "Marcar como feita" button be hidden?**
   - What we know: CONTEXT.md doesn't mention hiding it
   - Recommendation: Keep it visible — it's a useful per-theme action regardless of mode. The checkbox and the "Marcar como feita" button serve different purposes.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JavaFX 25 | UI rendering | ✓ | 25 (pom.xml) | — |
| Maven | Build/test | ✓ | — | — |
| JUnit 5.11.0 | Tests | ✓ | pom.xml | — |

**Missing dependencies with no fallback:** none

## Sources

### Primary (HIGH confidence)
- Direct codebase reading — all source files listed in CONTEXT.md canonical references
- `pom.xml` — verified dependency versions

### Secondary (MEDIUM confidence)
- JavaFX 25 official API — CheckBox, ComboBox, Dialog, TabPane patterns (standard JavaFX API, well-documented)

### Tertiary (LOW confidence)
- None — all findings are from direct codebase inspection

## Metadata

**Confidence breakdown:**
- Standard Stack: HIGH — no new dependencies, all JavaFX controls already in project
- Architecture: HIGH — full understanding of RoundState factory pattern, StatsService recording, StudyRoundController flow
- Pitfalls: HIGH — identified via direct code reading (private constructors, constructor signatures, mode state management)

**Research date:** 2026-07-24
**Valid until:** 2026-08-24 (stable — JavaFX API is stable, project is self-contained)
