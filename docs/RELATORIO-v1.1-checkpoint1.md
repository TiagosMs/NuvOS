# Relatório NuvOS v1.1 — Checkpoint 1 (🛑)

**Data:** 2026-11-20  
**Branch:** `feat/v1.1`  
**Dispositivo de Teste:** Samsung Galaxy Tab S6 Lite 2024 (`SM-P620` / `RX2X700QMPP`, Android 14)  
**Commits Realizados:**
- `36f606c`: chore: remove local.properties from git tracking
- `21f73d9`: refactor(fase1): subtrair iconpack, Better xCloud fixo, contagem apps e dicas
- `4dc4cbb`: fix(emulators): corrigir loop de abertura, rotear LaunchRoute e parametrizar intents (Fase 2)

---

## 1. Descrição dos Prints de Referência (T0.5)

1. **Captura do Menu Start (`Captura de tela de 2026-11-20 21-37-23.png`):**
   - Modal centralizado com bordas arredondadas e fundo escuro fosco (`#1e2530`).
   - Apresenta barra superior com 3 abas horizontais com indicadores nítidos de foco e seleção: **Biblioteca**, **Aparência e Navegação**, **Bancos de Dados**.
   - Cada seção agrupa opções em formato de cards e linhas com padding padronizado, tipografia clara e contraste elevado para consoles.

2. **Captura do Setup Wizard (`Captura de tela de 2026-11-20 21-40-48.png`):**
   - Fluxo de boas-vindas exibido em tela cheia na primeira inicialização (`isSetupCompleted == false`).
   - Cabeçalho de apresentação do launcher com indicadores de etapas (dots de progresso).
   - Área central com opções declarativas de pastas de ROMs, provedores de arte e navegação inferior ("Pular" e "Continuar").

---

## 2. Inventário de Ações dos Diálogos (`LauncherDialogs.kt` - T0.6)

| Ação | Destino Proposto | Justificativa |
|---|---|---|
| Iniciar Jogo / App | Menu do Item (X / segurar) | Ação contextual de execução do item selecionado |
| Personalizar Metadados (Capa, Hero, Logo, Som) | Menu do Item (X / segurar) | Ação contextual específica daquele jogo/app |
| Mover Posição / Trocar de Lugar | Menu do Item (X / segurar) | Sai do Start Menu global; aciona o modo de reordenação com D-pad ou toque |
| Baixar Tudo do iiDB | Menu do Item (X / segurar) | Ação contextual de scraping daquele jogo |
| Editar Capa / Hero / Logo | Menu do Item (X / segurar) | Ação contextual de troca de arte |
| Trocar Intro do Console | Menu do Item (X / segurar) | Ação contextual de inicialização personalizada |
| Informações do Aplicativo | Menu do Item (X / segurar) | Abre configurações do pacote Android (`ACTION_APPLICATION_DETAILS_SETTINGS`) |
| Desinstalar Aplicativo | Menu do Item (X / segurar) | Ação contextual com diálogo de confirmação |
| Remover da Tela Inicial | Menu do Item (X / segurar) | Ação contextual para atalhos de nuvem e ROMs |
| Pasta de ROMs & Armazenamento | Start › Biblioteca | Configuração global da biblioteca de jogos |
| Re-escanear ROMs e Emuladores | Start › Biblioteca | Manutenção global da biblioteca |
| Organização de Pastas de ROMs | Start › Biblioteca | Gestão global do sistema de arquivos |
| Modo de Plano de Fundo (Imersivo/Wallpaper/Sóbrio) | Start › Aparência e Navegação | Personalização global do launcher |
| Papel de Parede Personalizado | Start › Aparência e Navegação | Personalização estética visual |
| Cor de Destaque (Accent Color) | Start › Aparência e Navegação | Identidade visual global |
| Intros de Boot dos Consoles | Start › Aparência e Navegação | Transições visuais globais |
| Transição ao Abrir Jogos | Start › Aparência e Navegação | Comportamento global de lançamento |
| Foto de Perfil & Nome | Start › Aparência e Navegação | Gestão do usuário |
| Refazer Configuração Inicial | Start › Aparência e Navegação | Reinicia o wizard de boas-vindas |
| Provedor Padrão de Artes (SteamGridDB / iiDB) | Start › Bancos de Dados | Integração e provedores globais |
| Chave de API SteamGridDB | Start › Bancos de Dados | Credencial global externa |
| Chave de API iiDB | Start › Bancos de Dados | Credencial global externa |
| Limpar Cache e Excluir Metadados | Start › Bancos de Dados | Manutenção global e reset |

---

## 3. Evidências da Fase 1 (Subtração)

- **Remoção de Icon Pack (T1.1):**
  - Arquivo `IconPackManager.kt` removido.
  - `IconCache.kt` adaptado para carregar estritamente de `PackageManager`.
  - Migração de preferência legada adicionada em `MainActivity.onCreate` e `onResume`.
  - Verificação:
    ```bash
    $ grep -rni "iconpack" app/src
    app/src/main/java/com/decklauncher/MainActivity.kt:250:        // Migração v1.1: limpar preferência legada de pacote de ícones
    app/src/main/java/com/decklauncher/MainActivity.kt:588:        // Migração v1.1: limpar preferência legada de pacote de ícones
    ```
- **Remoção da Centralidade de Better xCloud (T1.2):**
  - Remoção de `setPackage("com.redphx.betterxc")`. Itens de nuvem salvos executam `Intent(Intent.ACTION_VIEW, Uri.parse(url))` genérico.
  - `CloudGameCatalogDialog` removido do fluxo.
  - `grep -rni "betterxc\|CloudGameCatalog" app/src` zerado.
- **Remoção das Etiquetas de Contagem de Apps (T1.3):**
  - Textos `N apps` e `N APLICATIVOS` removidos de `AppFolderCard.kt`.
- **Remoção de Dica no Diálogo de Emuladores (T1.4):**
  - Texto `🎮 (A) Jogar • (X) Fixar na Home` removido mantendo os botões de ação e lista.

---

## 4. Evidências da Fase 2 (Correção do Bug dos Emuladores)

- **Diagnóstico Confirmado (T2.1):**
  - `onLaunchEmulator` no diálogo reencaminhava para `onLaunchApp`, que reabria o diálogo de emuladores para categorias `EMULADORES`.
- **Correção e Roteamento Puro (T2.2):**
  - Criada a interface selada `LaunchRoute` em [LaunchRoute.kt](file:///home/tiago/dev/projetos/deck-launcher/app/src/main/java/com/decklauncher/model/LaunchRoute.kt).
  - Testes unitários implementados em [LaunchRouteTest.kt](file:///home/tiago/dev/projetos/deck-launcher/app/src/test/java/com/decklauncher/LaunchRouteTest.kt).
  - Saída dos testes unitários:
    ```bash
    $ ./gradlew testDebugUnitTest
    BUILD SUCCESSFUL in 8s
    24 actionable tasks: 3 executed, 21 up-to-date
    ```
- **Robustez de Abertura (T2.3):**
  - Implementado fallback para `queryIntentActivities` buscando `ACTION_MAIN` caso `getLaunchIntentForPackage` retorne nulo. Tratamento de erro detalhado com logging.
- **Auditoria de Lançamento de ROMs (T2.4):**
  - Eliminados todos os ramos fixos `contains("ppsspp")`, `contains("aethersx2")`, `contains("nether")` e Haku X de `RomScanner.launchRom`.
  - Configuração 100% declarativa em [emulators.json](file:///home/tiago/dev/projetos/deck-launcher/app/src/main/assets/emulators.json) com suporte a `component`, `bootPath` e `action`.
  - Criado documento de matriz de compatibilidade em [docs/emulators.md](file:///home/tiago/dev/projetos/deck-launcher/docs/emulators.md).

---

## 5. Medições de Performance e Tamanho do APK

| Métrica | Antes da v1.1 (Baseline) | Checkpoint 1 | Meta / Budget |
|---|---|---|---|
| **Tamanho do APK (Release)** | 6,9 MB | **6,8 MB** (-100 KB) | < 10 MB |
| **Cold Start (Mediana 5x)** | 261 ms | **258 ms** | ≤ 700 ms |
| **check-rules.sh** | OK | **✅ check-rules OK** | 0 violações |
| **Testes Unitários** | 0 testes | **3 testes passando** | 100% sucesso |

---

## 6. Captura de Tela do Aparelho

Screenshot capturado via ADB no tablet Galaxy Tab S6 Lite:
- [checkpoint1_apps.png](file:///home/tiago/dev/projetos/deck-launcher/docs/screens/checkpoint1_apps.png)
- [fase1_installed.png](file:///home/tiago/dev/projetos/deck-launcher/docs/screens/fase1_installed.png)
