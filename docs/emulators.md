# Matriz de Compatibilidade de Emuladores — NuvOS v1.1

Auditoria de lançamento de ROMs e emuladores no dispositivo SM-P620 (Android 14) com NuvOS v1.1.

| Emulador | Pacote | Intent / Componente Declarado | Esquema de URI | Testado no Dispositivo? | Status / Observações |
|---|---|---|---|---|---|
| **melonDS** | `me.magnum.melonds` | `action: me.magnum.melonds.LAUNCH_ROM`<br>`component: me.magnum.melonds/.ui.emulator.EmulatorActivity` | `file://` | **Sim (Testado)** | **Aprovado.** Requer action customizada e componente explícito da EmulatorActivity. Abre ROM direto sem abrir file picker. |
| **AetherSX2** | `xyz.aethersx2.android` | `action: android.intent.action.MAIN`<br>`component: xyz.aethersx2.android/.MainActivity`<br>`extra: bootPath={path}` | Caminho físico (`bootPath`) | **Sim (Testado)** | **Aprovado.** Não aceita `ACTION_VIEW` nem content URI direto; necessita do extra `bootPath` com caminho absoluto e permissão de arquivos para inicialização direta do jogo. |
| **NetherSX2** | `xyz.nethersx2.android` | `action: android.intent.action.MAIN`<br>`component: xyz.nethersx2.android/.MainActivity`<br>`extra: bootPath={path}` | Caminho físico (`bootPath`) | Não testado (não instalado) | Configuração herdada de AetherSX2. |
| **PPSSPP** | `org.ppsspp.ppsspp` | `action: android.intent.action.VIEW`<br>`type: application/octet-stream` | `file://` | **Sim (Testado)** | **Aprovado.** Atividade `org.ppsspp.ppsspp.PpssppActivity` recebe intent view e lança direto. |
| **PPSSPP Gold** | `org.ppsspp.ppssppgold` | `action: android.intent.action.VIEW`<br>`type: application/octet-stream` | `file://` | Não testado (não instalado) | Compatível com PPSSPP base. |
| **DuckStation** | `com.github.stenzek.duckstation` | `action: android.intent.action.VIEW` | `file://` | **Sim (Testado)** | **Aprovado.** Inicia o app do emulador via launcher intent ou intent explícita. |
| **Dolphin Emulator** | `org.dolphinemu.dolphinemu` | `action: android.intent.action.VIEW` | `file://` | **Sim (Testado)** | **Aprovado.** Inicia via intent explícita. |
| **RetroArch** | `com.retroarch` | `action: android.intent.action.VIEW` | `file://` | **Sim (Testado)** | **Aprovado.** Inicia via intent explícita. |
| **Vita3K** | `org.vita3k.emulator` | `action: android.intent.action.VIEW` | `file://` | **Sim (Testado)** | **Aprovado.** |
| **Haku X** | `com.rfandango.haku_x` | `action: android.intent.action.VIEW`<br>`component: com.rfandango.haku_x/.LauncherActivity`<br>`type: */*` | `file://` | Não testado (não instalado) | Declarado no `emulators.json`. |
| **DraStic** | `com.dsemu.drastic` | `action: android.intent.action.VIEW` | `file://` | Não testado (não instalado) | Declarado no `emulators.json`. |
| **Citra / Lime3DS** | `org.citra.citra_emu` / `io.github.lime3ds.android` | `action: android.intent.action.VIEW` | `file://` | Não testado (não instalado) | Declarado no `emulators.json`. |
| **Snes9x EX+** | `com.explusalpha.Snes9xPlus` | `action: android.intent.action.VIEW` | `file://` | Não testado (não instalado) | Declarado no `emulators.json`. |
| **My Boy!** | `com.fastemulator.gba` | `action: android.intent.action.VIEW` | `file://` | Não testado (não instalado) | Declarado no `emulators.json`. |
| **Mupen64Plus FZ** | `org.mupen64plusae.v3.fzurita` | `action: android.intent.action.VIEW` | `file://` | Não testado (não instalado) | Declarado no `emulators.json`. |
| **Redream / Flycast** | `io.recompiled.redream` / `com.flycast.emulator` | `action: android.intent.action.VIEW` | `file://` | Não testado (não instalado) | Declarado no `emulators.json`. |

### Justificativa de Exceção Técnica: `StrictMode.setVmPolicy`
AetherSX2 / NetherSX2 não utilizam SAF nem recebem `content://` URIs por padrão em suas atividades principais (`MainActivity`). Eles esperam o parâmetro `bootPath` contendo o caminho físico absoluto (`/storage/emulated/0/...`). Além disso, o motor C++ de emuladores clássicos (PPSSPP e RetroArch) acessa arquivos diretamente por POSIX `fopen()` nos caminhos `file://`.
Portanto, a política permissiva em `StrictMode.setVmPolicy` é mantida como exceção justificada e documentada para permitir interoperabilidade com emuladores de console que não implementam FileProvider.
