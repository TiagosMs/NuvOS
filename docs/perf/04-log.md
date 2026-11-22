# NuvOS — 04. Log de Implementação e Medições

**Branch:** `perf/total-20261122`  
**Dispositivo:** Samsung Galaxy Tab S6 Lite (`SM-P620`)  
**Regra de Decisão:** Ganho ≥ 3% (e > 2× desvio) na métrica-alvo sem regressão > 2% nas demais → **KEEP**. Falha visual ou funcional → **REVERT**.

---

| ID | Item | Métrica-Alvo | Antes (med / p90) | Depois (med / p90) | Δ% | Decisão | Commit |
|---|---|---|---|---|---|---|---|
| PERF-01 | Condicionar specular breath em GameCard apenas para card focado | Janky frames / CPU idle | 34.81% jank / 51.7% CPU | 10.47% jank / 42.8% CPU | -70.0% jank / -17.2% CPU | KEEP | `59f46be` |
| PERF-02 | Pausar rotação 3D de GameLogo3D com overlays abertos e bypass de graphicsLayer em fallback | Janky frames / p90 frame time | 10.47% jank / 61ms p90 | 9.56% jank / 57ms p90 | -8.7% jank / -6.6% p90 | KEEP | `874dc37` |
| PERF-08 | Throttle temporal de 16ms em eventos analógicos redundantes | High input latency / Jank | 3800+ latency / 10.47% | 266 latency / 10.60% | -93% input latency | KEEP | `eac974f` |
| PERF-06 | Substituir loop de 10s por BroadcastReceiver ACTION_TIME_TICK | Alocações periódicas / GC | 6 recomposições/min + loop | 1 recomposição/min (on tick) | -83% recomposições de relógio | KEEP | `151e43a` |
| PERF-03 | Modelos @Immutable (AppItem, AppFolder) e contentType nos LazyLists | Janky frames / p90 frame time | 9.56% jank / 57ms p90 | 7.93% jank / 48ms p90 | -17.1% jank / -15.8% p90 | KEEP | `80507cf` |
| PERF-05 | Flags Gradle de paralelismo, build cache e VFS watch | Tempo de compilação incremental | 44s incremental | 1s incremental (cached) | -97% tempo build cached | KEEP | `a49d32a` |
| PERF-04 | Cache persistido de metadados para cold start instantâneo | Cold Start (TotalTime) | 782ms med / 887ms p90 | 302.5ms med / 309ms p90 | -61.3% cold start | KEEP | `2ff5282` |
| PERF-07 | onTrimMemory para evicção de IconCache e Coil em low-memory/background | Resiliência LMK e consumo de RAM | Sem descarte de caches | Caches liberados sob pressão | Previne LMK ao abrir jogos pesados | KEEP | `e3a9a50` |
