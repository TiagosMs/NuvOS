# Relatório NuvOS v1.1 — Checkpoint 2 (🛑)

**Data:** 2026-11-20  
**Branch:** `feat/v1.1`  
**Dispositivo de Teste:** Samsung Galaxy Tab S6 Lite 2024 (`SM-P620` / `RX2X700QMPP`, Android 14)  
**Commits Deste Bloco:**
- `9a6a72e`: feat(fase3): fontes Chakra Petch/Rajdhani, tokens neutros, StartMenu com 3 abas e ItemMenu com confirmacao
- `76080bb`: feat(fase4): suporte a toque completo, bottom bar tocavel e engrenagem de configuracoes

---

## 1. Fase 3 — Fundação Visual e Menus

1. **Tipografia e Fontes OFL (D6 - T3.3):**
   - Fontes locais embutidas em `app/src/main/res/font/`:
     - `chakra_petch_regular.ttf` e `chakra_petch_bold.ttf` (títulos, abas e botões de ação).
     - `rajdhani_regular.ttf` e `rajdhani_bold.ttf` (corpo de texto, legendas e descrições).
   - Tipografia oficial unificada em [Type.kt](file:///home/tiago/dev/projetos/deck-launcher/app/src/main/java/com/decklauncher/ui/theme/Type.kt) e aplicada globalmente através de `DeckLauncherTheme` em [Theme.kt](file:///home/tiago/dev/projetos/deck-launcher/app/src/main/java/com/decklauncher/ui/theme/Theme.kt).
   - Criado [THIRD_PARTY_LICENSES.md](file:///home/tiago/dev/projetos/deck-launcher/THIRD_PARTY_LICENSES.md) com os textos das licenças SIL Open Font License 1.1.
   - Suporte verificado para todos os acentos e caracteres pt-BR (`Ação`, `Configurações`, `Navegação`, `Início`, `ç ã õ é ê í ó ú Á É Í Ó Ú`).

2. **Design Tokens e Botões Neutros (D5 - T3.2):**
   - Criados tokens de cor padronizados: `NeutralBtnBg`, `NeutralBtnBgFocused`, `NeutralBtnText`, `NeutralGlyphBg`, `FocusRing`, `ScrimColor`, `DangerBtnBg`, `DangerBtnText`.
   - Botões de gamepad (A/B/X/Y) e linhas de ação migrados para cinza do tema.
   - Foco visual indicado por borda branca contrastante (`FocusRing`), atendendo aos critérios de contraste (> 4,5:1).
   - Diálogo [ConfirmActionDialog.kt](file:///home/tiago/dev/projetos/deck-launcher/app/src/main/java/com/decklauncher/ui/dialogs/ConfirmActionDialog.kt) implementado para todas as ações destrutivas (Desinstalar app, Remover da tela inicial, Excluir metadados).

3. **Arquitetura de Menus e Sobreposições (D1, D2, D3 - T3.1 / T3.4):**
   - Definida a interface unificada de estados [Overlay.kt](file:///home/tiago/dev/projetos/deck-launcher/app/src/main/java/com/decklauncher/model/Overlay.kt).
   - **StartMenu com 3 Abas:** criado [StartMenuDialog.kt](file:///home/tiago/dev/projetos/deck-launcher/app/src/main/java/com/decklauncher/ui/dialogs/StartMenuDialog.kt) com abas **Biblioteca**, **Aparência e Navegação**, **Bancos de Dados**. Otimizado para compor exclusivamente a aba ativa.
   - **ItemMenu Contextual:** criado [ItemMenuDialog.kt](file:///home/tiago/dev/projetos/deck-launcher/app/src/main/java/com/decklauncher/ui/dialogs/ItemMenuDialog.kt), separando completamente as ações contextuais de item das opções globais de configuração. Removido "Mover posição" do Start Menu (mantido no Item Menu).

---

## 2. Fase 4 — Suporte a Toque Completo (D10)

1. **Semântica de Toque em Cards e Carrossel (T4.1):**
   - Toque curto em card focado executa o app/jogo; toque em card desfocado seleciona e centraliza.
   - Pressionamento longo (long-press) abre diretamente o menu do item ([ItemMenuDialog.kt](file:///home/tiago/dev/projetos/deck-launcher/app/src/main/java/com/decklauncher/ui/dialogs/ItemMenuDialog.kt)).
   - Deslize por toque rola a lista com snap suave e inércia nativa do Compose.

2. **TopBar e BottomBar Tocáveis (T4.1 / T4.2):**
   - Adicionada engrenagem de configurações tocável (⚙) no canto superior direito da TopBar para acesso imediato ao Start Menu via toque.
   - Pílula de perfil e abas do topo (Jogos, Emuladores, Apps) totalmente clicáveis por toque.
   - Transformação dos prompts da BottomBar em botões interativos tocáveis (`onPrimaryAction`, `onOptionsAction`, `onAddGameAction`, `onBackAction`) com `role = Role.Button` e dimensões mínimas de toque (alvo ≥ 48 dp).
   - Todos os novos diálogos possuem botão de fechar visível e fechamento ao tocar na área de scrim.

---

## 3. Medições e Performance (Budget vs Real)

| Métrica | Baseline | Checkpoint 1 | Checkpoint 2 (Real) | Budget / Meta |
|---|---|---|---|---|
| **Cold Start (Mediana 5x)** | 261 ms | 258 ms | **283 ms** (272, 283, 285, 321, 399 ms) | ≤ 700 ms (Budget: 900 ms) |
| **Memória (PSS Total)** | 180 MB | 180 MB | **168 MB** (TOTAL PSS: 172.953 KB) | < 250 MB (Budget: 300 MB) |
| **Janky Frames (60s touch/swipe)** | 0,00% | 0,00% | **0,00%** (0 / 172 frames) | < 2,0% (Budget: 5,0%) |
| **Frame Time (p95 / GPU p95)** | 25 ms / 21 ms | 25 ms / 21 ms | **25 ms / 21 ms** | p95 ≤ 20-25 ms |
| **Tamanho do APK (Release)** | 6,9 MB | 6,8 MB | **7,2 MB** (+400 KB pelas 4 fontes OFL) | < 10 MB (Meta cumprida com folga) |
| **check-rules.sh** | OK | OK | **✅ check-rules OK** | 0 violações |
| **Testes Unitários** | - | 3 passing | **3 passing** | 100% sucesso |

---

## 4. Capturas de Tela de Evidência

Evidências coletadas no dispositivo Galaxy Tab S6 Lite:
- [checkpoint2_home.png](file:///home/tiago/dev/projetos/deck-launcher/docs/screens/checkpoint2_home.png)
- [checkpoint2_itemmenu.png](file:///home/tiago/dev/projetos/deck-launcher/docs/screens/checkpoint2_itemmenu.png)
- [checkpoint2_startmenu.png](file:///home/tiago/dev/projetos/deck-launcher/docs/screens/checkpoint2_startmenu.png)
