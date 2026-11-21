# NuvOS — Guia de boas práticas (a partir de hoje)

> Objetivo: **nunca mais** reintroduzir os problemas de performance e segurança que foram corrigidos na branch `perf/refactor`.
> Este documento é a **regra do projeto**. Quem escreve código (você ou uma IA) segue isto, sem exceção.

## As 3 regras de ouro

1. **Nada entra sem medir.** Toda mudança que mexe em UI, imagens, rede, listas ou animação é medida em **build release** antes do merge.
2. **Nada secreto entra no git.** Nunca. Nem "temporariamente", nem em branch, nem em commit que "vou apagar depois".
3. **Nada é dado como "pronto" sem evidência.** "Pronto" = testes passando + números medidos + checklist do PR marcado.

---

## 1. Onde o projeto está hoje (estado real)

Resultados informados no tablet SM-P620 (comparados às metas do plano):

| Métrica | Antes | Depois | Meta | Situação |
|---|---|---|---|---|
| Janky frames | 16,58 % | 1,07 % | < 5 % | Atingido |
| Frame time p95 (GPU) | 113 ms | 20 ms | < 16,6 ms (60 Hz) | Perto, **ainda acima** do ideal |
| Memória gráfica | 113 MB | 61 MB | < 80 MB | Atingido |
| PSS total | 342 MB | 287 MB | < 250 MB | **Não atingido** |
| Cold start | 1443 ms | 987 ms (debug) | < 700 ms | **Medido em debug, refazer em release** |
| APK | 21 MB | 6,8 MB | < 10 MB | Atingido |

### Pendências que precisam ser conferidas no código
O relatório da implementação diz "executado integralmente", mas **não menciona** estes itens do plano. Confirme cada um (abra o código ou pergunte à IA, exigindo o arquivo e a linha):

- [ ] Índice de ROMs em Room com scan incremental (plano 2.4)
- [ ] `AppRepository` com reload coalescido (`debounce` + `collectLatest`) (2.2)
- [ ] Race do hero/logo corrigida com `produceState` (2.7)
- [ ] `ViewModel` + `UiState` + `Overlay` único (3.1) e DataStore (3.2)
- [ ] Acesso a ROMs por SAF / remoção do hack de `StrictMode` (5.3)
- [ ] Modo desempenho (Completo/Equilibrado/Econômico) (4.11)
- [ ] **Baseline Profile + Macrobenchmark** (6.1) — sem isso o cold start em release não fica no melhor valor
- [ ] Ícone adaptativo, `README.md` e `LICENSE`
- [ ] Chave antiga do SteamGridDB **revogada de fato no site** (tirar do código não basta: ela continua no histórico do git)

### Cuidados com a medição atual
- O tablet SM-P620 é **mais potente** que um handheld de entrada. Valide também em um aparelho fraco (o pior que você tiver).
- Cold start e jank só valem em **release** (R8 ligado). Debug engana.
- Fixe o procedimento (Seção 2) para os números serem comparáveis entre versões.

---

## 2. Orçamentos de performance (budgets)

Estes números são **limites**. Passou do limite = PR não entra.

| Métrica | Limite (falha) | Alvo | Como medir |
|---|---|---|---|
| Cold start (release) | 900 ms | < 700 ms | `adb shell am start -W -n <pacote>/.MainActivity` (rodar 5×, usar a mediana) |
| Janky frames | 5 % | < 2 % | `dumpsys gfxinfo` (abaixo) |
| Frame time p95 | 20 ms | < 16,6 ms | `dumpsys gfxinfo` |
| Recomposições com tela parada | > 0/s | 0/s | Layout Inspector → Recomposition counts |
| PSS total (100+ jogos) | 300 MB | < 250 MB | `dumpsys meminfo` |
| Memória gráfica | 80 MB | < 65 MB | `dumpsys meminfo` |
| Tamanho do APK (release) | 10 MB | < 8 MB | tamanho do arquivo |
| Requisições de rede na 1ª abertura com tudo em cache | > 0 | 0 | Logcat/OkHttp interceptor em debug |

### Procedimento de medição padrão (copie e use sempre igual)
```bash
# 1) Cold start
adb shell am force-stop <pacote>
adb shell am start -W -n <pacote>/.MainActivity        # anote TotalTime

# 2) Jank: navegue 60 segundos (abas, carrossel, pasta, menu)
adb shell dumpsys gfxinfo <pacote> reset
# ... usar o app ...
adb shell dumpsys gfxinfo <pacote>                     # anote Janky frames e percentis

# 3) Memória
adb shell dumpsys meminfo <pacote>                     # anote TOTAL PSS e Graphics
```
Regras: sempre **release**, sempre o **mesmo aparelho**, sempre a **mesma biblioteca de teste** (ex.: 100 jogos + 40 apps), aparelho com a tela ligada, sem outros apps pesados abertos.

Registre o resultado em `docs/perf.md` a cada release (data, versão, aparelho, números).

---

## 3. Regras de performance (SEMPRE / NUNCA)

### 3.1 Animações e Compose

**NUNCA** leia valor animado no corpo da composição. Isso recompõe a tela a cada frame.
```kotlin
// ❌ RUIM: recompõe a subárvore inteira a 60 fps
val y by infiniteTransition.animateFloat(...)
Box(Modifier.offset(y = y.dp))

// ✅ BOM: lido só na fase de draw
val y = infiniteTransition.animateFloat(...)          // sem "by"
Box(Modifier.graphicsLayer { translationY = y.value.dp.toPx() })
```
- **SEMPRE** animar só `translation`, `scale`, `rotation` e `alpha` dentro de `graphicsLayer`.
- **NUNCA** animar `elevation`/`shadow`, largura de borda, `size`, `padding` ou `blur` (forçam novo layout/draw ou re-gravam sombra a cada frame).
- **SEMPRE** usar os tokens de `Motion.kt` (tempos e curvas). Nada de `tween(317)` solto.
- **NUNCA** deixar animação infinita rodando com diálogo aberto, aba em transição ou app em segundo plano. Passe um parâmetro `idleEnabled`.
- **SEMPRE** usar **uma** animação de fase com harmônicos inteiros (`sin(p)`, `sin(2p)`, `sin(3p)`) em vez de várias animações independentes.
- **NUNCA** usar `Modifier.blur()` / `RenderEffect` em área grande (tela cheia). Não funciona abaixo do Android 12 e pesa em GPU de entrada. Pré-renderize o desfoque (reduzir a imagem a 1/16 e ampliar com filtro bilinear).
- **NUNCA** usar `FLAG_BLUR_BEHIND` em janelas/diálogos. Use um scrim (`Color(0xB3000000)`).
- **SEMPRE** respeitar o **Modo desempenho**: em "Econômico", sem 3D, sem halo, sem animação idle.

### 3.2 Imagens

- **NUNCA** guarde `Bitmap`/`ImageBitmap` em modelos de dados (`AppItem`). Guarde **caminho de arquivo**.
- **SEMPRE** carregue imagens com Coil informando o tamanho do slot (`.size(w, h)`), para decodificar já no tamanho certo.
- **NUNCA** decodifique imagem na main thread (wallpaper, capa, logo, hero, ícone).
- **SEMPRE** processe **ao salvar**, não ao exibir:
  - Capa: ~350×500, WebP lossy.
  - Hero: ~1280 px de largura, WebP lossy + `hero_blur` (1/16).
  - Logo: largura máxima da UI, WebP lossless (precisa de alpha).
- **NUNCA** salve capa como PNG (a "qualidade" é ignorada e o arquivo fica enorme).
- **SEMPRE** use cache com limite: `LruCache` por **bytes**. **NUNCA** `mutableMapOf` como cache.
- **NUNCA** mantenha mais de uma resolução da mesma imagem decodificada ao mesmo tempo sem necessidade.

### 3.3 Threads e corrotinas

- **NUNCA** faça I/O (disco, rede, `PackageManager` pesado, parse de JSON grande) na main thread ou dentro de `@Composable`.
- **SEMPRE** use `viewModelScope`/`lifecycleScope`. **NUNCA** `GlobalScope`, `runBlocking` ou `CoroutineScope(Dispatchers.X)` solto.
- **SEMPRE** que um trabalho for substituído por outro mais novo (busca, reload, hero), use `collectLatest` / `produceState` / `LaunchedEffect(key)` para **cancelar o antigo**.
- **NUNCA** lance `scope.launch` dentro de `LaunchedEffect` (escapa do cancelamento).
- **SEMPRE** use `Dispatchers.IO` para disco/rede e `Dispatchers.Default` para CPU (hash, detecção de sistema, extrusão do logo).

### 3.4 Dados e carregamento

- **NUNCA** recarregue a lista inteira por qualquer evento. Eventos de pacote são **coalescidos** (`debounce(400)`), e mudanças pequenas (trocar capa) atualizam **só o item**.
- **NUNCA** faça rede dentro do carregamento da lista. Rede é **sob demanda**, para itens visíveis, e em segundo plano.
- **SEMPRE** guarde resultados negativos (cache negativo, ex.: 7 dias) para não repetir buscas que falharam.
- **SEMPRE** compartilhe a busca do jogo (um `sgdbGameId` por item) entre capa, hero e logo.
- **NUNCA** escaneie pastas inteiras a cada abertura. Use índice persistido e atualização incremental (`path + size + mtime`).
- **SEMPRE** use `id` estável por hash do **caminho completo** (nome de arquivo sozinho colide).
- **NUNCA** crie `Regex` dentro de função chamada em loop. Pré-compile em `val` de nível de arquivo/objeto.

### 3.5 Listas (LazyRow/LazyColumn)

- **SEMPRE** passe `key = { it.id }` e `contentType`.
- **NUNCA** crie objetos pesados (listas, `Brush`, `Regex`, formatadores) dentro do item sem `remember`.
- **SEMPRE** use `derivedStateOf` para estados derivados de scroll.
- **NUNCA** chame `animateScrollToItem` em toda repetição de tecla: durante repetição use `scrollToItem`.
- **SEMPRE** calcule filtros/agrupamentos **uma vez** (no ViewModel), nunca a cada tecla ou a cada recomposição.

### 3.6 Ciclo de vida

- **SEMPRE** registre receivers em `onStart` e remova em `onStop`. Se algo mudar enquanto parado, marque `dirty` e recarregue no `onResume`.
- **NUNCA** deixe trabalho pesado rodando enquanto um jogo/emulador está em primeiro plano (o launcher compete por CPU e RAM com ele).
- **SEMPRE** cancele jobs de repetição de input em `onStop`.

### 3.7 Build e dependências

- **SEMPRE** `isMinifyEnabled = true` e `isShrinkResources = true` em release.
- **NUNCA** adicione dependência sem justificar no PR: para que serve, tamanho que adiciona ao APK, alternativa sem dependência.
- **NUNCA** use `material-icons-extended` (vira dezenas de MB de DEX). Use ícones vetoriais próprios (só os que usa).
- **SEMPRE** compare o tamanho do APK antes/depois de qualquer dependência nova.
- **SEMPRE** mantenha o Baseline Profile atualizado quando a navegação principal mudar.

---

## 4. Regras de segurança

### 4.1 Segredos
- **NUNCA** coloque chave de API, token, senha ou keystore no código, em `strings.xml`, em `gradle.properties` versionado ou em qualquer arquivo versionado.
- Lugar certo: `local.properties` (no `.gitignore`) para desenvolvimento e **Secrets do GitHub** para CI.
- **Lembrete:** `BuildConfig` **não é cofre**. Qualquer valor embutido no APK pode ser extraído. Trate chave embutida como **pública** e revogável. O ideal é a **chave do próprio usuário**, informada no app.
- **NUNCA** imprima chave, token ou URL com chave em log (`Log.d`, exceções, telas de erro). Em release, remova logs verbosos via R8 (`-assumenosideeffects` para `Log.d/v`).
- Se a chave do usuário for salva, fica em armazenamento **privado** do app, nunca em pasta pública (`/sdcard`) e **fora do backup** (`allowBackup="false"` já está certo).
- **Se vazar:** siga a Seção 9.

### 4.2 Rede
- **SEMPRE** HTTPS. **NUNCA** `usesCleartextTraffic="true"` e nenhuma URL `http://` no código (o CI barra, ver Seção 6).
- **SEMPRE** um único `OkHttpClient` compartilhado, com timeouts explícitos (conexão e leitura), limite de tamanho de resposta e cache em disco.
- **NUNCA** confie em conteúdo remoto sem validar (tamanho máximo, tipo, esquema).
- **SEMPRE** limite concorrência (`Semaphore(2)`) e respeite limites do serviço.

### 4.3 Intents, broadcasts e componentes
- **SEMPRE** `android:exported="false"` por padrão. Só `MainActivity` (HOME/LAUNCHER) é exportada.
- **NUNCA** adicione `BroadcastReceiver` exportado ou "atalho de desenvolvimento" (ex.: o antigo receiver que recebia a chave por `adb`). Se precisar para depuração, só em `src/debug/`.
- **SEMPRE** use **intents explícitas** (com `ComponentName`/pacote) para abrir emuladores.
- **`emulators.json` e qualquer JSON editável/remoto é entrada NÃO CONFIÁVEL.** Valide antes de usar:
  - permitir só as actions `android.intent.action.VIEW` e `android.intent.action.MAIN`;
  - exigir `package` preenchido e instalado;
  - recusar componentes de pacotes do sistema/Configurações;
  - limitar tamanho do arquivo e quantidade de entradas;
  - recusar campos desconhecidos ou fora do esquema.
- **NUNCA** execute intent montada a partir de dado remoto sem passar pela validação acima.
- **NUNCA** use `PendingIntent` sem `FLAG_IMMUTABLE` (a menos que precise mesmo de mutável).

### 4.4 Arquivos e permissões
- **SEMPRE** o menor conjunto de permissões. Cada permissão nova exige justificativa no PR.
- **NUNCA** adicione `RECORD_AUDIO`, localização, contatos etc. sem necessidade real e visível ao usuário.
- **SEMPRE** prefira **SAF** (`content://` + `FLAG_GRANT_READ_URI_PERMISSION`) a caminhos físicos. **NUNCA** reintroduzir o hack do `StrictMode` para permitir `file://` em release.
- `MANAGE_EXTERNAL_STORAGE`: só se for impossível usar SAF, e com fluxo claro para o usuário conceder.
- **NUNCA** grave dados do app em pastas públicas sem necessidade; use `filesDir`/`cacheDir`.

### 4.5 Dependências e cadeia de suprimentos
- **SEMPRE** versões fixas (sem `+` nem `latest`).
- Ative **Dependabot** (alertas e PRs de atualização) no GitHub.
- Ative **Secret scanning + Push protection** nas configurações do repositório.
- **NUNCA** adicione biblioteca de fonte desconhecida/JitPack aleatório sem revisar (popularidade, manutenção, licença).
- Considere `gradle --write-verification-metadata sha256` para verificar checksums das dependências.

### 4.6 Release e assinatura
- Keystore **nunca** no repositório. Em CI via Secrets do GitHub (arquivo em base64 + senhas).
- **SEMPRE** assine o release; guarde backup do keystore em lugar seguro (se perder, não consegue atualizar o app).
- `.gitignore` obrigatório: `*.apk`, `*.aab`, `*.jks`, `*.keystore`, `local.properties`, `keystore.properties`, `scratch/`.

### 4.7 Privacidade e legalidade
- **NUNCA** adicione analytics/telemetria sem aviso e opção de desligar.
- Vídeos de intro de consoles (Nintendo/Sony/Microsoft) e capas são conteúdo protegido. **NUNCA** os coloque no repositório público nem no APK distribuído; deixe o usuário fornecer os seus (`/sdcard/Launcher/intros/`).
- Adicione uma `LICENSE` ao projeto e liste licenças de terceiros.

---

## 5. Fluxo de trabalho (do primeiro dia ao release)

### 5.1 Branches e commits
- `main` **protegida**: só entra por Pull Request (mesmo trabalhando sozinho; é o que permite o CI barrar problemas).
- Uma branch por tarefa: `feat/...`, `fix/...`, `perf/...`.
- Commits pequenos, no formato `tipo: o que mudou` (ex.: `perf: pré-renderiza extrusão do logo`).
- **NUNCA** `git add .` às cegas: revise `git status` e `git diff --staged` (evita commitar APK, keystore, `local.properties`).

### 5.2 Antes de começar uma tarefa
1. Qual regra deste documento essa mudança toca? (animação, imagem, rede, lista, permissão...)
2. Preciso de dependência nova? Se sim, justifique antes.
3. Se for UI/animação/lista: faça uma **medição antes** (Seção 2).

### 5.3 Antes de abrir o PR
```bash
./scripts/check-rules.sh          # regras automáticas (Seção 6)
./gradlew testDebugUnitTest lintDebug
./gradlew assembleRelease         # compila com R8
```
Depois, instale o **release** e rode o procedimento de medição se a mudança tocar UI, imagens, rede ou listas.

### 5.4 Checklist do Pull Request (cole em `.github/pull_request_template.md`)
```markdown
## O que mudou
(2-3 linhas)

## Checklist
### Performance
- [ ] Nenhum valor animado é lido no corpo da composição (só em lambdas/graphicsLayer)
- [ ] Nenhum Bitmap em modelo; imagens via Coil com tamanho do slot
- [ ] Nenhum I/O na main thread ou em @Composable
- [ ] Nenhum blur/RenderEffect/FLAG_BLUR_BEHIND/sombra animada nova
- [ ] Listas com key/contentType; cálculos pesados fora da composição
- [ ] Trabalho antigo é cancelado quando substituído (collectLatest/produceState)
- [ ] Medi em RELEASE (se mexeu em UI/imagem/rede/lista): janky ___%, p95 ___ms, PSS ___MB

### Segurança
- [ ] Nenhum segredo no diff (chave, token, keystore, senha)
- [ ] Sem http://, sem componente exportado novo, sem permissão nova (ou justificada abaixo)
- [ ] Entrada externa (JSON, intent, arquivo) é validada
- [ ] Dependência nova: justificativa + impacto no tamanho do APK: ___

### Qualidade
- [ ] Testes unitários adicionados/atualizados
- [ ] `check-rules.sh`, lint e testes passando
- [ ] README/docs atualizados se necessário
```

### 5.5 Merge e release
1. CI verde (Seção 6).
2. `docs/perf.md` atualizado com os números da versão.
3. `versionCode`/`versionName` incrementados.
4. Tag `vX.Y.Z`; APK assinado publicado em **GitHub Releases** com notas.
5. **Nunca** versionar o APK no git.

---

## 6. Automação: deixe o computador barrar os erros

### 6.1 Script de regras rápidas (`scripts/check-rules.sh`)
```bash
#!/usr/bin/env bash
# Falha se encontrar padrões proibidos. Para exceção justificada, adicione "// rule-ok: motivo" na linha.
set -u
fail=0
check() {  # $1=descrição  $2=regex  $3=caminho
  hits=$(grep -rnE "$2" $3 2>/dev/null | grep -v "rule-ok")
  if [ -n "$hits" ]; then echo "❌ $1"; echo "$hits"; echo; fail=1; fi
}

SRC="app/src/main"
check "blur ao vivo / blur de janela"       '\.blur\(|RenderEffect|FLAG_BLUR_BEHIND'         "$SRC/java"
check "GlobalScope / runBlocking"            'GlobalScope|runBlocking'                        "$SRC/java"
check "CoroutineScope solto"                 'CoroutineScope\(Dispatchers'                    "$SRC/java"
check "HTTP sem TLS"                         'http://'                                        "$SRC/java"
check "cleartext/backup no manifest"         'usesCleartextTraffic="true"|allowBackup="true"' "$SRC/AndroidManifest.xml"
check "icons-extended"                       'material-icons-extended'                        "app/build.gradle.kts"
check "permissão de áudio sem uso"           'RECORD_AUDIO'                                   "$SRC/AndroidManifest.xml"
check "possível segredo hardcoded"           '(api[_-]?key|secret|token|password)[^=\n]{0,20}=\s*"[A-Za-z0-9_\-]{20,}"' "$SRC/java"
check "Log de dados sensíveis"               'Log\.[dvi]\(.*(key|token|senha|password)'      "$SRC/java"
check "mutableMapOf como cache"              'memoryCache\s*=\s*mutableMapOf'                 "$SRC/java"

[ "$fail" -eq 0 ] && echo "✅ check-rules OK" || { echo "Corrija os itens acima."; exit 1; }
```
(`chmod +x scripts/check-rules.sh`.) Ajuste/estenda a lista sempre que um erro novo acontecer: **todo bug de performance/segurança vira uma regra automática**.

### 6.2 Verificação de segredos (gitleaks)
- Local, como hook de commit (`.git/hooks/pre-commit`): `gitleaks protect --staged`.
- No CI: use a action oficial do gitleaks. Também ligue **Secret scanning/Push protection** no GitHub.

### 6.3 Workflow de CI (`.github/workflows/build.yml`)
```yaml
name: CI
on: [push, pull_request]
jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with: { fetch-depth: 0 }
      - uses: gitleaks/gitleaks-action@v2
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./scripts/check-rules.sh
      - run: ./gradlew testDebugUnitTest lintDebug assembleRelease
      - name: Orçamento de tamanho do APK (10 MB)
        run: |
          APK=$(ls app/build/outputs/apk/release/*.apk | head -n1)
          SIZE=$(stat -c%s "$APK"); echo "APK: $SIZE bytes"
          [ "$SIZE" -le 10485760 ] || { echo "APK acima de 10 MB"; exit 1; }
      - uses: actions/upload-artifact@v4
        with: { name: nuvos-apk, path: app/build/outputs/apk/release/*.apk }
```
Em **Settings → Branches**, exija esse job verde para fazer merge na `main`.

### 6.4 Ferramentas que pegam erro cedo
- **StrictMode só em debug** (acusa I/O na main e vazamentos):
```kotlin
if (BuildConfig.DEBUG) {
    StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
    StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectLeakedClosableObjects().detectLeakedSqlLiteObjects().penaltyLog().build())
}
```
- **LeakCanary** em debug (vazamento de Activity/receiver).
- **Compose compiler reports** (acha composables instáveis que não "pulam"): ative `reportsDestination`/`metricsDestination` no plugin do compilador do Compose e revise de tempos em tempos.
- **Layout Inspector → Recomposition counts**: tela parada deve mostrar 0 recomposições/s.
- **JankStats** em debug para logar quedas de frame.
- **Macrobenchmark** (cold start e rolagem) rodando manualmente antes de cada release.
- **Detekt/ktlint** para estilo e alguns problemas de código.
- **Perfetto** quando um número piorar e você não souber por quê.

---

## 7. Regras para a IA que escreve o código

Cole o bloco abaixo em um arquivo `AGENTS.md` (ou o equivalente da sua ferramenta) na raiz do repositório, para a IA **sempre** ler antes de mexer no projeto.

```markdown
# Regras do projeto NuvOS (obrigatórias)

Leia `docs/BOAS-PRATICAS.md` antes de qualquer alteração.

## Proibido
- Segredos no código/versionados (chaves, tokens, keystore). Use local.properties/Secrets.
- Modifier.blur, RenderEffect, FLAG_BLUR_BEHIND, sombra/elevação animada.
- Ler State animado no corpo da composição (use lambdas/graphicsLayer).
- Bitmap/ImageBitmap em modelos de dados; decodificar imagem na main thread.
- I/O ou rede na main thread, GlobalScope, runBlocking, CoroutineScope solto.
- Recarregar a lista inteira por evento pequeno; rede dentro do carregamento da lista.
- mutableMapOf como cache (use LruCache por bytes).
- http://, cleartext, componente exportado novo, permissão nova sem justificativa.
- Dependência nova sem justificar função e impacto no tamanho do APK.
- material-icons-extended.

## Obrigatório
- Usar tokens de Motion.kt em animações; só transform/alpha em graphicsLayer.
- Coil com tamanho do slot; WebP; caches com limite.
- viewModelScope/lifecycleScope; collectLatest/produceState para cancelar trabalho velho.
- Entradas externas (JSON/intent/arquivo) validadas (allowlist de actions, esquema, tamanho).
- Rodar `./scripts/check-rules.sh`, testes e `assembleRelease` antes de concluir.
- Medir em RELEASE quando mexer em UI/imagem/rede/lista (procedimento do docs/BOAS-PRATICAS.md).

## Como reportar
- Nunca escrever "concluído" sem evidência: informe arquivos alterados e saída dos comandos.
- Listar explicitamente o que NÃO foi feito ou não foi medido.
- Se uma meta de performance não foi atingida, dizer o número real, sem mudar a meta.
- Mudanças pequenas e focadas; não refatorar o que não foi pedido.
```

Por que isso importa: no relatório anterior, itens do plano ficaram sem menção e uma meta (p95) foi relaxada de 16,6 ms para 20 ms sem aviso. A seção "Como reportar" existe para impedir isso.

---

## 8. Rotina periódica

### A cada Pull Request
- Checklist da Seção 5.4 e CI verde.

### A cada release
- [ ] Medição completa em **release** (Seção 2) em pelo menos **2 aparelhos** (um forte e um fraco)
- [ ] `docs/perf.md` atualizado
- [ ] Tamanho do APK dentro do orçamento
- [ ] Macrobenchmark rodado; Baseline Profile regenerado se a navegação mudou
- [ ] APK assinado, publicado em Releases, **sem** segredo/intro protegida dentro
- [ ] Revisar permissões do manifest (nada novo sem motivo)

### Todo mês
- [ ] Revisar PRs do Dependabot (atualizar e testar)
- [ ] Atualizar AGP/Kotlin/Compose BOM para a última estável e rodar toda a medição
- [ ] Revisar `check-rules.sh`: algum erro novo virou regra?
- [ ] Testar em um aparelho real fraco com biblioteca grande (200+ jogos)

### A cada trimestre
- [ ] Rodar `gitleaks detect` no histórico inteiro
- [ ] Revisar licenças de dependências e assets
- [ ] Reavaliar orçamentos (apertar, nunca afrouxar sem registrar o motivo em `docs/perf.md`)

---

## 9. Se algo der errado

### 9.1 Vazou uma chave/segredo
1. **Revogar** a chave no site do serviço (primeiro passo, antes de qualquer outra coisa).
2. Gerar uma nova e colocar em `local.properties`/Secrets.
3. Remover do código e do **histórico** (`git filter-repo --replace-text` ou BFG) e forçar o push (avise colaboradores).
4. Procurar o mesmo valor em outros lugares (`grep -r`, forks, releases já publicados).
5. Registrar o incidente e criar uma **regra no `check-rules.sh`** para impedir de novo.

### 9.2 Performance piorou
1. Reproduza com o procedimento da Seção 2 (release, mesmo aparelho).
2. Descubra **qual commit** piorou (`git bisect` com a medição).
3. Abra o Layout Inspector (recomposições) e o Perfetto no ponto lento.
4. Corrija e **adicione uma regra** (script ou checklist) para o erro não voltar.
5. Atualize `docs/perf.md` com a causa.

### 9.3 O app está sendo morto/lento com emulador aberto
- Verifique se há trabalho em segundo plano (receivers fora de `onStart/onStop`, jobs sem cancelamento) e consumo de RAM (`dumpsys meminfo`). Launcher precisa ficar quase "dormindo" enquanto o jogo roda.

---

## 10. Próximos passos sugeridos (ordem)

1. **Confirmar pendências** da Seção 1 (principalmente: chave revogada, Baseline Profile, ViewModel/Overlay, índice de ROMs).
2. Criar `scripts/check-rules.sh`, `.github/pull_request_template.md`, `AGENTS.md` e `docs/BOAS-PRATICAS.md` (este arquivo) e proteger a `main`.
3. Ligar CI com gitleaks + orçamento de APK.
4. Refazer a medição em **release** e em um **aparelho fraco**; atacar PSS (< 250 MB) e p95 (< 16,6 ms).
5. Gerar Baseline Profile e medir cold start em release.
6. Só depois de tudo isso estável, voltar a adicionar funcionalidades — sempre dentro do fluxo da Seção 5.

---

## 11. Novas Regras e Padrões da Versão 1.1

### 11.1 Sistema de Cores e Tokens Neutros
- Cores de botões, glifos de controle (A/B/X/Y/LB/RB) e diálogos devem derivar exclusivamente de `Theme.kt` (`NeutralBtnBg`, `NeutralBtnBgFocused`, `FocusRing`, `ScrimColor`, etc.).
- Proibido hardcodear `Color(0x...)` diretamente em novos componentes sem justificativa marcada com `// rule-ok: <motivo>`.
- Ações destrutivas (ex: Remover, Desinstalar) usam confirmação obrigatória via `ConfirmActionDialog`.

### 11.2 Atalhos Fixados de Nuvem (PinItemRequest)
- Atalhos de streaming e web apps devem ser recebidos via `android.content.pm.action.CONFIRM_PIN_SHORTCUT` e lançados estritamente via `LauncherApps.startShortcut(...)`.
- Nunca montar Intents avulsas a partir de dados não confiáveis de atalhos.

### 11.3 Organização e Segurança de ROMs
- Operações de organização de ROMs devem ser estritamente puras na fase de planejamento (`RomOrganizer.plan`).
- Apenas renomeações atômicas no mesmo sistema de arquivos (`renameTo`) são permitidas. Chamadas de deleção física de ROMs são expressamente proibidas.

### 11.4 Tipografia e Fontes Embutidas
- Títulos: **Chakra Petch** (OFL).
- Corpo: **Rajdhani** (OFL).
- Ambas devem estar embutidas localmente em `res/font` com cobertura completa para caracteres e acentos da língua portuguesa.

---

## Resumo de uma página (cole na parede)

**Antes de codar:** qual regra isso toca? Preciso medir antes?
**Ao codar:** valor animado só em `graphicsLayer`; imagem via Coil com tamanho; nada na main thread; trabalho velho é cancelado; cache com limite; nada de blur; nada de segredo.
**Antes do PR:** `check-rules.sh` + testes + `assembleRelease` + medição em **release**.
**No PR:** checklist completo, CI verde, números preenchidos.
**No release:** 2 aparelhos, `docs/perf.md`, APK assinado e sem conteúdo protegido.
**Se errar:** corrija e **transforme o erro em regra automática**.
