# Phase 12: Spaced Repetition & Visual Review Dashboard - Context

**Gathered:** 2026-07-21
**Status:** Ready for planning

<domain>
## Phase Boundary

Implement SM-2 spaced repetition algorithm with named fixation phases, and a visual review dashboard that replaces the "Jogar" tab. The dashboard shows all themes with priority ordering (overdue > due today > no immediate review > alphabetical), color-coded priority (red/orange/green), search by theme name, action buttons per theme (start review / mark as done), auto-recording of reviews on round completion, and a vertical timeline showing study history with fixation phases and next review dates.

</domain>

<decisions>
## Implementation Decisions

### Dashboard Placement
- Replaces the "Jogar" tab in the TabPane (home screen)
- The dashboard becomes the main screen: theme list with priorities, search bar, timeline section
- Starting a review from the dashboard creates a reinforcement round within the existing StudyRound flow (separate screen via ScreenController)
- After round completes, return to dashboard with updated data

### SM-2 Algorithm Parameters
- **Quality model:** Binary correct/wrong — `correct→4`, `wrong→1` (no self-assessment UI)
- **Default easeFactor:** 2.5 (min 1.3, max 2.5)
- **Default interval:** 0 (unreviewed)
- **First review:** interval = 1 day
- **Correct:** `interval = ceil(interval * easeFactor)`; `easeFactor += 0.1` (max 2.5); `repCount++`
- **Wrong:** `interval = 1`; `easeFactor -= 0.2` (min 1.3); `repCount = 0`
- **Score (-10..+5) removed** from `recordCorrect/recordWrong` logic — replaced by SM-2 fields (`easeFactor`, `interval`, `repCount`, `lastReviewTimestamp`, `nextReviewTimestamp`)
- Existing `score` field in JSON can remain (backward compat) but is no longer updated

### Fixation Phases (derived from SM-2 data)

| Phase | Condition | Display |
|-------|-----------|---------|
| **Aprendendo** | New or last answer was wrong (repCount = 0 or interval <= 1) | Red badge |
| **Revisão** | 2-3 consecutive correct (repCount 1-2, interval 2-7 days) | Orange badge |
| **Fixa** | 4-6 consecutive correct (repCount 3-5, interval 8-30 days) | Green badge |
| **Domínio** | 7+ consecutive correct (repCount >= 6, interval > 30 days) | Blue/teal badge |

Fixation phase is computed from SM-2 fields — NOT stored separately.

### Theme Card Layout
Each theme card shows:
- Theme name (bold, white text)
- ⏰ Overdue count (orange badge if > 0)
- Domínio percentage
- Fixation phase distribution bar (4 segments: Aprendendo/Revisão/Fixa/Domínio with proportional widths)
- Action buttons: **Revisar** (starts review round with due questions) and **Feito** (marks all overdue as perfect)

### "Marcar como feita" Behavior
- Simulates a perfect SM-2 answer (quality=5) for all overdue questions in that theme
- `interval` advances as if answered correctly; `easeFactor += 0.1`
- Intended as "I already know this topic" shortcut
- Does NOT affect questions that are not overdue

### "Iniciar Revisão" Behavior
- Creates a round with only overdue questions (`nextReview <= now`) for the selected theme
- If no overdue questions: creates a round with new/unreviewed questions (`repCount == 0`)
- Uses existing study round screen (StudyRoundView + StudyRoundController)
- Results are auto-recorded on round completion/exit (existing `recordRound` flow)

### Theme Ordering
1. Overdue (nextReview < now) — red indicator
2. Due today (nextReview == today) — orange indicator
3. No immediate review (nextReview > today or never reviewed) — green indicator
4. Within each group: alphabetical by theme name

### Priority Colors
- **Vermelho (#e74c3c):** Overdue (past nextReview)
- **Laranja (#fe9a00):** Due today (nextReview == today)
- **Verde (#27ae60):** No immediate review (nextReview > today or never reviewed)
- Card left border or full background tint using the priority color

### Search
- Filters themes by name only (real-time, as user types)
- No search across question text

### Timeline
- Vertical layout, grouped by day (most recent first)
- Each day entry shows which themes were studied, how many questions, and fixation phase distribution at that time
- Style: date on left, vertical line with dots, entries on right (GitHub contribution log style)
- Shows all available history (no depth limit)

### Auto-Recording
- Completing a study round (via existing StudyRoundController flow) automatically records results with SM-2 update
- Manual "Feito" button is an ADDITIONAL mechanism — both paths count as review
- The `recordRound` method in StatsService is updated to call SM-2 algorithm instead of old score deltas

### the agent's Discretion
- Exact CSS classes for theme cards (follow `.surface`, `.button-primary` etc. from Phase 10)
- Timeline visual details (dot color, line style, date format)
- Fixation phase badge colors for Domínio (suggest teal/blue)
- Card dimensions, spacing, responsive behavior
- Whether to show the timeline inline on dashboard or as expandable section
- Transition animation when filtering by search

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Data Model & Services
- `src/main/java/org/IsmaelSS/model/StatsData.java` — `QuestionScore` (needs SM-2 fields), `ThemeStats`, `OverallStats`
- `src/main/java/org/IsmaelSS/service/StatsService.java` — `recordRound()`, query methods (needs SM-2 update, `getDueQuestions()`)
- `src/main/java/org/IsmaelSS/model/RoundResult.java` — result record (may need review timestamp)
- `src/main/java/org/IsmaelSS/model/RoundState.java` — `createReinforcementRound()` (needs due-date selection)

### Controllers & Views
- `src/main/java/org/IsmaelSS/controller/StudyRoundController.java` — round flow, `handleExit()` calls `recordRound()`
- `src/main/java/org/IsmaelSS/controller/ScreenController.java` — scene/zoom management
- `src/main/java/org/IsmaelSS/view/ThemeSelectionView.java` — will be replaced/refactored into dashboard

### Styling
- `src/main/resources/styles/theme.css` — CSS palette: `.background` (#020817), `.button-primary` (#fe9a00), `.surface` (#1a1a2e), etc.

### Prior Phase Contexts
- `.planning/phases/11-question-file-manager/11-CONTEXT.md` — TabPane pattern, lazy-init, MVC conventions
- `.planning/phases/10-visual-theming/10-CONTEXT.md` — color identity, CSS classes
- `.planning/phases/08-resizable-window-ctrl-scroll-zoom/08-CONTEXT.md` — zoom via root font-size

### Requirements & Roadmap
- `.planning/ROADMAP.md` — Phase 12 entry
- `.planning/REQUIREMENTS.md` — V2-01 (Spaced repetition algorithm SM-2)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- **StatsService.java** — existing recording and query infrastructure; needs SM-2 methods added
- **RoundState.java** — reinforcement round constructor; needs due-date-based selection
- **ScreenController.java** — scene registration, zoom management, CSS loading
- **theme.css** — dark theme classes ready for new dashboard components
- **ThemeLoader.java** — JSON I/O pattern; may need `loadAllThemes()` for dashboard

### Established Patterns
- **MVC:** View wraps JavaFX nodes, Controller handles logic, Service for I/O
- **TabPane + lazy-init:** Tabs created on first click (from Phase 11)
- **Jackson persistence:** ObjectMapper shared for JSON read/write
- **Scene-based navigation:** Separate screens via ScreenController (study round remains separate)
- **CSS class naming:** `.background`, `.surface`, `.button-primary`, `.title`, `.accent`, `.correct`, `.wrong`

### Integration Points
- `ThemeSelectionView.java` — becomes the dashboard (TabPane "Jogar" tab content)
- `ThemeSelectionController.java` — manages new dashboard logic (search, ordering, action buttons, timeline)
- `StatsService.java` — new methods: `updateWithSM2()`, `getDueQuestions(theme)`, `getDueCount(theme)`, `getFixationPhases()`
- `RoundState.java` — new factory method or constructor for due-only review rounds
- `QuestionScore` (in StatsData) — new fields: `easeFactor`, `interval`, `repCount`, `lastReviewTimestamp`, `nextReviewTimestamp`

</code_context>

<specifics>
## Specific Ideas

- Red/orange/green priority colors from user: #e74c3c (overdue), #fe9a00 (today), #27ae60 (no review)
- Timeline style inspired by GitHub contribution log: vertical, grouped by day, dots on timeline
- Card layout must include: name, ⏰ count, dominio%, fixation phase bar, action buttons
- "Marcar como feita" = perfect SM-5 quality simulation
- Dashboard replaces "Jogar" tab — no separate Reviews tab

</specifics>

<deferred>
## Deferred Ideas

- UI de autoavaliação 1-5 (tipo Anki) para quality granular — fase separada
- Limite de revisões/dia — fase separada
- Estatísticas com gráficos (V2-02) — fase separada
- Search across question text — não necessário na fase atual

</deferred>

---

*Phase: 12-spaced-repetition-visual-review-dashboard*
*Context gathered: 2026-07-21*
