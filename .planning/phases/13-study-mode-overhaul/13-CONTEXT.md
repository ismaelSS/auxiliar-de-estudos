# Phase 13: Study Mode Overhaul - Context

**Gathered:** 2026-07-24
**Status:** Ready for planning
**Source:** User direct description

<domain>
## Phase Boundary

Overhaul the "Treinar" (Train) tab to become "Estudar" (Study), with:
1. Rename tab "Treinar" → "Estudar"
2. Rename "Revisar" button → "Estudar"
3. Question count selection before starting a round
4. New custom study section with theme selection via checkboxes on theme cards
5. Custom study questions count as studied (SM-2 integration)

</domain>

<decisions>
## Implementation Decisions

### Tab Rename
- "Treinar" tab label → "Estudar"
- "Revisar" button in theme cards → "Estudar"

### Question Count Selection
- Before starting a study round, show a prompt/spinner to choose number of questions
- Default: all questions in theme
- User can select 5, 10, 15, 20, or "All"

### Custom Study Section
- New subsection in the "Estudar" tab
- Theme cards display checkboxes for multi-select
- User selects themes, then starts a mixed round from all selected themes
- Questions answered in custom round are recorded via SM-2 (count as studied)

### the agent's Discretion
- UI implementation details (spinner vs dialog for question count)
- Checkbox styling (match existing dark theme)
- How custom study integrates with existing RoundState/StatsService

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Core Files
- `src/main/java/org/IsmaelSS/view/ThemeSelectionView.java` — Tab structure (jogarTab, relatoriosTab, gerenciarTab)
- `src/main/java/org/IsmaelSS/controller/ThemeSelectionController.java` — Tab wiring, handleReviewTheme
- `src/main/java/org/IsmaelSS/view/ReviewDashboardView.java` — Dashboard with theme cards
- `src/main/java/org/IsmaelSS/view/ThemeCardNode.java` — Individual theme card UI
- `src/main/java/org/IsmaelSS/service/StatsService.java` — SM-2 integration, recordRound
- `src/main/java/org/IsmaelSS/model/RoundState.java` — Round state management

### Styling
- `src/main/resources/styles/theme.css` — Existing dark theme CSS

</canonical_refs>

<specifics>
## Specific Ideas

- Checkbox on ThemeCardNode: positioned at top-right of card, visible only in custom study mode
- Custom study mode activated by a toggle/button in the Estudar tab
- Question count selector: ComboBox or dialog before round starts

</specifics>

<deferred>
## Deferred Ideas

None — full scope captured in decisions

</deferred>

---

*Phase: 13-study-mode-overhaul*
*Context gathered: 2026-07-24 via user description*
