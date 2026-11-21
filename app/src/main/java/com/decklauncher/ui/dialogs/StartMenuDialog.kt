package com.decklauncher.ui.dialogs

import android.content.Context
import android.graphics.Bitmap
import android.view.KeyEvent as AndroidKeyEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.decklauncher.data.*
import com.decklauncher.ui.theme.*

enum class StartMenuTab(val title: String) {
    BIBLIOTECA("Biblioteca"),
    APARENCIA("Aparência e Navegação"),
    BANCOS_DADOS("Bancos de Dados")
}

@Composable
fun StartMenuDialog(
    onDismiss: () -> Unit,
    onRequestChangeAvatar: () -> Unit,
    onRequestSteamGridKey: () -> Unit,
    onRequestIidbKey: () -> Unit = {},
    onArtworkProviderSelected: (ArtworkProvider) -> Unit = {},
    onRescanRoms: () -> Unit,
    onRequestSetupFolders: () -> Unit = {},
    profileName: String = "Tiago",
    avatarId: String = "deck_blue",
    currentBgMode: BackgroundMode = BackgroundMode.IMMERSIVE,
    onBgModeSelected: (BackgroundMode) -> Unit = {},
    onRequestPickWallpaper: () -> Unit = {},
    hasCustomWallpaper: Boolean = false,
    currentAccentColor: Long = 0xFF1A73E8,
    onAccentColorSelected: (Long) -> Unit = {},
    currentLaunchTransition: LaunchTransitionType = LaunchTransitionType.FADE,
    onLaunchTransitionSelected: (LaunchTransitionType) -> Unit = {},
    onClearAllMetadata: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(StartMenuTab.BIBLIOTECA) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(initialScale = 0.94f, animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)) + fadeIn(tween(180)),
                exit = scaleOut(targetScale = 0.94f, animationSpec = tween(150)) + fadeOut(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .width(520.dp)
                        .fillMaxHeight(0.88f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(ColorBgDeep)
                        .border(1.2.dp, CardBorderUnselected, RoundedCornerShape(26.dp))
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header com Título e Botão Fechar visível para Touch (D10 / T4.2)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MENU PRINCIPAL",
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                color = TextWhite
                            )

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(48.dp) // rule-ok: alvo de toque >= 48dp
                                    .clip(CircleShape)
                                    .background(NeutralBtnBg)
                            ) {
                                Text("✕", color = TextWhite, fontSize = 16.sp)
                            }
                        }

                        // Abas horizontais: Biblioteca | Aparência e Navegação | Bancos de Dados
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NeutralBtnBg)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StartMenuTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) NeutralBtnBgFocused else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.dp,
                                            color = if (isSelected) FocusRing else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedTab = tab }
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab.title,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) TextWhite else TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Conteúdo da Aba Ativa (D2: compõe apenas a aba ativa para economia de recursos)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (selectedTab) {
                                StartMenuTab.BIBLIOTECA -> {
                                    StartMenuTabBiblioteca(
                                        onRescanRoms = onRescanRoms,
                                        onRequestSetupFolders = onRequestSetupFolders,
                                        onDismiss = onDismiss
                                    )
                                }
                                StartMenuTab.APARENCIA -> {
                                    StartMenuTabAparencia(
                                        profileName = profileName,
                                        avatarId = avatarId,
                                        onRequestChangeAvatar = onRequestChangeAvatar,
                                        currentBgMode = currentBgMode,
                                        onBgModeSelected = onBgModeSelected,
                                        onRequestPickWallpaper = onRequestPickWallpaper,
                                        hasCustomWallpaper = hasCustomWallpaper,
                                        currentAccentColor = currentAccentColor,
                                        onAccentColorSelected = onAccentColorSelected,
                                        currentLaunchTransition = currentLaunchTransition,
                                        onLaunchTransitionSelected = onLaunchTransitionSelected,
                                        onDismiss = onDismiss
                                    )
                                }
                                StartMenuTab.BANCOS_DADOS -> {
                                    StartMenuTabBancosDados(
                                        onRequestSteamGridKey = onRequestSteamGridKey,
                                        onRequestIidbKey = onRequestIidbKey,
                                        onArtworkProviderSelected = onArtworkProviderSelected,
                                        currentAccentColor = currentAccentColor,
                                        onClearAllMetadata = onClearAllMetadata,
                                        onDismiss = onDismiss
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StartMenuTabBiblioteca(
    onRescanRoms: () -> Unit,
    onRequestSetupFolders: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Gerenciamento de Jogos & Emuladores", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = TextWhite)

        StartOptionRow(
            label = "Re-escanear Armazenamento e ROMs",
            onClick = {
                onRescanRoms()
                onDismiss()
            }
        )

        StartOptionRow(
            label = "Configurar Pastas de ROMs e Downloads",
            onClick = {
                onRequestSetupFolders()
                onDismiss()
            }
        )
    }
}

@Composable
private fun StartMenuTabAparencia(
    profileName: String,
    avatarId: String,
    onRequestChangeAvatar: () -> Unit,
    currentBgMode: BackgroundMode,
    onBgModeSelected: (BackgroundMode) -> Unit,
    onRequestPickWallpaper: () -> Unit,
    hasCustomWallpaper: Boolean,
    currentAccentColor: Long,
    onAccentColorSelected: (Long) -> Unit,
    currentLaunchTransition: LaunchTransitionType,
    onLaunchTransitionSelected: (LaunchTransitionType) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activePreset = remember(avatarId) {
        ProfileManager.PRESET_AVATARS.firstOrNull { it.id == avatarId } ?: ProfileManager.PRESET_AVATARS.first()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Perfil do Jogador", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = TextWhite)

        var isProfileFocused by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .onFocusChanged { isProfileFocused = it.isFocused }
                .clip(RoundedCornerShape(18.dp))
                .background(if (isProfileFocused) NeutralBtnBgFocused else NeutralBtnBg)
                .border(
                    width = if (isProfileFocused) 2.dp else 1.dp,
                    color = if (isProfileFocused) FocusRing else CardBorderUnselected,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable {
                    onRequestChangeAvatar()
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val customAvatar = remember(avatarId, ProfileManager.avatarVersion) {
                    if (avatarId == ProfileManager.CUSTOM_AVATAR_ID) ProfileManager.loadCustomAvatar(context) else null
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeutralGlyphBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (customAvatar != null) {
                        Image(
                            bitmap = customAvatar.asImageBitmap(),
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = activePreset.initial, color = TextWhite, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    }
                }

                Column {
                    Text(text = profileName, color = TextWhite, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                    Text(text = "Alterar avatar ou nome", color = TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                }
            }

            Text("Editar", color = TextPrimary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        }

        Text("Plano de Fundo", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = TextWhite)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BackgroundMode.values().forEach { mode ->
                val isSelected = currentBgMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) NeutralBtnBgFocused else NeutralBtnBg)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) FocusRing else CardBorderUnselected,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onBgModeSelected(mode) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (mode) {
                            BackgroundMode.IMMERSIVE -> "Imersivo"
                            BackgroundMode.WALLPAPER -> "Wallpaper"
                            BackgroundMode.SOLID -> "Sóbrio"
                        },
                        color = if (isSelected) TextWhite else TextPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        if (currentBgMode == BackgroundMode.WALLPAPER) {
            StartOptionRow(
                label = if (hasCustomWallpaper) "Wallpaper ativo ✓ (Trocar)" else "Escolher Imagem do Dispositivo",
                onClick = onRequestPickWallpaper
            )
        }

        Text("Intros de Boot & Transições", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = TextWhite)

        var introsActive by remember { mutableStateOf(IntroManager.isIntroEnabled(context)) }
        StartOptionRow(
            label = if (introsActive) "Intros de Boot dos Consoles: Ativado ✓" else "Intros de Boot dos Consoles: Desativado",
            onClick = {
                val next = !introsActive
                introsActive = next
                IntroManager.setIntroEnabled(context, next)
            }
        )
    }
}

@Composable
private fun StartMenuTabBancosDados(
    onRequestSteamGridKey: () -> Unit,
    onRequestIidbKey: () -> Unit,
    onArtworkProviderSelected: (ArtworkProvider) -> Unit,
    currentAccentColor: Long,
    onClearAllMetadata: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var preferredProvider by remember { mutableStateOf(ArtworkProviderManager.getPreferredProvider(context)) }
    var showConfirmReset by remember { mutableStateOf(false) }

    if (showConfirmReset) {
        ConfirmActionDialog(
            title = "Limpar Todos os Metadados?",
            message = "Esta ação excluirá todas as capas, banners, logos e áudios baixados. Deseja continuar?",
            confirmLabel = "Excluir Tudo",
            isDestructive = true,
            onConfirm = {
                onClearAllMetadata()
                onDismiss()
            },
            onDismiss = { showConfirmReset = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Provedor Padrão de Artes", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = TextWhite)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ArtworkProvider.values().forEach { provider ->
                val isSelected = preferredProvider == provider
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) NeutralBtnBgFocused else NeutralBtnBg)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) FocusRing else CardBorderUnselected,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            preferredProvider = provider
                            ArtworkProviderManager.setPreferredProvider(context, provider)
                            onArtworkProviderSelected(provider)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = provider.title,
                        color = if (isSelected) TextWhite else TextPrimary,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        StartOptionRow(
            label = "Configurar Chave SteamGridDB",
            onClick = {
                onRequestSteamGridKey()
                onDismiss()
            }
        )

        StartOptionRow(
            label = "Configurar Chave iiDB (iiSU Network)",
            onClick = {
                onRequestIidbKey()
                onDismiss()
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = { showConfirmReset = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DangerBtnBg,
                contentColor = DangerBtnText
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Limpar Cache e Metadados Salvos", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun StartOptionRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp) // rule-ok: toque minimo 48dp
            .clip(RoundedCornerShape(16.dp))
            .background(if (isFocused) NeutralBtnBgFocused else NeutralBtnBg)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusRing else CardBorderUnselected,
                shape = RoundedCornerShape(16.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .clickable(role = Role.Button) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (isFocused) TextWhite else NeutralBtnText,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )

            if (isFocused) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(FocusRing)
                )
            }
        }
    }
}
