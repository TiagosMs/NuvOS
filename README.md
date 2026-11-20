# NuvOS — Gaming & Media Launcher para Android

NuvOS é um launcher moderno, rápido e leve para handhelds, tablets e dispositivos Android em formato paisagem (16:9 / widescreen), inspirado na experiência visual de consoles (Nintendo Switch, Steam Deck).

## Recursos

- **Interface estilo Console:** Navegação fluida otimizada para controles físicos (D-Pad, analógicos, botões A/B/X/Y) e toque.
- **Logos 3D Volumétricos:** Extrusão 3D e física de flutuação em tempo real com iluminação ambiente.
- **Biblioteca Unificada:** Abas para Jogos nativos/nuvem (xCloud), Emuladores e Aplicativos em pastas organizadas por categorias.
- **Integração SteamGridDB:** Busca automatizada de capas, planos de fundo (heroes) e logos oficiais em alta resolução.
- **Gerenciador de ROMs e Emuladores:** Detecção e lançamento direto de jogos retro.

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
