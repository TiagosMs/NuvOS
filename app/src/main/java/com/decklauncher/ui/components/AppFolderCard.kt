package com.decklauncher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import com.decklauncher.ui.theme.Motion
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.AppFolder
import com.decklauncher.model.AppItem
import com.decklauncher.ui.theme.BtnAGreen
import com.decklauncher.ui.theme.BtnBRed
import com.decklauncher.ui.theme.BtnXBlue
import com.decklauncher.ui.theme.DotGreen
import com.decklauncher.ui.theme.TextWhite
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Desenha símbolos vetoriais nítidos e reconhecíveis para cada categoria de aplicativo:
 * - SYSTEM: Engrenagem / Cogwheel mecânico
 * - MEDIA: Botão Play com arcos de áudio / onda sonora
 * - SOCIAL: Balão de fala com 3 pontos de conversa
 * - PRODUCTIVITY: Prancheta / Checklist com marcação de concluído
 * - TOOLS: Chave de boca / ferramenta mecânica
 * - CREATIVE: Paleta de pintura com orifício e tintas
 * - OTHERS: Grid 2x2 de blocos de aplicativos
 */
fun DrawScope.drawCategorySymbol(
    categoryId: String,
    color: Color,
    centerOffset: Offset = center,
    symbolSize: Float = size.minDimension * 0.52f
) {
    val cx = centerOffset.x
    val cy = centerOffset.y
    val r = symbolSize * 0.5f

    when (categoryId.lowercase()) {
        "system" -> {
            // Engrenagem mecânica precisa (8 dentes + furo central)
            val numTeeth = 8
            val outerR = r * 0.95f
            val valleyR = r * 0.72f
            val holeR = r * 0.35f

            val gearPath = Path().apply {
                for (i in 0 until numTeeth * 2) {
                    val angle = (i * PI.toFloat() / numTeeth) - (PI.toFloat() / 2f)
                    val rad = if (i % 2 == 0) outerR else valleyR
                    val px = cx + cos(angle) * rad
                    val py = cy + sin(angle) * rad
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(gearPath, color)
            // Furo central com cor de contraste
            drawCircle(Color(0xFF303030), radius = holeR, center = Offset(cx, cy))
        }

        "media" -> {
            // Botão Play triangular estilizado + arco de som
            val triPath = Path().apply {
                val left = cx - r * 0.65f
                val right = cx + r * 0.25f
                val top = cy - r * 0.65f
                val bottom = cy + r * 0.65f
                moveTo(left, top)
                lineTo(right, cy)
                lineTo(left, bottom)
                close()
            }
            drawPath(triPath, color)

            // Arco de onda sonora à direita
            drawArc(
                color = color,
                startAngle = -48f,
                sweepAngle = 96f,
                useCenter = false,
                topLeft = Offset(cx - r * 0.1f, cy - r * 0.75f),
                size = Size(r * 1.05f, r * 1.5f),
                style = Stroke(width = r * 0.22f, cap = StrokeCap.Round)
            )
        }

        "social" -> {
            // Balão de chat arredondado com ponta
            val bw = r * 1.75f
            val bh = r * 1.35f
            val bx = cx - bw * 0.5f
            val by = cy - bh * 0.58f
            val cr = bh * 0.32f

            val bubblePath = Path().apply {
                addRoundRect(RoundRect(Rect(bx, by, bx + bw, by + bh), CornerRadius(cr, cr)))
                // Rabicho do balão no canto inferior esquerdo
                moveTo(bx + bw * 0.22f, by + bh - 1f)
                lineTo(bx + bw * 0.08f, by + bh + r * 0.42f)
                lineTo(bx + bw * 0.42f, by + bh - 1f)
                close()
            }
            drawPath(bubblePath, color)

            // 3 pontos de conversa internos
            val dotR = r * 0.13f
            val dotY = by + bh * 0.5f
            drawCircle(Color(0xFF303030), radius = dotR, center = Offset(cx - r * 0.42f, dotY))
            drawCircle(Color(0xFF303030), radius = dotR, center = Offset(cx, dotY))
            drawCircle(Color(0xFF303030), radius = dotR, center = Offset(cx + r * 0.42f, dotY))
        }

        "productivity" -> {
            // Prancheta de tarefas com clipe superior e checkmark
            val bw = r * 1.4f
            val bh = r * 1.75f
            val bx = cx - bw * 0.5f
            val by = cy - bh * 0.48f
            val cr = bw * 0.18f

            // Corpo da prancheta
            drawRoundRect(
                color = color,
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = CornerRadius(cr, cr),
                style = Stroke(width = r * 0.22f)
            )

            // Clipe metálico no topo
            drawRoundRect(
                color = color,
                topLeft = Offset(cx - bw * 0.28f, by - r * 0.22f),
                size = Size(bw * 0.56f, r * 0.38f),
                cornerRadius = CornerRadius(cr * 0.6f, cr * 0.6f)
            )

            // Visto / Checkmark nítido central
            val checkPath = Path().apply {
                moveTo(cx - bw * 0.22f, cy + r * 0.05f)
                lineTo(cx - bw * 0.04f, cy + r * 0.32f)
                lineTo(cx + bw * 0.26f, cy - r * 0.18f)
            }
            drawPath(
                path = checkPath,
                color = color,
                style = Stroke(width = r * 0.22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        "tools" -> {
            // Chave de boca mecânica angulada
            val wrenchPath = Path().apply {
                // Cabeça circular da chave com abertura de garra
                val headR = r * 0.72f
                val hx = cx - r * 0.18f
                val hy = cy - r * 0.18f

                // Traço do cabo até o canto inferior direito
                moveTo(hx + headR * 0.45f, hy + headR * 0.45f)
                lineTo(cx + r * 0.75f, cy + r * 0.75f)
                lineTo(cx + r * 0.58f, cy + r * 0.92f)
                lineTo(hx + headR * 0.28f, hy + headR * 0.62f)

                // Garra circular aberta
                arcTo(
                    rect = Rect(hx - headR, hy - headR, hx + headR, hy + headR),
                    startAngleDegrees = 110f,
                    sweepAngleDegrees = 260f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(
                path = wrenchPath,
                color = color,
                style = Stroke(width = r * 0.22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Abertura da chave
            drawCircle(color, radius = r * 0.18f, center = Offset(cx + r * 0.66f, cy + r * 0.84f))
        }

        "creative" -> {
            // Paleta de pintura de artista com furo para o polegar e manchas de cor
            val pw = r * 1.8f
            val ph = r * 1.5f
            val px = cx - pw * 0.5f
            val py = cy - ph * 0.52f

            drawOval(
                color = color,
                topLeft = Offset(px, py),
                size = Size(pw, ph)
            )

            // Orifício para o dedo
            drawCircle(
                color = Color(0xFF303030),
                radius = r * 0.22f,
                center = Offset(cx + pw * 0.26f, cy + ph * 0.14f)
            )

            // 3 pontos de tinta em baixo relevo
            val dotR = r * 0.14f
            drawCircle(Color(0xFF303030), radius = dotR, center = Offset(cx - pw * 0.24f, cy - ph * 0.15f))
            drawCircle(Color(0xFF303030), radius = dotR, center = Offset(cx - pw * 0.04f, cy - ph * 0.24f))
            drawCircle(Color(0xFF303030), radius = dotR, center = Offset(cx - pw * 0.32f, cy + ph * 0.12f))
        }

        else -> {
            // OUTROS: Grid 2x2 com 4 pequenos cartuchos / blocos de apps
            val sq = r * 0.62f
            val gap = r * 0.24f
            val cr = sq * 0.28f

            drawRoundRect(color, Offset(cx - sq - gap * 0.5f, cy - sq - gap * 0.5f), Size(sq, sq), CornerRadius(cr, cr))
            drawRoundRect(color, Offset(cx + gap * 0.5f, cy - sq - gap * 0.5f), Size(sq, sq), CornerRadius(cr, cr))
            drawRoundRect(color, Offset(cx - sq - gap * 0.5f, cy + gap * 0.5f), Size(sq, sq), CornerRadius(cr, cr))
            drawRoundRect(color, Offset(cx + gap * 0.5f, cy + gap * 0.5f), Size(sq, sq), CornerRadius(cr, cr))
        }
    }
}

/**
 * Gráfico vetorial preciso da pasta estilo console / OS moderno,
 * com aba superior chanfrada, cantos arredondados, símbolo individualizado da categoria
 * e suporte a sombra projetada.
 */
@Composable
fun FolderIconGraphic(
    modifier: Modifier = Modifier,
    folderId: String? = null,
    color: Color = Color(0xFFD6D6D6),
    symbolColor: Color = Color(0xFF2A2E36),
    hasShadow: Boolean = false,
    shadowColor: Color = Color(0x77000000)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val tabW = w * 0.32f
        val slopeW = w * 0.12f
        val tabH = h * 0.16f
        val r = (w * 0.08f).coerceAtMost(h * 0.08f)

        fun drawSingleFolder(offsetX: Float, offsetY: Float, fillColor: Color) {
            val path = Path().apply {
                moveTo(offsetX, offsetY + r)
                quadraticBezierTo(offsetX, offsetY, offsetX + r, offsetY)
                lineTo(offsetX + tabW - r, offsetY)
                quadraticBezierTo(offsetX + tabW, offsetY, offsetX + tabW + r * 0.4f, offsetY + r * 0.2f)
                lineTo(offsetX + tabW + slopeW - r * 0.4f, offsetY + tabH - r * 0.2f)
                quadraticBezierTo(offsetX + tabW + slopeW, offsetY + tabH, offsetX + tabW + slopeW + r, offsetY + tabH)
                lineTo(offsetX + w - r, offsetY + tabH)
                quadraticBezierTo(offsetX + w, offsetY + tabH, offsetX + w, offsetY + tabH + r)
                lineTo(offsetX + w, offsetY + h - r)
                quadraticBezierTo(offsetX + w, offsetY + h, offsetX + w - r, offsetY + h)
                lineTo(offsetX + r, offsetY + h)
                quadraticBezierTo(offsetX, offsetY + h, offsetX, offsetY + h - r)
                lineTo(offsetX, offsetY + r)
                close()
            }
            drawPath(path, fillColor)
        }

        if (hasShadow) {
            drawSingleFolder(
                offsetX = -w * 0.02f,
                offsetY = h * 0.05f,
                fillColor = shadowColor
            )
        }

        // Desenha a pasta base
        drawSingleFolder(offsetX = 0f, offsetY = 0f, fillColor = color)

        // Se houver categoria, desenha o símbolo exclusivo centrado no corpo da pasta
        if (folderId != null) {
            val bodyCenterY = (tabH + h) * 0.5f
            val symbolSize = (w * 0.42f).coerceAtMost((h - tabH) * 0.62f)
            drawCategorySymbol(
                categoryId = folderId,
                color = symbolColor,
                centerOffset = Offset(w * 0.5f, bodyCenterY),
                symbolSize = symbolSize
            )
        }
    }
}

/**
 * Card da pasta no carrossel horizontal (Aba Apps),
 * com símbolo diferenciado para cada categoria, título e contagem de apps.
 */
@Composable
fun AppFolderCard(
    folder: AppFolder,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 0.96f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
        label = "folder_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 1.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 200f),
        label = "folder_border"
    )

    val shape = RoundedCornerShape(if (isCompactHeight) 16.dp else 20.dp)
    val cardWidth = if (isCompactHeight) 130.dp else 165.dp
    val cardHeight = if (isCompactHeight) 88.dp else 115.dp

    Card(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .scale(scale)
            .clip(shape)
            .border(
                BorderStroke(borderWidth, if (isSelected) Color.White else Color(0x28FFFFFF)),
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isSelected) onOpen() else onSelect()
            }
            .semantics { contentDescription = "Pasta ${folder.title}" },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF383838) else Color(0xFF262626)
        ),
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompactHeight) 8.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FolderIconGraphic(
                modifier = Modifier.size(
                    width = if (isCompactHeight) 44.dp else 56.dp,
                    height = if (isCompactHeight) 32.dp else 40.dp
                ),
                folderId = folder.id,
                color = if (isSelected) Color(0xFFE4E4E4) else Color(0xFFCBCBCB),
                symbolColor = if (isSelected) Color(0xFF22262E) else Color(0xFF2D323B)
            )

            Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 6.dp))

            Text(
                text = folder.title,
                color = if (isSelected) Color.White else Color(0xFFC0C0C0),
                fontSize = if (isCompactHeight) 12.sp else 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${folder.apps.size} ${if (folder.apps.size == 1) "app" else "apps"}",
                color = if (isSelected) DotGreen else Color(0x88FFFFFF),
                fontSize = if (isCompactHeight) 10.sp else 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Cartucho / Cartão de aplicativo espiando de dentro da Pasta 3D.
 */
@Composable
private fun PeekAppCard(
    app: AppItem?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val appIcon = com.decklauncher.data.rememberAppIcon(app)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF353942), Color(0xFF1E2126))
                )
            )
            .border(1.2.dp, Color(0x55FFFFFF), shape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (appIcon != null) {
            Image(
                bitmap = appIcon,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(7.dp))
            )
        } else if (app != null) {
            Text(
                text = app.label.take(1).uppercase(),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
            )
        }
    }
}

/**
 * Painel direito do launcher: Pasta 3D animada em perspectiva isométrica estilo Console,
 * com física flutuante contínua, cartões de apps peeking de dentro da pasta,
 * aba frontal em relevo metálico com o brasão da categoria e sombra dinâmica no piso.
 * Sem clipping durante a troca de seleção (SizeTransform clip = false).
 */
@Composable
fun FolderDetailPreview(
    folder: AppFolder,
    accentColor: Color = Color(0xFF627B56),
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    val phase = rememberInfiniteTransition(label = "folder_3d_idle").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "folder_phase"
    )

    val folderWidth = if (isCompactHeight) 155.dp else 215.dp
    val folderHeight = if (isCompactHeight) 105.dp else 145.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Palco da Pasta 3D + Sombra (amplo para impedir qualquer corte nas rotações)
        Box(
            modifier = Modifier
                .width(folderWidth * 1.45f)
                .height(folderHeight * 1.55f),
            contentAlignment = Alignment.Center
        ) {
            // 1. Sombra e Glow no Piso do Console (Fase 4.3: drawBehind com phase)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(folderWidth * 0.95f)
                    .height(folderHeight * 0.24f)
                    .offset(y = 2.dp)
                    .drawBehind {
                        val p = phase.value
                        val norm = sin(p) * 0.5f + 0.5f
                        val sAlpha = (0.35f + 0.15f * norm).coerceIn(0f, 1f)
                        val scaleW = 0.94f + 0.12f * norm

                        val sw = size.width * scaleW
                        val sh = size.height
                        val left = (size.width - sw) * 0.5f

                        // Brilho ambiente suave na cor de destaque
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = (0.30f * sAlpha).coerceIn(0f, 1f)),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.5f),
                                radius = sw * 0.55f
                            ),
                            topLeft = Offset(left, 0f),
                            size = Size(sw, sh)
                        )

                        // Sombra de contato escura
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = sAlpha),
                                    Color.Black.copy(alpha = (sAlpha * 0.4f).coerceIn(0f, 1f)),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.5f, size.height * 0.5f),
                                radius = sw * 0.48f
                            ),
                            topLeft = Offset(left, size.height * 0.15f),
                            size = Size(sw, size.height * 0.85f)
                        )
                    }
            )

            // 2. Pasta 3D Flutuante com troca animada de categoria sem corte de bordas
            AnimatedContent(
                targetState = folder,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(Motion.Base)) + scaleIn(initialScale = 0.94f, animationSpec = tween(Motion.Base)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(Motion.Fast))
                        ) using SizeTransform(clip = false)
                },
                modifier = Modifier.wrapContentSize(unbounded = false),
                label = "folder_3d_switch"
            ) { activeFolder ->
                Box(
                    modifier = Modifier
                        .width(folderWidth * 1.30f)
                        .height(folderHeight * 1.40f)
                        .padding(horizontal = 14.dp, vertical = 20.dp)
                        .graphicsLayer {
                            val p = phase.value
                            translationY = sin(p) * (if (isCompactHeight) 4.dp.toPx() else 6.dp.toPx())
                            rotationX = 13.5f + sin(p) * 2.5f
                            rotationY = -10.5f + sin(p + 1f) * 3.5f
                            cameraDistance = 16f * density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(folderWidth)
                            .height(folderHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        // Camada Traseira: Chapa posterior da pasta com a aba (tab)
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val tabW = w * 0.34f
                            val slopeW = w * 0.13f
                            val tabH = h * 0.17f
                            val r = w * 0.08f

                            val backPath = Path().apply {
                                moveTo(0f, r)
                                quadraticBezierTo(0f, 0f, r, 0f)
                                lineTo(tabW - r, 0f)
                                quadraticBezierTo(tabW, 0f, tabW + r * 0.4f, r * 0.2f)
                                lineTo(tabW + slopeW - r * 0.4f, tabH - r * 0.2f)
                                quadraticBezierTo(tabW + slopeW, tabH, tabW + slopeW + r, tabH)
                                lineTo(w - r, tabH)
                                quadraticBezierTo(w, tabH, w, tabH + r)
                                lineTo(w, h - r)
                                quadraticBezierTo(w, h, w - r, h)
                                lineTo(r, h)
                                quadraticBezierTo(0f, h, 0f, h - r)
                                close()
                            }

                            // Preenchimento acetinado grafite escuro da chapa traseira
                            drawPath(
                                path = backPath,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF4A4E57), Color(0xFF23262D))
                                )
                            )

                            // Borda chanfrada de luz na aba
                            drawPath(
                                path = backPath,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0x77FFFFFF), Color(0x18FFFFFF))
                                ),
                                style = Stroke(width = 1.6.dp.toPx())
                            )
                        }

                        // Camada Intermediária: Cartões de Apps espiando de dentro da pasta
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(
                                    top = folderHeight * 0.05f,
                                    start = folderWidth * 0.12f,
                                    end = folderWidth * 0.12f
                                ),
                            horizontalArrangement = Arrangement.spacedBy(folderWidth * 0.04f),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val previewApps = activeFolder.apps.take(3)
                            val appsToRender = if (previewApps.isNotEmpty()) {
                                previewApps
                            } else {
                                listOf(null)
                            }

                            appsToRender.forEachIndexed { i, app ->
                                val cardElevation = if (i == 1) (-14).dp else (-5).dp
                                val cardRotation = if (i == 0) (-6f) else if (i == 2) 7f else 1f

                                PeekAppCard(
                                    app = app,
                                    modifier = Modifier
                                        .offset(y = cardElevation)
                                        .graphicsLayer {
                                            rotationZ = cardRotation
                                            cameraDistance = 16f * density
                                        }
                                        .weight(1f)
                                        .height(folderHeight * 0.48f)
                                )
                            }
                        }

                        // Camada Frontal: Aba frontal da pasta em 3D com o brasão da categoria gravado
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(folderHeight * 0.74f)
                                .graphicsLayer {
                                    rotationX = -5f
                                    cameraDistance = 16f * density
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val r = w * 0.08f

                                val frontPath = Path().apply {
                                    moveTo(0f, r * 0.6f)
                                    quadraticBezierTo(0f, 0f, r * 0.6f, 0f)
                                    lineTo(w - r, 0f)
                                    quadraticBezierTo(w, 0f, w, r)
                                    lineTo(w, h - r)
                                    quadraticBezierTo(w, h, w - r, h)
                                    lineTo(r, h)
                                    quadraticBezierTo(0f, h, 0f, h - r)
                                    close()
                                }

                                // Preenchimento estilo carcaça de console (alumínio satinado premium)
                                drawPath(
                                    path = frontPath,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFE8E8E8),
                                            Color(0xFFD4D4D4),
                                            Color(0xFFBEBEBE)
                                        )
                                    )
                                )

                                // Linha de luz especular no topo da aba frontal
                                drawLine(
                                    color = Color.White.copy(alpha = 0.9f),
                                    start = Offset(r * 0.6f, 1.2.dp.toPx()),
                                    end = Offset(w - r, 1.2.dp.toPx()),
                                    strokeWidth = 2.dp.toPx()
                                )

                                // Contorno suave em toda a borda
                                drawPath(
                                    path = frontPath,
                                    color = Color(0x33000000),
                                    style = Stroke(width = 1.dp.toPx())
                                )

                                // Brasão da categoria gravado no centro da aba frontal
                                drawCategorySymbol(
                                    categoryId = activeFolder.id,
                                    color = Color(0x40000000),
                                    centerOffset = Offset(w * 0.5f, h * 0.55f + 1f),
                                    symbolSize = h * 0.46f
                                )
                                drawCategorySymbol(
                                    categoryId = activeFolder.id,
                                    color = Color(0xFF555B68),
                                    centerOffset = Offset(w * 0.5f, h * 0.55f),
                                    symbolSize = h * 0.46f
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isCompactHeight) 6.dp else 10.dp))

        // Título em destaque da categoria
        Text(
            text = folder.title,
            color = Color.White,
            fontSize = if (isCompactHeight) 22.sp else 28.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(if (isCompactHeight) 3.dp else 5.dp))

        // Pílula com contagem de aplicativos estilo console
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x28FFFFFF))
                .border(1.dp, Color(0x2AFFFFFF), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = if (isCompactHeight) 2.dp else 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCompactHeight) 5.dp else 6.dp)
                    .clip(CircleShape)
                    .background(DotGreen)
            )
            Text(
                text = "${folder.apps.size} ${if (folder.apps.size == 1) "APLICATIVO" else "APLICATIVOS"}",
                color = TextWhite,
                fontSize = if (isCompactHeight) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

/**
 * Tile de aplicativo no carrossel de console dentro de pastas,
 * com ícones 100% preenchidos de ponta a ponta (sem bordas/padding interno),
 * cantos suaves em squircle, escala de foco animada e suporte a controle.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConsoleAppTile(
    app: AppItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onLaunch: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 0.94f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 170f),
        label = "console_tile_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 200f),
        label = "console_tile_border"
    )

    val tileSize = if (isCompactHeight) 80.dp else 105.dp
    val tileShape = RoundedCornerShape(if (isCompactHeight) 16.dp else 22.dp)

    Column(
        modifier = modifier
            .width(tileSize + 12.dp)
            .scale(scale)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (isSelected) onLaunch() else onSelect()
                },
                onLongClick = {
                    onSelect()
                    onLongClick?.invoke()
                }
            )
            .semantics { contentDescription = app.label },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(tileSize)
                .clip(tileShape)
                .background(Color(0xFF22242B))
                .border(
                    BorderStroke(borderWidth, if (isSelected) Color.White else Color(0x28FFFFFF)),
                    shape = tileShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val icon = com.decklauncher.data.rememberAppIcon(app)
            if (icon != null) {
                Image(
                    bitmap = icon,
                    contentDescription = app.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(tileShape)
                )
            } else {
                Text(
                    text = app.label.take(1).uppercase(),
                    color = Color.White,
                    fontSize = if (isCompactHeight) 24.sp else 30.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 6.dp))

        Text(
            text = app.label,
            color = if (isSelected) Color.White else Color(0xFF9E9E9E),
            fontSize = if (isCompactHeight) 10.sp else 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
        )
    }
}

/**
 * Showcase superior de aplicativo focado no estilo console,
 * apresentando ícone amplo edge-to-edge, título de alto impacto, tag da pasta
 * e botões de ação interativos do controle ([A] Iniciar, [X] Opções, [B] Voltar).
 */
@Composable
fun FolderAppShowcase(
    app: AppItem?,
    folderTitle: String,
    currentIndex: Int,
    totalApps: Int,
    accentColor: Color,
    onLaunch: () -> Unit,
    onManage: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    if (app == null) return

    val iconSize = if (isCompactHeight) 76.dp else 105.dp
    val iconShape = RoundedCornerShape(if (isCompactHeight) 18.dp else 24.dp)
    val appIcon = com.decklauncher.data.rememberAppIcon(app)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompactHeight) 28.dp else 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 20.dp else 28.dp)
    ) {
        // Grande Ícone do App em destaque (100% preenchido)
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(iconShape)
                .background(Color(0xFF252830))
                .border(2.5.dp, Color.White, iconShape),
            contentAlignment = Alignment.Center
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = app.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(iconShape)
                )
            } else {
                Text(
                    text = app.label.take(1).uppercase(),
                    color = Color.White,
                    fontSize = if (isCompactHeight) 28.sp else 38.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Coluna de Informações e Botões Console
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Tag da Pasta e Contagem
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x28FFFFFF))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(DotGreen)
                )
                Text(
                    text = "$folderTitle  •  ${currentIndex + 1} DE $totalApps".uppercase(),
                    color = Color(0xFFDDDDDD),
                    fontSize = if (isCompactHeight) 9.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 8.dp))

            // Título do App
            Text(
                text = app.label,
                color = Color.White,
                fontSize = if (isCompactHeight) 22.sp else 30.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Pacote / Subtítulo
            Text(
                text = app.packageName,
                color = Color(0x88FFFFFF),
                fontSize = if (isCompactHeight) 10.sp else 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(if (isCompactHeight) 8.dp else 14.dp))

            // Linha de Ações Estilo Console ([A] Iniciar, [X] Opções, [B] Voltar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botão [A] INICIAR
                ConsoleActionButton(
                    buttonLabel = "A",
                    actionText = "INICIAR",
                    badgeBgColor = BtnAGreen,
                    badgeTextColor = Color.Black,
                    containerBg = Color(0x3328A745),
                    borderColor = BtnAGreen.copy(alpha = 0.5f),
                    onClick = onLaunch,
                    isCompact = isCompactHeight
                )

                // Botão [X] OPÇÕES
                ConsoleActionButton(
                    buttonLabel = "X",
                    actionText = "OPÇÕES",
                    badgeBgColor = BtnXBlue,
                    badgeTextColor = Color.White,
                    containerBg = Color(0x28FFFFFF),
                    borderColor = Color(0x33FFFFFF),
                    onClick = onManage,
                    isCompact = isCompactHeight
                )

                // Botão [B] VOLTAR
                ConsoleActionButton(
                    buttonLabel = "B",
                    actionText = "VOLTAR",
                    badgeBgColor = BtnBRed,
                    badgeTextColor = Color.White,
                    containerBg = Color(0x28FFFFFF),
                    borderColor = Color(0x33FFFFFF),
                    onClick = onBack,
                    isCompact = isCompactHeight
                )
            }
        }
    }
}

@Composable
private fun ConsoleActionButton(
    buttonLabel: String,
    actionText: String,
    badgeBgColor: Color,
    badgeTextColor: Color,
    containerBg: Color,
    borderColor: Color,
    onClick: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = if (isCompact) 10.dp else 12.dp,
                vertical = if (isCompact) 4.dp else 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompact) 16.dp else 19.dp)
                .clip(CircleShape)
                .background(badgeBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buttonLabel,
                color = badgeTextColor,
                fontSize = if (isCompact) 9.sp else 11.sp,
                fontWeight = FontWeight.Black
            )
        }
        Text(
            text = actionText,
            color = Color.White,
            fontSize = if (isCompact) 10.sp else 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
