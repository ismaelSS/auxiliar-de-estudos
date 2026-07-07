# Phase 5: Error Handling & UX Refinements — UI-SPEC

## Design System (Existing, to be externalized to CSS)

### Current State
- All views use inline styles via `setStyle()` calls
- No external CSS file; every style is hardcoded per component

### Target State — External CSS

**File:** `src/main/resources/org/IsmaelSS/style.css`

| Element | Property | Value |
|---------|----------|-------|
| Root VBox | -fx-padding | 20 |
| Title Labels | -fx-font-size | 18px; -fx-font-weight: bold |
| Section Labels | -fx-font-size | 14px |
| Feedback/Error | -fx-text-fill | red |
| Correct Answer | -fx-background-color | #c8e6c9 (light green) |
| Wrong Answer | -fx-background-color | #ffcdd2 (light red) |
| Buttons | -fx-font-size | 14px; -fx-padding | 8 16 |
| Spinner | -fx-font-size | 14px |

### Error Handling Patterns

| Scenario | UI Pattern | Description |
|----------|-----------|-------------|
| Malformed JSON | Alert (ERROR) | Skip theme file, show Alert with filename and error detail |
| Empty themes folder | Inline message | Same as current "Nenhum tema encontrado" but styled |
| Stats file corrupt | Alert (WARNING) | Silently reset stats, show non-blocking warning |
| File I/O read failure | Alert (ERROR) | Show error with details, app continues with partial data |
| File I/O write failure | Alert (WARNING) | Log warning, show non-blocking alert, app continues |
| General unexpected error | Alert (ERROR) | Show error message, app doesn't crash |

### Layout Rules
- All screens: min width 600, min height 400
- Theme selection: VBox with scrollable theme list
- Study round: centered question, options as VBox of buttons, feedback overlay
- Reports: VBox with sections separated by spacing, scrollable if needed

### Color Palette (JavaFX CSS)
- Background: white/transparent (default)
- Primary button: System default (no custom color)
- Correct: #c8e6c9 (light green) — background
- Wrong: #ffcdd2 (light red) — background
- Error text: red (standard JavaFX)
