# NuvOS — 01. Inventário Completo e Arquitetura

**Data:** 2026-11-22  
**Branch:** `perf/total-20261122`  
**Commit base:** `9aac623`  
**Dispositivo de Teste:** Samsung Galaxy Tab S6 Lite (`SM-P620`), Android 14/15 (SDK 36), 60 Hz (16.66 ms frame budget)  

---

## 1. Mapa Quantitativo do Repositório

- **Total de arquivos rastreados no Git:** 179 arquivos
- **Total de linhas de código Kotlin:** 16.237 linhas em 49 arquivos `.kt`
- **Tamanho do APK Release (baseline pré-otimização):** 8.21 MB (R8 minificado)
- **Top 10 Maiores Fontes Kotlin:**
  1. `app/src/main/java/com/decklauncher/ui/dialogs/LauncherDialogs.kt` (3.920 linhas) — Diálogos gerais, metadados, seleção de provedores
  2. `app/src/main/java/com/decklauncher/MainActivity.kt` (1.684 linhas) — Activity central, gamepad dispatcher, ciclo de vida
  3. `app/src/main/java/com/decklauncher/ui/components/AppFolderCard.kt` (1.046 linhas) — Visualizador de pastas, grid interno, animações de pasta
  4. `app/src/main/java/com/decklauncher/ui/DeckHomeScreen.kt` (988 linhas) — Tela principal, carrosséis, hero background, Adaptive Aura
  5. `app/src/main/java/com/decklauncher/ui/dialogs/StartMenuDialog.kt` (773 linhas) — Menu de configurações do console
  6. `app/src/main/java/com/decklauncher/ui/wizard/SetupWizardScreen.kt` (575 linhas) — Wizard inicial de setup de pastas
  7. `app/src/main/java/com/decklauncher/data/IidbManager.kt` (532 linhas) — Cliente HTTP/JSON para API do iiDB
  8. `app/src/main/java/com/decklauncher/ui/dialogs/EmulatorGamesDrawer.kt` (431 linhas) — Gaveta de ROMs por emulador
  9. `app/src/main/java/com/decklauncher/ui/components/TopBar.kt` (412 linhas) — Barra superior com abas e HUD
  10. `app/src/main/java/com/decklauncher/ui/components/QuickAccessDrawer.kt` (384 linhas) — Gaveta lateral de acesso rápido
- **Maiores Ativos Binários no APK:**
  - Fontes TTF: `rajdhani_bold.ttf` (392 KB), `rajdhani_regular.ttf` (372 KB), `chakra_petch_*.ttf` (~150 KB)
  - Intros de vídeo MP4 em `assets/intros/`: `xbox_classic.mp4` (1.2 MB), `xbox_360.mp4` (740 KB), `ps2.mp4` (600 KB), `psp.mp4` (532 KB), etc. (total ~5 MB em vídeos)

---

## 2. Inventário de Arquivos e Avaliação de Riscos

| Arquivo | Resumo de 1 Linha | Riscos / Suspeitas de Desempenho |
|---|---|---|
| `MainActivity.kt` | Controlador central de ciclo de vida, input de joystick e estado | Startup bloqueado por queries repetitivas; ausência de cache em disco no primeiro frame |
| `DeckHomeScreen.kt` | Orquestrador da Home (Aura, TopBar, Carrossel, Dialogs) | Recomposição de LazyRow; overlays de gradiente com drawWithCache |
| `GameCard.kt` | Card vertical de jogo com mola harmônica e respiração especular | Decodificação de imagem via Coil; recomposição de sel/breathAlpha |
| `GameLogo3D.kt` | Renderizador de logo 3D com rotação senoidal fluida | Interpolação contínua em draw phase (`graphicsLayer`); conferir se pausa em background |
| `TopBar.kt` | Abas com cápsula deslizante e HUD com números tabulares | Polling de relógio a cada 10s via corrotina com delay |
| `GameHubSheet.kt` | Painel inferior de atividades do jogo (PS5 style) | Animações de entrada escalonada; leituras de SharedPreferences em formato de texto |
| `QuickAccessDrawer.kt` | Gaveta lateral rápida com sliders de brilho e volume | Chamadas a AudioManager e WindowManager durante navegação |
| `AppFolderCard.kt` | Renderização de pastas e carrossel de apps internos | Múltiplas instâncias de AsyncImage em grid interno |
| `IconCache.kt` | Cache LRU de 8 MB para ícones de aplicativos | Bitmap.Config.ARGB_8888; pode migrar para HARDWARE em exibição pura |
| `ThemeMusicManager.kt` | Player de áudio singleton para temas de jogos | MediaPlayer mantido em memória; deve liberar recursos estritamente em onStop |
| `SoundEffectManager.kt` | SoundPool para áudio de navegação e dynamic pitch | Alocação de stream em rolagem rápida |
| `RomScanner.kt` | Varredura de ROMs no armazenamento externo | I/O síncrono no disco se chamado repetidamente |
| `OrderManager.kt` | Ordenação de itens, histórico recente e contagem de jogos | Leitura e escrita síncrona em SharedPreferences |
| `AdaptiveAuraManager.kt` | Extrator de cores dominantes para iluminação OLED | Amostragem de Bitmap; usa LRU Cache (bom) |
| `SteamGridDbManager.kt` / `IidbManager.kt` | Clientes de rede para metadados e capas | Requisições HTTP em Dispatchers.IO |

---

## 3. Mapa Arquitetural

- **UI Stack:** 100% Jetpack Compose (BOM 2024.06.00, Compose Compiler integrado no Kotlin 2.0.21). Zero Views legadas em XML (exceto decorView de Window).
- **Gerenciamento de Estado:** State Hoisting no `MainActivity` com `mutableStateOf` e `produceState`. Sem bibliotecas externas pesadas (sem MVI redundante, sem Dagger/Hilt — DI manual e singleton managers).
- **Fluxo de Dados:** `PackageManager.queryIntentActivities` + `RomScanner` -> `CategoryManager` -> `OrderManager` -> `appsListState` -> `DeckHomeScreen` -> `GameCard`.
- **Threading:** `lifecycleScope.launch(Dispatchers.IO)` para scanning e persistência; renderização direta na UI Thread / RenderThread do Compose.
- **Caches Existentes:**
  - `IconCache`: LRU Cache de 8MB para `ImageBitmap`.
  - `Coil`: Memory Cache e Disk Cache automáticos para capas e heroes decodificados.
  - `AdaptiveAuraManager`: LRU Cache de 64 entradas para cores RGB dominantes.
- **Receivers e Listeners:**
  - `BroadcastReceiver` dinâmico para `ACTION_PACKAGE_ADDED/REMOVED` em `MainActivity`.
  - Polling com `delay(10000)` para relógio em `TopBar`.

---

## 4. Matriz de Caminhos Críticos

| ID | Cenário | Métricas Principais |
|---|---|---|
| **C1** | Cold start até o primeiro frame útil | `am start -W` (TotalTime, WaitTime), tempo até primeiro card visível |
| **C2** | Abrir a gaveta / aba de Emuladores e Apps | Transição de abas (LB/RB), tempo de resposta, janky frames |
| **C3** | Rolar o carrossel de jogos horizontalmente | `dumpsys gfxinfo` (janky frame %, p90/p95 frame time) |
| **C4** | Foco/navegação por D-pad contínuo (segurando o direcional) | Janky %, resposta de áudio, frame drops com pitch modulation |
| **C5** | Lançar um jogo / app | Tempo até intent de lançamento e animação de fade |
| **C6** | Instalar/desinstalar app (reação dinâmica) | Reatividade do BroadcastReceiver sem travamento de UI |
| **C7** | Voltar à home (onResume) | Tempo de recuperação do estado de foco e áudio |
| **C8** | Ociosidade com a tela parada | `top -b -n 1` CPU% (meta: próximo de 0%) |
| **C9** | App em segundo plano | Liberação de memória (`onTrimMemory`), áudio pausado |

---

## 5. Auditoria de Higiene do Repositório

- [x] Nenhum arquivo `local.properties` rastreado no Git.
- [x] Nenhum arquivo `*.apk`, `*.jks` ou `*.keystore` rastreado no Git.
- [x] Segredos: chaves locais lidas via `local.properties` ou SharedPreferences do app.
- [!] `gradle.properties`: faltam flags de build paralela e cache de configuração.
- [!] Primeiro frame depende de `loadInstalledApps()` em background, exibindo tela de carregamento por ~1 segundo antes dos cards.
