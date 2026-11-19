package com.decklauncher.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.model.AppItem
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun GameLogo3D(
    currentApp: AppItem?,
    logoBitmap: Bitmap?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (currentApp == null) return

    val density = LocalDensity.current.density

    // 1. Nintendo 3DS Idle Floating & 3D Tilt Oscillations
    val infiniteTransition = rememberInfiniteTransition(label = "3ds_float_loop")

    // Oscilação vertical Y contínua (flutuando suavemente como modelo 3D no 3DS)
    val floatProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "float_sine"
    )

    // Cálculo senoidal puro para fluidez orgânica estilo Nintendo
    val floatY = (sin(floatProgress) * 9.dp.value).dp
    val tiltX = sin(floatProgress) * 4.5f
    val tiltY = sin(floatProgress + PI.toFloat() / 2f) * 6.5f
    val shadowScale = 1f - (sin(floatProgress) * 0.12f)
    val shadowAlpha = 0.40f - (sin(floatProgress) * 0.15f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Transição animada de troca de jogo estilo 3DS
        AnimatedContent(
            targetState = Pair(currentApp.id, logoBitmap),
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + androidx.compose.animation.scaleIn(
                    initialScale = 0.82f,
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 180f)
                )) togetherWith (fadeOut(animationSpec = tween(280)))
            },
            label = "3ds_game_logo_switch"
        ) { (_, bmp) ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(vertical = 10.dp)
            ) {
                // Sombra 3D projetada na base (encolhe e expande conforme o logo flutua)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 18.dp)
                        .width(if (bmp != null) 240.dp else 220.dp)
                        .height(18.dp)
                        .graphicsLayer {
                            scaleX = shadowScale
                            scaleY = shadowScale * 0.6f
                            alpha = shadowAlpha
                        }
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xBB000000),
                                    Color(0x44000000),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Logo 3D Flutuante com Perspectiva, Profundidade e Efeito de Iluminação
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = floatY.toPx()
                            rotationX = tiltX
                            rotationY = tiltY
                            cameraDistance = 16f * density
                        }
                ) {
                    if (bmp != null) {
                        // Logo Oficial do SteamGridDB em alta definição transparente
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = currentApp.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .heightIn(max = 140.dp)
                                .widthIn(max = 420.dp)
                                .padding(horizontal = 12.dp)
                        )
                    } else {
                        // Fallback 3DS: Tipografia 3D em relevo metálico com iluminação frontal
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = currentApp.label.uppercase(),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White,
                                            Color(0xFFE2E8F0),
                                            Color(0xFF94A3B8)
                                        )
                                    ),
                                    shadow = Shadow(
                                        color = Color(0xDD000000),
                                        offset = Offset(3f, 6f),
                                        blurRadius = 12f
                                    )
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Friso decorativo sob o título inspirado na UI do 3DS
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                accentColor.copy(alpha = 0.8f),
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
