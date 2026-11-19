package com.decklauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.ProfileManager
import com.decklauncher.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TopBar(
    selectedTab: Int,
    isTabAreaFocused: Boolean,
    onTabSelected: (Int) -> Unit,
    batteryPct: Int,
    profileName: String = "Tiago",
    avatarId: String = "deck_blue",
    accentColor: Color = Color(0xFF1A73E8),
    isTopBarFocused: Boolean = false,
    topBarIndex: Int = 0,
    onOpenProfileSettings: () -> Unit = {},
    onOpenAddGame: () -> Unit = {},
    onOpenDiscord: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activePreset = remember(avatarId) {
        ProfileManager.PRESET_AVATARS.firstOrNull { it.id == avatarId } ?: ProfileManager.PRESET_AVATARS.first()
    }

    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(10000)
        }
    }

    val tabs = listOf("Jogos", "Emuladores", "Apps")

    // Animações de cursor e foco para TopBar
    val isProfileFocused = isTopBarFocused && topBarIndex == 0
    val profileScale by animateFloatAsState(targetValue = if (isProfileFocused) 1.08f else 1.0f, label = "profile_scale")
    val profileBorderWidth by animateDpAsState(targetValue = if (isProfileFocused) 2.5.dp else 0.dp, label = "profile_border")
    val profileBg by animateColorAsState(targetValue = if (isProfileFocused) Color(0x40FFFFFF) else Color.Transparent, label = "profile_bg")

    val isAddFocused = isTopBarFocused && topBarIndex == 1
    val addScale by animateFloatAsState(targetValue = if (isAddFocused) 1.15f else 1.0f, label = "add_scale")
    val addBorderWidth by animateDpAsState(targetValue = if (isAddFocused) 2.5.dp else 1.dp, label = "add_border")

    val isDiscordFocused = isTopBarFocused && topBarIndex == 2
    val discordScale by animateFloatAsState(targetValue = if (isDiscordFocused) 1.18f else 1.0f, label = "discord_scale")
    val discordBorderWidth by animateDpAsState(targetValue = if (isDiscordFocused) 3.dp else 1.dp, label = "discord_border")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Canto superior esquerdo: Ícone de Perfil / Menu de Configurações do Launcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .scale(profileScale)
                .clip(RoundedCornerShape(20.dp))
                .background(profileBg)
                .border(profileBorderWidth, Color.White, RoundedCornerShape(20.dp))
                .clickable { onOpenProfileSettings() }
                .padding(vertical = 4.dp, horizontal = 10.dp)
        ) {
            // Avatar Circular com anel de destaque
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(activePreset.primaryColor),
                                Color(activePreset.accentColor)
                            )
                        )
                    )
                    .border(if (isProfileFocused) 2.dp else 1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activePreset.initial,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Text(
                text = profileName,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = if (isProfileFocused) FontWeight.ExtraBold else FontWeight.Bold
            )

            if (isProfileFocused) {
                // Ponto indicador de foco
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        // Center Tabs: [LB] Jogos Emuladores Apps [RB]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // [LB] Shoulder Button Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(LbRbPillBg)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LB",
                    color = LbRbPillText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Tabs row
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val tabBg by animateColorAsState(
                    targetValue = if (isSelected) accentColor.copy(alpha = 0.35f) else Color.Transparent,
                    animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
                    label = "tab_bg"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(tabBg)
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) accentColor.copy(alpha = 0.6f) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) }
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) TextWhite else TabInactiveText,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // [RB] Shoulder Button Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(LbRbPillBg)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RB",
                    color = LbRbPillText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Right Status: Add Game + Discord Quick Icon + Signal + Battery + Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Botão Adicionar Jogo (+)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .scale(addScale)
                    .shadow(if (isAddFocused) 14.dp else 0.dp, CircleShape, spotColor = Color.White)
                    .clip(CircleShape)
                    .background(if (isAddFocused) Color(0x55FFFFFF) else Color(0x22FFFFFF))
                    .border(addBorderWidth, if (isAddFocused) Color.White else Color(0x33FFFFFF), CircleShape)
                    .clickable { onOpenAddGame() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Discord Quick Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .scale(discordScale)
                    .shadow(if (isDiscordFocused) 18.dp else 0.dp, CircleShape, spotColor = Color(0xFF5865F2))
                    .clip(CircleShape)
                    .background(Color(0xFF5865F2))
                    .border(discordBorderWidth, if (isDiscordFocused) Color.White else Color(0x44FFFFFF), CircleShape)
                    .clickable { onOpenDiscord() }
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                DiscordClydeIcon(modifier = Modifier.fillMaxSize())
            }

            // Signal + Battery + Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.semantics {
                    contentDescription = "Bateria: $batteryPct%, Horário: $currentTime"
                }
            ) {
                SignalBarsIcon(
                    modifier = Modifier.size(width = 16.dp, height = 12.dp)
                )

                BatteryIcon(
                    percentage = batteryPct,
                    modifier = Modifier.size(width = 22.dp, height = 11.dp)
                )

                Text(
                    text = currentTime,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DiscordClydeIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.25f)
            lineTo(w * 0.35f, h * 0.15f)
            quadraticBezierTo(w * 0.5f, h * 0.22f, w * 0.65f, h * 0.15f)
            lineTo(w * 0.8f, h * 0.25f)
            quadraticBezierTo(w * 0.95f, h * 0.6f, w * 0.85f, h * 0.85f)
            lineTo(w * 0.72f, h * 0.75f)
            quadraticBezierTo(w * 0.5f, h * 0.82f, w * 0.28f, h * 0.75f)
            lineTo(w * 0.15f, h * 0.85f)
            quadraticBezierTo(w * 0.05f, h * 0.6f, w * 0.2f, h * 0.25f)
            close()
        }
        drawPath(path, color = Color.White)

        val eyeRadius = w * 0.08f
        val eyeY = h * 0.52f
        drawCircle(color = Color(0xFF5865F2), radius = eyeRadius, center = Offset(w * 0.38f, eyeY))
        drawCircle(color = Color(0xFF5865F2), radius = eyeRadius, center = Offset(w * 0.62f, eyeY))
    }
}

@Composable
fun SignalBarsIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val totalBars = 4
        val barWidth = size.width / (totalBars * 1.6f)
        val gap = barWidth * 0.6f

        for (i in 0 until totalBars) {
            val h = size.height * ((i + 1f) / totalBars)
            val left = i * (barWidth + gap)
            drawRect(
                color = Color.White,
                topLeft = Offset(left, size.height - h),
                size = Size(barWidth, h)
            )
        }
    }
}

@Composable
fun BatteryIcon(percentage: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val capWidth = 2.dp.toPx()
        val bodyWidth = size.width - capWidth - 1.dp.toPx()
        val bodyHeight = size.height
        val corner = 2.dp.toPx()

        // Battery outer shell
        drawRoundRect(
            color = Color.White,
            topLeft = Offset.Zero,
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 1.3.dp.toPx())
        )

        // Positive terminal cap
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(bodyWidth + 1.dp.toPx(), bodyHeight * 0.25f),
            size = Size(capWidth, bodyHeight * 0.5f),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )

        // Inside fill
        val fillPadding = 2.dp.toPx()
        val maxFillWidth = bodyWidth - fillPadding * 2
        val currentFill = (maxFillWidth * (percentage.coerceIn(0, 100) / 100f)).coerceAtLeast(1f)

        drawRoundRect(
            color = if (percentage <= 20) Color(0xFFEF4444) else Color.White,
            topLeft = Offset(fillPadding, fillPadding),
            size = Size(currentFill, bodyHeight - fillPadding * 2),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
    }
}
