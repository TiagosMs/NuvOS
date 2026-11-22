# Proposta de Motion Design, Fluidez de Console e Design System Global
**Projeto:** NuvOS (Deck Launcher)  
**Autor:** Console Motion & Ergonomics UX Team  
**Data:** 22 de Novembro de 2026  
**Status:** Proposta de Arquitetura Visual & Física de Movimento  

---

## 1. Diagnóstico do Estado Atual & Filosofia de Console

Atualmente, o launcher opera de forma responsiva, porém suas animações ainda recorrem a curvas mecânicas convencionais (`tween`, `FastOutSlowInEasing`) e transformações isoladas de propriedades (apenas `scale` num eixo de coordenada estático). Em consoles topo de linha como o **PlayStation 5**, **Nintendo Switch** e **SteamOS (Deck)**, o movimento nunca é puramente matemático: ele é **orientado a física de materiais**, **continuidade espacial** e **profundidade cinemática**.

### Os Três Pilares do Novo Motion System:
1. **Harmonic Spring Physics (Mola Amortecida)**: Eliminar tempos fixos em milissegundos para elementos de foco. Quando o jogador move o analógico, o card deve reagir como um objeto físico com massa, aceleração e amortecimento crítico, transmitindo peso e solidez.
2. **Depth Elevation (Profundidade & Foco Concentrado)**: O item focado não deve apenas aumentar de tamanho em cima dos outros. Ele deve "saltar" no eixo Z em direção ao jogador, projetando sombra suave, enquanto os itens vizinhos recuam e perdem levemente saturação e brilho (*Tunnel of Focus*).
3. **Kinetic Continuity & Staggering (Continuidade e Cascata)**: Painéis, gavetas e menus não devem surgir em bloco rígido. O container principal desacelera enquanto os itens internos surgem em cascata orgânica milissegundo a milissegundo.

> **Nota de Execução**: O recurso **Alive Canvas** (paralaxe de giroscópio e stick direito) foi **completamente removido** do código-fonte nesta iteração, eliminando consumo desnecessário de bateria e mantendo a cena 100% estável e nítida a 120 FPS cravados.

---

## 2. Nova Arquitetura de Motion dos Itens (Cards, Carrossel e Grade)

### 2.1. O "Living Focus" dos Cards de Jogos
* **Problema atual**: O card focado escala uniformemente de `0.96f` para `1.03f` e a borda branca simplesmente surge com fade. Parece um recorte 2D estático.
* **Proposta Console Premium**:
  - **Elevação no Eixo Y**: Ao focar, o card sobe `-8.dp` (`translationY = -8.dp * sel`), dando a sensação de flutuação magnética.
  - **Escala de Ouro**: Card focado em `1.06f`; cards vizinhos não focados comprimem para `0.95f` com transparência atenuada em `0.72f`. Isso cria uma hierarquia visual instantânea sem poluição.
  - **Curva de Mola `ConsoleFocusSpring`**:
    ```kotlin
    val ConsoleFocusSpring = spring<Float>(
        dampingRatio = 0.76f, // Leve overshoot tátil de console (bounce imperceptível mas satisfatório)
        stiffness = 420f       // Resposta instantânea no D-pad (baixa latência perceptível)
    )
    ```
  - **Living Specular Border (Borda Viva)**: A borda de seleção não deve ser um traço estático. Propomos uma respiração sutil de brilho especular (`alpha` oscilando organicamente entre `0.85f` e `1.0f` em ciclo senoidal lento de 2.8s), transmitindo vida ao console enquanto o usuário pensa no que jogar.

### 2.2. Efeito "Kinetic Lean" (Inércia e Compressão no Carrossel)
* Durante a navegação rápida com o analógico ou D-Pad:
  - Aplicar uma leve rotação de inércia angular no carrossel (`rotationZ = velocity * -0.8f`, limitado a `±1.5°`).
  - Quando a rolagem para e o card central encaixa, a rotação retorna a `0°` com a mola `ConsoleFocusSpring`.
  - Transmite a mesma satisfação tátil de folhear fitas físicas ou estojos de jogos em alta velocidade.

---

## 3. Motion Global do Sistema (Transições, Painéis e Diálogos)

### 3.1. Game Hub Sheet (Painel Inferior de Detalhes)
* **Entrada**:
  - Em vez de deslizar linearmente da base, utilizar **Cubic Bezier Desacelerado** (`CubicBezierEasing(0.05f, 0.70f, 0.10f, 1.00f)`).
  - **Staggered Entrance (Cascata de Elementos)**:
    - *0ms*: Fundo translúcido escurece e sobe.
    - *35ms*: Badges de plataforma e título do jogo entram com leve fade-up (`translationY: 12.dp -> 0.dp`).
    - *70ms*: Linha de estatísticas ("Nunca iniciado • Tempo: 10 min") surge.
    - *100ms*: Botões de ação rápida (`[A] Iniciar Jogo`, `[X] Metadados`, `[SEL] Opções`) entram com mola suave.
* **Saída**: Desaparecimento rápido de 140ms (`FastOutLinearInEasing`), garantindo que o jogador não espere animação para voltar ao carrossel.

### 3.2. Quick Access Drawer (Gaveta Lateral)
* **Depth Squeeze da Tela Principal**:
  - Quando o Quick Access Drawer for acionado (`R3` ou `WINDOW`), a tela principal de jogos deve recuar suavemente no eixo Z (`scale: 1.0f -> 0.965f`) e receber um escurecimento de `25%`.
  - A gaveta lateral entra da borda direita com desaceleração enfática.
  - Ao fechar, a tela principal se expande de volta para `1.0f`.
  - Esse padrão (adotado pelo SteamOS e Nintendo Switch) isola completamente o contexto de ajuste rápido sem desorientar o jogador.

### 3.3. TopBar com Indicador Deslizante Líquido (Sliding Pill Indicator)
* **Problema atual**: Ao trocar entre "Jogos", "Emuladores" e "Apps", o pill preto apenas salta ou faz um Crossfade simples.
* **Proposta**:
  - Um único indicador em formato de cápsula (`RoundedCornerShape(20.dp)`) que desliza continuamente pelas coordenadas horizontais das abas (`animateDpAsState` com mola `stiffness = 500f, dampingRatio = 0.82f`).
  - O texto da aba selecionada se acende enquanto as não selecionadas permanecem em cinza translúcido fosco.

---

## 4. Tipografia de Console (Legibilidade, Escala e Caráter)

Interfaces de console e portáteis (10-foot e handheld OLED) exigem fontes com proporções geométricas bem definidas, altura-de-x generosa (para leitura instantânea à distância) e suporte nativo a **números tabulares (`tnum`)** para evitar tremor de layout em contadores.

### 4.1. Fonte Primária Recomendada: **Plus Jakarta Sans**
* **Por que ela**: Possui curvas limpas, abertura generosa nos glifos (`a`, `e`, `c`), e densidade moderna idêntica às interfaces de última geração da Sony e Microsoft. Não cansa a vista e tem presença marcante em displays OLED de 120Hz.
* **Hierarquia de Aplicação**:
  - **Títulos de Jogos / Header**: `Plus Jakarta Sans Bold` (20sp a 24sp).
  - **Navegação / Abas**: `Plus Jakarta Sans SemiBold` (14sp).
  - **Corpo / Sinopse / Menus**: `Plus Jakarta Sans Medium` (13sp).
  - **Labels de Apoio**: `Plus Jakarta Sans Regular` (11sp).

### 4.2. Fonte Secundária Técnica / HUD: **Geist Mono** ou **JetBrains Mono**
* **Por que ela**: Usar fonte proporcional para números no HUD faz com que a barra superior e o menu rápido fiquem "dançando" quando o relógio muda de `13:19` para `13:20` ou a bateria cai de `100%` para `99%`.
* **Aplicações Específicas**:
  - Relógio da TopBar (`13:20`).
  - Porcentagem de bateria (`87%`).
  - Badge de Taxa de Atualização (`[120 HZ]`).
  - Tempo de Jogo no Game Hub (`10 min`, `2h 45m`).

---

## 5. Espaçamentos, Proporções e Grid Modular (Base 8dp)

### 5.1. Proporção Áurea dos Cards de Jogos (Box Art 2:3)
* **Ajuste Proposto**:
  - **Card Normal (Tela Cheia / Landscape)**:
    - *Largura*: `180.dp`
    - *Altura*: `270.dp` (Proporção exata **2:3**)
    - *Raio dos Cantos*: `16.dp`
  - **Card Compacto (Handheld / Telas Menores)**:
    - *Largura*: `130.dp`
    - *Altura*: `195.dp` (Proporção exata **2:3**)
    - *Raio dos Cantos*: `12.dp`
* **Vantagem**: A proporção 2:3 é o padrão universal da indústria para Steam Grid Vertical, capas de Nintendo Switch e caixas de jogos. Elimina barras pretas internas e cortes em capas oficiais.

### 5.2. Escala de Espaçamento Modular (Tokens de Spacing)
* `space-xxs`: **4.dp** (separadores internos e ícones inline)
* `space-xs`: **8.dp** (padding de badges, chips e botões de atalho)
* `space-sm`: **12.dp** (espaçamento entre cards no carrossel)
* `space-md`: **16.dp** (margens internas de cartões e drawers)
* `space-lg`: **24.dp** (distância entre seções e grupos)
* `space-xl`: **32.dp** (margem lateral segura contra toques acidentais nos cantos)
* `space-xxl`: **48.dp** (respiro vertical da base até o carrossel)

### 5.3. Zona de Respiro do Palco Central
* O logo 3D do jogo agora repousa sobre uma área desobstruída com margem superior de `28.dp` e margem inferior de `20.dp` antes da linha de cards. Sem o paralaxe de sensor, o logo flutua com uma onda senoidal perfeitamente equilibrada e cinematográfica.

---

## 6. Comparativo: Estado Atual vs Proposta Next-Gen

| Elemento | Estado Atual | Proposta Nova | Impacto na Experiência |
| :--- | :--- | :--- | :--- |
| **Card Focus** | Escala 2D `1.03f` com `tween` | Elevação Y (`-8dp`) + Escala `1.06f` com Mola (`damping: 0.76f`) | Sensação física de flutuação e peso tátil de console |
| **Cards Vizinhos** | Estáticos sem atenuação | Recuo para `0.95f` + `alpha: 0.72f` | Elimina ruído periférico; foco total no jogo selecionado |
| **Borda de Seleção** | Traço estático branco | Borda viva com micro-pulsação especular (2.8s) | Evita sensação de UI congelada; ar premium |
| **Game Hub Sheet** | Slide rígido único | Desaceleração enfática + Cascata interna de elementos (0-100ms) | Fluidez orgânica estilo PS5 Activity Cards |
| **Quick Access** | Drawer simples sobreposto | Zoom out da tela principal (`0.965f`) + Slide da gaveta | Separação de camadas clara e cinematográfica |
| **Indicador TopBar** | Crossfade de botão | Cápsula física deslizante (`animateDp`) entre abas | Navegação lateral limpa e contínua |
| **Tipografia** | Roboto padrão do sistema | Plus Jakarta Sans + Geist Mono (números tabulares) | Estética de hardware dedicado; zero jitter em números |
| **Proporção Cards** | Proporção irregular (~1:1.43) | Proporção universal de Box Art 2:3 (`180x270dp`) | Enquadramento perfeito de capas sem distorção |
| **Alive Canvas** | Giroscópio/Stick oscilando a tela | **REMOVIDO** (Câmera 100% estável e sólida) | Elimina consumo de bateria e distração visual |

---

## 7. Próximos Passos Sugeridos para Implementação

1. **Atualizar `Motion.kt`**: Adicionar molas `ConsoleFocusSpring`, `ConsoleDrawerSpring` e easing `EmphasizedDecelerate`.
2. **Atualizar `GameCard.kt`**: Aplicar elevação no eixo Y, recuo dos vizinhos e proporção 2:3 (`180dp x 270dp`).
3. **Atualizar `TopBar.kt`**: Implementar a cápsula deslizante contínua entre as abas.
4. **Atualizar `GameHubSheet.kt` & `QuickAccessDrawer.kt`**: Adicionar animação de profundidade de fundo e stagger de entrada.
5. **Integrar Tipografia**: Importar a família *Plus Jakarta Sans* e *Geist Mono* no tema do Compose.
