package com.decklauncher.ui.components

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.SoundEffectManager
import com.decklauncher.ui.theme.*

/**
 * NuvOS Quick Access Menu (Feature 3 Next-Gen):
 * Menu lateral deslizante estilo Steam Deck (...) com controle rápido de brilho,
 * volume geral, música de fundo e salto alfabético por controle físico.
 */
@Composable
fun QuickAccessDrawer(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    batteryPct: Int,
    currentTime: String,
    onJumpToLetter: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520
    val focusRequester = remember { FocusRequester() }

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    val maxVolume = remember { audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15 }

    var currentSysVolume by remember {
        mutableFloatStateOf((audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)?.toFloat() ?: 10f) / maxVolume.toFloat())
    }

    var currentBrightness by remember {
        val cur = activity?.window?.attributes?.screenBrightness ?: -1f
        mutableFloatStateOf(if (cur < 0f) 0.65f else cur)
    }

    var is120Hz by remember { mutableStateOf(true) }
    var selectedRowIndex by remember { mutableIntStateOf(0) }
    var selectedLetterChar by remember { mutableStateOf('A') }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(durationMillis = 280, easing = Motion.EmphasizedDecelerate)
        ) + fadeIn(tween(durationMillis = 180, easing = Motion.EmphasizedDecelerate)),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(durationMillis = 160)
        ) + fadeOut(tween(durationMillis = 120)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(if (isCompactHeight) 290.dp else 340.dp)
                .fillMaxHeight()
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            android.view.KeyEvent.KEYCODE_BUTTON_B,
                            android.view.KeyEvent.KEYCODE_BACK,
                            android.view.KeyEvent.KEYCODE_ESCAPE -> {
                                onDismiss()
                                SoundEffectManager.playMenuClose()
                                true
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                if (selectedRowIndex > 0) {
                                    selectedRowIndex--
                                    SoundEffectManager.playNavMove()
                                }
                                true
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                if (selectedRowIndex < 4) {
                                    selectedRowIndex++
                                    SoundEffectManager.playNavMove()
                                }
                                true
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                when (selectedRowIndex) {
                                    0 -> { // Brilho
                                        currentBrightness = (currentBrightness - 0.10f).coerceIn(0.10f, 1.0f)
                                        applyBrightness(activity, currentBrightness)
                                        SoundEffectManager.playNavMove()
                                    }
                                    1 -> { // Volume
                                        currentSysVolume = (currentSysVolume - 0.10f).coerceIn(0f, 1.0f)
                                        applyVolume(audioManager, maxVolume, currentSysVolume)
                                        SoundEffectManager.playNavMove()
                                    }
                                    2 -> { // Taxa
                                        is120Hz = false
                                        SoundEffectManager.playConfirm()
                                    }
                                    3 -> { // Letra
                                        if (selectedLetterChar > 'A') {
                                            selectedLetterChar--
                                            SoundEffectManager.playNavMove()
                                        }
                                    }
                                }
                                true
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                when (selectedRowIndex) {
                                    0 -> { // Brilho
                                        currentBrightness = (currentBrightness + 0.10f).coerceIn(0.10f, 1.0f)
                                        applyBrightness(activity, currentBrightness)
                                        SoundEffectManager.playNavMove()
                                    }
                                    1 -> { // Volume
                                        currentSysVolume = (currentSysVolume + 0.10f).coerceIn(0f, 1.0f)
                                        applyVolume(audioManager, maxVolume, currentSysVolume)
                                        SoundEffectManager.playNavMove()
                                    }
                                    2 -> { // Taxa
                                        is120Hz = true
                                        SoundEffectManager.playConfirm()
                                    }
                                    3 -> { // Letra
                                        if (selectedLetterChar < 'Z') {
                                            selectedLetterChar++
                                            SoundEffectManager.playNavMove()
                                        }
                                    }
                                }
                                true
                            }
                            android.view.KeyEvent.KEYCODE_BUTTON_A,
                            android.view.KeyEvent.KEYCODE_ENTER,
                            android.view.KeyEvent.KEYCODE_DPAD_CENTER -> {
                                if (selectedRowIndex == 2) {
                                    is120Hz = !is120Hz
                                    SoundEffectManager.playConfirm()
                                } else if (selectedRowIndex == 3) {
                                    onJumpToLetter(selectedLetterChar)
                                    onDismiss()
                                    SoundEffectManager.playConfirm()
                                }
                                true
                            }
                            else -> false
                        }
                    } else false
                }
                .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF161920), Color(0xFF111418))
                    )
                )
                .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
                .padding(if (isCompactHeight) 16.dp else 22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 12.dp else 16.dp)
            ) {
                // Header: Título, bateria e hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Acesso Rápido",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Bateria $batteryPct% • $currentTime",
                            color = TextPrimary.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (is120Hz) "120 HZ" else "60 HZ",
                            color = if (is120Hz) Color(0xFF10B981) else NeutralHigh,
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 1. Slider de Brilho
                QuickControlRow(
                    label = "Brilho da Tela (${(currentBrightness * 100).toInt()}%)",
                    isSelected = selectedRowIndex == 0
                ) {
                    Slider(
                        value = currentBrightness,
                        onValueChange = {
                            currentBrightness = it
                            applyBrightness(activity, it)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 2. Slider de Volume de Mídia
                QuickControlRow(
                    label = "Volume do Sistema (${(currentSysVolume * 100).toInt()}%)",
                    isSelected = selectedRowIndex == 1
                ) {
                    Slider(
                        value = currentSysVolume,
                        onValueChange = {
                            currentSysVolume = it
                            applyVolume(audioManager, maxVolume, it)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 3. Taxa de Atualização
                QuickControlRow(
                    label = "Taxa de Atualização",
                    isSelected = selectedRowIndex == 2
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RateChip("60 Hz", isActive = !is120Hz, onClick = { is120Hz = false })
                        RateChip("120 Hz (Fluido)", isActive = is120Hz, onClick = { is120Hz = true })
                    }
                }

                // 4. Salto Rápido Alfabético
                QuickControlRow(
                    label = "Saltar para Jogo ($selectedLetterChar)",
                    isSelected = selectedRowIndex == 3
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pressione [A] para pular para '$selectedLetterChar'",
                            color = NeutralHigh,
                            fontSize = 11.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x33FFFFFF))
                                .clickable {
                                    onJumpToLetter(selectedLetterChar)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Pular", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickControlRow(
    label: String,
    isSelected: Boolean,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0x22FFFFFF) else Color(0x0EFFFFFF))
            .border(if (isSelected) 1.5.dp else 0.8.dp, if (isSelected) Color.White else Color(0x18FFFFFF), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) TextWhite else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun RateChip(label: String, isActive: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0x40FFFFFF) else Color(0x15FFFFFF))
            .border(if (isActive) 1.dp else 0.dp, if (isActive) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isActive) TextWhite else TextPrimary,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun applyBrightness(activity: Activity?, value: Float) {
    if (activity == null) return
    val lp = activity.window.attributes
    lp.screenBrightness = value.coerceIn(0.05f, 1.0f)
    activity.window.attributes = lp
}

private fun applyVolume(audioManager: AudioManager?, maxVolume: Int, value: Float) {
    if (audioManager == null) return
    val target = (value * maxVolume).toInt().coerceIn(0, maxVolume)
    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
}
