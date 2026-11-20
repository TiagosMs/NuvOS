# Métricas de Performance — NuvOS

Dispositivo de medição: Samsung Galaxy Tab S6 Lite 2024 (`SM-P620` / `RX2X700QMPP`, Android 14, 1200×2000 @ 240 dpi)
Data da medição inicial (versão limpa instalada no tablet): 2026-11-20
Data da medição oficial (build release com R8): 2026-11-20

| Métrica | Antes (Baseline) | Meta | Limite Orçamentário | Depois (Release R8) | Situação |
|---|---|---|---|---|---|
| Cold start (TotalTime mediana 5x) | 1443 ms | < 700 ms | 900 ms | **285 ms** | Atingido (-80%) |
| Janky frames (%) | 16.58 % | < 5 % | 5 % | **0.58 %** | Atingido (-96%) |
| Frame time p95 (GPU) | 113 ms | < 16.6 ms | 20 ms | **20 ms** | No limite orçamentário |
| PSS total | 342 MB | < 250 MB | 300 MB | **266 MB** | Dentro do orçamento |
| Tamanho do APK | 21 MB | < 10 MB | 10 MB | **6.8 MB** | Atingido (-67%) |

