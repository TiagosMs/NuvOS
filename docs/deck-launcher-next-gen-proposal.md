# NuvOS / Deck Launcher — Proposta de Evolução UI/UX Next-Gen
**Documento Executivo de Inovação e Novas Funcionalidades**  
**Data:** 22 de Novembro de 2026  
**Autor:** Especialista Sênior em Arquitetura de UI/UX de Consoles  
**Destino:** `/home/tiago/Downloads/deck-launcher-next-gen-proposal.md`

---

## 1. Premissa: O Conceito Original Elevado ao Estado da Arte

O **NuvOS** já consolidou uma base técnica robusta e invejável:
- Navegação nativa horizontal fluida com suporte total a controles físicos (Gamepad/D-Pad).
- Separação clara em 3 abas fundamentais: **Jogos**, **Emuladores** e **Apps**.
- Palco central com **Logo 3D flutuante** e **Hero Background** imersivo.
- Integração de metadados rica (SteamGridDB, iiDB) com BGM e soundbites.
- Código limpo, sem dependências inchadas, rodando a 120Hz com taxa de jank inferior a 0.25%.

A presente proposta **mantém 100% dessa estrutura e conceitos originais**. Nenhuma tela será descaracterizada; em vez disso, propomos **recursos de nível console de nova geração (PS5, SteamOS 3.5 e Switch OLED)** que transformam o launcher em uma experiência de hardware premium.

---

## 2. As 7 Inovações Propostas (Next-Gen Features)

```
+-----------------------------------------------------------------------------------------+
|                                    NUVOS NEXT-GEN MAP                                   |
+-----------------------------------------------------------------------------------------+
| [1. Alive Canvas]       Giroscópio / Right-Stick Parallax reativo em 3D                |
| [2. Game Hub Sheet]     D-Pad Down revela detalhes, tempo de jogo e trailers            |
| [3. Quick Access Bar]   Menu lateral estilo Steam Deck (...) para ajustes instantâneos |
| [4. Adaptive Aura]      Iluminação ambiente OLED baseada na cor dominante do jogo       |
| [5. Dynamic Audio]      Pitch modulation ao navegar rápido no carrossel                |
| [6. Smart Sorting]      Fila inteligente (Últimos jogados na frente + Favoritos)       |
| [7. 10-Foot TV Engine]  Modo Docked automático para TV/Monitor via USB-C HDMI           |
+-----------------------------------------------------------------------------------------+
```

---

### Inovação 1: "Alive Canvas" — Paralaxe Reativo com Giroscópio e Analógico Direito
- **Conceito**: Em consoles de mesa como o PS5 e no próprio 3DS, a tela de repouso não é estática. 
- **O que faz**:
  - Aproveita o sensor de giroscópio do tablet (ou o **Right Stick / R3** do gamepad, que hoje está ocioso na Home).
  - Ao inclinar o tablet ou mover o analógico direito suavemente:
    - O fundo **Hero** se desloca sutilmente em profundidade (multi-plane depth: 4px).
    - O **Logo 3D** rotaciona e ganha iluminação especular dinâmica (offset de 12px a 16px).
    - A sensação tátil é de olhar para uma vitrine física real de vidro e luz.
- **Impacto no Usuário**: Sensação imediata de sofisticação e hardware vivo sem consumo perceptível de bateria (processado estritamente em `graphicsLayer` com 0 recomposição).

---

### Inovação 2: "Game Hub Sheet" (Expansão com D-Pad Down)
- **Conceito**: Hoje, pressionar `D-PAD DOWN` no carrossel move o foco para a BottomBar.
- **O que faz**:
  - No card selecionado, a indicação sutil `[▼ Ver Detalhes]` convida o jogador a puxar para baixo.
  - Ao pressionar `D-PAD DOWN`, um **painel elegante translúcido (Glassmorphism OLED)** sobe suavemente da base, revelando informações ricas do jogo sem sair da tela:
    1. **Tempo Jogado & Última Sessão** ("Jogado por 14h", "Última sessão: Hoje às 10:30").
    2. **Badges de Plataforma & Resolução** (ex: badge oficial `Nintendo DS` ou `PlayStation 2` com a cor característica do sistema, "60 FPS", "Compatível com Controle").
    3. **Sinopse do Jogo** (obtida automaticamente via iiDB / SteamGridDB).
    4. **Mini-Ações**: "Iniciar do Início", "Continuar do Último Save", "Gerenciar Conquistas/Saves".
  - Pressionar `B` ou `D-PAD UP` recolhe o Hub instantaneamente com retorno suave ao carrossel.
- **Impacto no Usuário**: Resolve a limitação de telas de jogos onde você só vê a capa. Transforma uma simples lista de ROMs em uma central completa de entretenimento estilo Steam Deck.

---

### Inovação 3: "NuvOS Quick Access Menu" (Menu Lateral Estilo Steam Deck `...`)
- **Conceito**: No Steam Deck e Xbox, o jogador nunca sai do jogo ou navega por menus de sistema para tarefas triviais.
- **O que faz**:
  - Pressionar um botão de atalho dedicado (segurar `SELECT`, ou botão `Home / Guide` do controle, ou swipe da borda direita da tela) desliza um menu compacto de 320dp da borda direita:
    - **Brilho da Tela e Volume Geral**: Sliders manipuláveis via D-pad horizontal sem abrir a barra de notificações do Android.
    - **Volume da Música Tema (BGM)**: Ajuste fino do som de fundo do launcher.
    - **Alternador de Taxa de Atualização**: Toggle rápido entre 60Hz (economia) e 120Hz (máxima fluidez).
    - **Busca Rápida de Jogos**: Pular direto para um jogo digitando ou selecionando a letra do alfabeto (`A`, `B`, `C`...).
- **Impacto no Usuário**: Elimina a necessidade de recorrer à interface do Android, mantendo o usuário 100% imerso na experiência de console dedicado.

---

### Inovação 4: "Adaptive Aura" (Iluminação Ambiente OLED Reativa)
- **Conceito**: O Unslop eliminou os halos difusos desnecessários em volta dos cards para evitar poluição visual. No entanto, a iluminação ambiente na **borda superior e no topo da tela** (estilo Ambilight / PS5) cria uma atmosfera rica e calma.
- **O que faz**:
  - Quando um jogo é focado, o sistema extrai 2 cores dominantes da sua capa (ex: azul safira de um Sonic, vermelho carmesim de um Persona).
  - Um gradiente ultra-suave e escurecido (com transparência de 85% a 95%) tinge a atmosfera do topo da tela, sincronizado com o crossfade do hero background.
  - Ao entrar em jogos OLED puros, o fundo mantém o preto absoluto (#000000), preservando a economia de energia e o contraste infinito.
- **Impacto no Usuário**: Cada jogo "possui" seu próprio universo estético ao navegar, tornando o launcher dinâmico sem parecer barulhento.

---

### Inovação 5: "Dynamic Audio & Pitch Modulation"
- **Conceito**: A Nintendo utiliza uma técnica sonora brilhante no Wii, Wii U e Switch: quando você clica em itens em sequência rápida, a frequência dos cliques sobe musicalmente (escala pentatônica ou micro-pitch shift).
- **O que faz**:
  - Ao navegar devagar: o som clássico `nav_move` toca na frequência natural (1.0x).
  - Ao manter o D-Pad pressionado para rolagem rápida: a cada 3 cards ultrapassados, o som sobe ligeiramente de tom (`1.0x -> 1.05x -> 1.10x -> 1.15x`) e reseta ao parar.
  - Adição de um **som ambiente sutil de fundo** ("Lounge Ambient Tone") sintetizado com ganho muito baixo (-26dB), desligável no menu rápido, que traz a assinatura acolhedora de um console ligado na sala.
- **Impacto no Usuário**: Transforma o feedback tátil sonoro de um "barulho repetitivo" em uma melodia fluida e prazerosa de operar.

---

### Inovação 6: "Smart Queue" (Fila Inteligente de Jogos Recentes e Fixados)
- **Conceito**: Em consoles modernos, o primeiro card da esquerda é sempre o que você jogou por último.
- **O que faz**:
  - Organização automática do Carrossel em 3 modos selecionáveis via botão `X`:
    1. **Modo Recentes (Padrão de Console)**: O último jogo executado vai automaticamente para a primeira posição, com um micro-badge discreto "Última sessão".
    2. **Modo Plataforma**: Jogos agrupados por sistema (GBA, SNES, PS2, Android).
    3. **Modo Alfabético**: Ordem clássica de catálogo de colecionador.
  - Jogos fixados com alfinete (Pinned) permanecem sempre em posição de honra no início da fila.
- **Impacto no Usuário**: Elimina a necessidade de rolar 50 itens para encontrar o jogo que você estava jogando há 10 minutos.

---

### Inovação 7: "10-Foot TV Engine" (Modo Dock / Conexão HDMI Automática)
- **Conceito**: O Snapdragon 865 suporta saída de vídeo DisplayPort sobre USB-C (Samsung DeX e espelhamento nativo).
- **O que faz**:
  - O launcher detecta a conexão de um display externo ou mudança de densidade para proporção de TV de sala.
  - O layout se reconfigura automaticamente para a escala ergonômica **10-Foot UI** (leitura confortável a 3 metros de distância do sofá):
    - Fontes aumentam proporcionalmente em 25%.
    - Espaçamento de margens laterais é ajustado para evitar corte de overscan de televisores antigos.
    - O consumo de bateria entra em perfil de alta performance (120Hz locked).
- **Impacto no Usuário**: O tablet se torna um Nintendo Switch legítimo: na mão é um portátil ultrarrápido; conectado à TV da sala com um controle Bluetooth, vira um console de mesa.

---

## 3. Matriz de Prioridade & Complexidade Técnica

| Funcionalidade | Impacto UX | Complexidade | Arquitetura Envolvida |
| :--- | :--- | :--- | :--- |
| **1. Alive Canvas (Right Stick Parallax)** | ★★★★★ (Alto) | Baixa | `DeckHomeScreen.kt` + `graphicsLayer` (sem recomposição) |
| **2. Game Hub Sheet (D-Pad Down)** | ★★★★★ (Alto) | Média | Novo componente `GameHubSheet.kt` integrado ao carrossel |
| **3. Quick Access Menu Lateral** | ★★★★★ (Alto) | Média | Novo componente `QuickAccessDrawer.kt` com `WindowManager` |
| **4. Adaptive Aura (Cores OLED)** | ★★★★☆ (Médio) | Baixa | Palette extraction assíncrona no `HeroManager.kt` |
| **5. Dynamic Pitch Audio** | ★★★★☆ (Médio) | Baixa | `SoundEffectManager.kt` via `SoundPool.setRate` |
| **6. Smart Queue (Recentes na frente)**| ★★★★☆ (Médio) | Baixa | `OrderManager.kt` com persistência de `lastLaunchedTimestamp` |
| **7. 10-Foot TV Engine** | ★★★☆☆ (Específico)| Média | `LocalConfiguration` com detecção de display secundário |

---

## 4. Plano de Implementação Sugerido em Fases

Para garantir estabilidade absoluta e testes em hardware real via ADB sem regressões, propomos a execução em **3 Fases**:

- **Fase 1 (Imersão Tátil & Áudio)**:
  - Implementar **Alive Canvas** (Right Stick & Giroscópio com paralaxe no Hero e Logo 3D).
  - Implementar **Dynamic Pitch Audio** no `SoundEffectManager` (elevação de tom em navegação rápida).
  - Implementar **Smart Queue** (últimos jogados automaticamente no início do carrossel).

- **Fase 2 (Game Hub & Quick Menu)**:
  - Criar o **Game Hub Sheet** expansível via `D-PAD DOWN` com tempo de jogo, sinopse e badges de sistema.
  - Criar o **Quick Access Menu Lateral** (`...` / segurar `SELECT`) com controles de brilho, volume e busca rápida.

- **Fase 3 (Atmosfera & Docked Mode)**:
  - Implementar a **Adaptive Aura** reativa à paleta de cada jogo.
  - Implementar a adaptação automática para modo **TV / Docked**.

---

## 5. Próximo Passo

Este documento está disponível em `/home/tiago/Downloads/deck-launcher-next-gen-proposal.md`.

Diga qual fase ou funcionalidade prefere que comecemos a construir primeiro, ou se aprova a execução integral a partir da **Fase 1**!
