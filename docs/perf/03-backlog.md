# NuvOS — 03. Backlog de Otimização Priorizado

**Critério de Ordenação:**  
`Prioridade = (Impacto × Frequência) ÷ (Risco + Esforço)`  
Critério de desempate: Startup → Primeiro Frame → Rolagem/Frames → CPU Ociosa → Memória → APK → Higiene.

---

| ID | Área | Evidência (arquivo:linha) | Hipótese de Ganho | Métrica-Alvo | Imp (1-5) | Freq (1-5) | Risco (1-5) | Esf (1-5) | Prioridade |
|---|---|---|---|---|---|---|---|---|---|
| **PERF-01** | G / D | `GameCard.kt:80-92` | Executar `specular_breath` (`rememberInfiniteTransition`) estritamente quando `isSelected == true`, evitando que dezenas de cards inativos invalidem frames | CPU Ociosa (51.7% → < 10%) | 5 | 5 | 1 | 1 | **12.5** |
| **PERF-02** | D / G | `GameLogo3D.kt:70-85` | Pausar loop contínuo de rotação senoidal 3D quando diálogos estiverem abertos ou quando a janela estiver ociosa | CPU Ociosa & Bateria | 4 | 5 | 1 | 1 | **10.0** |
| **PERF-08** | E | `MainActivity.kt:1540-1570` | Throttle de 16ms no `onGenericMotionEvent` do analógico para eliminar os 60 eventos de *High input latency* do gfxinfo | Janky frames & Input latency | 4 | 4 | 1 | 1 | **8.0** |
| **PERF-06** | G | `TopBar.kt:74-79` | Substituir polling contínuo `while(true) delay(10000)` do relógio por `BroadcastReceiver(ACTION_TIME_TICK)` | CPU Ociosa & Wakelocks | 3 | 5 | 1 | 1 | **7.5** |
| **PERF-03** | D | `GameCard.kt:100-115`, `DeckHomeScreen.kt:600-615` | Adicionar `@Immutable` nas classes de modelo (`AppItem`, `AppFolder`), `contentType = { "game_card" }` no `LazyRow`, e mover leitura de `sel.value` para lambda gráfica | Janky frames (34.8% → < 5%), p95 frame time | 5 | 5 | 2 | 2 | **6.25** |
| **PERF-05** | A | `gradle.properties:1-4` | Ativar `org.gradle.parallel=true`, `org.gradle.caching=true` e `org.gradle.configuration-cache=true` | Tempo de build limpo e incremental | 3 | 4 | 1 | 1 | **6.0** |
| **PERF-04** | B | `MainActivity.kt:780-870` | Inicializar lista de apps a partir de cache persistido em disco para renderizar o primeiro frame instantaneamente sem exibir spinner de loading | Cold Start (782ms → < 450ms) | 5 | 4 | 2 | 2 | **5.0** |
| **PERF-07** | F | `IconCache.kt:19-35`, `MainActivity.kt:730-745` | Adicionar `onTrimMemory` limpando `IconCache` em situações de `TRIM_MEMORY_RUNNING_CRITICAL` e otimizar decodificação | PSS de Memória (520MB → < 350MB) | 4 | 3 | 1 | 2 | **4.0** |
