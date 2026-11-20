package com.decklauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.decklauncher.data.rememberAppIcon
import com.decklauncher.model.AppItem
import com.decklauncher.ui.theme.Motion
import com.decklauncher.ui.theme.StatusPlaying
import com.decklauncher.ui.theme.TextPrimary
import com.decklauncher.ui.theme.TextWhite
import java.io.File

/**
 * Card vertical de Jogo no carrossel horizontal estilo Console (Fase 4.5):
 * - Progresso único de seleção (0->1) lido estritamente em drawBehind e graphicsLayer.
 * - Halo iluminado via drawBehind (zero recriação de sombra por frame).
 * - Carregamento otimizado de capa via Coil AsyncImage decodificado no tamanho exato.
 * - Ícone em cache LRU sob demanda (Fase 2.1).
 */
@Composable
fun GameCard(
    app: AppItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onLaunch: () -> Unit,
    isReorderMode: Boolean = false,
    accentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    val cardWidth = if (isCompactHeight) 118.dp else 175.dp
    val cardHeight = if (isCompactHeight) 168.dp else 250.dp
    val cornerRadiusDp = if (isCompactHeight) 16.dp else 22.dp
    val fallbackIconSize = if (isCompactHeight) 48.dp else 72.dp
    val labelFontSize = if (isCompactHeight) 10.sp else 13.sp

    // Progresso único 0->1 de foco com Spring Console (Fase 4.5)
    val sel = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = Motion.SelectSpring,
        label = "game_card_sel"
    )

    val shape = RoundedCornerShape(cornerRadiusDp)
    val appIcon = rememberAppIcon(app)

    Column(
        modifier = modifier
            .width(cardWidth)
            .semantics { contentDescription = app.label },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .graphicsLayer {
                    val s = if (isReorderMode) {
                        0.94f + 0.15f * sel.value
                    } else {
                        0.94f + 0.14f * sel.value
                    }
                    scaleX = s
                    scaleY = s
                }
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
                .clip(shape)
                .drawWithContent {
                    drawContent()
                    val a = sel.value.coerceIn(0f, 1f)
                    if (a > 0.01f) {
                        val strokeColor = if (isReorderMode) Color(0xFFF59E0B) else Color.White
                        val strokeWidth = if (isCompactHeight) 2.8.dp.toPx() else 3.5.dp.toPx()
                        drawRoundRect(
                            color = strokeColor.copy(alpha = a),
                            cornerRadius = CornerRadius(cornerRadiusDp.toPx()),
                            style = Stroke(strokeWidth)
                        )
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isSelected) onLaunch() else onSelect()
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
            ) {
                if (app.coverPath != null) {
                    // Capa decodificada no tamanho exato do card pelo Coil (Fase 2.6)
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(app.coverPath))
                            .size(350, 500)
                            .memoryCacheKey(app.id)
                            .crossfade(false)
                            .build(),
                        contentDescription = app.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (appIcon != null) {
                    // Fundo gradiente estilizado com o ícone do app em cache LRU
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF2E2E2E),
                                        Color(0xFF141414)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = appIcon,
                            contentDescription = app.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(fallbackIconSize)
                                .clip(RoundedCornerShape(if (isCompactHeight) 14.dp else 20.dp))
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF282828)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = app.label.take(1).uppercase(),
                            color = TextWhite,
                            fontSize = if (isCompactHeight) 28.sp else 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isCompactHeight) 6.dp else 10.dp))

        // Indicador de reprodução sob o card selecionado (Fase 4.5: animação via alpha/translation no graphicsLayer)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    alpha = sel.value
                    translationY = (1f - sel.value) * 6.dp.toPx()
                },
            contentAlignment = Alignment.Center
        ) {
            if (isReorderMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF59E0B))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "◀ MOVER ▶",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    val badgeSize = if (isCompactHeight) 10.dp else 14.dp
                    Box(
                        modifier = Modifier
                            .size(badgeSize)
                            .clip(CircleShape)
                            .background(StatusPlaying),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.size(if (isCompactHeight) 4.5.dp else 6.5.dp)
                        ) {
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                lineTo(size.width, size.height * 0.5f)
                                lineTo(0f, size.height)
                                close()
                            }
                            drawPath(path, Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.width(if (isCompactHeight) 4.dp else 6.dp))

                    Text(
                        text = app.label,
                        color = TextPrimary,
                        fontSize = labelFontSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
