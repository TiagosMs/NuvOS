package com.decklauncher.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.OrderManager
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import com.decklauncher.ui.theme.*

/**
 * Game Hub Sheet (Feature 2 Next-Gen):
 * Painel inferior elegante de console (PS5 Activity Sheet) revelado ao pressionar D-PAD DOWN no card selecionado.
 */
@Composable
fun GameHubSheet(
    app: AppItem,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onOptions: () -> Unit,
    onManageMetadata: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520
    val focusRequester = remember { FocusRequester() }
    var selectedActionIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            selectedActionIndex = 0
            focusRequester.requestFocus()
        }
    }

    val platformColor = remember(app) {
        getPlatformColor(app)
    }

    val platformName = remember(app) {
        getPlatformName(app)
    }

    val entryProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 320, easing = Motion.EmphasizedDecelerate),
        label = "hub_stagger"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 280, easing = Motion.EmphasizedDecelerate)
        ) + fadeIn(tween(durationMillis = 180, easing = Motion.EmphasizedDecelerate)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 160)
        ) + fadeOut(tween(durationMillis = 120)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            android.view.KeyEvent.KEYCODE_DPAD_UP,
                            android.view.KeyEvent.KEYCODE_BUTTON_B,
                            android.view.KeyEvent.KEYCODE_BACK,
                            android.view.KeyEvent.KEYCODE_ESCAPE -> {
                                onDismiss()
                                true
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                if (selectedActionIndex > 0) {
                                    selectedActionIndex--
                                    com.decklauncher.data.SoundEffectManager.playNavMove()
                                }
                                true
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (selectedActionIndex < 2) {
                                    selectedActionIndex++
                                    com.decklauncher.data.SoundEffectManager.playNavMove()
                                }
                                true
                            }
                            android.view.KeyEvent.KEYCODE_BUTTON_A,
                            android.view.KeyEvent.KEYCODE_ENTER,
                            android.view.KeyEvent.KEYCODE_DPAD_CENTER -> {
                                when (selectedActionIndex) {
                                    0 -> { onLaunch(); onDismiss() }
                                    1 -> { onManageMetadata(); onDismiss() }
                                    2 -> { onOptions(); onDismiss() }
                                }
                                true
                            }
                            else -> false
                        }
                    } else false
                }
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFA161A22),
                            Color(0xFF101318)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0x30FFFFFF),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(
                    horizontal = if (isCompactHeight) 24.dp else 36.dp,
                    vertical = if (isCompactHeight) 12.dp else 18.dp
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 8.dp else 12.dp)
            ) {
                // Top Indicator de arraste / fechar
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0x35FFFFFF))
                        .align(Alignment.CenterHorizontally)
                        .clickable { onDismiss() }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = (entryProgress * 1.2f).coerceIn(0f, 1f)
                            translationY = (1f - entryProgress) * 14.dp.toPx()
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Badge de Plataforma com cor oficial
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(platformColor.copy(alpha = 0.20f))
                                    .border(0.8.dp, platformColor.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = platformName.uppercase(),
                                    color = platformColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Tag de controle compatível
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x18FFFFFF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GAMEPAD 100%",
                                    color = NeutralHigh,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = app.label,
                            color = TextWhite,
                            fontSize = if (isCompactHeight) 16.sp else 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Informações de Sessão
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = OrderManager.formatLastPlayed(context, app.id),
                            color = NeutralHigh,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tempo: " + OrderManager.formatPlayTime(context, app.id),
                            color = TextPrimary.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    }
                }

                // Linha de Ações Rápidas do Hub
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = (entryProgress * 1.5f - 0.5f).coerceIn(0f, 1f)
                            translationY = (1f - entryProgress) * 10.dp.toPx()
                        },
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HubActionButton(
                        letter = "A",
                        label = "Iniciar Jogo",
                        isSelected = selectedActionIndex == 0,
                        isPrimary = true,
                        accentColor = accentColor,
                        onClick = { onLaunch(); onDismiss() }
                    )

                    HubActionButton(
                        letter = "X",
                        label = "Metadados",
                        isSelected = selectedActionIndex == 1,
                        isPrimary = false,
                        accentColor = accentColor,
                        onClick = { onManageMetadata(); onDismiss() }
                    )

                    HubActionButton(
                        letter = "SELECT",
                        label = "Opções",
                        isSelected = selectedActionIndex == 2,
                        isPrimary = false,
                        accentColor = accentColor,
                        onClick = { onOptions(); onDismiss() }
                    )
                }
            }
        }
    }
}

@Composable
private fun HubActionButton(
    letter: String,
    label: String,
    isSelected: Boolean,
    isPrimary: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val bg = when {
        isSelected && isPrimary -> accentColor
        isSelected -> NeutralBtnBgFocused
        isPrimary -> Color(0x30FFFFFF)
        else -> Color(0x18FFFFFF)
    }

    val borderCol = if (isSelected) Color.White else Color(0x25FFFFFF)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(if (isSelected) 1.5.dp else 1.dp, borderCol, RoundedCornerShape(10.dp))
            .clickable(role = Role.Button) { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isSelected && isPrimary) Color(0x33000000) else Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                color = TextWhite,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            color = TextWhite,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

private fun getPlatformColor(app: AppItem): Color {
    val name = app.label.lowercase()
    val pkg = (app.packageName ?: "").lowercase()
    val romPath = (app.romPath ?: "").lowercase()

    return when {
        romPath.endsWith(".nds") || pkg.contains("drastic") -> Color(0xFF0082C8) // NDS Azul
        romPath.endsWith(".3ds") || pkg.contains("citra") -> Color(0xFFD12228)  // 3DS Vermelho
        romPath.endsWith(".gba") || romPath.endsWith(".gbc") -> Color(0xFF7E38B7) // GBA Roxo
        romPath.endsWith(".iso") || romPath.endsWith(".chd") || pkg.contains("aethersx2") || pkg.contains("nether") -> Color(0xFF003791) // PS2 Azul Marinho
        romPath.endsWith(".nsp") || romPath.endsWith(".xci") || pkg.contains("yuzu") || pkg.contains("suyu") -> Color(0xFFE60012) // Switch Vermelho
        romPath.endsWith(".pbp") || pkg.contains("ppsspp") -> Color(0xFF2672EC) // PSP Azul
        app.itemType == ItemType.CLOUD -> Color(0xFF0078D7) // Cloud Azul
        else -> Color(0xFF3DDC84) // Android Verde
    }
}

private fun getPlatformName(app: AppItem): String {
    val pkg = (app.packageName ?: "").lowercase()
    val romPath = (app.romPath ?: "").lowercase()

    return when {
        romPath.endsWith(".nds") || pkg.contains("drastic") -> "Nintendo DS"
        romPath.endsWith(".3ds") || pkg.contains("citra") -> "Nintendo 3DS"
        romPath.endsWith(".gba") -> "Game Boy Advance"
        romPath.endsWith(".gbc") -> "Game Boy Color"
        romPath.endsWith(".iso") || romPath.endsWith(".chd") || pkg.contains("aethersx2") || pkg.contains("nether") -> "PlayStation 2"
        romPath.endsWith(".nsp") || romPath.endsWith(".xci") || pkg.contains("yuzu") || pkg.contains("suyu") -> "Nintendo Switch"
        romPath.endsWith(".pbp") || pkg.contains("ppsspp") -> "PSP"
        app.itemType == ItemType.CLOUD -> "Cloud Streaming"
        app.itemType == ItemType.ROM -> "Emulador"
        else -> "Android Nativo"
    }
}
