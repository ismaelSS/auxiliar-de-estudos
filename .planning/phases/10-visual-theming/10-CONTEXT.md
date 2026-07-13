# Phase 10: Visual Theming & Color Identity - Context

**Gathered:** 2026-07-13
**Status:** Ready for planning
**Source:** User design requirements

<domain>
## Phase Boundary

Aplicar identidade visual consistente em toda a aplicação: fundo escuro (#020817), destaque laranja (#fe9a00), texto branco para contraste, e consistência visual em todas as telas via CSS externo. Substituir estilos inline espalhados pelo código por uma folha de estilo centralizada.

</domain>

<decisions>
## Implementation Decisions

### Mandatory Colors (do NOT change)
- **Background:** `#020817` (very dark navy) — fundo principal de todas as telas
- **Accent / Primary:** `#fe9a00` (orange/gold) — botões, destaques, elementos interativos, bordas de foco
- **White:** `#ffffff` — texto principal sobre fundo escuro
- **Black:** `#000000` — texto sobre superfícies claras (se houver)

### Complementary Palette (agent's suggestion)
- **Surface / Cards:** `#1a1a2e` — containers, cards, painéis (contraste sutil com o fundo)
- **Hover state:** `#ffb340` — hover de botões e links (laranja mais claro)
- **Pressed state:** `#cc7c00` — active/pressed de botões (laranja mais escuro)
- **Secondary text:** `#e0e0e0` — textos secundários, labels menos importantes
- **Borders / Dividers:** `#333355` — separadores, bordas de containers
- **Error:** `#e74c3c` — vermelho para erro (padrão)
- **Success:** `#27ae60` — verde para acerto (padrão)

### Architecture Decisions
- **External CSS:** Criar arquivo `src/main/resources/styles/theme.css` carregado via `scene.getStylesheets().add()`
- **No inline styles:** Remover todos os `setStyle()` das views, mover para CSS
- **CSS class names:** Usar classes semânticas (`.background`, `.button-primary`, `.title`, `.label`, `.accent`)
- **Scene registration:** Carregar CSS no `ScreenController.registerScreen()` ao registrar cada cena
- **Zoom compatibility:** CSS `-fx-font-size` still applied via root inline (zoom feature, Phase 8) — CSS variables or root class for font scaling
- **Preserve JavaFX selector specificity:** CSS external tem precedência sobre inline apenas quando inline não define a mesma propriedade

### the agent's Discretion
- Efeitos de hover e transição (ex: botão escurece/clareia no hover)
- Tipografia exata (font-family, font-weight, sizes)
- Spacing/padding values específicos
- Se usar CSS variables (`-fx-*-*`) ou classes tradicionais
- Gradientes ou sombras para profundidade
</decisions>

<canonical_refs>
## Canonical References

### Views com estilos inline atuais
- `src/main/java/org/IsmaelSS/view/ThemeSelectionView.java` — background, título, labels, botões
- `src/main/java/org/IsmaelSS/view/ReportsView.java` — background, título, seções, Accordion
- `src/main/java/org/IsmaelSS/view/StudyRoundView.java` — background, progresso, questão, feedback, completion
- `src/main/java/org/IsmaelSS/view/ThemeSelectionView.java:25-60` — estilos inline atuais

### Controllers
- `src/main/java/org/IsmaelSS/controller/ScreenController.java` — registerScreen (onde carregar CSS), zoom

### Phase 8 zoom feature
- `.planning/phases/08-resizable-window-ctrl-scroll-zoom/08-CONTEXT.md` — zoom aplica `-fx-font-size` via root inline style

### Estilos existentes
- `src/main/java/org/IsmaelSS/Main.java` — stage title e configuração inicial
</canonical_refs>

<specifics>
## Specific Ideas

- Create theme.css with:
  ```css
  .background { -fx-background-color: #020817; }
  .surface { -fx-background-color: #1a1a2e; }
  .button-primary { -fx-background-color: #fe9a00; -fx-text-fill: white; }
  .button-primary:hover { -fx-background-color: #ffb340; }
  .button-primary:pressed { -fx-background-color: #cc7c00; }
  .title { -fx-text-fill: white; -fx-font-weight: bold; }
  .label { -fx-text-fill: #e0e0e0; }
  .accent { -fx-text-fill: #fe9a00; }
  .correct { -fx-background-color: #27ae60; }
  .wrong { -fx-background-color: #e74c3c; }
  ```
- Carregar CSS em ScreenController.registerScreen() uma única vez
- Remover progressivamente `setStyle()` de cada view
- Garantir que zoom via root inline continue funcionando (CSS e zoom não conflitam: zoom mexe em `-fx-font-size` no root, CSS cuida do resto)
</specifics>

<deferred>
## Deferred Ideas
- Modo claro/escuro toggle (futuro)
- Temas customizáveis pelo usuário (futuro)
- Animações complexas de transição de tema (futuro)
</deferred>

---

*Phase: 10-visual-theming*
*Context gathered: 2026-07-13 via user design requirements*
