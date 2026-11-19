package com.decklauncher.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import com.decklauncher.ui.theme.*

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
    // Ultra-smooth slower spring physics for fluid console feel
    val scale by animateFloatAsState(
        targetValue = if (isSelected) (if (isReorderMode) 1.09f else 1.07f) else 0.94f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 160f),
        label = "game_card_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 1.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
        label = "game_card_border"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isSelected) 28.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
        label = "game_card_shadow"
    )

    val activeBorderColor = if (isReorderMode) Color(0xFFF59E0B) else (if (isSelected) Color.White else accentColor)

    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = modifier
            .width(175.dp)
            .semantics { contentDescription = app.label },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .width(175.dp)
                .height(250.dp)
                .scale(scale)
                .shadow(
                    elevation = shadowElevation,
                    shape = shape,
                    spotColor = if (isReorderMode) Color(0xFFF59E0B) else accentColor,
                    ambientColor = Color.White
                )
                .clip(shape)
                .border(
                    BorderStroke(borderWidth, if (isSelected) Color.White else Color(0x22FFFFFF)),
                    shape = shape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (isSelected) onLaunch() else onSelect()
                },
            colors = CardDefaults.cardColors(containerColor = CardBgDark),
            shape = shape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1C1C))
            ) {
                // Full-bleed edge-to-edge artwork (Preenche todo o retângulo)
                if (app.coverBitmap != null) {
                    Image(
                        bitmap = app.coverBitmap,
                        contentDescription = app.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (app.iconBitmap != null) {
                    // Stylized full-card backdrop with prominent icon
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
                            bitmap = app.iconBitmap,
                            contentDescription = app.label,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(24.dp))
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
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Indicator below selected card: Nome do jogo no cursor com barra nítida de foco ou [◀ MOVER ▶]
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (isReorderMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF59E0B))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "◀ MOVER ▶",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    // Pílula nítida e brilhante de cursor ativo
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White)
                    )
                    Text(
                        text = app.label,
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
