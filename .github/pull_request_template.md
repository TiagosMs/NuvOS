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
