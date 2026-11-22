# Relatório de Beta Test & Auditoria Técnica de Qualidade
## Sistema: Deck Launcher (NuvOS) — Versão 1.1 (Branch `feat/v1.1`)
**Perfil do Testador:** Beta Tester / Engenheiro de QA & Performance  
**Plataforma de Teste:** Snapdragon 865 (Kryo 585 Octa-core, Adreno 650), Display OLED 120Hz (taxa de atualização de 8.33ms por quadro), Controle Físico Integrado/Gamepad Bluetooth (Layout padrão Xbox/PlayStation/Switch).  
**Data:** 22 de Novembro de 2026  
**Status do Projeto:** Análise Estática, UX, Acessibilidade de Hardware e Framerate

---

## 1. Sumário Executivo & Diagnóstico do Dispositivo

O **NuvOS / Deck Launcher** apresenta uma proposta visual e arquitetural ambiciosa, com excelente direcionamento para emulação e jogos retrô, transições fluidas e um ecossistema completo de extração de metadados (SteamGridDB, iiDB/iiSU).
f
Contudo, ao submeter o sistema a uma análise minuciosa sob a perspectiva de um jogador com **Snapdragon 865 + Tela OLED 120Hz + Gamepad Físico**, foram identificados pontos críticos que quebram a imersão de um console portátil dedicado (*handheld console experience*):

1. **Gaps Graves de Controle Físico (Gamepad Trap):** Múltiplos diálogos modais e menus de configuração não possuem escuta de D-Pad nem foco navegável, forçando o jogador a tocar na tela para sair de telas ou selecionar itens. No menu de configurações principal (`StartMenuDialog`), o D-Pad navega apenas nas abas superiores, sem permitir descer para os itens de configuração.
2. **Gargalos de GPU a 120Hz (Frame Budget de 8.33ms):** O uso de múltiplos passes de `Modifier.blur()` aplicados em tela cheia em tempo real sobre imagens em alta resolução satura a taxa de preenchimento (*fillrate*) da GPU Adreno 650, provocando quedas bruscas de quadros (*micro-stutters* / *jank*) durante a rolagem rápida do carrossel.
3. **Código Morto e Dissonância de UX:** Mais de 570 linhas de código legado órfão (`LauncherSettingsDialog`), prompts de botões no `BottomBar` que não correspondem à ação executada (ex: prompt "Ver Jogos" na aba Emuladores que abre o executável direto) e falhas lógicas no filtro de categorias que suprimem a tela de estado vazio (*empty state*).
4. **Ausência de Otimizações OLED:** Falta de modo preto absoluto (`#000000`) para desativação física dos subpixels OLED e ausência de mecanismos de proteção contra *burn-in* nos elementos estáticos fixos da interface (TopBar e BottomBar).

---

## 2. Matriz Consolidada de Problemas e Inconsistências

| ID | Categoria | Severidade | Descrição do Problema | Arquivo / Componente |
|---|---|---|---|---|
| **BUG-01** | 🐛 Bug / Bloat | **Alta** | ~580 linhas de código morto órfão (`LauncherSettingsDialog`) não referenciado | `LauncherDialogs.kt` |
| **BUG-02** | 🎮 Controle | **Crítica** | `ConfirmActionDialog` não possui navegação D-pad nem interceptação de tecla B; fecha diálogos pais no `MainActivity` | `ConfirmActionDialog.kt`, `MainActivity.kt` |
| **BUG-03** | 🎮 Controle | **Crítica** | Gamepad Trap em `SteamGridApiKeyDialog` e `IidbApiKeyDialog`: sem foco, sem D-pad, sem cancelamento por controle | `LauncherDialogs.kt` |
| **BUG-04** | 🎮 Controle | **Alta** | `ChangeAvatarDialog` não permite focar nem escolher avatares via D-Pad ou botão A | `LauncherDialogs.kt` |
| **BUG-05** | 🎨 UX / Lógica | **Alta** | `BottomBar` exibe "A: Ver Jogos" na aba Emuladores, mas pressionar 'A' inicia o app direto | `BottomBar.kt`, `MainActivity.kt` |
| **BUG-06** | ⚡ Performance | **Média** | `iconpack_prefs` limpo desnecessariamente em todo `onResume()` da Activity | `MainActivity.kt` |
| **BUG-07** | 🐛 Bug | **Média** | `steamGridBroadcastReceiver` instanciado mas nunca registrado/desregistrado nos ciclos de vida | `MainActivity.kt` |
| **BUG-08** | 🐛 Bug / Concorrência | **Alta** | Race condition no `BootIntroPlayer`: `isFinished` sem thread-safety causa disparos duplos de inicialização | `BootIntroPlayer.kt` |
| **BUG-09** | 🎨 UX / Visual | **Média** | `BootIntroPlayer` estica vídeos de intro 4:3 (PS1, PS2) para preencher a tela inteira sem preservar aspect ratio | `BootIntroPlayer.kt` |
| **BUG-10** | 🐛 Bug / Metadados | **Alta** | `EmulatorGamesDrawer` busca assets no iiDB passando `rom.id.hashCode().toLong()` em vez do ID de busca do jogo | `EmulatorGamesDrawer.kt` |
| **UX-01** | 🎮 Controle | **Crítica** | `StartMenuDialog` só aceita D-Pad Left/Right nas abas superiores; impossível descer para alterar configurações | `StartMenuDialog.kt` |
| **UX-02** | 🎮 Controle | **Alta** | Diálogos de Seleção de Arte (`EditCoverSteamGridDialog`, etc.) não possuem navegação na grade por D-Pad | `LauncherDialogs.kt` |
| **UX-03** | 🔊 Áudio | **Baixa** | Ausência de efeitos sonoros (`SoundEffectManager.playNavMove()`) ao navegar itens dentro de diálogos | `ItemMenuDialog.kt`, `OptionsDialog` |
| **UX-04** | 🎮 Controle | **Média** | Pulo de Intro desbalanceado: touch exige segurar 500ms com barra de progresso, mas controle físico corta instantâneo no `ACTION_DOWN` | `MainActivity.kt`, `BootIntroPlayer.kt` |
| **UX-05** | 🎨 UX / Lógica | **Média** | `getFilteredApps()` retorna todos os apps do sistema quando a categoria está vazia, quebrando o Empty State | `MainActivity.kt` |
| **UX-06** | 🎨 UX / Visual | **Baixa** | Conflito de foco: quando `focusArea == TOP_BAR`, o carrossel mantém arte de fundo sem indicar inatividade visual evidente | `DeckHomeScreen.kt` |
| **UX-07** | 🎨 UX / Design | **Baixa** | String de perfil estática `@Without_` em layout herdado | `LauncherDialogs.kt` |
| **PERF-01** | ⚡ Performance | **Alta** | `CategoryManager.groupIntoFolders()` executado síncrono a cada toque de navegação e confirmação no UI Thread | `MainActivity.kt` |
| **PERF-02** | ⚡ 120Hz / GPU | **Crítica** | Camada dupla de `Modifier.blur()` fullscreen em tempo real excede os 8.33ms na GPU Adreno 650 | `DeckHomeScreen.kt` |
| **PERF-03** | ⚡ OLED | **Média** | Ausência de modo True Black (#000000) e falta de proteção contra Burn-in nas barras estáticas | `Color.kt`, `DeckHomeScreen.kt` |

---

## 3. Análise Detalhada das Falhas e Soluções Técnicas

### 3.1. [BUG-01] Limpeza de Código Morto: `LauncherSettingsDialog` (576 Linhas)
* **Localização:** `app/src/main/java/com/decklauncher/ui/dialogs/LauncherDialogs.kt` (linhas 1648 a 2224)
* **Diagnóstico:** O método composable `@Composable fun LauncherSettingsDialog(...)` possui 576 linhas de código legado. Quando o usuário clica na engrenagem de configurações na `TopBar` ou aciona o menu no `DeckHomeScreen.kt` (linha 798), a UI invoca o novo `StartMenuDialog`, tornando `LauncherSettingsDialog` 100% não utilizado. Isso aumenta o tamanho do bytecode compilado do APK e causa confusão de manutenção.
* **Solução Técnica:**
  Remover completamente a função `LauncherSettingsDialog` de `LauncherDialogs.kt`.

```kotlin
// Em LauncherDialogs.kt:
// DELETAR o bloco completo das linhas 1648 a 2224:
// - @Composable fun LauncherSettingsDialog(...) { ... }
```

---

### 3.2. [BUG-02] Inacessibilidade por Gamepad no `ConfirmActionDialog` & Conflito de B-Dismiss
* **Localização:** `app/src/main/java/com/decklauncher/ui/dialogs/ConfirmActionDialog.kt` e `MainActivity.kt` (linhas 1173-1184)
* **Diagnóstico:** 
  1. `ConfirmActionDialog` não implementa `Modifier.onPreviewKeyEvent`. Embora anexe `FocusRequester` ao botão Confirmar, ele não intercepta `KEYCODE_DPAD_LEFT` / `KEYCODE_DPAD_RIGHT` explicitamente para alternar o foco para o botão Cancelar.
  2. Como `ConfirmActionDialog` é invocado de dentro de diálogos como `ItemMenuDialog` ou `StartMenuDialog`, quando o usuário aperta o botão **B** no controle, o evento sobe para o `MainActivity.dispatchKeyEvent()`, onde a checagem `if (isAnyDialogOpen)` dispara `closeAllDialogs()`. Isso causa o fechamento simultâneo do diálogo de confirmação E do menu pai, em vez de apenas fechar o modal de confirmação.
* **Solução Técnica:**
  Adicionar captura de eventos de teclado/controle no contêiner do `ConfirmActionDialog` com gerenciamento de estado de índice (0 = Cancelar, 1 = Confirmar) e interrupção do evento (`true`) para impedir propagação indesejada ao `MainActivity`.

```kotlin
// Em ConfirmActionDialog.kt:
@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirmar",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedButtonIndex by remember { mutableIntStateOf(1) } // 0 = Cancelar, 1 = Confirmar
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_DPAD_LEFT -> {
                                selectedButtonIndex = 0
                                SoundEffectManager.playNavMove()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> {
                                selectedButtonIndex = 1
                                SoundEffectManager.playNavMove()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                if (selectedButtonIndex == 1) {
                                    onConfirm()
                                }
                                onDismiss()
                                SoundEffectManager.playConfirm()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                onDismiss()
                                SoundEffectManager.playMenuClose()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Renderizar botões com destaque baseado em selectedButtonIndex
            // ...
        }
    }
}
```

---

### 3.3. [BUG-03] Gamepad Trap em `SteamGridApiKeyDialog` e `IidbApiKeyDialog`
* **Localização:** `app/src/main/java/com/decklauncher/ui/dialogs/LauncherDialogs.kt` (linhas 1445-1540)
* **Diagnóstico:** Ambos os diálogos não possuem `focusRequester`, não interceptam eventos de tecla de controle (`BUTTON_B`, `BUTTON_A`, `DPAD`) e o diálogo do `Dialog` não possui um contêiner de scrim clicável padronizado. O jogador que utiliza um gamepad fica preso na tela sem conseguir fechar a janela com o botão B nem submeter a chave pelo controle.
* **Solução Técnica:**
  Adicionar scrim escurecido em tela cheia com `focusRequester`, `onPreviewKeyEvent` capturando `BUTTON_B` para cancelar e navegação entre o campo de texto e os botões Cancelar/Salvar.

```kotlin
// Em LauncherDialogs.kt (SteamGridApiKeyDialog e IidbApiKeyDialog):
@Composable
fun SteamGridApiKeyDialog(
    onDismiss: () -> Unit,
    onSaveKey: (String) -> Unit
) {
    val context = LocalContext.current
    var currentKey by remember { mutableStateOf(SteamGridDbManager.getApiKey(context)) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                onDismiss()
                                SoundEffectManager.playMenuClose()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            // Caixa de conteúdo do diálogo...
        }
    }
}
```

---

### 3.4. [BUG-04] `ChangeAvatarDialog` sem Suporte a Controle Físico
* **Localização:** `app/src/main/java/com/decklauncher/ui/dialogs/LauncherDialogs.kt` (linhas 2226-2350)
* **Diagnóstico:** O seletor de avatares é composto por uma lista/grade de ícones predefinidos (`ProfileManager.PRESET_AVATARS`) e um botão de importar imagem da galeria. Não há nenhum estado de índice selecionado (`selectedAvatarIndex`) vinculado ao D-pad nem requisição de foco. Em um console portátil, o usuário não consegue navegar entre os avatares disponíveis utilizando o controle.
* **Solução Técnica:**
  Implementar navegação por grade D-pad com `selectedAvatarIndex` e confirmação pelo botão A, exibindo anel de foco dinâmico (`FocusRing`) no avatar atualmente destacado.

---

### 3.5. [BUG-05] Dissonância de UX no `BottomBar`: "A: Ver Jogos" Abre o Emulador Direto
* **Localização:** `app/src/main/java/com/decklauncher/ui/components/BottomBar.kt` (linha 69) e `MainActivity.kt` (linhas 1591-1596)
* **Diagnóstico:**
  Na barra de rodapé, o código define:
  ```kotlin
  actionLabel = if (isAppsTab && !isInsideFolder) "Abrir" else if (isEmulatorsTab) "Ver Jogos" else "Jogar"
  ```
  Na aba "Emuladores", o prompt informa claramente ao jogador: **[A] Ver Jogos**. No entanto, quando o jogador aperta o botão **A**, a execução em `MainActivity.kt` chama diretamente:
  ```kotlin
  val current = filtered.getOrNull(selectedIndexState.intValue)
  if (current != null) {
      requestLaunchItem(current) // Dispara o emulador no Android!
  }
  ```
  Isso frustra a expectativa do usuário. O launcher já possui o componente `EmulatorGamesDrawer.kt` projetado especificamente para abrir uma gaveta lateral com a lista de ROMs associadas àquele emulador.
* **Solução Técnica:**
  Se o objetivo é listar os jogos ao pressionar A no emulador, abrir o drawer de jogos:

```kotlin
// Em MainActivity.kt (handleConfirmPress):
} else if (selectedTabState.intValue == 1) {
    // Aba Emuladores: Abre o Drawer de Jogos / ROMs do emulador selecionado
    val currentEmulator = filtered.getOrNull(selectedIndexState.intValue)
    if (currentEmulator != null) {
        activeEmulatorAppState.value = currentEmulator
        showEmulatorGamesDialogState.value = true
        SoundEffectManager.playConfirm()
    }
} else {
    // Aba Jogos: Inicia o jogo diretamente
    val current = filtered.getOrNull(selectedIndexState.intValue)
    if (current != null) {
        requestLaunchItem(current)
    }
}
```
*Caso o usuário deseje iniciar o emulador diretamente sem ver a lista de ROMs, o label na `BottomBar` deve ser alterado para "Iniciar", eliminando a incoerência.*

---

### 3.6. [BUG-06 & BUG-07] Limpeza Inútil de Preferências no `onResume()` e BroadcastReceiver Zumbi
* **Localização:** `MainActivity.kt` (linhas 205, 268, 652)
* **Diagnóstico:**
  1. `getSharedPreferences("iconpack_prefs", Context.MODE_PRIVATE).edit().clear().apply()` é chamado tanto no `onCreate()` quanto em todo retorno de `onResume()`. A limpeza de migração legada deve ser executada apenas uma vez (via flag booleana em prefs), evitando escrita desnecessária em disco flash a cada troca de app.
  2. A variável `steamGridBroadcastReceiver` é instanciada na linha 205 como campo privado da Activity, mas nunca é registrada via `registerReceiver()` no `onStart()` nem removida no `onStop()`, constituindo código morto.
* **Solução Técnica:**
  1. Adicionar chave de migração única:
  ```kotlin
  val prefs = getSharedPreferences("nuvos_app_prefs", Context.MODE_PRIVATE)
  if (!prefs.getBoolean("legacy_iconpack_cleaned", false)) {
      getSharedPreferences("iconpack_prefs", Context.MODE_PRIVATE).edit().clear().apply()
      prefs.edit().putBoolean("legacy_iconpack_cleaned", true).apply()
  }
  ```
  2. Remover ou registrar adequadamente `steamGridBroadcastReceiver`.

---

### 3.7. [BUG-08 & BUG-09] Race Condition e Deformação de Proporção no `BootIntroPlayer`
* **Localização:** `app/src/main/java/com/decklauncher/ui/components/BootIntroPlayer.kt`
* **Diagnóstico:**
  1. A variável `var isFinished by remember { mutableStateOf(false) }` é acessada por corrotinas assíncronas concorrentes (listener de término do `MediaPlayer`, toque na tela e evento de tecla física). Como não é atômica (`AtomicBoolean`), pode ocorrer invocação dupla de `onIntroFinished()`, disparando duas intents de inicialização no Android.
  2. O vídeo usa preenchimento absoluto em tela cheia (`fillMaxSize()`) sem preservar proporção (*aspect ratio*). Intros clássicas em formato 4:3 (como as de PS1, PS2 e GameCube) aparecem esticadas horizontalmente, arruinando a qualidade visual em telas 16:9/16:10.
* **Solução Técnica:**
  1. Utilizar `java.util.concurrent.atomic.AtomicBoolean(false)` para controlar a finalização do player.
  2. Adicionar ajuste de escala de textura mantendo o aspect ratio correto com barras pretas laterais puras (ideal para telas OLED):

```kotlin
// Em BootIntroPlayer.kt:
val isFinished = remember { java.util.concurrent.atomic.AtomicBoolean(false) }

fun triggerFinishWithFade() {
    if (!isFinished.compareAndSet(false, true)) return
    coroutineScope.launch {
        // Fade suave e finalização segura
        fadeAnim.animateTo(0f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {}
        onIntroFinished()
    }
}
```

---

### 3.8. [BUG-10] HashCode Inválido na Busca Automática de Metadados em `EmulatorGamesDrawer`
* **Localização:** `app/src/main/java/com/decklauncher/ui/dialogs/EmulatorGamesDrawer.kt` (linha 78)
* **Diagnóstico:**
  O drawer tenta carregar artes de jogos do banco do iiDB com a seguinte instrução:
  ```kotlin
  val assets = IidbManager.getArtworkForParent(context, rom.id.hashCode().toLong())
  ```
  `IidbManager.getArtworkForParent` espera um `gameId` numérico retornado pela API de busca do iiDB (identificador real da base de dados). O `hashCode()` de uma string de caminho de arquivo de ROM local gera um número pseudoaleatório que nunca existirá no banco de dados do iiDB.
* **Solução Técnica:**
  Buscar o jogo pelo nome sanitizado (`rom.label`) usando `IidbManager.searchGames(context, rom.label)` para obter o ID legítimo, ou utilizar o sistema de cache de metadados local de capas.

---

### 3.9. [UX-01] Paralisia do D-Pad no `StartMenuDialog`
* **Localização:** `app/src/main/java/com/decklauncher/ui/dialogs/StartMenuDialog.kt` (linhas 100-130)
* **Diagnóstico:**
  O bloco `onPreviewKeyEvent` trata exclusivamente:
  - `KEYCODE_DPAD_LEFT` e `KEYCODE_DPAD_RIGHT` (alternam entre as abas Geral, Aparencia, etc.)
  - `KEYCODE_BUTTON_B` (fecha o menu)
  Não existe manipulador para `KEYCODE_DPAD_DOWN`. Ao abrir o menu de configurações usando o controle, o jogador consegue navegar apenas entre as abas superiores. É **impossível** descer para a área de conteúdo para mudar tema, alterar wallpaper, reescanear ROMs ou configurar chaves de API sem tocar na tela com o dedo.
* **Solução Técnica:**
  Introduzir um enum de foco (`enum class StartMenuFocusArea { TABS, CONTENT }`) e gerenciar o índice do item focado no conteúdo com navegação vertical completa:

```kotlin
// Em StartMenuDialog.kt:
enum class MenuFocusState { TABS, CONTENT }
var currentFocusState by remember { mutableStateOf(MenuFocusState.TABS) }
var contentItemIndex by remember { mutableIntStateOf(0) }

// No onPreviewKeyEvent:
AndroidKeyEvent.KEYCODE_DPAD_DOWN -> {
    if (currentFocusState == MenuFocusState.TABS) {
        currentFocusState = MenuFocusState.CONTENT
        contentItemIndex = 0
        SoundEffectManager.playNavMove()
        true
    } else {
        contentItemIndex = (contentItemIndex + 1).coerceAtMost(maxItemsInTab - 1)
        SoundEffectManager.playNavMove()
        true
    }
}
AndroidKeyEvent.KEYCODE_DPAD_UP -> {
    if (currentFocusState == MenuFocusState.CONTENT) {
        if (contentItemIndex == 0) {
            currentFocusState = MenuFocusState.TABS
        } else {
            contentItemIndex--
        }
        SoundEffectManager.playNavMove()
        true
    } else false
}
```

---

### 3.10. [UX-02] Diálogos de Seleção de Arte sem Navegação D-Pad na Grade
* **Localização:** `EditCoverSteamGridDialog`, `SteamGridHeroSelectDialog`, `SteamGridLogoSelectDialog` em `LauncherDialogs.kt`
* **Diagnóstico:**
  Após a busca de capas ou banners no SteamGridDB/iiDB, os resultados são dispostos em um `LazyVerticalGrid`. O diálogo escuta apenas o botão **B** para fechar. O jogador com gamepad não tem como mover o cursor entre as capas retornadas nem selecionar a imagem desejada com o botão **A**.
* **Solução Técnica:**
  Controlar `selectedArtworkIndex: Int` e tratar `DPAD_LEFT`, `DPAD_RIGHT`, `DPAD_UP`, `DPAD_DOWN` considerando o número de colunas da grade (`columns = 3`), permitindo selecionar com `BUTTON_A`.

---

### 3.11. [UX-04] Inconsistência de Pulo de Intro entre Touch e Gamepad
* **Localização:** `MainActivity.kt` (linhas 1148-1171) e `BootIntroPlayer.kt`
* **Diagnóstico:**
  Ao usar a tela sensível ao toque, pular a introdução exige pressionar e segurar por 500ms, exibindo um indicador circular suave com fade. No controle físico, no entanto, qualquer clique rápido em botões como A, B, X, Y ou D-Pad durante o `ACTION_DOWN` corta o vídeo instantaneamente a seco. Ao pegar o console ou ligá-lo com o botão A, a animação de abertura é cancelada acidentalmente.
* **Solução Técnica:**
  Padronizar a experiência de skip no controle exigindo segurar o botão A ou B por ~400ms com feedback na tela, ou limitar o skip imediato exclusivamente ao botão Start/Menu.

---

### 3.12. [UX-05] Falha no Fallback de Categorias em `getFilteredApps()`
* **Localização:** `MainActivity.kt` (linhas 718-727)
* **Diagnóstico:**
  ```kotlin
  private fun getFilteredApps(): List<AppItem> {
      val all = appsListState.value
      val cat = when (selectedTabState.intValue) {
          0 -> AppCategory.JOGOS
          1 -> AppCategory.EMULADORES
          else -> AppCategory.APPS
      }
      val filtered = all.filter { it.category == cat }
      return if (filtered.isEmpty() && cat != AppCategory.APPS) all else filtered
  }
  ```
  Se o jogador não possuir nenhum jogo cadastrado na categoria `JOGOS` ou nenhum emulador em `EMULADORES`, a função retorna `all` (todos os aplicativos do dispositivo, incluindo Calculadora, Configurações e Navegadores). Isso impede que a tela de estado vazio (*empty state* do `DeckHomeScreen.kt`) seja exibida, gerando uma experiência caótica e confusa.
* **Solução Técnica:**
  Retornar `emptyList()` se a categoria não possuir itens, permitindo que a interface mostre a tela orientando o usuário a adicionar jogos (pelo botão [Y]):

```kotlin
private fun getFilteredApps(): List<AppItem> {
    val all = appsListState.value
    val cat = when (selectedTabState.intValue) {
        0 -> AppCategory.JOGOS
        1 -> AppCategory.EMULADORES
        else -> AppCategory.APPS
    }
    return all.filter { it.category == cat }
}
```

---

## 4. Otimizações Específicas de Performance: Snapdragon 865 & Display OLED 120Hz

O Snapdragon 865 possui a GPU **Adreno 650**, capaz de alto rendimento gráfico, mas que opera sob restrições térmicas severas em consoles portáteis. Em uma tela **120Hz**, o tempo máximo por quadro (*frametime budget*) é de **8.33 milissegundos**. Qualquer computação que ultrapasse esse limite gera queda perceptível de quadros.

### 4.1. [PERF-01] Recálculo Redundante de Pastas no Thread Principal
* **Localização:** `MainActivity.kt` (linhas 1254, 1503, 1574)
* **Diagnóstico:** O método `CategoryManager.groupIntoFolders(appsListState.value)` percorre e reorganiza toda a lista de aplicativos instalados. Ele é executado de forma síncrona dentro de `executeNavigation()` e `handleConfirmPress()`. Durante uma rolagem contínua via analógico/D-Pad, recalcular as pastas em cada *tick* consome de 2 a 5ms no processador, roubando tempo do renderizador gráfico.
* **Solução Técnica:**
  Armazenar o agrupamento de pastas em um `derivedStateOf` ou `StateFlow` e recalcular apenas quando a lista de aplicativos instalados (`appsListState`) for modificada.

```kotlin
// Em MainActivity.kt:
private val cachedFoldersState = derivedStateOf {
    CategoryManager.groupIntoFolders(appsListState.value)
}
// Utilizar cachedFoldersState.value nas chamadas de navegação
```

---

### 4.2. [PERF-02] Gargalo de Fillrate de GPU por RenderEffect Blur Fullscreen a 120Hz
* **Localização:** `DeckHomeScreen.kt` (linhas 367 e 375)
* **Diagnóstico:**
  O launcher aplica dois modificadores de blur em tempo real sobre imagens em tela cheia:
  ```kotlin
  AsyncImage(model = blurFile, modifier = Modifier.fillMaxSize().blur(14.dp))
  AsyncImage(model = file, modifier = Modifier.fillMaxWidth().fillMaxHeight(0.72f).blur(10.dp))
  ```
  No Android 12+ (API 31+), `Modifier.blur()` utiliza o `RenderEffect.createBlurEffect()`, realizando passes de convolução gaussiana na GPU a cada quadro. Em uma tela de alta densidade a 120Hz, aplicar blur em duas camadas simultâneas de resolução total (ex: 2000x1200) pode consumir mais de **6ms de GPU**, deixando menos de 2.3ms para o restante de toda a interface. Isso causa travamentos visíveis durante a navegação.
* **Solução Técnica:**
  1. Pré-computar e salvar em disco/memória uma versão redimensionada com blur (ex: miniatura de 320x180 pixels). Como a imagem já está borrada, a baixa resolução não tem perda perceptível de qualidade e custa quase zero para a GPU renderizar.
  2. Ou aplicar um `Coil` `Transformation` de blur em tempo de carregamento do bitmap, eliminando os passes de shader do compositor do Compose.

---

### 4.3. [PERF-03] Otimizações para Painel OLED: True Black & Mitigação de Burn-In
* **Diagnóstico:**
  1. O tema utiliza a cor `ConsoleBg` (`#111418` - cinza grafite). Em telas OLED, pixels nessa tonalidade continuam acesos consumindo corrente elétrica. No modo preto absoluto (`#000000`), os subpixels desligam-se completamente, proporcionando contraste infinito e economia expressiva de bateria.
  2. Em launchers para consoles portáteis, o dispositivo pode permanecer por horas ligado na tela inicial. Elementos estáticos de alto contraste (bateria, logo, botões da `TopBar` e `BottomBar`) expõem o painel OLED ao risco de retenção de imagem (*burn-in*).
* **Soluções Técnicas:**
  1. **Modo OLED True Black:** Adicionar opção no `StartMenuTabAparencia` que define o fundo para `#000000`.
  2. **Pixel Shifting Sub-milimétrico:** Implementar um sutil deslocamento de 1 ou 2 pixels nos elementos fixos a cada 3 a 5 minutos para alternar a carga entre subpixels adjacentes.
  3. **Auto-dimming da UI:** Após 60 segundos sem entrada do controle, reduzir a opacidade dos elementos fixos de 1.0f para 0.4f.

---

## 5. Roteiro de Implementação Recomendado (Checklist Técnico)

Para elevar a versão 1.1 ao nível de estabilidade e polimento exigido por um console de jogos dedicado, recomenda-se a seguinte ordem de execução:

- [ ] **Fase 1: Correções de Acessibilidade de Hardware (Gamepad)**
  - Implementar navegação vertical e seleção no `StartMenuDialog.kt`.
  - Adicionar controle de teclas D-pad e botão B no `ConfirmActionDialog.kt`, `SteamGridApiKeyDialog` e `IidbApiKeyDialog`.
  - Permitir seleção de capas por D-pad em `EditCoverSteamGridDialog.kt`.
  - Harmonizar o prompt da aba Emuladores no `BottomBar.kt` para abrir o `EmulatorGamesDrawer.kt`.

- [ ] **Fase 2: Estabilidade e Higiene de Código**
  - Deletar as ~580 linhas mortas de `LauncherSettingsDialog` em `LauncherDialogs.kt`.
  - Proteger a concorrência do `BootIntroPlayer` com `AtomicBoolean`.
  - Ajustar `getFilteredApps()` para respeitar listas vazias sem contaminar a aba Jogos com apps utilitários.
  - Corrigir a busca por hashcode inválido em `EmulatorGamesDrawer.kt`.

- [ ] **Fase 3: Performance 120Hz & Otimização OLED**
  - Cachear o agrupamento de pastas (`groupIntoFolders`) fora dos loops de navegação.
  - Substituir o duplo `Modifier.blur()` em tempo real por bitmaps de baixa resolução pré-borrados via Coil/Glide.
  - Implementar o tema **Pure Black OLED (#000000)** no seletor de aparência.
  - Adicionar proteção contra burn-in (dimming automático e pixel-shift).

---
*Relatório técnico finalizado e salvo em `~/Downloads/deck-launcher-beta-report.md`.*
