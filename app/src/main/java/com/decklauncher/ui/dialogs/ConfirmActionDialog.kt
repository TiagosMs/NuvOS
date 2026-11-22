package com.decklauncher.ui.dialogs

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.decklauncher.data.SoundEffectManager
import com.decklauncher.ui.theme.*

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirmar",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var selectedButtonIndex by remember { mutableIntStateOf(1) } // 0: Cancelar, 1: Confirmar
    var isConfirmFocused by remember { mutableStateOf(false) }
    var isCancelFocused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor) // rule-ok: scrim escurecido padrão
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_DPAD_LEFT -> {
                                selectedButtonIndex = 0
                                SoundEffectManager.playNavMove()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> {
                                selectedButtonIndex = 1
                                SoundEffectManager.playNavMove()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                if (selectedButtonIndex == 1) {
                                    SoundEffectManager.playConfirm()
                                    onConfirm()
                                    onDismiss()
                                } else {
                                    SoundEffectManager.playMenuClose()
                                    onDismiss()
                                }
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
                                onDismiss()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(420.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ColorBgDeep)
                    .border(1.5.dp, if (selectedButtonIndex == 1 || isConfirmFocused || selectedButtonIndex == 0 || isCancelFocused) FocusRing else CardBorderUnselected, RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = title,
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        color = TextWhite
                    )
                    Text(
                        text = message,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isCancelActive = selectedButtonIndex == 0 || isCancelFocused
                        Button(
                            onClick = {
                                SoundEffectManager.playMenuClose()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .onFocusChanged { isCancelFocused = it.isFocused }
                                .border(
                                    width = if (isCancelActive) 2.5.dp else 0.dp,
                                    color = if (isCancelActive) FocusRing else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCancelActive) NeutralBtnBgFocused else NeutralBtnBg,
                                contentColor = NeutralBtnText
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Cancelar", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                        }

                        val isConfirmActive = selectedButtonIndex == 1 || isConfirmFocused
                        Button(
                            onClick = {
                                SoundEffectManager.playConfirm()
                                onConfirm()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .onFocusChanged { isConfirmFocused = it.isFocused }
                                .border(
                                    width = if (isConfirmActive) 2.5.dp else 0.dp,
                                    color = if (isConfirmActive) FocusRing else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDestructive) DangerBtnBg else (if (isConfirmActive) NeutralBtnBgFocused else NeutralBtnBg),
                                contentColor = if (isDestructive) DangerBtnText else TextWhite
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(confirmLabel, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}
