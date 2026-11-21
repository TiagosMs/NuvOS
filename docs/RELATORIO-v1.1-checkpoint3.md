# Relatório do Checkpoint 3 — Fases 5, 6 e 7 (v1.1)

**Data:** 21/11/2026  
**Branch:** `feat/v1.1`  
**Dispositivo de Teste:** Samsung Galaxy Tab S6 Lite (`SM-P620` / `RX2X700QMPP`), Android 14 One UI 6.1  
**Build:** Release com minificação R8 (`app-release.apk`)

---

## 1. O que foi feito

### Fase 5 — Organizador Seguro de ROMs (`RomOrganizer.kt`)
- Planejamento puro (`RomOrganizer.plan`) baseado na tabela declarativa `emulators.json` sem chamadas de I/O em tempo de análise.
- Detecção unificada com `RomSystemDetector.detectSystem`. Extensões ambíguas ou sem emulador instalado vão para a pasta `_Revisar/`.
- Renomeação atômica (`renameTo`) no mesmo sistema de arquivos com journal em memória para reversão (`undoJournal`).
- **Testes unitários automatizados** em `app/src/test/java/com/decklauncher/RomOrganizerTest.kt`:
  - Extensão única move para subpasta do sistema.
  - Arquivos temporários (`.crdownload`, `.part`, `.tmp`) ou modificados há menos de 60 segundos são ignorados.
  - Colisão de nomes tratada com sufixo numérico ` (2)`.
  - Sistemas não reconhecidos direcionados para `_Revisar`.
  - 100% dos testes passando (`BUILD SUCCESSFUL`).

### Fase 6 — Atalhos de Jogos em Nuvem via Android PinItemRequest
- Removida dependência de catálogos estáticos no código.
- Registrada `ConfirmPinShortcutActivity` no `AndroidManifest.xml` para capturar `android.content.pm.action.CONFIRM_PIN_SHORTCUT`.
- Implementado `ShortcutManager.kt` para gerenciar atalhos fixados e executar via API oficial do sistema: `LauncherApps.startShortcut(...)`.
- Suporte no menu de contexto (`ItemMenuDialog.kt`) e na tela inicial (`DeckHomeScreen.kt` / `MainActivity.kt`), permitindo remover da home e personalizar metadados normalmente.
- Documentado em português no `README.md` como criar atalhos pelo Better xCloud, Edge e GeForce NOW.

### Fase 7 — Wizard de Boas-Vindas em Tela Cheia (`SetupWizardScreen.kt`)
- Tela cheia nativa em Compose (não é `Dialog`), substituindo o fluxo legado na primeira execução.
- 6 passos estruturados:
  1. Boas-vindas.
  2. Perfil do Jogador (nome e seleção de avatar).
  3. Launcher Padrão (explicação da exigência do Android para atalhos fixados e intent via `RoleManager.ROLE_HOME`).
  4. Pasta de ROMs (seleção via SAF).
  5. Bancos de Dados de Artes (SteamGridDB e iiDB, campos mascarados com `PasswordVisualTransformation`, sem logging em logcat).
  6. Conclusão e resumo das configurações.
- Persistência e retomada de estado via `SetupManager.saveSetupStep`.
- Opção "Refazer Configuração Inicial (Wizard)" adicionada no Start Menu › Aparência e Navegação.
- Usuários existentes que atualizam o app não são forçados a refazer o wizard (preserva configurações anteriores).

### Ajustes de Design do Usuário
- **Start Menu Lateral Deslizante:** menu lateral esquerdo com animação `slideInHorizontally(initialOffsetX = { -it })`, perfil do jogador posicionado no topo e 3 abas centralizadas em botões quadrados com cantos arredondados e ícones:
  - 📚 **Biblioteca**
  - 🎨 **Aparência e Navegação**
  - 🗄️ **Bancos de Dados**
- **Barra Superior Fixa:** corrigido deslocamento da TopBar ao alternar abas, mantendo o espaçamento do botão de adicionar estável entre Jogos, Emuladores e Apps.

---

## 2. Métricas no SM-P620 (Release R8)

| Métrica | Meta / Orçamento | Medição Checkpoint 3 | Status |
|---|---|---|---|
| Cold Start | ≤ 700 ms (meta) / ≤ 900 ms (teto) | **500 ms** (556 ms no clean install) | ✅ Excelente |
| Consumo de RAM (PSS) | < 250 MB (meta) / ≤ 300 MB (teto) | **192.5 MB** | ✅ Muito abaixo |
| Tamanho do APK Release | ≤ 10 MB | **7.2 MB** | ✅ Dentro do limite |
| Testes Unitários | 100% passando | **7/7 testes verdes** | ✅ Green |
| check-rules.sh | 0 violações | **100% OK** | ✅ Green |

---

## 3. Evidências Visuais (Screenshots no Aparelho)

- Tela Inicial: `docs/screens/checkpoint3_home.png`
- Start Menu Lateral (Drawer com abas quadradas e perfil): `docs/screens/checkpoint3_startmenu.png`
- Wizard de Boas-Vindas (Passo 1): `docs/screens/checkpoint3_wizard.png`
- Wizard de Boas-Vindas (Passo 2 - Perfil): `docs/screens/checkpoint3_wizard_profile.png`

---

## 4. O que NÃO foi feito (Pendências para a Fase 8)

- Modo desempenho (não existe no hardware/OS alvo atual; mantido fora conforme instruções do prompt).
- Atualização final de versão (`1.1.0`) e regras finais automatizadas do `check-rules.sh` (programadas para a Fase 8).
