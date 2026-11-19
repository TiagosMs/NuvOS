package com.decklauncher.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.model.AppItem

/**
 * Logo 3D Animada dos Jogos com física flutuante contínua estilo Console,
 * extrusão volumétrica multicamada em tempo real com paralaxe reactivo,
 * sombra no piso sincronizada e sem clipping nas transições de seleção.
 */
@Composable
fun GameLogo3D(
    currentApp: AppItem?,
    logoBitmap: Bitmap?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (currentApp == null) return

    val density = LocalDensity.current.density
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    val maxLogoHeight = if (isCompactHeight) 76.dp else 138.dp
    val maxLogoWidth = if (isCompactHeight) 280.dp else 440.dp
    val fallbackFontSize = if (isCompactHeight) 20.sp else 32.sp

    // Física de Animação 3D estilo Console (mesma base da pasta 3D, com movimentação mais expressiva)
    val infiniteTransition = rememberInfiniteTransition(label = "game_logo_3d_physics")

    // 1. Flutuação vertical contínua mais ampla e viva
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = if (isCompactHeight) -8f else -13f,
        targetValue = if (isCompactHeight) 8f else 13f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_float_y"
    )

    // 2. Balanço Yaw (Eixo Y) dinâmico
    val tiltYAnim by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_tilt_y"
    )

    // 3. Inclinação Pitch (Eixo X)
    val tiltXAnim by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_tilt_x"
    )

    // 4. Balanço sutil Roll (Eixo Z) para organicidade
    val rollAnim by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_roll_z"
    )

    // 5. Escala e opacidade da sombra no piso sincronizadas com a altura
    val shadowScaleX by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_shadow_scale"
    )

    val shadowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_shadow_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompactHeight) 12.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Transição suave de troca de jogo sem corte de bordas (SizeTransform clip = false)
        AnimatedContent(
            targetState = Pair(currentApp.id, logoBitmap),
            transitionSpec = {
                (fadeIn(animationSpec = tween(380, easing = FastOutSlowInEasing)) +
                        androidx.compose.animation.scaleIn(
                            initialScale = 0.85f,
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = 160f)
                        ))
                    .togetherWith(
                        fadeOut(animationSpec = tween(220, easing = FastOutSlowInEasing))
                    ) using SizeTransform(clip = false)
            },
            label = "game_logo_3d_switch"
        ) { (_, bmp) ->
            // Container amplo com margens para impedir clipping durante rotação e flutuação
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .wrapContentSize(unbounded = false)
                    .padding(
                        horizontal = 20.dp,
                        vertical = if (isCompactHeight) 6.dp else 14.dp
                    )
            ) {
                // Sombra 3D projetada no piso com brilho dinâmico na cor de destaque
                val shadowWidth = if (isCompactHeight) (if (bmp != null) 160.dp else 140.dp) else (if (bmp != null) 260.dp else 230.dp)
                val shadowHeight = if (isCompactHeight) 14.dp else 22.dp
                val shadowOffsetY = if (isCompactHeight) 38.dp else 68.dp

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = shadowOffsetY)
                        .width(shadowWidth)
                        .height(shadowHeight)
                ) {
                    // Glow de luz ambiente colorida no piso
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = shadowScaleX * 1.15f
                                scaleY = shadowScaleX * 0.75f
                                alpha = shadowAlpha * 0.45f
                            }
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        accentColor.copy(alpha = 0.5f),
                                        accentColor.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Sombra de oclusão de contato escura
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = shadowScaleX
                                scaleY = shadowScaleX * 0.6f
                                alpha = shadowAlpha
                            }
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xDD000000),
                                        Color(0x55000000),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                // Palco do Logo 3D com Perspectiva, Parallax e Extrusão Volumétrica
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = floatAnim.dp.toPx()
                            rotationX = tiltXAnim
                            rotationY = tiltYAnim
                            rotationZ = rollAnim
                            cameraDistance = 16f * density
                        }
                ) {
                    if (bmp != null) {
                        val imageBitmap = bmp.asImageBitmap()

                        // Cálculo do vetor de deslocamento de extrusão volumétrica proporcional à rotação 3D
                        val dxExtrusion = (-tiltYAnim * 0.18f).dp
                        val dyExtrusion = (tiltXAnim * 0.15f).dp

                        // Camada 4 (Mais profunda): Sombra de oclusão ambiente traseira
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(Color(0xAA080B10), BlendMode.SrcIn),
                            modifier = Modifier
                                .offset(x = dxExtrusion * 3.6f, y = dyExtrusion * 3.6f + 3.8.dp)
                                .heightIn(max = maxLogoHeight)
                                .widthIn(max = maxLogoWidth)
                        )

                        // Camada 3: Base metálica escura de profundidade
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(Color(0xFF161A22), BlendMode.SrcIn),
                            modifier = Modifier
                                .offset(x = dxExtrusion * 2.6f, y = dyExtrusion * 2.6f + 2.6.dp)
                                .heightIn(max = maxLogoHeight)
                                .widthIn(max = maxLogoWidth)
                        )

                        // Camada 2: Corpo de extrusão intermediário com leve tom do tema
                        val midExtrusionTint = Color(0xFF282F3B)
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(midExtrusionTint, BlendMode.SrcIn),
                            modifier = Modifier
                                .offset(x = dxExtrusion * 1.6f, y = dyExtrusion * 1.6f + 1.6.dp)
                                .heightIn(max = maxLogoHeight)
                                .widthIn(max = maxLogoWidth)
                        )

                        // Camada 1: Chanfro / Borda de relevo frontal iluminada
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(Color(0xFF4A5568), BlendMode.SrcIn),
                            modifier = Modifier
                                .offset(x = dxExtrusion * 0.8f, y = dyExtrusion * 0.8f + 0.8.dp)
                                .heightIn(max = maxLogoHeight)
                                .widthIn(max = maxLogoWidth)
                        )

                        // Camada Frontal: Logo Oficial Full-Color em Alta Resolução
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = currentApp.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .heightIn(max = maxLogoHeight)
                                .widthIn(max = maxLogoWidth)
                                .padding(horizontal = 4.dp)
                        )
                    } else {
                        // Fallback 3D Volumétrico: Tipografia em relevo esculpido com sombra de extrusão
                        val textDx = (-tiltYAnim * 0.25f)
                        val textDy = (tiltXAnim * 0.25f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = if (isCompactHeight) 4.dp else 10.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                // Sombra profunda de extrusão do texto
                                Text(
                                    text = currentApp.label.uppercase(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontSize = fallbackFontSize,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = if (isCompactHeight) 1.2.sp else 1.8.sp,
                                        color = Color(0xDD000000)
                                    ),
                                    modifier = Modifier.offset(
                                        x = (textDx * 2f).dp,
                                        y = (textDy * 2f + 4f).dp
                                    )
                                )

                                // Camada intermediária de chanfro
                                Text(
                                    text = currentApp.label.uppercase(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(
                                        fontSize = fallbackFontSize,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = if (isCompactHeight) 1.2.sp else 1.8.sp,
                                        color = Color(0xFF334155)
                                    ),
                                    modifier = Modifier.offset(
                                        x = (textDx * 1f).dp,
                                        y = (textDy * 1f + 2f).dp
                                    )
                                )

                                // Face frontal em degradê metálico escovado
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
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Friso decorativo iluminado sob o título
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
                    }
                }
            }
        }
    }
}
