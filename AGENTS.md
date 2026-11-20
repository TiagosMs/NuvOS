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
