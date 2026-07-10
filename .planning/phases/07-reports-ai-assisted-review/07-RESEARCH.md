# Phase 7: Reports & AI-Assisted Review - Research

**Researched:** 2026-07-10
**Domain:** JavaFX UI refactoring — Accordion drawers, clipboard API, stats service extension
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** Prompt gerado automaticamente e copiado direto para a área de transferência (sem prévia editável), usando `javafx.scene.input.Clipboard`
- **D-02:** Conteúdo do prompt: tema sendo estudado + lista das questões de menor pontuação daquele tema, solicitando à IA que explique cada tópico em detalhes
- **D-03:** Cada seção/gaveta de tema no relatório terá um botão "Copiar prompt IA"
- "Domínio" = percentage of questions with positive score (score > 0)
- Reports focused on lowest-scoring questions per theme, drawer-style expandable sections
- Limit of 10 lowest-scoring questions per theme
- Drawers: use JavaFX native `Accordion` + `TitledPane`

### the agent's Discretion
- **Cálculo do Domínio:** Decidir implementação — novo método em StatsService ou cálculo inline. Exibir na mesma linha da Pontuação ou separadamente.
- **Estrutura do Relatório:** Decidir se mantém Resumo Geral simplificado ou foca apenas nas gavetas por tema.
- **Implementação das Gavetas:** Decidir entre Accordion + TitledPane nativo do JavaFX ou VBox customizado com toggle.
- **Posição do botão de prompt:** Dentro de cada gaveta de tema ou no cabeçalho.

### Deferred Ideas (OUT OF SCOPE)
None — discussion stayed within phase scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| DOM-01 | Theme selection shows "Domínio: X%" (percentage of questions with positive score) | New `getDominio()` method in StatsService + `updateDominio()` in ThemeSelectionView |
| DOM-02 | Reports screen focused on lowest-scoring questions per theme | Refactored ReportsView with Accordion replacing flat themeBox+errorBox |
| DOM-03 | Each theme has drawer-style expandable section with up to 10 lowest-scoring questions | Accordion + TitledPane pattern from Oracle docs; new `getLowestScoreQuestionsByTheme()` method |
| DOM-04 | Each theme has button that copies AI prompt about difficult questions to clipboard | `Clipboard.getSystemClipboard().setContent()` pattern from Oracle docs |
| DOM-05 | All existing tests still pass | No changes to existing tested behavior; new methods are additive |
</phase_requirements>

## Summary

Phase 7 requires three interconnected changes: (1) adding a "Domínio" percentage to the theme selection screen alongside the existing Pontuação, (2) refactoring the Reports screen from a flat list layout to an Accordion-based drawer layout with per-theme expandable sections showing the 10 lowest-scoring questions, and (3) adding a "Copiar prompt IA" button in each drawer that auto-generates and copies an AI prompt to the clipboard.

The StatsService currently has `getLowestScoreQuestions(int limit)` which returns a flat list across ALL themes — this needs a new per-theme variant. The `getAproveitamento()` method returns a weighted sum string (not a percentage), so dominio calculation requires either a new method or inline computation in the controller. The ReportsView uses a simple VBox with three child boxes (overallBox, themeBox, errorBox) that need to be restructured around JavaFX's `Accordion` + `TitledPane` components. The clipboard copy uses `javafx.scene.input.Clipboard.getSystemClipboard()` with `ClipboardContent.putString()` — a straightforward 3-line pattern from Oracle's official docs.

**Primary recommendation:** Add `getLowestScoreQuestionsByTheme(String themeName, int limit)` and `getDominio(String themeName)` to StatsService; restructure ReportsView with Accordion replacing themeBox+errorBox; add `updateDominio()` to ThemeSelectionView; implement clipboard copy in ReportsController with a simple helper method.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Domain % calculation | Service | Controller | StatsService owns data; controller orchestrates display |
| Lowest-score questions per theme | Service | — | Data query belongs in service layer |
| Accordion drawer UI | View | Controller | View builds components; controller populates them |
| AI prompt generation | Controller | — | Business logic: theme name + question list formatting |
| Clipboard copy | Controller | — | Side effect triggered by button action |
| Theme selection dominio display | View | Controller | View exposes label; controller calls update |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| JavaFX Controls | 25 | Accordion, TitledPane, Button, Label, VBox | Built-in JavaFX — project already uses these |
| JavaFX Graphics | 25 | Clipboard, ClipboardContent | Built-in JavaFX clipboard API |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| (none needed) | — | — | All functionality uses existing JavaFX APIs |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Accordion + TitledPane | Custom VBox with toggle buttons | Accordion is native, handles expand/collapse/animation automatically; custom is more work for no benefit |
| Clipboard API | Runtime.exec + xclip/clip | Cross-platform clipboard is built-in; external tools are fragile |
| New StatsService method | Inline calculation in controller | Service method is testable, reusable, keeps controller thin |

**Installation:** No new dependencies required — all JavaFX modules already in pom.xml.

## Package Legitimacy Audit

> No external packages installed in this phase — all functionality uses built-in JavaFX APIs already in the project.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| (none) | — | — | — | — | — | No packages to audit |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
ThemeSelectionScreen                    ReportsScreen
┌─────────────────────────┐            ┌──────────────────────────────┐
│ [Theme list]            │            │ Resumo Geral                  │
│  ☐ tema1  P:5  D:80%   │    ──→     │  Total respondidas: X         │
│  ☐ tema2  P:-3 D:40%   │  (click    │  Total acertos: Y             │
│  ☐ tema3  P:2  D:100%  │   "Relat") │                               │
│                         │            │ ┌────────────────────────────┐ │
│ [Controls]              │            │ │ ▶ tema1 (Domínio: 80%)    │ │
│ [Iniciar] [Relatórios]  │            │ │   [Copiar prompt IA]      │ │
└─────────────────────────┘            │ │   q1: "Qual é..." → -6    │ │
                                       │ │   q3: "Qual é..." → -3    │ │
                                       │ ├────────────────────────────┤ │
                                       │ │ ▶ tema2 (Domínio: 40%)    │ │
                                       │ │   [Copiar prompt IA]      │ │
                                       │ │   q2: "Qual é..." → -10   │ │
                                       │ └────────────────────────────┘ │
                                       │                               │
                                       │ [Voltar]                      │
                                       └──────────────────────────────┘
```

**Data Flow:**
1. ThemeSelectionController.refreshScores() → StatsService.getAproveitamento() + getDominio() → ThemeSelectionView.updateAproveitamento() + updateDominio()
2. ReportsController.refresh() → StatsService.getThemeStats(), getLowestScoreQuestionsByTheme(), getDominio() → ReportsView builds Accordion with TitledPanes

### Recommended Project Structure
```
src/main/java/org/IsmaelSS/
├── controller/
│   ├── ReportsController.java        # MODIFY: drawer-based refresh, AI prompt copy
│   └── ThemeSelectionController.java  # MODIFY: add dominio update call
├── service/
│   └── StatsService.java             # MODIFY: add getDominio(), getLowestScoreQuestionsByTheme()
├── view/
│   ├── ReportsView.java              # MODIFY: Accordion replacing flat boxes
│   └── ThemeSelectionView.java       # MODIFY: add updateDominio() method
└── model/
    └── StatsData.java                # NO CHANGE
```

### Pattern 1: Accordion Drawer Sections (ReportsView)
**What:** Replace the flat themeBox + errorBox VBox with an Accordion containing one TitledPane per theme.
**When to use:** When you need expandable/collapsible sections — exactly the "drawer" pattern requested.
**Example:**
```java
// Source: Oracle JavaFX TitledPaneSample (docs.oracle.com/javafx/2/ui_controls/TitledPaneSample.java.html)
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;

Accordion accordion = new Accordion();

for (String themeName : themesWithData) {
    VBox themeContent = new VBox(5);
    themeContent.setPadding(new Insets(5));

    // Add lowest-score questions
    List<Map.Entry<String, Integer>> lowest = statsService.getLowestScoreQuestionsByTheme(themeName, 10);
    for (Map.Entry<String, Integer> entry : lowest) {
        themeContent.getChildren().add(
            new Label(entry.getKey() + " — Pontuação: " + entry.getValue()));
    }

    // Add AI prompt button
    Button aiButton = new Button("Copiar prompt IA");
    aiButton.setOnAction(e -> copyAIPrompt(themeName, lowest));
    themeContent.getChildren().add(aiButton);

    TitledPane pane = new TitledPane(themeName + " (Domínio: " + dominio + "%)", themeContent);
    pane.setExpanded(false);
    accordion.getPanes().add(pane);
}

// Replace themeBox + errorBox with accordion in the content VBox
```

### Pattern 2: Clipboard Copy (ReportsController)
**What:** Copy auto-generated AI prompt text to system clipboard using JavaFX built-in API.
**When to use:** Button click handler for "Copiar prompt IA".
**Example:**
```java
// Source: Oracle JavaFX Clipboard docs (docs.oracle.com/en/java/java-components/javafx/21/docs/javafx.graphics/javafx/scene/input/Clipboard.html)
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

private void copyAIPrompt(String themeName, List<Map.Entry<String, Integer>> lowestQuestions) {
    StringBuilder sb = new StringBuilder();
    sb.append("Explique os seguintes tópicos de ").append(themeName).append(" em detalhes:\n\n");
    for (Map.Entry<String, Integer> entry : lowestQuestions) {
        sb.append("- Questão ").append(entry.getKey())
          .append(" (pontuação: ").append(entry.getValue()).append(")\n");
    }

    ClipboardContent content = new ClipboardContent();
    content.putString(sb.toString());
    Clipboard.getSystemClipboard().setContent(content);
}
```

### Pattern 3: Dominio Calculation (StatsService)
**What:** Calculate the percentage of questions in a theme with positive score (score > 0).
**When to use:** Called by both ThemeSelectionController and ReportsController.
**Example:**
```java
// No external source — project-specific logic
public String getDominio(String themeName) {
    ThemeStats ts = data.getThemes().get(themeName);
    if (ts == null || ts.getQuestions().isEmpty()) return "N/A";
    long positiveCount = ts.getQuestions().values().stream()
            .filter(qs -> qs.getScore() > 0)
            .count();
    int percentage = (int) ((positiveCount * 100) / ts.getQuestions().size());
    return String.valueOf(percentage);
}
```

### Anti-Patterns to Avoid
- **Don't put Accordion in ScrollPane's content VBox and then wrap Accordion itself in another ScrollPane:** The Accordion already handles scrolling internally when it has many panes. Just put the Accordion in the main content VBox and let the outer ScrollPane handle overflow.
- **Don't use setExpanded(true) on TitledPane before the scene is shown:** Use `accordion.setExpandedPane(pane)` instead, or set expanded after stage.show(). For initial state, `pane.setExpanded(false)` works fine during construction.
- **Don't create ClipboardContent per-call without checking:** Always call `Clipboard.getSystemClipboard().setContent(content)` directly — don't cache the Clipboard reference as it's a system singleton.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Expandable/collapsible sections | Custom VBox + toggle Button + visibility binding | `Accordion` + `TitledPane` | Accordion handles single-expand, animation, keyboard nav, styling automatically |
| Copy to clipboard | Robot class + keyboard simulation, or Runtime.exec | `Clipboard.getSystemClipboard().setContent()` | Built-in, cross-platform, reliable, 3 lines of code |
| Percentage calculation | Multiple methods for numerator/denominator | Stream filter + count in single method | Simpler, testable, single responsibility |

**Key insight:** JavaFX provides all the building blocks needed — Accordion/TitledPane for drawers, Clipboard for copy. No external libraries or custom implementations needed.

## Common Pitfalls

### Pitfall 1: getAproveitamento() returns weight sum, not percentage
**What goes wrong:** Confusing the existing "Pontuação" value (a weighted sum like "-3" or "5") with a percentage.
**Why it happens:** The method name "getAproveitamento" sounds like it should return a percentage, but it actually returns a sum of weights (+2 for positive, -3 for negative).
**How to avoid:** Create a separate `getDominio()` method for the percentage. Keep `getAproveitamento()` unchanged for backward compatibility with the existing Pontuação display.
**Warning signs:** If theme selection shows "Domínio: -3" instead of "Domínio: 40%", the wrong method is being used.

### Pitfall 2: getLowestScoreQuestions() is global, not per-theme
**What goes wrong:** The current `getLowestScoreQuestions(limit)` returns questions from ALL themes sorted together, not filtered by theme.
**Why it happens:** It was built for the global "Menor Pontuação" list, not per-theme drawers.
**How to avoid:** Add a new method `getLowestScoreQuestionsByTheme(String themeName, int limit)` that filters by theme first, then sorts and limits.
**Warning signs:** If one drawer shows questions from a different theme, the global method is being used instead of the per-theme one.

### Pitfall 3: Accordion in ScrollPane layout conflict
**What goes wrong:** Accordion inside a VBox inside a ScrollPane may not expand properly, or the Accordion's internal scrolling conflicts with the outer ScrollPane.
**Why it happens:** Accordion's preferred height is computed from its collapsed state, not its expanded state.
**How to avoid:** Set `accordion.setMaxHeight(Double.MAX_VALUE)` and let the outer ScrollPane handle the total content height. Don't nest scrollable containers.
**Warning signs:** Accordion panes appear cut off, or scrolling is jerky.

### Pitfall 4: Question IDs stored as String in ThemeStats but int in Question model
**What goes wrong:** `ThemeStats.getQuestions()` uses `Map<String, QuestionScore>` where keys are `String.valueOf(questionId)` (from `RoundResult.questionId` which is int). The `Question.id` field is also int. Need to convert consistently.
**Why it happens:** Phase 6 used `String.valueOf(result.questionId())` as the map key in `StatsService.recordRound()` (line 99). Theme questions in JSON have integer `id` fields.
**How to avoid:** When building the per-theme lowest-score list, iterate `ThemeStats.getQuestions()` entries (already String keys). When generating the AI prompt, the String key IS the question ID — use it directly.
**Warning signs:** If the prompt shows "0" instead of the question ID, or if key lookup fails, there's a type mismatch.

### Pitfall 5: ThemeStats.questions may not contain all questions
**What goes wrong:** Only questions that have been answered at least once appear in `ThemeStats.getQuestions()`. Questions never attempted won't appear in the drawer.
**Why it happens:** `computeIfAbsent` in `recordRound()` only creates entries when a question is answered.
**How to avoid:** This is actually correct behavior — the drawer should show questions that HAVE been answered and scored. Unanswered questions are irrelevant to the report. Just handle the case where a theme has 0 answered questions (show a message like "Nenhuma questão respondida ainda").
**Warning signs:** If a user expects to see all questions in a drawer but only sees some, this is the reason — and it's by design.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit Jupiter 5.11.0 |
| Config file | none — standard Maven surefire convention |
| Quick run command | `mvn test -pl . -Dtest=StatsServiceTest` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| DOM-01 | Dominio shows % of positive-score questions | unit | `mvn test -Dtest=StatsServiceTest -x` | ✅ StatsServiceTest exists (add new test) |
| DOM-02 | Reports show drawers per theme | manual-only | Visual inspection — JavaFX UI requires display | ❌ New test file needed |
| DOM-03 | Each drawer shows ≤10 lowest-scoring questions | unit | `mvn test -Dtest=StatsServiceTest -x` | ✅ Add new test for getLowestScoreQuestionsByTheme |
| DOM-04 | AI prompt copy to clipboard | manual-only | Requires JavaFX toolkit init for Clipboard API | ❌ Manual verification |
| DOM-05 | All existing tests still pass | unit | `mvn test` | ✅ All existing test files |

### Sampling Rate
- **Per task commit:** `mvn test -pl . -Dtest=StatsServiceTest`
- **Per wave merge:** `mvn test`
- **Phase gate:** Full suite green before `/gsd-verify-work`

### Wave 0 Gaps
- [ ] `StatsServiceTest.java` — add tests for `getDominio()` and `getLowestScoreQuestionsByTheme()`
- [ ] No controller/view tests exist (existing pattern: no view tests in project) — manual UAT for UI
- Framework install: none needed — JUnit 5 already configured in pom.xml

## Assumptions Log

> No claims tagged `[ASSUMED]` in this research. All findings verified against source code and official documentation.

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| (none) | — | — | — |

## Open Questions

1. **Theme labels format in ThemeSelectionView**
   - What we know: Current format is `"themeName (N perguntas) — Pontuação: X"` (line 72, ThemeSelectionView.java)
   - What's unclear: Exact format for adding dominio — same line or separate line?
   - Recommendation: Add on same line: `"themeName (N perguntas) — Pontuação: X | Domínio: Y%"` — consistent with existing layout pattern, keeps each theme as one row.

2. **ReportsView "Resumo Geral" section handling**
   - What we know: CONTEXT.md says "refocar relatório nas questões de menor pontuação" — focus on lowest-score questions
   - What's unclear: Should the "Resumo Geral" and "Desempenho por Tema" sections be removed or kept simplified?
   - Recommendation: Keep "Resumo Geral" (overall stats is useful context), remove "Desempenho por Tema" section (its info is now in the Accordion drawer headers), and replace "Questões com Menor Pontuação" flat list with the Accordion. The Accordion IS the per-theme breakdown.

3. **Dominio when no questions answered**
   - What we know: ThemeStats.getQuestions() will be empty for themes never studied
   - What's unclear: What dominio to show — "N/A" or "0%"?
   - Recommendation: Show "N/A" — consistent with how Pontuação already handles this case (ThemeSelectionView line 72 shows "N/A").

## Environment Availability

> SKIPPED — no external dependencies. All functionality uses built-in JavaFX APIs already in pom.xml.

## Sources

### Primary (HIGH confidence)
- Source code: StatsService.java (lines 129-153) — getAproveitamento(), getLowestScoreQuestions() signatures and behavior
- Source code: ThemeSelectionView.java (lines 62-85, 121-130) — setThemes() and updateAproveitamento() patterns
- Source code: ReportsView.java (lines 1-75) — full view structure, clearContent() pattern
- Source code: ReportsController.java (lines 29-65) — refresh() method, all data flow
- Source code: ThemeSelectionController.java (lines 48-53) — refreshScores() method
- Source code: StatsData.java (lines 17-58) — ThemeStats, QuestionScore structures

### Secondary (MEDIUM confidence)
- [CITED: docs.oracle.com/javafx/2/ui_controls/TitledPaneSample.java.html] — Accordion + TitledPane usage pattern
- [CITED: docs.oracle.com/en/java/java-components/javafx/21/docs/javafx.graphics/javafx/scene/input/Clipboard.html] — Clipboard + ClipboardContent API

### Tertiary (LOW confidence)
- (none — all findings verified against primary sources)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — all APIs are built-in JavaFX, confirmed in Oracle docs and project pom.xml
- Architecture: HIGH — full source code read, patterns are consistent across all existing views
- Pitfalls: HIGH — all pitfalls identified from direct code inspection, not speculation

**Research date:** 2026-07-10
**Valid until:** 2026-08-10 (JavaFX APIs are stable, no rapid changes expected)
