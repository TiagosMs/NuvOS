# 🎮 Guia de Estilo & Design System — Launcher

> **Documento de Referência de Design & UI/UX**  
> **Fonte Original:** `laucher.figcraft`  
> **Resolução Base:** 1920 × 1080 px (Full HD 16:9 — TV / Handheld / Console Launcher)  
> **Tema Base:** Dark Gaming UI (Inspirado em Nintendo Switch, Steam Deck e interfaces modernas de consoles)  
> **Preview Visual:** [preview_thumbnail.png](file:///home/tiago/Downloads/launcher/preview_thumbnail.png)

---

## 1. Visão Geral do Projeto

A interface é um **Launcher de Jogos & Aplicativos** otimizado para navegação via controle (D-Pad / Analógico / Teclado) e telas widescreen Full HD (1920×1080).

### Estrutura Principal das Telas:
1. **Tela Inicial — Jogos (`Home - Jogos`):**
   - Header superior com perfil do usuário, abas de navegação centrais e barra de status do sistema.
   - Logo destacado do jogo em foco no terço superior.
   - Carrossel horizontal de capas de jogos verticais (proporção ~2:3).
   - Card selecionado ampliado com moldura branca luminosa e indicador de reprodução (*Play*).
   - Rodapé com atalhos de controle (*A: Jogar*, *X: Opções*).
2. **Tela de Aplicativos (`Frame 1 - Apps`):**
   - Grade 3×3 de blocos/pastas de aplicativos à esquerda com barra de rolagem estilizada.
   - Bloco em destaque à direita com grande ícone de pasta e tipografia de exibição (*Sistema*).
3. **Painel Lateral / Drawer de Configurações (`Perfil & Sistema`):**
   - Painel translúcido sobreposto com efeito *backdrop blur* de 118px.
   - Card de perfil com foto circular, apelido e handle (`@Without_`).
   - Lista de opções de biblioteca e navegação com cantos arredondados de 30px.
   - Seletor de **Cor de Destaque (*Accent Color*)** com 11 opções de círculos cromáticos.

---

## 2. Paleta de Cores Completa

### 2.1 Cores Estruturais & Fundos (Dark Theme)

| Amostra | Nome do Token | HEX | RGB | Alpha | Aplicação / Uso |
| :---: | :--- | :--- | :--- | :---: | :--- |
| ![#1C212A](https://via.placeholder.com/16/1C212A/000000?text=+) | `--color-bg-deep` | `#1C212A` | `rgb(28, 33, 42)` | 100% | Fundo profundo dos ícones de controle, glifos de botões e vetores |
| ![#191919](https://via.placeholder.com/16/191919/000000?text=+) | `--color-nav-pill` | `#191919` | `rgb(25, 25, 25)` | 100% | Fundo da cápsula/pílula ativa de navegação do menu superior |
| ![#292727](https://via.placeholder.com/16/292727/000000?text=+) | `--color-surface-item` | `#292727` | `rgb(41, 39, 39)` | 100% | Cards de perfil e barras de navegação do menu lateral (Drawer) |
| ![#2A2A2A](https://via.placeholder.com/16/2A2A2A/000000?text=+) | `--color-frame-bg` | `#2A2A2A` | `rgb(42, 42, 42)` | 100% | Fundo alternativo do canvas/molduras |
| ![#373737](https://via.placeholder.com/16/373737/000000?text=+) | `--color-scrollbar` | `#373737` | `rgb(55, 55, 55)` | 100% | Polegar da barra de rolagem (Scrollbar thumb) |
| ![#3D3A3A](https://via.placeholder.com/16/3D3A3A/000000?text=+) | `--color-surface-card` | `#3D3A3A` | `rgb(61, 58, 58)` | 100% | Superfície dos cards da grade de apps (pastas) |
| ![#484848](https://via.placeholder.com/16/484848/000000?text=+) | `--color-drawer-bg` | `#484848` | `rgb(72, 72, 72)` | 100% | Fundo do Drawer lateral (combinado com blur de 118px) |
| ![#6A6A6A](https://via.placeholder.com/16/6A6A6A/000000?text=+) | `--color-overlay` | `#6A6A6A` | `rgb(106, 106, 106)`| 100% | Camada de overlay escurecedora |

---

### 2.2 Tipografia & Elementos Neutros

| Amostra | Nome do Token | HEX | RGB | Alpha | Aplicação / Uso |
| :---: | :--- | :--- | :--- | :---: | :--- |
| ![#CBCBCB](https://via.placeholder.com/16/CBCBCB/000000?text=+) | `--color-text-primary` | `#CBCBCB` | `rgb(203, 203, 203)` | 100% | **Cor padrão de todos os textos** (títulos, menus, relógio, botões) |
| ![#D9D9D9](https://via.placeholder.com/16/D9D9D9/000000?text=+) | `--color-neutral-high` | `#D9D9D9` | `rgb(217, 217, 217)` | 100% | Barras de status (bateria, wifi), ícone hambúrguer, amostra cinza |
| ![#FFFFFF](https://via.placeholder.com/16/FFFFFF/000000?text=+) | `--color-white-focus` | `#FFFFFF` | `rgb(255, 255, 255)` | 100% | **Moldura de foco/seleção** do card ativo e anel indicador |
| ![#000000](https://via.placeholder.com/16/000000/000000?text=+) | `--color-black` | `#000000` | `rgb(0, 0, 0)` | 100% | Ícone do Play (triângulo) e swatch preto |

---

### 2.3 Botões de Ação & Controles (Gamepad Prompts)

| Amostra | Nome do Token | HEX | RGB | Alpha | Aplicação / Uso |
| :---: | :--- | :--- | :--- | :---: | :--- |
| ![#55A43B](https://via.placeholder.com/16/55A43B/000000?text=+) | `--color-btn-a` | `#55A43B` | `rgb(85, 164, 59)` | 100% | **Botão (A) — Jogar / Confirmar** (Círculo de prompt verde) |
| ![#039AC9](https://via.placeholder.com/16/039AC9/000000?text=+) | `--color-btn-x` | `#039AC9` | `rgb(3, 154, 201)` | 100% | **Botão (X) — Opções** (Círculo de prompt azul) |
| ![#DA1725](https://via.placeholder.com/16/DA1725/000000?text=+) | `--color-btn-b` | `#DA1725` | `rgb(218, 23, 37)` | 100% | **Botão (B) — Voltar / Cancelar** (Círculo vermelho) |
| ![#F1D514](https://via.placeholder.com/16/F1D514/000000?text=+) | `--color-btn-y` | `#F1D514` | `rgb(241, 213, 20)` | 100% | **Botão (Y) — Ação Auxiliar** (Círculo amarelo) |
| ![#D6E1F6](https://via.placeholder.com/16/D6E1F6/000000?text=+) | `--color-shoulder-btn` | `#D6E1F6` | `rgb(214, 225, 246)` | 100% | **Botões de Ombro (L1 / R1, LB / RB)** e glifos do D-Pad |
| ![#1E7900](https://via.placeholder.com/16/1E7900/000000?text=+) | `--color-status-playing` | `#1E7900` | `rgb(30, 121, 0)` | 100% | Círculo indicador do jogo em execução / selecionado |

---

### 2.4 Paleta de Destaque (*Accent Swatches* — Configurações do Sistema)

Os círculos de amostra de cor de destaque configurável pelo usuário na gaveta lateral (ordenados da esquerda para a direita no design):

| Ordem | Amostra | Nome da Cor | HEX | RGB | Status no Design |
| :---: | :---: | :--- | :--- | :--- | :---: |
| 1 | ![#FF0000](https://via.placeholder.com/16/FF0000/000000?text=+) | Red Classic | `#FF0000` | `rgb(255, 0, 0)` | Opção |
| 2 | ![#FFE200](https://via.placeholder.com/16/FFE200/000000?text=+) | Yellow Bright | `#FFE200` | `rgb(255, 226, 0)` | Opção |
| 3 | ![#000FFF](https://via.placeholder.com/16/000FFF/000000?text=+) | Electric Blue | `#000FFF` | `rgb(0, 15, 255)` | Opção |
| 4 | ![#FF0077](https://via.placeholder.com/16/FF0077/000000?text=+) | Hot Pink | `#FF0077` | `rgb(255, 0, 119)` | Opção |
| 5 | ![#FFFFFF](https://via.placeholder.com/16/FFFFFF/000000?text=+) | Pure White | `#FFFFFF` | `rgb(255, 255, 255)` | Opção |
| 6 | ![#000000](https://via.placeholder.com/16/000000/000000?text=+) | Deep Black | `#000000` | `rgb(0, 0, 0)` | Opção |
| 7 | ![#977CFF](https://via.placeholder.com/16/977CFF/000000?text=+) | Soft Violet | `#977CFF` | `rgb(151, 124, 255)` | Opção |
| 8 | ![#429A73](https://via.placeholder.com/16/429A73/000000?text=+) | Jade Green | `#429A73` | `rgb(66, 154, 115)` | Opção |
| 9 | ![#627B56](https://via.placeholder.com/16/627B56/000000?text=+) | **Olive / Forest Green** | `#627B56` | `rgb(98, 123, 86)` | **Ativo / Selecionado** (possui anel branco) |
| 10 | ![#E00000](https://via.placeholder.com/16/E00000/000000?text=+) | Crimson Red | `#E00000` | `rgb(224, 0, 0)` | Opção |
| 11 | ![#D9D9D9](https://via.placeholder.com/16/D9D9D9/000000?text=+) | Slate Gray | `#D9D9D9` | `rgb(217, 217, 217)` | Opção |

---

## 3. Sistema Tipográfico

- **Família da Fonte Única:** `Noto Sans` (Fallback: `system-ui, -apple-system, "Segoe UI", Roboto, sans-serif`)
- **Cor Global de Texto:** `#CBCBCB` (`rgba(203, 203, 203, 1.0)`) em 100% das ocorrências de texto.

### Tabela de Escala Tipográfica:

| Nível / Papel | Peso | Tamanho | Line-Height | Letter-Spacing | Exemplo / Uso |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Display / Título Principal** | Bold (700) | `43 px` | Auto | `0%` | "Sistema" (Destaque da pasta de apps) |
| **Heading / Aba Ativa** | Bold (700) | `24 px` | Auto | `0%` | "Jogos" (Menu selecionado) |
| **Heading / Título Seção** | Bold (700) | `24 px` | Auto | `0%` | "Perfil", "Biblioteca e Navegação", "cor de destaque" |
| **Heading / Usuário & Relógio** | Bold (700) | `24 px` | Auto | `0%` | "Tiago", "11:29" |
| **Body / Aba Inativa** | Regular (400) | `24 px` | Auto | `0%` | "Emuladores", "Apps" |
| **Body / Handle & Ações** | Regular (400) | `24 px` | Auto | `0%` | "@Without_", "Trocar" |
| **Prompt de Controle / Botão** | Regular (400) | `16 px` | Auto | `0%` | "Jogar" (após botão A), "Opções" (após botão X) |
| **Subtítulo / Rótulo de Jogo** | Bold (700) | `14 px` | Auto | `0%` | "Kirby and the ForgottenLand", "GameHub" |

---

## 4. Raios de Arredondamento (*Corner Radius*)

O design utiliza um sistema muito consistente de cantos arredondados:

| Raio (`border-radius`) | Aplicação | Observações |
| :---: | :--- | :--- |
| **`49 px`** | Cards de jogos padrão (259×393 px), moldura externa do card focado (276×393 px), blocos da grade de apps (191.7×208 px), painel da gaveta lateral | Raio de curvatura padrão dos containers principais |
| **`43.6 px` ~ `44.7 px`** | Card interno com imagem dentro do card focado (262.8×379.5 px) | Acompanha a curvatura externa de 49px mantendo borda concêntrica |
| **`[49px, 0, 0, 49px]`** | Card de transbordo (peek/overflow) na margem direita (213×393 px) | Cantos esquerdos arredondados, cantos direitos retos para simular continuidade |
| **`27.5 px`** | Pílula da aba ativa (`height: 55px`), círculos de swatch (`55×55px`) | Pílula perfeita / Círculo perfeito (`height / 2`) |
| **`50.0 px` ~ `55.7 px`** | Fotos de Avatar do usuário (60.6×60.6 px e 119×119 px) | Círculo perfeito para imagens de perfil |
| **`53.7 px`** | Card principal de Perfil no drawer lateral (716×220 px) | Cantos orgânicos para o card de usuário |
| **`30.1 px`** | Itens da lista do menu lateral (716×86.1 px) | Altura de 86px com cantos suaves |
| **`10.0 px`** | Indicador da barra de rolagem (Scrollbar thumb) | Cápsula fina para o indicador |
| **`15.0 px`** | Abas e bases dos ícones de pasta (`[0, 15px, 15px, 15px]`) | Formato de aba de pasta de arquivos |

---

## 5. Efeitos & Desfoques (*Effects & Blurs*)

| Elemento | Tipo de Efeito | Propriedades | Finalidade |
| :--- | :--- | :--- | :--- |
| **Painel Lateral (Drawer)** | `backgroundBlur` | `radius: 118 px` | Efeito de vidro fosco translúcido (*frosted glass*) que desfoca o fundo dos jogos |
| **Card de Foco Ativo** | `layerBlur` | `radius: 12 px` | Brilho/glow suave branco (`#FFFFFF`) ao redor da moldura do card selecionado |
| **Fundo Tela Inicial** | `backgroundBlur` | `radius: 4 px` | Leve suavização do papel de parede para elevar o contraste dos cards |
| **Card de App Ativo** | `backgroundBlur` | `radius: 4 px` | Desfoque sutil no item selecionado da grade de apps |
| **Pasta Grande (Sistema)** | `dropShadow` | `offset: [-26px, 20px]`, `radius: 4 px`, `color: rgba(0, 0, 0, 0.25)` | Sombra projetada direcional para dar profundidade ao bloco em destaque |
| **Card Secundário** | `dropShadow` | `offset: [0px, 4px]`, `radius: 4 px`, `color: rgba(0, 0, 0, 0.25)` | Elevação discreta de 4px |

---

## 6. Anatomia & Dimensões dos Componentes

### 6.1 Barra Superior (*Header Bar*) — Altura ~60 px
- **Lado Esquerdo:**
  - **Menu Hambúrguer:** 3 traços horizontais de `24 × 3.53 px`, espaçamento vertical de `~2.1 px`, cor `#D9D9D9`.
  - **Avatar de Perfil:** Círculo de `60.6 × 60.6 px` (radius 50%), posicionado a `15 px` do menu hambúrguer.
  - **Nome do Usuário:** "Tiago" (`24 px Bold`, `#CBCBCB`), centralizado verticalmente com o avatar.
- **Centro (Navegação por Abas):**
  - **Botão Ombro Esquerdo (LB / L1):** Ícone em cápsula `33.3 × 19 px` (cor `#D6E1F6` com glifo `#1C212A`).
  - **Abas:** "Jogos", "Emuladores", "Apps".
  - **Pílula Ativa:** Fundo `#191919` com dimensões `216 × 55 px` (ou `100 × 55 px` quando na aba Apps), radius `27.5 px`. Texto ativo em `24 px Bold`.
  - **Botão Ombro Direito (RB / R1):** Ícone em cápsula `33.3 × 19 px` no lado direito da última aba.
- **Lado Direito (Barra de Status do Sistema):**
  - **Indicador de Wifi / Rede:** 4 barras verticais de `4 px` de largura com alturas progressivas: `5.6 px`, `10.5 px`, `13.5 px` e `17.5 px` (cor `#D9D9D9`).
  - **Ícone de Bateria:** Corpo de `29.7 × 18 px` + polo positivo de `5.7 × 6.75 px` (cor `#D9D9D9`).
  - **Relógio Digital:** "11:29" (`24 px Bold`, `#CBCBCB`).

---

### 6.2 Carrossel de Jogos (*Home — Jogos*)
- **Logo de Destaque Superior:** Dimensão aproximada de `588.5 × 330.7 px`, posicionado acima do card ativo.
- **Cards Inativos (Capa de Jogo):**
  - Largura: `259 px` | Altura: `393 px`
  - Raio dos cantos: `49 px`
  - Gap horizontal entre cards: `15 px` a `17 px`
- **Card Focado / Selecionado:**
  - Moldura Externa (Focus Ring): `276 × 393 px`, fundo branco `#FFFFFF` com `layerBlur: 12 px`, radius `49 px`.
  - Imagem Interna: `262.8 × 379.5 px`, radius `43.6 px` (margem interna de ~6.6 px).
- **Indicador de Reprodução Abaixo do Card Focado:**
  - Badge circular verde: `14 × 14 px`, cor `#1E7900`.
  - Ícone Play (Triângulo): `6.3 × 7.1 px`, cor preta `#000000`.
  - Título do Jogo: `14 px Bold`, cor `#CBCBCB` (ex: "Kirby and the ForgottenLand").
- **Card de Borda Direita (Transbordo):**
  - Dimensões: `213 × 393 px`, cantos `radii: [49, 0, 0, 49]` para indicar continuidade no carrossel.

---

### 6.3 Grade de Aplicativos (*Tela Apps*)
- **Blocos de Pasta (Grade 3×3 à esquerda):**
  - Dimensões do bloco: `191.7 × 208 px`, radius `49 px`, cor de fundo `#3D3A3A`.
  - Ícone de pasta interno: `126 × 107 px`.
  - Item ativo: Card branco `#FFFFFF` de `191.7 × 208 px` com card interno de `184.3 × 201.3 px` (radius `44.7 px`).
- **Barra de Rolagem:**
  - Dimensão: `13 × 140.5 px`, radius `10 px`, cor `#373737`.
- **Bloco de Destaque à Direita:**
  - Grande ícone de pasta: `439.7 × 366.4 px` com sombra projetada `dropShadow`.
  - Título em destaque: "Sistema" (`43 px Bold`, cor `#CBCBCB`).

---

### 6.4 Painel Lateral de Configurações (*Settings Drawer*)
- **Container do Painel:**
  - Largura: `908 px` | Altura: `1379 px`
  - Radius: `49 px` (com `smoothing: 0.6`)
  - Fundo: `#484848` com `backgroundBlur: 118 px` (vidro fumê/fosco).
- **Card de Perfil:**
  - Container: `716 × 220 px`, radius `53.7 px`, cor `#292727`.
  - Avatar: `119 × 119 px`, radius `55.7 px` (circular).
  - Nome: "Tiago" (`24 px Bold`).
  - Username: "@Without_" (`24 px Regular`).
  - Botão de Ação: "Trocar" (`24 px Regular`).
- **Lista "Biblioteca e Navegação":**
  - Título da Seção: `24 px Bold`.
  - 5 botões de navegação: `716 × 86.1 px`, radius `30.1 px`, cor `#292727`.
  - Espaçamento vertical entre botões: `~15 px`.
- **Seletor de "cor de destaque":**
  - Título da Seção: `24 px Bold`.
  - Botões de Cor: Círculos de `55 × 55 px`, radius `27.5 px`, gap horizontal `~9.5 px`.
  - Amostra Ativa (`#627B56`): Possui anel externo indicador branco `#FFFFFF` de `55 × 55 px` e círculo interno de `49 × 49 px`.

---

### 6.5 Rodapé / Barra de Ações (*Footer*)
- **Posicionamento:** Fixo na margem inferior (`bottom: ~40px`).
- **Ação Primária (Confirmar / Iniciar):**
  - Ícone Botão (A): Círculo verde `#55A43B` (`23.86 × 23.86 px`) com letra "A" em `#1C212A`.
  - Rótulo: "Jogar" (`16 px Regular`, `#CBCBCB`).
- **Ação Secundária (Menu de Opções):**
  - Ícone Botão (X): Círculo azul `#039AC9` (`23.86 × 23.86 px`) com letra "X" em `#1C212A`.
  - Rótulo: "Opções" (`16 px Regular`, `#CBCBCB`).

---

## 7. Tokens de Design (Código para Devs & UI)

### 7.1 Variáveis CSS / Design Tokens

```css
:root {
  /* Resolução de Referência */
  --canvas-width: 1920px;
  --canvas-height: 1080px;

  /* Tipografia */
  --font-family: 'Noto Sans', system-ui, sans-serif;
  --text-primary: #CBCBCB;

  /* Escala de Texto */
  --text-display: 700 43px/1.2 var(--font-family);
  --text-heading: 700 24px/1.3 var(--font-family);
  --text-body:    400 24px/1.4 var(--font-family);
  --text-action:  400 16px/1.4 var(--font-family);
  --text-caption: 700 14px/1.4 var(--font-family);

  /* Superfícies & Fundos */
  --bg-deep:         #1C212A;
  --bg-nav-pill:     #191919;
  --bg-surface-item: #292727;
  --bg-surface-card: #3D3A3A;
  --bg-drawer:       rgba(72, 72, 72, 0.85);
  --bg-scrollbar:    #373737;

  /* Elementos de Destaque & Foco */
  --focus-ring:      #FFFFFF;
  --focus-blur:      0 0 12px rgba(255, 255, 255, 0.6);
  --glass-blur:      blur(118px);

  /* Botões de Controle Gamepad */
  --btn-a:           #55A43B;
  --btn-b:           #DA1725;
  --btn-x:           #039AC9;
  --btn-y:           #F1D514;
  --btn-shoulder:    #D6E1F6;
  --status-playing:  #1E7900;

  /* Raios de Arredondamento */
  --radius-card:     49px;
  --radius-card-inner: 43.6px;
  --radius-pill:     27.5px;
  --radius-avatar:   50%;
  --radius-drawer:   49px;
  --radius-list-item: 30px;
  --radius-scroll:   10px;
}
```

### 7.2 Configuração Tailwind CSS (v3/v4)

```javascript
module.exports = {
  theme: {
    extend: {
      colors: {
        launcher: {
          deep: '#1C212A',
          pill: '#191919',
          surface: '#292727',
          card: '#3D3A3A',
          drawer: '#484848',
          text: '#CBCBCB',
          neutral: '#D9D9D9',
          btnA: '#55A43B',
          btnB: '#DA1725',
          btnX: '#039AC9',
          btnY: '#F1D514',
          shoulder: '#D6E1F6',
          playing: '#1E7900'
        }
      },
      borderRadius: {
        'card': '49px',
        'card-inner': '43.6px',
        'pill': '27.5px',
        'drawer': '49px',
        'list': '30.1px'
      },
      fontFamily: {
        launcher: ['"Noto Sans"', 'sans-serif']
      }
    }
  }
}
```

---

## 8. Checklist para o Designer

- [ ] Manter resolução estrita de **1920 × 1080 px (16:9)** para compatibilidade direta com monitores e TVs.
- [ ] O contraste de texto utiliza `#CBCBCB` sobre tons escuros (`#191919`, `#292727`, `#3D3A3A`).
- [ ] O estado de foco/seleção dos cards de jogos **sempre** utiliza a moldura branca (`#FFFFFF`) com efeito luminoso de `12px` de desfoque.
- [ ] As proporções das capas de jogos devem seguir o padrão vertical `259 × 393 px` (~1:1.52).
- [ ] O menu lateral (Drawer) deve utilizar efeito de **desfoque de fundo (*backdrop-filter: blur(118px)*)** com tom base `#484848`.
- [ ] Os prompts de botões de controle seguem o padrão clássico: Verde para confirmação (A), Azul para opções (X), Vermelho para cancelar (B) e Amarelo (Y).
