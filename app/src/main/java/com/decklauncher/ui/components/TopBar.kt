package com.decklauncher.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
    onOpenStartMenu: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activePreset = remember(avatarId) {
        ProfileManager.PRESET_AVATARS.firstOrNull { it.id == avatarId } ?: ProfileManager.PRESET_AVATARS.first()
    }
    val customAvatar = remember(avatarId, ProfileManager.avatarVersion) {
        if (avatarId == ProfileManager.CUSTOM_AVATAR_ID) ProfileManager.loadCustomAvatar(context) else null
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var currentTime by remember {
        mutableStateOf(timeFormat.format(Date()))
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                currentTime = timeFormat.format(Date())
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    val tabs = listOf("Jogos", "Emuladores", "Apps")

    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    // Animações de cursor e foco para TopBar (Unslop v1.1)
    val isProfileFocused = isTopBarFocused && topBarIndex == 0
    val profileScale by animateFloatAsState(targetValue = if (isProfileFocused) 1.02f else 1.0f, label = "profile_scale")
    val profileBorderWidth by animateDpAsState(targetValue = if (isProfileFocused) 1.5.dp else 1.dp, label = "profile_border")
    val profileBorderColor by animateColorAsState(targetValue = if (isProfileFocused) Color.White else Color(0x25FFFFFF), label = "profile_border_color")
    val profileBg by animateColorAsState(targetValue = if (isProfileFocused) Color(0x28FFFFFF) else Color(0x12FFFFFF), label = "profile_bg")

    val isAddFocused = isTopBarFocused && topBarIndex == 1
    val addScale by animateFloatAsState(targetValue = if (isAddFocused) 1.08f else 1.0f, label = "add_scale")
    val addBorderWidth by animateDpAsState(targetValue = if (isAddFocused) 1.5.dp else 1.dp, label = "add_border")

    val isSettingsFocused = isTopBarFocused && topBarIndex == 2
    val settingsScale by animateFloatAsState(targetValue = if (isSettingsFocused) 1.08f else 1.0f, label = "settings_scale")
    val settingsBorderWidth by animateDpAsState(targetValue = if (isSettingsFocused) 1.5.dp else 1.dp, label = "settings_border")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isCompactHeight) 20.dp else 32.dp,
                vertical = if (isCompactHeight) 6.dp else 18.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Canto superior esquerdo: Pílula de Perfil silenciosa (Quiet Chrome)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 6.dp else 8.dp),
            modifier = Modifier
                .scale(profileScale)
                .clip(RoundedCornerShape(16.dp))
                .background(profileBg)
                .border(profileBorderWidth, profileBorderColor, RoundedCornerShape(16.dp))
                .clickable { onOpenProfileSettings() }
                .padding(
                    vertical = if (isCompactHeight) 3.dp else 5.dp,
                    horizontal = if (isCompactHeight) 8.dp else 12.dp
                )
        ) {
            // Avatar Circular proporcional
            Box(
                modifier = Modifier
                    .size(if (isCompactHeight) 24.dp else 28.dp)
                    .clip(CircleShape)
                    .then(
                        if (customAvatar == null) {
                            Modifier.background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(activePreset.primaryColor),
                                        Color(activePreset.accentColor)
                                    )
                                )
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (customAvatar != null) {
                    Image(
                        bitmap = customAvatar.asImageBitmap(),
                        contentDescription = "Foto de Perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = activePreset.initial,
                        color = Color.White,
                        fontSize = if (isCompactHeight) 12.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = profileName,
                color = TextPrimary,
                fontSize = if (isCompactHeight) 11.sp else 13.sp,
                fontWeight = if (isProfileFocused) FontWeight.SemiBold else FontWeight.Medium
            )
        }

        // Center Tabs: [LB] Jogos Emuladores Apps [RB]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // [LB] Shoulder Button Micro-Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x22FFFFFF))
                    .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                    .padding(horizontal = if (isCompactHeight) 5.dp else 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LB",
                    color = TextPrimary.copy(alpha = 0.8f),
                    fontSize = if (isCompactHeight) 8.sp else 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Tabs row container com indicador deslizante console
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(20.dp))
                    .padding(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        val tabBg by animateColorAsState(
                            targetValue = if (isSelected) Color(0xFF1E222B) else Color.Transparent,
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
                            label = "tab_bg_$index"
                        )
                        val tabBorderColor by animateColorAsState(
                            targetValue = if (isSelected) Color(0x44FFFFFF) else Color.Transparent,
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
                            label = "tab_border_$index"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else TextPrimary.copy(alpha = 0.55f),
                            animationSpec = tween(Motion.Fast),
                            label = "tab_text_$index"
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(tabBg)
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = tabBorderColor,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onTabSelected(index) }
                                .padding(
                                    horizontal = if (isCompactHeight) 14.dp else 20.dp,
                                    vertical = if (isCompactHeight) 5.dp else 7.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = textColor,
                                fontSize = if (isCompactHeight) 11.sp else 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // [RB] Shoulder Button Micro-Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0x22FFFFFF))
                    .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                    .padding(horizontal = if (isCompactHeight) 5.dp else 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RB",
                    color = TextPrimary.copy(alpha = 0.8f),
                    fontSize = if (isCompactHeight) 8.sp else 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Right Status: Add Game (+) + Signal + Battery + Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 8.dp else 14.dp)
        ) {
            // Botão Adicionar Jogo (+) - Presente com largura fixa para não deslocar as abas centrais
            Box(
                modifier = Modifier
                    .size(if (isCompactHeight) 26.dp else 34.dp)
                    .then(
                        if (selectedTab == 0) {
                            Modifier
                                .scale(addScale)
                                .clip(CircleShape)
                                .background(if (isAddFocused) Color(0x30FFFFFF) else Color(0x15FFFFFF))
                                .border(addBorderWidth, if (isAddFocused) Color.White else Color(0x25FFFFFF), CircleShape)
                                .clickable { onOpenAddGame() }
                        } else {
                            Modifier // invisivel mantendo o espaco exato para layout fixo
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectedTab == 0) {
                    Text(
                        text = "+",
                        color = Color.White,
                        fontSize = if (isCompactHeight) 15.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Engrenagem Start Menu tocável (T4.1) / Configurações do Tablet
            Box(
                modifier = Modifier
                    .size(if (isCompactHeight) 26.dp else 34.dp)
                    .scale(settingsScale)
                    .clip(CircleShape)
                    .background(if (isSettingsFocused) Color(0x30FFFFFF) else Color(0x15FFFFFF))
                    .border(settingsBorderWidth, if (isSettingsFocused) Color.White else Color(0x25FFFFFF), CircleShape)
                    .clickable { onOpenStartMenu() },
                contentAlignment = Alignment.Center
            ) {
                VectorIconGear(
                    modifier = Modifier.size(if (isCompactHeight) 14.dp else 18.dp),
                    tint = Color.White
                )
            }

            // Signal + Battery + Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 6.dp else 10.dp),
                modifier = Modifier.semantics {
                    contentDescription = "Bateria: $batteryPct%, Horário: $currentTime"
                }
            ) {
                SignalBarsIcon(
                    modifier = Modifier.size(
                        width = if (isCompactHeight) 13.dp else 16.dp,
                        height = if (isCompactHeight) 10.dp else 12.dp
                    )
                )

                BatteryIcon(
                    percentage = batteryPct,
                    modifier = Modifier.size(
                        width = if (isCompactHeight) 18.dp else 22.dp,
                        height = if (isCompactHeight) 9.dp else 11.dp
                    )
                )

                Text(
                    text = currentTime,
                    color = TextPrimary,
                    fontSize = if (isCompactHeight) 11.sp else 13.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
                color = NeutralHigh,
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
            color = NeutralHigh,
            topLeft = Offset.Zero,
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 1.3.dp.toPx())
        )

        // Positive terminal cap
        drawRoundRect(
            color = NeutralHigh,
            topLeft = Offset(bodyWidth + 1.dp.toPx(), bodyHeight * 0.25f),
            size = Size(capWidth, bodyHeight * 0.5f),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )

        // Inside fill
        val fillPadding = 2.dp.toPx()
        val maxFillWidth = bodyWidth - fillPadding * 2
        val currentFill = (maxFillWidth * (percentage.coerceIn(0, 100) / 100f)).coerceAtLeast(1f)

        drawRoundRect(
            color = if (percentage <= 20) Color(0xFFDA1725) else NeutralHigh,
            topLeft = Offset(fillPadding, fillPadding),
            size = Size(currentFill, bodyHeight - fillPadding * 2),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
    }
}
