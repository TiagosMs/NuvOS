package com.decklauncher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.AppFolder
import com.decklauncher.model.AppItem
import com.decklauncher.ui.theme.DotGreen
import com.decklauncher.ui.theme.TextWhite

/**
 * Gráfico vetorial preciso da pasta estilo console / OS moderno,
 * com aba superior chanfrada, cantos arredondados e suporte a sombra projetada.
 */
@Composable
fun FolderIconGraphic(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD6D6D6),
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

        drawSingleFolder(offsetX = 0f, offsetY = 0f, fillColor = color)
    }
}

/**
 * Card da pasta no grid 3x3 à esquerda (Mockup da Aba Apps).
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
        targetValue = if (isSelected) 1.04f else 0.98f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
        label = "folder_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 200f),
        label = "folder_border"
    )

    val shape = RoundedCornerShape(if (isCompactHeight) 16.dp else 22.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.12f)
            .scale(scale)
            .clip(shape)
            .border(
                BorderStroke(borderWidth, if (isSelected) Color.White else Color.Transparent),
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
            containerColor = if (isSelected) Color(0xFF3A3A3A) else Color(0xFF303030)
        ),
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompactHeight) 10.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            FolderIconGraphic(
                modifier = Modifier.size(
                    width = if (isCompactHeight) 44.dp else 60.dp,
                    height = if (isCompactHeight) 32.dp else 44.dp
                ),
                color = Color(0xFFD6D6D6)
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
        if (app?.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
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
 * aba frontal em relevo metálico e sombra dinâmica no piso.
 */
@Composable
fun FolderDetailPreview(
    folder: AppFolder,
    accentColor: Color = Color(0xFF1A73E8),
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    val infiniteTransition = rememberInfiniteTransition(label = "folder_3d_idle")

    // Flutuação vertical suave contínua
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "folder_float_y"
    )

    // Balanço sutil em 3D (Yaw / Eixo Y)
    val tiltYAnim by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "folder_tilt_y"
    )

    // Inclinação para frente estilo Console (Pitch / Eixo X)
    val tiltXAnim by infiniteTransition.animateFloat(
        initialValue = 11f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "folder_tilt_x"
    )

    // Escala e intensidade da sombra sincronizada com a altura da flutuação
    val shadowScaleX by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow_scale"
    )

    val shadowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow_alpha"
    )

    val folderWidth = if (isCompactHeight) 175.dp else 235.dp
    val folderHeight = if (isCompactHeight) 120.dp else 160.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Palco da Pasta 3D + Sombra
        Box(
            modifier = Modifier
                .width(folderWidth * 1.25f)
                .height(folderHeight * 1.35f),
            contentAlignment = Alignment.Center
        ) {
            // 1. Sombra e Glow no Piso do Console
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(folderWidth * 0.95f)
                    .height(folderHeight * 0.24f)
                    .offset(y = (-6).dp)
                    .scale(scaleX = shadowScaleX, scaleY = 1f)
            ) {
                // Brilho ambiente suave na cor de destaque
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.28f * shadowAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width * 0.55f
                    )
                )

                // Sombra de contato escura
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = shadowAlpha),
                            Color.Black.copy(alpha = shadowAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width * 0.48f
                    ),
                    size = Size(size.width, size.height * 0.85f),
                    topLeft = Offset(0f, size.height * 0.15f)
                )
            }

            // 2. Pasta 3D Flutuante com troca animada de categoria
            AnimatedContent(
                targetState = folder,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing)))
                        .togetherWith(fadeOut(animationSpec = tween(240, easing = FastOutSlowInEasing)))
                },
                label = "folder_3d_switch"
            ) { activeFolder ->
                Box(
                    modifier = Modifier
                        .width(folderWidth)
                        .height(folderHeight)
                        .graphicsLayer {
                            rotationX = tiltXAnim
                            rotationY = tiltYAnim
                            cameraDistance = 16f * density
                            translationY = floatAnim.dp.toPx()
                        },
                    contentAlignment = Alignment.BottomCenter
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

                    // Camada Frontal: Aba frontal da pasta em 3D (inclinada e em relevo prateado)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(folderHeight * 0.74f)
                            .graphicsLayer {
                                rotationX = -5f
                                cameraDistance = 16f * density
                            }
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

                            // Vinco / Detalhe de relevo sutil de console no canto inferior
                            drawRoundRect(
                                color = Color(0x18000000),
                                topLeft = Offset(w * 0.12f, h * 0.32f),
                                size = Size(w * 0.76f, 3.dp.toPx()),
                                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isCompactHeight) 10.dp else 18.dp))

        // Título em destaque
        Text(
            text = folder.title,
            color = Color.White,
            fontSize = if (isCompactHeight) 24.sp else 32.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 6.dp))

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
