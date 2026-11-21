# NuvOS — Gaming & Media Launcher para Android

NuvOS é um launcher moderno, rápido e leve para handhelds, tablets e dispositivos Android em formato paisagem (16:9 / widescreen), inspirado na experiência visual de consoles (Nintendo Switch, Steam Deck).

## Recursos

- **Interface estilo Console:** Navegação fluida otimizada para controles físicos (D-Pad, analógicos, botões A/B/X/Y) e toque.
- **Logos 3D Volumétricos:** Extrusão 3D e física de flutuação em tempo real com iluminação ambiente.
- **Biblioteca Unificada:** Abas para Jogos nativos/nuvem (xCloud), Emuladores e Aplicativos em pastas organizadas por categorias.
- **Integração SteamGridDB e iiDB:** Busca automatizada de capas, planos de fundo (heroes), logos oficiais em alta resolução e músicas tema.
- **Gerenciador e Organizador de ROMs:** Detecção por extensão declarativa e organização segura em subpastas com diário de alterações.
- **Atalhos de Jogos em Nuvem via Android PinItemRequest:** Suporte universal a jogos via Better xCloud, Xbox Cloud Gaming, GeForce NOW e navegadores (Chrome/Edge/Firefox).

## Como Usar Atalhos de Jogos em Nuvem (Cloud Gaming)

O NuvOS suporta a API padrão do Android de atalhos fixados (`PinItemRequest`):
1. Defina o NuvOS como Launcher padrão do sistema (ou selecione-o quando o Android solicitar).
2. No seu app de streaming favorito (ex: **Better xCloud**, **Xbox Cloud Gaming / Edge**, **GeForce NOW**):
   - Abra a página do jogo desejado.
   - Selecione a opção "Criar Atalho" ou "Adicionar à Tela Inicial".
3. O NuvOS exibirá uma tela de confirmação para fixar o atalho.
4. O jogo aparecerá diretamente na aba **Jogos**, com suporte a personalização de metadados, capas e lançamento em tela cheia sem URLs fixas no código.

## Compilação

Requisitos:
- JDK 17+
- Android SDK (API 34)

```bash
# Compilar APK Debug
./gradlew assembleDebug

# Compilar APK Release otimizado com R8
./gradlew assembleRelease
```

### Configuração da Chave SteamGridDB

Para configurar uma chave de API própria:
1. Obtenha uma chave gratuita em [SteamGridDB](https://www.steamgriddb.com/profile/preferences/api).
2. Configure diretamente no diálogo inicial do app ou adicione em `local.properties`:
```properties
sgdb.apiKey=SUA_CHAVE_AQUI
```

## Licença

MIT License — veja o arquivo [LICENSE](LICENSE).
