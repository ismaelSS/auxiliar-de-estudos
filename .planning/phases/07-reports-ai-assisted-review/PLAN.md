# Phase 7: Reports & AI-Assisted Review — Plan

## Wave Structure

| Wave | Plan | Objective | Dependencies |
|------|------|-----------|--------------|
| 1 | [07-01](./07-01-PLAN.md) | StatsService: `getDominio()` + `getLowestScoreQuestionsByTheme()` + tests | — |
| 2 | [07-02](./07-02-PLAN.md) | ThemeSelection dominio display + controller wiring | 07-01 |
| 2 | [07-03](./07-03-PLAN.md) | Reports Accordion drawers + AI prompt clipboard copy | 07-01 |

## Execution Order

```
Wave 1:  [07-01] ────────────────────┐
                                     ├──→ Wave 2: [07-02] (parallel)
                                     └──→ Wave 2: [07-03] (parallel)
```

All plans are autonomous (`type: execute`). Plans 07-02 and 07-03 share zero files and execute in parallel after 07-01 completes.

## Requirements Coverage

| Req | Description | Plan |
|-----|-------------|------|
| DOM-01 | Theme selection shows "Domínio: X%" | 07-01, 07-02 |
| DOM-02 | Reports focused on lowest-scoring questions per theme | 07-03 |
| DOM-03 | Drawer-style expandable sections per theme (≤10 questions) | 07-01, 07-03 |
| DOM-04 | "Copiar prompt IA" button per theme | 07-03 |
| DOM-05 | All existing tests still pass | All |

## Threat Model Summary

See individual plans for full threat registers. Key accept risks:
- **T-07-01**: Tampering with `flashcard-stats.json` — accept (local file, user-owned)
- **T-07-05**: Clipboard information disclosure — accept (question IDs/scores only, user-initiated)
- **T-07-06/07**: DoS on Accordion — accept (bounded by limit=10, finite themes)

## Verification

1. `mvn test` — full suite green (DOM-05)
2. `mvn compile` — code compiles without errors
3. Each plan has per-task verification steps
