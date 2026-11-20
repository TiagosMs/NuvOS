# Métricas de Performance — NuvOS

Dispositivo de medição: Samsung Galaxy Tab S6 Lite 2024 (`SM-P620` / `RX2X700QMPP`, Android 14, 1200×2000 @ 240 dpi)
Data da medição inicial (versão limpa instalada no tablet): 2026-11-20

| Métrica | Antes (Baseline) | Meta | Depois (Refatoração) |
|---|---|---|---|
| Cold start (TotalTime) | 1443 ms | < 700 ms | 987 ms (debug) |
| Janky frames (%) | 16.58 % | < 5 % | 1.07 % |
| Frame time p95 (GPU) | 113 ms | < 20 ms | 20 ms |
| PSS total | 342 MB (Graphics 113MB) | < 250 MB | 287 MB (Graphics 61MB) |
| Tamanho do APK | 21 MB (Debug) | < 10 MB (Release) | 6.8 MB (Release R8) |
