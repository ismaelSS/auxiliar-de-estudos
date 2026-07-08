# Phase 6 Validation: Scoring System & Question ID Rework

## Nyquist Validation

### Req ID → Test Mapping

| Req ID | Verification | Type | Status |
|--------|-------------|------|--------|
| SCORE-01 | Question.java has `id` field; theme JSONs annotated; RoundResult has `questionId` | Compile + Unit | Planned |
| SCORE-02 | Wrong = -3, floor -10 (StatsDataTest.questionScoreRecordWrongFloorsAtMinusTen) | Unit | Planned |
| SCORE-03 | Correct = +2, cap +5 (StatsDataTest.questionScoreRecordCorrectCapsAtFive) | Unit | Planned |
| SCORE-04 | Aproveitamento display replaces hit rate (weight formula tests) | Unit + Manual | Planned |
| SCORE-05 | Integer weight: negative -3, zero 0, positive +2 | Unit | Planned |
| REINF-01 | Reinforcement round selects questions with lowest score first | Unit | Planned |
| REINF-02 | Migration preserves theme totals from old format | Unit | Planned |

### Test Coverage

| Test File | Test Count | Req Mapping |
|-----------|-----------|-------------|
| StatsDataTest.java | 7 | QuestionScore bounds, serialization, defaults |
| StatsServiceTest.java | 13 | Scoring, aproveitamento, lowest-score ranking, migration, persistence |
| RoundStateReinforcementTest.java | 8 | ID-based reinforcement selection, multi-theme, edge cases |

### Verification Gates

- `mvn clean compile` — zero errors
- `mvn test` — all 28+ tests green
- Manual: Play round → verify score updates in theme selection ("Pontuação")
- Manual: Toggle reinforcement → verify lowest-score questions selected first
- Manual: Check reports → "Questões com Menor Pontuação" shows correct data
- Manual: Old `flashcard-stats.json` → migration preserves theme totals (verified via unit test)
