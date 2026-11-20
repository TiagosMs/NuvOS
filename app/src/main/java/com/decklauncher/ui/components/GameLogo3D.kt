package com.decklauncher.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.LogoManager
import com.decklauncher.model.AppItem
import com.decklauncher.ui.theme.Motion
import kotlin.math.PI
import kotlin.math.sin

/**
 * Logo 3D Animada com alta performance (Fase 4.3):
 * - Extrusão pré-renderizada fora da main thread (1 imagem em vez de 5 camadas).
 * - 1 única animação de fase contínua lida estritamente dentro de graphicsLayer (zero recomposições por frame).
 * - Sombra no piso via drawBehind reativo.
 * - SizeTransform(clip = false) para prevenir qualquer corte em transições.
 */
@Composable
fun GameLogo3D(
    currentApp: AppItem?,
    logoBitmap: Bitmap?,
    accentColor: Color,
    modifier: Modifier = Modifier,
    metadataUpdateCounter: Int = 0
) {
    if (currentApp == null) return

    val context = LocalContext.current
    val density = LocalDensity.current.density
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    val maxLogoHeight = if (isCompactHeight) 76.dp else 138.dp
    val maxLogoWidth = if (isCompactHeight) 280.dp else 440.dp
    val fallbackFontSize = if (isCompactHeight) 20.sp else 32.sp

    // Uma só animação de fase contínua com harmônicos inteiros (Fase 4.3 Passo B)
    // Mantém o objeto State para ler phase.value somente dentro da fase de draw (graphicsLayer)
    val phase = rememberInfiniteTransition(label = "logo_3d_idle").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Pré-renderiza a extrusão em memória para renderização direta a 60/120 fps
    val extrudedBitmap = remember(currentApp.id, logoBitmap) {
        if (logoBitmap != null) {
            LogoManager.getExtrudedLogo(context, currentApp.id, logoBitmap)
        } else {
            null
        }
    }

    val hasLocalLogo = remember(currentApp.id, metadataUpdateCounter) {
        LogoManager.getLogoFile(context, currentApp.id).exists()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompactHeight) 12.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            targetState = Pair(currentApp.id, extrudedBitmap),
            transitionSpec = {
                (fadeIn(animationSpec = tween(Motion.Base)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(Motion.Base)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(Motion.Fast))
                    ) using SizeTransform(clip = false)
            },
            label = "game_logo_3d_switch"
        ) { (_, extruded) ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .wrapContentSize(unbounded = false)
                    .padding(
                        horizontal = 20.dp,
                        vertical = if (isCompactHeight) 6.dp else 14.dp
                    )
            ) {
                val shadowWidthDp = if (isCompactHeight) 160.dp else 250.dp
                val shadowHeightDp = if (isCompactHeight) 14.dp else 20.dp
                val shadowOffsetYDp = if (isCompactHeight) 36.dp else 64.dp

                // Sombra do piso calculada via drawBehind com alpha derivado da phase (Fase 4.3 Passo C)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = shadowOffsetYDp)
                        .width(shadowWidthDp)
                        .height(shadowHeightDp)
                        .drawBehind {
                            val p = phase.value
                            val normSine = (sin(p) * 0.5f + 0.5f)
                            val sAlpha = (0.32f + 0.16f * normSine).coerceIn(0f, 1f)
                            val scaleW = 0.92f + 0.16f * normSine

                            val sw = size.width * scaleW
                            val sh = size.height * scaleW
                            val left = (size.width - sw) * 0.5f
                            val top = (size.height - sh) * 0.5f

                            // Glow ambiente de destaque
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = (0.40f * sAlpha).coerceIn(0f, 1f)),
                                        accentColor.copy(alpha = (0.10f * sAlpha).coerceIn(0f, 1f)),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                                    radius = sw * 0.55f
                                ),
                                topLeft = Offset(left, top),
                                size = Size(sw, sh)
                            )

                            // Sombra escura de oclusão
                            drawOval(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = sAlpha),
                                        Color.Black.copy(alpha = (sAlpha * 0.35f).coerceIn(0f, 1f)),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                                    radius = sw * 0.48f
                                ),
                                topLeft = Offset(left, top),
                                size = Size(sw, sh * 0.85f)
                            )
                        }
                )

                // Palco do Logo 3D: valores animados lidos estritamente dentro da fase de draw do graphicsLayer
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            val p = phase.value
                            translationY = sin(p) * (if (isCompactHeight) 6.dp.toPx() else 10.dp.toPx())
                            rotationY = sin(p + 1f) * 9.5f
                            rotationX = sin(2f * p) * 4.5f
                            rotationZ = sin(3f * p + 2f) * 1.4f
                            cameraDistance = 16f * density
                        }
                ) {
                    if (extruded != null) {
                        Image(
                            bitmap = extruded.asImageBitmap(),
                            contentDescription = currentApp.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .heightIn(max = maxLogoHeight)
                                .widthIn(max = maxLogoWidth)
                                .padding(horizontal = 4.dp)
                        )
                    } else if (!hasLocalLogo) {
                        // Fallback tipográfico em relevo para títulos que realmente não possuem logo
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = if (isCompactHeight) 4.dp else 10.dp)
                        ) {
                            Text(
                                text = currentApp.label.uppercase(),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontSize = fallbackFontSize,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = if (isCompactHeight) 1.2.sp else 1.8.sp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color(0xFFE2E8F0),
                                            Color(0xFF94A3B8)
                                        )
                                    ),
                                    shadow = Shadow(
                                        color = Color(0xBB000000),
                                        offset = Offset(2f, 4f),
                                        blurRadius = 8f
                                    )
                                )
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .width(if (isCompactHeight) 70.dp else 100.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                accentColor.copy(alpha = 0.85f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                    } else {
                        // Enquanto o logo local estiver decodificando, preserva o espaço sem piscar texto
                        Spacer(modifier = Modifier.height(maxLogoHeight).widthIn(max = maxLogoWidth))
                    }
                }
            }
        }
    }
}
