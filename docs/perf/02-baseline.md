# NuvOS — 02. Medições de Baseline (Pré-Otimização Total)

**Data:** 2026-11-22  
**Commit base:** `9aac623` (`feat/v1.1` -> `perf/total-20261122`)  
**Dispositivo:** Samsung Galaxy Tab S6 Lite (`SM-P620`), Android 14/15 (SDK 36)  
**Taxa do painel:** 60.0 Hz (Frame Budget: 16.66 ms)  
**Build:** Release minificado com R8 (`app-release.apk`)  

---

## 1. Condições Padronizadas de Teste
- Aparelho conectado via USB com depuração ADB ativa
- Apps de segundo plano finalizados (`am kill-all`)
- Estado de compilação resetado pós-instalação (`cmd package compile --reset com.decklauncher`)
- Amostragem: 10 repetições para cold start; 2 rodadas completas de validação de repetibilidade

---

## 2. Resultados Consolidados de Baseline

| Métrica | Rodada 1 | Rodada 2 | Desvio | Baseline Final (Mediana / p90) |
|---|---|---|---|---|
| **Cold Start TotalTime** | Med: 782.0ms / p90: 887ms | Med: 766.5ms / p90: 868ms | 1.98% (< 5%) | **782.0 ms (p90: 887 ms)** |
| **Janky Frames (C3/C4 Scroll)** | 34.81% (55/158 frames) | - | - | **34.81%** |
| **Frame Time p90 (C3/C4)** | 150 ms | - | - | **150 ms** |
| **Frame Time p95 (C3/C4)** | 200 ms | - | - | **200 ms** |
| **Total PSS (Memória)** | 520.062 KB (~520 MB) | - | - | **520 MB** |
| **Graphics Memory** | 391.699 KB (~391 MB) | - | - | **391 MB** |
| **Java Heap** | 14.928 KB (~14.9 MB) | - | - | **14.9 MB** |
| **Native Heap** | 70.292 KB (~70.3 MB) | - | - | **70.3 MB** |
| **CPU Ociosa (Home parada)** | 51.7% | - | - | **51.7%** (Gargalo crítico de animação contínua) |
| **Tamanho do APK Release** | 8.21 MB (8.608.826 bytes) | - | - | **8.21 MB** |

---

## 3. Diagnóstico dos Principais Gargalos Identificados

1. **CPU Ociosa Crítica (~51.7%)**:
   - `rememberInfiniteTransition` executando continuamente em `GameLogo3D.kt` (rotação 3D) e em **todos** os `GameCard.kt` (`specular_breath`), invalidando a recomposição e o frame loop SKIA a cada 16ms mesmo com o launcher parado na home.
2. **Janky Frames Altos no Carrossel (34.81%)**:
   - Recomposição pesada de itens não visíveis em `LazyRow`
   - Alocações em `graphicsLayer` e cálculos de animação contínuos
   - Re-decodificação de capas via Coil em cards vizinhos sem chaves estáveis
3. **Cold Start (~782 ms)**:
   - Bloqueio inicial em `loadInstalledApps()` realizando queries completas no `PackageManager` e escaneando o disco de ROMs em tempo real sem renderizar primeiro frame de cache.
4. **Memória Gráfica (391 MB)**:
   - Texturas não comprimidas mantidas no pipeline Skia e buffers do Gralloc para múltiplos layers translúcidos de tela cheia.
