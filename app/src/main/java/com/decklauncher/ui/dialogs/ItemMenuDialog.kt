package com.decklauncher.ui.dialogs

import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.decklauncher.data.ArtworkProviderManager
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import com.decklauncher.ui.theme.*

private data class ItemMenuAction(
    val label: String,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun ItemMenuDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onRequestManageMetadata: () -> Unit = {},
    onRequestEditCover: () -> Unit,
    onRequestEditHero: () -> Unit = {},
    onRequestEditLogo: () -> Unit = {},
    onRequestSelectIntro: () -> Unit = {},
    onRequestFetchIidb: () -> Unit = {},
    onOpenAppSettings: () -> Unit,
    onUninstallApp: () -> Unit,
    onRemoveItem: () -> Unit,
    onRequestReorder: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferredProvider = remember { ArtworkProviderManager.getPreferredProvider(context) }
    var confirmTitle by remember { mutableStateOf<String?>(null) }
    var confirmMsg by remember { mutableStateOf<String?>(null) }
    var confirmAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    if (confirmTitle != null && confirmMsg != null && confirmAction != null) {
        ConfirmActionDialog(
            title = confirmTitle!!,
            message = confirmMsg!!,
            confirmLabel = "Confirmar",
            isDestructive = true,
            onConfirm = {
                confirmAction?.invoke()
                confirmTitle = null
                confirmMsg = null
                confirmAction = null
                onDismiss()
            },
            onDismiss = {
                confirmTitle = null
                confirmMsg = null
                confirmAction = null
            }
        )
    }

    val actions = remember(app, preferredProvider) {
        val list = mutableListOf<ItemMenuAction>()
        list.add(ItemMenuAction("Iniciar") { onLaunch(); onDismiss() })
        list.add(ItemMenuAction("Personalizar Metadados (Capa, Banner, Logo, Som)") { onRequestManageMetadata(); onDismiss() })
        list.add(ItemMenuAction("Mover Posição / Trocar de Lugar") { onRequestReorder(); onDismiss() })
        list.add(ItemMenuAction("Baixar Tudo do iiDB (Capa, Hero, Logo, Áudio)") { onRequestFetchIidb(); onDismiss() })
        list.add(ItemMenuAction("Editar Capa (${preferredProvider.shortName})") { onRequestEditCover(); onDismiss() })
        list.add(ItemMenuAction("Editar Fundo / Hero (${preferredProvider.shortName})") { onRequestEditHero(); onDismiss() })
        list.add(ItemMenuAction("Editar Logo 3D (${preferredProvider.shortName})") { onRequestEditLogo(); onDismiss() })
        list.add(ItemMenuAction("Trocar Intro do Console") { onRequestSelectIntro(); onDismiss() })

        if (app.itemType == ItemType.APP) {
            list.add(ItemMenuAction("Informações do Aplicativo") { onOpenAppSettings(); onDismiss() })
            list.add(ItemMenuAction("Desinstalar Aplicativo", isDestructive = true) {
                confirmTitle = "Desinstalar Aplicativo"
                confirmMsg = "Deseja realmente desinstalar ${app.label} do sistema?"
                confirmAction = onUninstallApp
            })
        } else {
            list.add(ItemMenuAction("Remover da Tela Inicial", isDestructive = true) {
                confirmTitle = "Remover da Tela Inicial"
                confirmMsg = "Remover o atalho de ${app.label} da tela principal?"
                confirmAction = onRemoveItem
            })
        }
        list
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        focusRequester.requestFocus()
    }

    LaunchedEffect(selectedIndex) {
        val target = (selectedIndex * 52).coerceAtLeast(0)
        scrollState.animateScrollTo(target)
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
                            AndroidKeyEvent.KEYCODE_DPAD_UP -> {
                                selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                                true
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_DOWN -> {
                                selectedIndex = (selectedIndex + 1).coerceAtMost(actions.size - 1)
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                actions.getOrNull(selectedIndex)?.onClick?.invoke()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
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
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(initialOffsetX = { -it }, animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)) + fadeIn(tween(200)),
                exit = slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(180)) + fadeOut(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 20.dp, bottom = 20.dp)
                        .width(420.dp)
                        .fillMaxHeight(0.90f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(ColorBgDeep)
                        .border(1.2.dp, CardBorderUnselected, RoundedCornerShape(26.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = app.label,
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                            color = TextWhite
                        )
                        Text(
                            text = when (app.itemType) {
                                ItemType.ROM -> "Jogo Emulado (ROM)"
                                ItemType.CLOUD -> "Jogo em Nuvem"
                                ItemType.SHORTCUT -> "Atalho de Jogo em Nuvem"
                                ItemType.APP -> "Aplicativo Android"
                            },
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        actions.forEachIndexed { index, action ->
                            val isFocused = selectedIndex == index
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp) // rule-ok: toque minimo 48dp
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (action.isDestructive) {
                                            if (isFocused) DangerBtnBg else NeutralBtnBg
                                        } else {
                                            if (isFocused) NeutralBtnBgFocused else NeutralBtnBg
                                        }
                                    )
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = if (isFocused) FocusRing else CardBorderUnselected,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(role = Role.Button) {
                                        action.onClick()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = action.label,
                                    color = if (action.isDestructive) DangerBtnText else TextWhite,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
