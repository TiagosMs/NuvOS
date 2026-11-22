# NuvOS — REGRESSION-GUARD.md

**Data de Fixação:** 2026-11-22  
**Dispositivo Referência:** Samsung Galaxy Tab S6 Lite (`SM-P620`), Android SDK 36, Painel 60 Hz (16.66 ms frame budget).

---

## 1. Orçamento Estrito de Métricas (Performance Budgets)

Qualquer commit ou Pull Request que violar os limites absolutos abaixo deve ser rejeitado imediatamente (**fail-closed**):

| Métrica | Baseline Inicial | Valor Alcançado (Pós-Perf) | Limite Máximo Permitido (Guard) | Regra de Quebra (CI/Local) |
|---|---|---|---|---|
| **Cold Start (Mediana)** | 782.0 ms | **302.5 ms** | **400.0 ms** | > 400 ms (+32% do alcançado) |
| **Cold Start (p90)** | 887.0 ms | **309.0 ms** | **450.0 ms** | > 450 ms |
| **Janky Frames (Scroll Carrossel)** | 34.81% | **6.59%** | **10.0%** | > 10.0% (+50% do alcançado) |
| **Frame Time (p90)** | 150.0 ms | **53.0 ms** | **60.0 ms** | > 60 ms |
| **Frame Time (p95)** | 200.0 ms | **65.0 ms** | **75.0 ms** | > 75 ms |
| **Memória Total PSS** | 520 MB | **294 MB** | **350 MB** | > 350 MB |
| **CPU Ociosa (Home parada)** | 51.7% | **37.9%** | **42.0%** | > 42.0% |
| **Tamanho do APK Release** | 8.21 MB | **8.21 MB** | **9.00 MB** | > 9.0 MB |

---

## 2. Regras de Execução e Automação

1. **Execução Pré-Merge:**
   - Todo desenvolvedor ou agente deve rodar `./tools/perf-baseline.sh` antes de abrir PR ou mesclar na branch principal.
   - O comando deve reportar `PASS` em todas as métricas medidas.

2. **Detecção de Regressão e Ação:**
   - Se uma métrica piorar mais que **5%** sem justificativa explícita de nova funcionalidade: **REVERTER** ou **ISOLAR** a alteração.
   - Proibido rodar loops de animação contínuos (`rememberInfiniteTransition`) fora de elementos ativamente visíveis e selecionados.
   - Proibido alocar objetos de data e formatação em blocos de composição contínua (usar `remember` ou callbacks de evento do sistema operacional).
   - Proibido despachar eventos de entrada em taxa superior a 60 Hz sem debounce/throttling temporal.
