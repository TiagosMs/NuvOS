# NuvOS / Deck Launcher — Relatório Executivo de Auditoria UX/UI & Diretrizes Unslop
**Autor:** Especialista Sênior em UX/UI de Consoles de Jogos (PS5, SteamOS, Switch, Xbox Series)  
**Data:** 22 de Novembro de 2026  
**Hardware Alvo:** Tablet Android Gamer (Snapdragon 865, Display OLED 120Hz 2000x1200, Controles Físicos Gamepad)  
**Documento de Origem:** `/home/tiago/Downloads/deck-launcher-ux-unslop-report.md`

---

## 1. Visão Geral & Direção Criativa (Unslop Direction)

Interfaces criadas com assistência de IA ou "vibecoding" tendem a cair em padrões visuais repetitivos e poluídos: tudo brilha com gradientes difusos, todo botão possui caixas e aros pesados, as fontes usam pesos extremos (`Black` / `ExtraBold`), os cantos arredondados são arbitrários e há múltiplos elementos disputando a atenção visual.

O objetivo desta auditoria é transformar o **NuvOS** de um aplicativo com traços de "IA/gamer espalhafatoso" em um **Sistema Operacional de Console Autêntico, Fluido e Silencioso (Quiet Chrome)**, no mesmo nível de excelência de um PlayStation 5 ou SteamOS 3.

```
+-----------------------------------------------------------------------------------+
| 5 LINHAS DIRETIVAS DO PRODUTO (UNSLOP DIRECTION)                                 |
+-----------------------------------------------------------------------------------+
| 1. Product:   Launcher para console portátil e tablet Android de alta performance,|
|               operado 100% por controle físico (D-Pad/Thumbstick) a 120Hz.        |
| 2. Character: Focado, sóbrio, tátil.                                              |
| 3. World:     Hardware de console de precisão: alumínio anodizado escuro, vidro   |
|               fosco antirreflexo e displays OLED com contraste infinito.          |
| 4. Moments:   A arte autêntica do jogo (Hero Banner e Logo 3D) é o único elemento |
|               vibrante; o sistema ao redor é quieto e minimalista.                |
| 5. Signature: Keycap glyphs monocromáticos de controle gravados com traço fino    |
|               de 1.5dp e hairlines de foco cirúrgico de 1.5dp.                    |
+-----------------------------------------------------------------------------------+
```

---

## 2. Auditoria Crítica dos Ícones Manuais (`AppVectorIcons.kt`)

### Diagnóstico dos Problemas Atuais
Na implementação atual de `AppVectorIcons.kt`:
1. **Inconsistência de Traço (Stroke Multipliers)**:
   - O traço padrão é `1.8dp`, mas o D-pad do controle usa `stroke * 1.5f` (2.7dp), a engrenagem usa `stroke * 1.6f` (2.88dp), os miolos usam círculos sólidos e as linhas de livro usam `stroke * 0.8f` (1.44dp). Isso quebra o peso ótico uniforme da interface.
2. **Geometria Deformada em Curvas Bézier**:
   - `VectorIconPalette`: O desenho usa pontos de controle manuais cúbicos que produzem um contorno irregular, parecendo uma bolha distorcida. Não há o orifício do polegar e sim três círculos brancos sólidos que poluem o interior.
3. **Falta de Reconhecimento de Forma (Affordance)**:
   - `VectorIconGamepad`: É apenas um retângulo com cantos arredondados e uma cruz. Não possui a silhueta clássica de pegas inferiores (grips) de um joystick moderno, parecendo um bip ou intercomunicador.
   - `VectorIconGear`: É desenhado com 6 linhas radiais com cantos retos (`StrokeCap.Butt`). O efeito visual é de palitos de fósforo espetados num anel, e não de uma engrenagem de precisão com dentes chanfrados.
   - `VectorIconBooks`: Dois blocos retangulares desalinhados que não transmitem o conceito de lombada ou livros numa estante.
4. **Bounding Boxes Discrepantes**:
   - Ícones com caixas variando entre 16dp, 20dp e 24dp sem padronização em uma grade vetorial fixa de 24x24dp.

### Solução Técnica (Diretriz Unslop de Ícones)
- Padronizar **todos** os ícones em um Canvas com **grid canônico de 24x24dp** (ou viewport unitária 24f).
- Adotar **Stroke unificado de 1.5dp** com `StrokeCap.Round` e `StrokeJoin.Round`.
- Redesenhar com geometria euclidiana pura (arcos limpos, proporções áureas e ângulos de 45°/90°).

#### Código Proposto para Substituição em `AppVectorIcons.kt`:

```kotlin
package com.decklauncher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.decklauncher.ui.theme.TextWhite

private const val ICON_BASE_STROKE = 1.5f

/**
 * Ícone Gamepad Ergonômico de Console:
 * Silhueta simétrica com pegas anatômicas inferiores, D-pad cruzado e botões frontais.
 */
@Composable
fun VectorIconGamepad(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val s = 1.5.dp.toPx()
        val w = size.width
        val h = size.height

        // Corpo anatômico do controle com pegas
        val bodyPath = Path().apply {
            moveTo(w * 0.28f, h * 0.28f)
            lineTo(w * 0.72f, h * 0.28f)
            cubicTo(w * 0.88f, h * 0.28f, w * 0.94f, h * 0.44f, w * 0.90f, h * 0.68f)
            cubicTo(w * 0.86f, h * 0.84f, w * 0.76f, h * 0.86f, w * 0.70f, h * 0.74f)
            lineTo(w * 0.62f, h * 0.58f)
            lineTo(w * 0.38f, h * 0.58f)
            lineTo(w * 0.30f, h * 0.74f)
            cubicTo(w * 0.24f, h * 0.86f, w * 0.14f, h * 0.84f, w * 0.10f, h * 0.68f)
            cubicTo(w * 0.06f, h * 0.44f, w * 0.12f, h * 0.28f, w * 0.28f, h * 0.28f)
            close()
        }
        drawPath(
            path = bodyPath,
            color = tint,
            style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // D-Pad minimalista (cruz de centro aberto)
        val dpadCenter = Offset(w * 0.30f, h * 0.44f)
        val arm = w * 0.07f
        drawLine(tint, Offset(dpadCenter.x - arm, dpadCenter.y), Offset(dpadCenter.x + arm, dpadCenter.y), strokeWidth = s, cap = StrokeCap.Round)
        drawLine(tint, Offset(dpadCenter.x, dpadCenter.y - arm), Offset(dpadCenter.x, dpadCenter.y + arm), strokeWidth = s, cap = StrokeCap.Round)

        // Botões de Ação X/Y/A/B (4 micro-glifos com raio proporcional)
        val btnCenter = Offset(w * 0.70f, h * 0.44f)
        val offset = w * 0.06f
        drawCircle(tint, radius = s * 0.75f, center = Offset(btnCenter.x, btnCenter.y - offset))
        drawCircle(tint, radius = s * 0.75f, center = Offset(btnCenter.x, btnCenter.y + offset))
        drawCircle(tint, radius = s * 0.75f, center = Offset(btnCenter.x - offset, btnCenter.y))
        drawCircle(tint, radius = s * 0.75f, center = Offset(btnCenter.x + offset, btnCenter.y))
    }
}

/**
 * Ícone de Biblioteca / Jogos:
 * Três volumes limpos com um ligeiramente inclinado para transmitir dinamismo visual.
 */
@Composable
fun VectorIconBooks(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val s = 1.5.dp.toPx()
        val w = size.width
        val h = size.height

        // Livro 1 (vertical)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.15f, h * 0.20f),
            size = Size(w * 0.20f, h * 0.65f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Livro 2 (vertical)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.40f, h * 0.15f),
            size = Size(w * 0.20f, h * 0.70f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Livro 3 (inclinado a 15 graus)
        val book3 = Path().apply {
            moveTo(w * 0.65f, h * 0.85f)
            lineTo(w * 0.78f, h * 0.28f)
            lineTo(w * 0.94f, h * 0.32f)
            lineTo(w * 0.82f, h * 0.85f)
            close()
        }
        drawPath(book3, color = tint, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/**
 * Ícone Engrenagem Mecânica de Precisão:
 * Dentes trapezoidais contínuos gerados parametricamente.
 */
@Composable
fun VectorIconGear(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val s = 1.5.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rOut = size.width * 0.42f
        val rIn = size.width * 0.32f
        val rHole = size.width * 0.16f

        val teeth = 8
        val gearPath = Path()
        for (i in 0 until teeth) {
            val a0 = (i * 2 * Math.PI / teeth).toFloat()
            val a1 = a0 + (0.35f * 2 * Math.PI / teeth).toFloat()
            val a2 = a0 + (0.50f * 2 * Math.PI / teeth).toFloat()
            val a3 = a0 + (0.85f * 2 * Math.PI / teeth).toFloat()

            val p0 = Offset(cx + Math.cos(a0.toDouble()).toFloat() * rIn, cy + Math.sin(a0.toDouble()).toFloat() * rIn)
            val p1 = Offset(cx + Math.cos(a1.toDouble()).toFloat() * rOut, cy + Math.sin(a1.toDouble()).toFloat() * rOut)
            val p2 = Offset(cx + Math.cos(a2.toDouble()).toFloat() * rOut, cy + Math.sin(a2.toDouble()).toFloat() * rOut)
            val p3 = Offset(cx + Math.cos(a3.toDouble()).toFloat() * rIn, cy + Math.sin(a3.toDouble()).toFloat() * rIn)

            if (i == 0) gearPath.moveTo(p0.x, p0.y) else gearPath.lineTo(p0.x, p0.y)
            gearPath.lineTo(p1.x, p1.y)
            gearPath.lineTo(p2.x, p2.y)
            gearPath.lineTo(p3.x, p3.y)
        }
        gearPath.close()

        drawPath(gearPath, color = tint, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color = tint, radius = rHole, center = Offset(cx, cy), style = Stroke(width = s))
    }
}
```

---

## 3. Quiet Chrome: TopBar e BottomBar

### A. Diagnóstico da TopBar
1. **Pílula de Perfil Supercarregada**:
   - Possui fundo esbranquiçado de 25% de opacidade, borda branca de 2dp, escala de 1.06f, avatar com gradiente multicolorido e texto em `FontWeight.ExtraBold`. Isso compete com as abas principais.
   - Em consoles (como Nintendo Switch e Steam Deck), o perfil do usuário fica no canto com opacidade de repouso sutil e sem fundos espalhafatosos.
2. **Abas com Pílulas de Atalho Conflitantes ([LB] / [RB])**:
   - As caixas `[LB]` e `[RB]` são blocos escuros opacos e com tipografia pesada (`FontWeight.Black`), que quebram o alinhamento visual das três abas ("Jogos", "Emuladores", "Apps").
   - O indicador de botão de ombro deve ser discreto (texto fino `LB` e `RB` a 60% de opacidade ou micro-keycaps transparentes).
3. **Botões de Ação da Direita (+) e Configurações**:
   - Usam `shadow(14.dp, spotColor = Color.White)` quando focados. Sombras brancas difusas produzem o temido efeito de "aura de jogo mobile" em vez de foco tátil de console.

### B. Diagnóstico da BottomBar
1. **Caixas Escuras Envolventes nos Prompts de Controle**:
   - `ControllerActionPrompt` desenha um retângulo espesso com fundo `#2A3240` e borda `#4A5568` para as letras "A", "SELECT", "Y", "B".
   - Isso adiciona 4 caixas retangulares pesadas na base da tela, fragmentando a leitura.
   - Em um console de jogos premium:
     - O botão é representado por um **glifo circular limpo** de 20dp de diâmetro, traço sutil e letra centralizada com peso `SemiBold`.
     - O texto da ação fica alinhado com espaçamento uniforme de 8dp.

### C. Especificação Técnica e Comparativo

| Componente | Implementação Atual (Slop) | Correção Unslop (Console Grade) |
| :--- | :--- | :--- |
| **Pílula de Perfil** | Borda 2dp, escala 1.06f, gradiente estridente, peso 800 | Fundo neutro `0x12FFFFFF`, borda hairline 1dp a 15% opacidade, tipografia `Medium` (500) |
| **Atalhos [LB] / [RB]** | Pílulas escuras opacas com texto `FontWeight.Black` | Micro-badges com cantos 4dp, texto `LB` / `RB` com opacidade 65% |
| **Botões TopBar (+ / Engrenagem)** | Sombra branca difusa 14dp + borda 2.5dp | Foco com anel nítido de 1.5dp em branco puro, sem sombras difusas |
| **Prompts da BottomBar** | Retângulos escuros com bordas grossas tipo botão web | Círculo keycap minimalista de 20dp com letra de 10sp gravada |

---

## 4. Hierarquia Visual & Princípio do "One Bright Element"

### O Conflito Central da Tela Inicial
Atualmente, quando o jogador seleciona um jogo no carrossel, a tela apresenta **5 fontes simultâneas de estímulo visual**:
1. O fundo com hero e blur gradiente.
2. O logotipo 3D girando no centro com sombra no piso e reflexo animado.
3. O card com escala 1.08x.
4. **O halo triplo difuso** desenhado via `drawBehind` no card (`repeat(3)` com expansão de 4dp, 9dp e 14dp).
5. A borda branca espessa de 3.5dp sobre o card.
6. O texto com o nome do jogo duplicado logo abaixo do card.

```
ESTRUTURA ATUAL (RUÍDO E MULTICAMADAS DE BRILHO):
[TopBar com Botões com Glow]
          ↓
[Logo 3D Flutuando com Sombra e Glow Radial] ← BRILHO 1
          ↓
[Card Selecionado com Halo Triplo (4dp, 9dp, 14dp) + Borda 3.5dp] ← BRILHO 2
          ↓
[Título em Texto repetido sob o card] ← REDUNDÂNCIA
          ↓
[BottomBar com Caixas com Bordas] ← RUÍDO 3
```

### A Correção com Princípio Unslop

```
ESTRUTURA UNSLOP (QUIET CHROME + MOMENTO ÚNICO):
[TopBar Limpa e Monocromática (0% ruído)]
          ↓
★ O MOMENTO: HERO ARTWORK & LOGO 3D (A arte autêntica do jogo é o elemento que brilha)
          ↓
[Carrossel: Foco Cirúrgico em Hairline Branco de 1.5dp, Elevação Z sutil, sem halo difuso]
          ↓
[Título centralizado no topo; texto sob o card apenas como indicador silencioso]
          ↓
[BottomBar: Keycaps minimalistas gravadas]
```

#### Código Técnico de Refinamento do `GameCard.kt`:
Eliminar o laço `repeat(3)` de glow difuso e aplicar borda hairline precisa com elevação sutil:

```kotlin
// ANTES (Slop: Halo difuso triplo que borra a tela OLED):
.drawBehind {
    val a = sel.value.coerceIn(0f, 1f)
    if (a > 0.01f) {
        val glowColor = if (isReorderMode) Color(0xFFF59E0B) else accentColor
        val baseRadius = cornerRadiusDp.toPx()
        repeat(3) { i ->
            val grow = (4 + i * 5).dp.toPx()
            drawRoundRect(
                color = glowColor.copy(alpha = (0.22f * a / (i + 1)).coerceIn(0f, 1f)),
                topLeft = Offset(-grow, -grow),
                size = Size(size.width + grow * 2, size.height + grow * 2),
                cornerRadius = CornerRadius(baseRadius + grow)
            )
        }
    }
}

// DEPOIS (Console Craft: Hairline cirúrgica de 1.5dp e leve sombra de oclusão neutra):
.graphicsLayer {
    val s = if (isReorderMode) 1.0f + 0.08f * sel.value else 0.96f + 0.06f * sel.value
    scaleX = s
    scaleY = s
    shadowElevation = if (sel.value > 0.1f) 12.dp.toPx() * sel.value else 0f
    shape = RoundedCornerShape(16.dp)
    clip = true
}
.drawWithContent {
    drawContent()
    val a = sel.value.coerceIn(0f, 1f)
    if (a > 0.01f) {
        val strokeColor = if (isReorderMode) Color(0xFFF59E0B) else Color.White
        val strokeWidth = 1.8.dp.toPx() // Borda nítida de console
        drawRoundRect(
            color = strokeColor.copy(alpha = a),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(strokeWidth)
        )
    }
}
```

---

## 5. Grid Matemático de 4px e Fórmula de Raios Aninhados (Nested Radius)

### A. A Regra do Nested Radius
Em interfaces com acabamento de alto nível, os cantos arredondados de elementos dentro de containers devem satisfazer a relação matemática:
$$\mathbf{R_{interno} = R_{externo} - Padding}$$

Se o container possui raio de `16dp` e o padding interno é de `8dp`, o elemento filho **deve** ter raio de exatamente `8dp`. Se o elemento filho usar `12dp` ou `4dp`, os cantos perdem a concentricidade e geram a sensação subconsciente de "interface torta" ou amadora.

#### Tabela de Harmonização de Raios no NuvOS:

| Elemento Container | Raio Externo | Padding Interno | Raio Filho Ideal | Implementação Atual | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **GameCard (Capa Externa)** | 16.dp | 0.dp (borda interna) | 16.dp | 22.dp | ⚠️ Inconsistente |
| **StartMenuDialog (Janela)** | 20.dp | 16.dp | **8.dp** (itens de menu) | 12.dp | ⚠️ Desalinhado |
| **TopBar Perfil Pill** | 16.dp | 4.dp | **12.dp** (ou círculo) | 20.dp com avatar | ⚠️ Corrigir |
| **Botões de Ação (Dialogs)**| 12.dp | 8.dp | **4.dp** (badges internos)| 8.dp / sem regra | ⚠️ Corrigir |

### B. Normalização do Grid de 4px
Todos os espaçamentos devem ser múltiplos de 4px. Valores ímpares encontrados no código (`9dp` na TopBar, `11dp` em margens de texto, `13sp` de fonte em botões) devem ser arredondados para a escala:
- Micro-espaçamentos: `4.dp`, `8.dp`
- Componentes e Gaps: `12.dp`, `16.dp`
- Separação de Seções: `24.dp`, `32.dp`, `48.dp`

---

## 6. Tipografia, Legibilidade e Conforto Visual

### Diagnóstico de Tipografia
1. **Saturação de Pesos 800 e 900 (`ExtraBold` / `Black`)**:
   - `Chakra Petch` e `Rajdhani` são tipografias display geométricas de personalidade forte. Usá-las com peso `Black` em labels secundárias ("MOVER", "LB", "RB", horário, perfil) torna a tela visualmente barulhenta.
2. **Uso de Caixa Alta (ALL CAPS) em Excessos**:
   - Menus e badges com "MOVER", "INFORMAÇÕES DO APLICATIVO", "SELECT".
   - Diretriz Unslop: **Sentence case** em 100% dos menus e botões (`Mover item`, `Informações do app`, `Select`).

#### Tabela de Refatoração de Tipografia:

| Localização | Tipografia Atual | Refatoração Unslop | Justificativa |
| :--- | :--- | :--- | :--- |
| **Abas da TopBar** | 13sp `FontWeight.Bold` | 13sp `FontWeight.Medium` (Inativa: 40% opacidade / Ativa: 100% branca) | Cria foco calmo na aba ativa sem gritar |
| **Keycaps LB / RB** | 9sp `FontWeight.Black` | 9sp `FontWeight.Bold` em caixa neutra translúcida | Reduz a concorrência visual com as abas |
| **Texto de Perfil** | 13sp `FontWeight.Black` | 12sp `FontWeight.SemiBold` | Informação secundária não deve competir com os jogos |
| **Legenda de Card** | 13sp `FontWeight.Bold` | 12sp `FontWeight.Medium`, cor `NeutralHigh` | Conforto visual na rolagem do carrossel |
| **Ações da BottomBar**| 13sp `FontWeight.SemiBold`| 13sp `FontWeight.Normal` (400) com keycap de contraste | Leitura instantânea e elegante |

---

## 7. Ergonomia dos Controles Físicos (Gamepad UX)

Em telas de 120Hz operadas por controle físico, a percepção de latência é brutalmente influenciada pelo comportamento das animações:
1. **Eliminar Springs com Bounce Excessivo no Foco**:
   - O `SelectSpring` atual com amortecimento `0.75f` oscila duas vezes antes de estabilizar no card selecionado. Quando o jogador aperta rapidamente o D-Pad para o lado 5 vezes seguidas, os springs se acumulam gerando sensação de "lama" ou atraso no cursor.
   - **Solução**: Usar mola superamortecida (`dampingRatio = 0.92f`) ou interpolação direta `tween(120, FastOutSlowInEasing)` para feedback tátil instantâneo.
2. **Área de Toque vs. Foco por Controle**:
   - Os elementos da TopBar (Perfil, +, Engrenagem) possuem navegação via D-pad mapeada no `topBarIndex`. No entanto, visualmente a transição de foco entre a linha de abas centrais e os botões da direita precisa de um anel de foco comum para não confundir o usuário.
3. **Persistência de Scroll no Carrossel**:
   - A centralização horizontal com `animateScrollToItem(safeIndex, offset)` está tecnicamente excelente (`offset = -(viewport - itemW) / 2`). Deve ser mantida e reforçada com o snap fling nativo.

---

## 8. Guia Técnico de Mudanças por Arquivo

### 1. `app/src/main/java/com/decklauncher/ui/components/AppVectorIcons.kt`
- Substituir as funções `VectorIconGamepad`, `VectorIconBooks`, `VectorIconGear` e `VectorIconPalette` pelo código vetorial canônico com traço uniforme de `1.5dp` e `StrokeCap.Round`.

### 2. `app/src/main/java/com/decklauncher/ui/components/GameCard.kt`
- Remover o bloco `drawBehind` com o loop `repeat(3)`.
- Reduzir `cornerRadiusDp` de `22.dp` para `16.dp` (proporção 2:3 padrão de cartuchos/capas de console).
- Definir o traço de seleção para `1.8.dp` em vez de `3.5.dp`.
- Ajustar a legenda de texto inferior para `FontWeight.Medium` com cor `NeutralHigh`.

### 3. `app/src/main/java/com/decklauncher/ui/components/TopBar.kt`
- Suavizar a pílula de perfil: remover `scale(1.06f)`, definir fundo neutro transparente (`0x15FFFFFF`) e borda hairline de `1.dp` (`0x25FFFFFF`).
- Converter as caixas `[LB]` e `[RB]` para estilo monochrome sutil com cantos `4.dp`.
- Remover `shadow(14.dp, spotColor = Color.White)` dos botões `+` e engrenagem, utilizando borda nítida de `1.5.dp` branca no estado de foco.

### 4. `app/src/main/java/com/decklauncher/ui/components/BottomBar.kt`
- Refatorar `ControllerActionPrompt`: substituir a caixa retangular `#2A3240` por um micro-círculo keycap de `20.dp` de diâmetro com borda de `1.dp` e texto de botão de `10.sp` gravado.
- Padronizar os espaçamentos internos para a régua de 4px (`8.dp` entre o botão e a legenda, `20.dp` entre cada par de prompt).

### 5. `app/src/main/java/com/decklauncher/ui/theme/Motion.kt`
- Ajustar `SelectSpring` para `dampingRatio = 0.92f` e `stiffness = Spring.StiffnessMedium`, eliminando o tremor residual na navegação por controle físico.

---

## 9. Conclusão & Próximos Passos

A aplicação destas diretrizes transformará o **NuvOS** em uma das interfaces de console mais refinadas do ecossistema Android, eliminando os vícios estéticos de designs sobrecarregados por IA e adotando o rigor ergonômico e visual dos líderes da indústria (Sony, Valve e Nintendo).

O código está modular, isolado e pronto para receber estas melhorias de forma cirúrgica, sem quebrar nenhuma funcionalidade de persistência, emulação ou som já consolidada.
