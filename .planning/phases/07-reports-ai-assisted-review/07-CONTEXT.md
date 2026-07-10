# Phase 7: Reports & AI-Assisted Review - Context

**Gathered:** 2026-07-10
**Status:** Ready for planning

<domain>
## Phase Boundary

Reformular a tela de seleção de temas e relatórios de desempenho para focar no sistema de pontuação da Fase 6 (QuestionScore +2/-3, -10/+5). Adicionar "domínio" (porcentagem de questões com pontuação positiva) na seleção de temas. Refocar relatório nas questões de menor pontuação por tema com seções expansíveis em estilo gaveta (limite 10). Adicionar botão de prompt IA por tema.

</domain>

<decisions>
## Implementation Decisions

### Botão de Prompt IA
- **D-01:** Prompt gerado automaticamente e copiado direto para a área de transferência (sem prévia editável), usando `javafx.scene.input.Clipboard`
- **D-02:** Conteúdo do prompt: tema sendo estudado + lista das questões de menor pontuação daquele tema, solicitando à IA que explique cada tópico em detalhes
- **D-03:** Cada seção/gaveta de tema no relatório terá um botão "Copiar prompt IA"

### the agent's Discretion
- **Cálculo do Domínio:** Decidir implementação — novo método em StatsService ou cálculo inline. Exibir na mesma linha da Pontuação ou separadamente.
- **Estrutura do Relatório:** Decidir se mantém Resumo Geral simplificado ou foca apenas nas gavetas por tema.
- **Implementação das Gavetas:** Decidir entre Accordion + TitledPane nativo do JavaFX ou VBox customizado com toggle.
- **Posição do botão de prompt:** Dentro de cada gaveta de tema ou no cabeçalho.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Requirements & Roadmap
- `.planning/ROADMAP.md` — Phase 7 goal and success criteria
- `.planning/REQUIREMENTS.md` — DOM-01..DOM-05

### Source Files (Views/Controllers)
- `src/main/java/org/IsmaelSS/view/ThemeSelectionView.java` — needs "Domínio" display alongside Pontuação
- `src/main/java/org/IsmaelSS/view/ReportsView.java` — needs drawer-style sections replacing flat theme list
- `src/main/java/org/IsmaelSS/controller/ReportsController.java` — wiring for drawer sections and per-theme data
- `src/main/java/org/IsmaelSS/controller/ThemeSelectionController.java` — wiring for domínio display

### Source Files (Model/Service)
- `src/main/java/org/IsmaelSS/service/StatsService.java` — query methods for domínio, lowest-score questions per theme
- `src/main/java/org/IsmaelSS/model/StatsData.java` — data model, QuestionScore

### Prior Phase Context
- `.planning/phases/04-reinforcement-mode-navigation-polish/04-CONTEXT.md` — prior decisions on toggle patterns and view wiring
- `.planning/phases/06-*/` — scoring system (QuestionScore, score deltas, migration)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `javafx.scene.control.Accordion` + `TitledPane` — built-in accordion/drawer component available in JavaFX, suitable for per-theme expandable sections
- `javafx.scene.input.Clipboard` — built-in clipboard API for copy-to-clipboard functionality
- `VBox`/`Label`/`Button` patterns — consistent across all existing views
- `StatsService.getLowestScoreQuestions(int limit)` — existing query, currently flat across all themes (needs per-theme variant)
- `StatsService.getAproveitamento(String theme)` — existing per-theme score query

### Established Patterns
- Programmatic views (no FXML) — all views extend from VBox with inline CSS styles
- MVC architecture — ThemeSelectionController/ReportsController/StudyRoundController pattern
- ScrollPane wrapping content VBox for scrollable screens
- `clearContent()` + rebuild pattern for refreshing reports

### Integration Points
- `ThemeSelectionController.refreshScores()` — called after round ends; should be extended to also update domínio
- `ReportsController.refresh()` — rebuilds full report content; should be restructured for drawer sections
- `StatsService` — needs per-theme lowest-score query (`getLowestScoreQuestions(themeName, limit)`)

</code_context>

<specifics>
## Specific Ideas

- Gavetas (drawers) devem ser expansíveis, uma por tema, mostrando as 10 questões de menor pontuação daquele tema
- Cada gaveta deve ter um botão "Copiar prompt IA" que gera prompt com o tema + as questões destacadas
- "Domínio" deve ser uma porcentagem: (questões com score > 0) / (total questões respondidas no tema) * 100

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope.

</deferred>

---

*Phase: 7 Reports & AI-Assisted Review*
*Context gathered: 2026-07-10*
