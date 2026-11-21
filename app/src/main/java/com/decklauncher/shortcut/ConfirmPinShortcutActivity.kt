package com.decklauncher.shortcut

import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.decklauncher.ui.dialogs.ConfirmActionDialog
import com.decklauncher.ui.theme.DeckLauncherTheme

class ConfirmPinShortcutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val launcherApps = getSystemService(LauncherApps::class.java)
            val pinItemRequest = launcherApps?.getPinItemRequest(intent)

            if (pinItemRequest != null && pinItemRequest.isValid && pinItemRequest.requestType == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT) {
                val shortcutInfo = pinItemRequest.shortcutInfo
                val label = shortcutInfo?.shortLabel?.toString() ?: shortcutInfo?.longLabel?.toString() ?: "Atalho"
                val pkg = shortcutInfo?.`package` ?: "App"

                setContent {
                    DeckLauncherTheme {
                        ConfirmActionDialog(
                            title = "Adicionar à Tela Inicial?",
                            message = "Deseja adicionar o atalho de jogo \"$label\" ($pkg) à tela principal do NuvOS?",
                            confirmLabel = "Adicionar",
                            isDestructive = false,
                            onConfirm = {
                                try {
                                    pinItemRequest.accept()
                                    if (shortcutInfo != null) {
                                        ShortcutManager.savePinnedShortcut(
                                            this@ConfirmPinShortcutActivity,
                                            id = shortcutInfo.id,
                                            packageName = shortcutInfo.`package`,
                                            label = label
                                        )
                                    }
                                    Toast.makeText(this@ConfirmPinShortcutActivity, "\"$label\" fixado na Home!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(this@ConfirmPinShortcutActivity, "Falha ao aceitar atalho", Toast.LENGTH_SHORT).show()
                                }
                                finish()
                            },
                            onDismiss = {
                                finish()
                            }
                        )
                    }
                }
                return
            }
        }

        Toast.makeText(this, "Pedido de atalho inválido ou não suportado", Toast.LENGTH_SHORT).show()
        finish()
    }
}
