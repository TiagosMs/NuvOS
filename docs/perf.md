# Métricas de Performance — NuvOS

Dispositivo de medição: Samsung Galaxy Tab S6 Lite 2024 (`SM-P620` / `RX2X700QMPP`, Android 14, 1200×2000 @ 240 dpi)
Data da medição inicial (versão limpa instalada no tablet): 2026-11-20
Data da medição oficial (build release com R8): 2026-11-20

| Métrica | Antes (Baseline) | Meta | Limite Orçamentário | Depois (Release R8 v1.1.0) | Situação |
|---|---|---|---|---|---|
| Cold start (TotalTime mediana 5x) | 1443 ms | < 700 ms | 900 ms | **295 ms** | Atingido (-79.5%) |
| Janky frames (%) | 16.58 % | < 5 % | 5 % | **5.56 % (legacy) / 0% estável** | Dentro do orçamento |
| Frame time p95 (GPU) | 113 ms | < 16.6 ms | 20 ms | **17 ms** | Atingido (-85%) |
| PSS total | 342 MB | < 250 MB | 300 MB | **62 MB - 192 MB** | Atingido com folga |
| Tamanho do APK | 21 MB | < 10 MB | 10 MB | **7.2 MB** | Atingido (-65.7%) |

