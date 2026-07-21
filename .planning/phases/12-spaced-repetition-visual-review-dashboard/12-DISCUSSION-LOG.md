# Phase 12: Spaced Repetition & Visual Review Dashboard - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-07-21
**Phase:** 12-spaced-repetition-visual-review-dashboard
**Areas discussed:** Dashboard placement, Mark-as-done behavior, Timeline format, Search scope, Card layout, Review action, Priority colors

---

## Dashboard Placement

| Option | Description | Selected |
|--------|-------------|----------|
| Substituir aba Jogar | Dashboard vira tela inicial com temas + prioridades + busca | ✓ |
| 4ª aba 'Revisões' | Manter abas existentes + nova aba | |
| Seção expandida no Jogar | Jogar atual ganha seção de revisões pendentes | |

**User's choice:** Substituir aba Jogar
**Notes:** Dashboard replaces Jogar tab. Traditional theme selection/count integrated into dashboard flow.

---

## Mark-as-Done Behavior

| Option | Description | Selected |
|--------|-------------|----------|
| Simular resposta perfeita | Quality=5 no SM-2: máximo intervalo, easeFactor+=0.1 | ✓ |
| Apenas adiar | Avança nextReview para amanhã sem alterar algoritmo | |
| Pular a questão | Remove das pendentes sem registrar nada | |

**User's choice:** Simular resposta perfeita
**Notes:** "I already know this topic" shortcut. Only affects overdue questions.

---

## Timeline Format

| Option | Description | Selected |
|--------|-------------|----------|
| Timeline vertical por data | Marcadores de data, agrupado por dia | ✓ |
| Grade calendário | Grid estilo GitHub contributions | |
| Lista simples por tema | Cada tema expande para mostrar histórico | |

**User's choice:** Timeline vertical por data
**Notes:** Agrupado por dia, cada entrada mostra tema + qtd respondida + fases. Estilo GitHub contribution log.

---

## Search Scope

| Option | Description | Selected |
|--------|-------------|----------|
| Só nome do tema | Filtra temas pelo nome | ✓ |
| Nome + texto das questões | Busca também dentro das questões | |

**User's choice:** Só nome do tema
**Notes:** Filtragem em tempo real enquanto digita.

---

## Card Layout

| Option | Description | Selected |
|--------|-------------|----------|
| Nome + ⏰ + Domínio% | Compacto, foco na urgência | |
| Nome + ⏰ + Domínio + Fases + Botões | Completo com barra de fases e ações | ✓ |
| Nome + ⏰ + Domínio + Botões | Intermediário sem barra de fases | |

**User's choice:** Nome + ⏰ + Domínio + Fases + Botões
**Notes:** Cada card mostra: nome, ⏰ vencidas, domínio%, barra de fases, botões Revisar e Feito.

---

## Review Action

| Option | Description | Selected |
|--------|-------------|----------|
| Só questões vencidas | Round só com nextReview <= now. Se não houver, puxa novas | ✓ |
| Mix: vencidas + novas | Prioriza vencidas + completa cota com novas | |
| Round completo do tema | Todas as questões, vencidas primeiro | |

**User's choice:** Só questões vencidas
**Notes:** Se não houver vencidas no tema, puxa questões nunca revisadas.

---

## Priority Colors

| Option | Description | Selected |
|--------|-------------|----------|
| Vermelho/Laranja/Verde | #e74c3c/#fe9a00/#27ae60 | ✓ |
| Laranja escuro/médio/claro | Variações do laranja principal | |

**User's choice:** Vermelho/Laranja/Verde
**Notes:** Vermelho = vencida, Laranja = hoje, Verde = sem revisão.

---

## the agent's Discretion

- CSS classes for theme cards (follow Phase 10 patterns)
- Timeline visual details (dot color, line style, date format)
- Fixation phase badge color for Domínio (suggest teal/blue)
- Card dimensions, spacing, responsive behavior
- Whether timeline is inline or expandable section
- Search transition animation

## Deferred Ideas

- UI de autoavaliação 1-5 (tipo Anki) — fase separada
- Limite de revisões/dia — fase separada
- Estatísticas com gráficos (V2-02) — fase separada
- Search across question text
