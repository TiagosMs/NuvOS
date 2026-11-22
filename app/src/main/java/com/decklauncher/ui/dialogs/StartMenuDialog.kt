package com.decklauncher.ui.dialogs

import android.content.Context
import android.net.Uri
import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.decklauncher.data.*
import com.decklauncher.ui.components.*
import com.decklauncher.ui.theme.*

enum class StartMenuTab(val title: String) {
    BIBLIOTECA("Jogos"),
    APARENCIA("Aparência"),
    BANCOS_DADOS("Metadados")
}

enum class StartMenuFocusArea {
    TABS,
    CONTENT
}

@Composable
fun StartMenuDialog(
    onDismiss: () -> Unit,
    onRequestChangeAvatar: () -> Unit,
    onRequestSteamGridKey: () -> Unit,
    onRequestIidbKey: () -> Unit = {},
    onArtworkProviderSelected: (ArtworkProvider) -> Unit = {},
    onRescanRoms: () -> Unit,
    onRequestSetupFolders: () -> Unit,
    onRequestRestartWizard: () -> Unit,
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
    var focusArea by remember { mutableStateOf(StartMenuFocusArea.TABS) }
    var selectedOptionIndex by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    var isVisible by remember { mutableStateOf(false) }

    val activePreset = remember(avatarId) {
        ProfileManager.PRESET_AVATARS.firstOrNull { it.id == avatarId } ?: ProfileManager.PRESET_AVATARS.first()
    }

    LaunchedEffect(Unit) {
        isVisible = true
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
                .background(ScrimColor) // rule-ok: scrim escurecido padrao
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_DPAD_LEFT -> {
                                if (focusArea == StartMenuFocusArea.TABS) {
                                    val tabs = StartMenuTab.values()
                                    val curIdx = tabs.indexOf(selectedTab)
                                    if (curIdx > 0) {
                                        selectedTab = tabs[curIdx - 1]
                                        SoundEffectManager.playTabSwitch()
                                    }
                                    true
                                } else false
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (focusArea == StartMenuFocusArea.TABS) {
                                    val tabs = StartMenuTab.values()
                                    val curIdx = tabs.indexOf(selectedTab)
                                    if (curIdx < tabs.size - 1) {
                                        selectedTab = tabs[curIdx + 1]
                                        SoundEffectManager.playTabSwitch()
                                    }
                                    true
                                } else false
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_DOWN -> {
                                if (focusArea == StartMenuFocusArea.TABS) {
                                    focusArea = StartMenuFocusArea.CONTENT
                                    selectedOptionIndex = 0
                                    SoundEffectManager.playNavMove()
                                    true
                                } else {
                                    val max = when (selectedTab) {
                                        StartMenuTab.BIBLIOTECA -> 2
                                        StartMenuTab.APARENCIA -> if (currentBgMode == BackgroundMode.WALLPAPER) 5 else 4
                                        StartMenuTab.BANCOS_DADOS -> 4
                                    }
                                    if (selectedOptionIndex < max - 1) {
                                        selectedOptionIndex++
                                        SoundEffectManager.playNavMove()
                                        true
                                    } else false
                                }
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_UP -> {
                                if (focusArea == StartMenuFocusArea.CONTENT) {
                                    if (selectedOptionIndex == 0) {
                                        focusArea = StartMenuFocusArea.TABS
                                        SoundEffectManager.playNavMove()
                                    } else {
                                        selectedOptionIndex--
                                        SoundEffectManager.playNavMove()
                                    }
                                    true
                                } else false
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                if (focusArea == StartMenuFocusArea.CONTENT) {
                                    when (selectedTab) {
                                        StartMenuTab.BIBLIOTECA -> {
                                            when (selectedOptionIndex) {
                                                0 -> { onRescanRoms(); onDismiss(); SoundEffectManager.playConfirm() }
                                                1 -> { onRequestSetupFolders(); onDismiss(); SoundEffectManager.playConfirm() }
                                            }
                                        }
                                        StartMenuTab.APARENCIA -> {
                                            when (selectedOptionIndex) {
                                                0 -> {
                                                    val activity = context as? android.app.Activity
                                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && activity != null) {
                                                        val roleManager = activity.getSystemService(android.app.role.RoleManager::class.java)
                                                        if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME)) {
                                                            val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
                                                            activity.startActivity(intent)
                                                            onDismiss()
                                                            return@onPreviewKeyEvent true
                                                        }
                                                    }
                                                    try {
                                                        val intent = android.content.Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                    onDismiss()
                                                    SoundEffectManager.playConfirm()
                                                }
                                                1 -> { onRequestRestartWizard(); onDismiss(); SoundEffectManager.playConfirm() }
                                                2 -> {
                                                    val modes = BackgroundMode.values()
                                                    val nextMode = modes[(modes.indexOf(currentBgMode) + 1) % modes.size]
                                                    onBgModeSelected(nextMode)
                                                    SoundEffectManager.playConfirm()
                                                }
                                                3 -> {
                                                    if (currentBgMode == BackgroundMode.WALLPAPER) {
                                                        onRequestPickWallpaper()
                                                        SoundEffectManager.playConfirm()
                                                    } else {
                                                        val next = !IntroManager.isIntroEnabled(context)
                                                        IntroManager.setIntroEnabled(context, next)
                                                        SoundEffectManager.playConfirm()
                                                    }
                                                }
                                                4 -> {
                                                    val next = !IntroManager.isIntroEnabled(context)
                                                    IntroManager.setIntroEnabled(context, next)
                                                    SoundEffectManager.playConfirm()
                                                }
                                            }
                                        }
                                        StartMenuTab.BANCOS_DADOS -> {
                                            when (selectedOptionIndex) {
                                                0 -> {
                                                    val provs = ArtworkProvider.values()
                                                    val nextProv = provs[(provs.indexOf(ArtworkProviderManager.getPreferredProvider(context)) + 1) % provs.size]
                                                    ArtworkProviderManager.setPreferredProvider(context, nextProv)
                                                    onArtworkProviderSelected(nextProv)
                                                    SoundEffectManager.playConfirm()
                                                }
                                                1 -> { onRequestSteamGridKey(); onDismiss(); SoundEffectManager.playConfirm() }
                                                2 -> { onRequestIidbKey(); onDismiss(); SoundEffectManager.playConfirm() }
                                                3 -> { onClearAllMetadata(); onDismiss(); SoundEffectManager.playConfirm() }
                                            }
                                        }
                                    }
                                    true
                                } else false
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                if (focusArea == StartMenuFocusArea.CONTENT) {
                                    focusArea = StartMenuFocusArea.TABS
                                    SoundEffectManager.playNavMove()
                                    true
                                } else {
                                    SoundEffectManager.playMenuClose()
                                    onDismiss()
                                    true
                                }
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
            // Slide saindo da lateral esquerda para a direita
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                ) + fadeIn(tween(200)),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(180)
                ) + fadeOut(tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 20.dp, bottom = 20.dp)
                        .width(480.dp)
                        .fillMaxHeight(0.92f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(ColorBgDeep)
                        .border(1.dp, CardBorderUnselected, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Topo: Perfil do Jogador centralizado no topo com avatar
                        var isProfileFocused by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .onFocusChanged { isProfileFocused = it.isFocused }
                                .focusable()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isProfileFocused) NeutralBtnBgFocused else NeutralBtnBg)
                                .border(
                                    width = if (isProfileFocused) 1.5.dp else 1.dp,
                                    color = if (isProfileFocused) FocusRing else CardBorderUnselected,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onRequestChangeAvatar() }
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
                                        .size(36.dp)
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
                                        Text(text = activePreset.initial, color = TextWhite, style = MaterialTheme.typography.titleMedium)
                                    }
                                }

                                Column {
                                    Text(text = profileName, color = TextWhite, style = MaterialTheme.typography.bodyLarge)
                                    Text(text = "Perfil do Jogador", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(NeutralGlyphBg)
                            ) {
                                VectorIconClose(tint = TextWhite)
                            }
                        }

                        // 2. Abas: Botões centralizados com cantos arredondados e ícones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StartMenuTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                val isTabActive = (focusArea == StartMenuFocusArea.TABS && isSelected)
                                var isTabFocused by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(68.dp)
                                        .onFocusChanged { isTabFocused = it.isFocused }
                                        .focusable()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected || isTabFocused || isTabActive) NeutralBtnBgFocused else NeutralBtnBg)
                                        .border(
                                            width = if (isTabActive || isTabFocused) 1.8.dp else if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isTabActive || isTabFocused) FocusRing else if (isSelected) Color.White else CardBorderUnselected,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedTab = tab
                                            focusArea = StartMenuFocusArea.TABS
                                        }
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        when (tab) {
                                            StartMenuTab.BIBLIOTECA -> VectorIconBooks(tint = if (isSelected) TextWhite else TextPrimary)
                                            StartMenuTab.APARENCIA -> VectorIconPalette(tint = if (isSelected) TextWhite else TextPrimary)
                                            StartMenuTab.BANCOS_DADOS -> VectorIconDatabase(tint = if (isSelected) TextWhite else TextPrimary)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = tab.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) TextWhite else TextPrimary,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Conteúdo da aba ativa
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            val isContentActive = (focusArea == StartMenuFocusArea.CONTENT)
                            when (selectedTab) {
                                StartMenuTab.BIBLIOTECA -> {
                                    StartMenuTabBiblioteca(
                                        onRescanRoms = onRescanRoms,
                                        onRequestSetupFolders = onRequestSetupFolders,
                                        onDismiss = onDismiss,
                                        selectedOptionIndex = if (isContentActive) selectedOptionIndex else -1
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
                                        onRequestRestartWizard = onRequestRestartWizard,
                                        onDismiss = onDismiss,
                                        selectedOptionIndex = if (isContentActive) selectedOptionIndex else -1
                                    )
                                }
                                StartMenuTab.BANCOS_DADOS -> {
                                    StartMenuTabBancosDados(
                                        onRequestSteamGridKey = onRequestSteamGridKey,
                                        onRequestIidbKey = onRequestIidbKey,
                                        onArtworkProviderSelected = onArtworkProviderSelected,
                                        currentAccentColor = currentAccentColor,
                                        onClearAllMetadata = onClearAllMetadata,
                                        onDismiss = onDismiss,
                                        selectedOptionIndex = if (isContentActive) selectedOptionIndex else -1
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
    onDismiss: () -> Unit,
    selectedOptionIndex: Int = -1
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Gerenciamento de Jogos & Emuladores", style = MaterialTheme.typography.titleMedium, color = TextWhite)

        StartOptionRow(
            label = "Re-escanear Armazenamento e ROMs",
            onClick = {
                onRescanRoms()
                onDismiss()
            },
            isExternalFocused = (selectedOptionIndex == 0)
        )

        StartOptionRow(
            label = "Configurar Pastas de ROMs e Downloads",
            onClick = {
                onRequestSetupFolders()
                onDismiss()
            },
            isExternalFocused = (selectedOptionIndex == 1)
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
    onRequestRestartWizard: () -> Unit,
    onDismiss: () -> Unit,
    selectedOptionIndex: Int = -1
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Configuração do Sistema", style = MaterialTheme.typography.titleMedium, color = TextWhite)

        StartOptionRow(
            label = "Definir NuvOS como Launcher Padrão",
            onClick = {
                val activity = context as? android.app.Activity
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && activity != null) {
                    val roleManager = activity.getSystemService(android.app.role.RoleManager::class.java)
                    if (roleManager != null && roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME)) {
                        val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
                        activity.startActivity(intent)
                        onDismiss()
                        return@StartOptionRow
                    }
                }
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                    context.startActivity(intent)
                } catch (_: Exception) {
                    android.widget.Toast.makeText(context, "Abra Configurações do Android › Apps padrão › Início", android.widget.Toast.LENGTH_LONG).show()
                }
                onDismiss()
            },
            isExternalFocused = (selectedOptionIndex == 0)
        )

        StartOptionRow(
            label = "Refazer Configuração Inicial (Wizard)",
            onClick = {
                onRequestRestartWizard()
                onDismiss()
            },
            isExternalFocused = (selectedOptionIndex == 1)
        )

        Text("Plano de Fundo", style = MaterialTheme.typography.titleMedium, color = TextWhite)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BackgroundMode.values().forEach { mode ->
                val isSelected = currentBgMode == mode
                val isModeActive = (selectedOptionIndex == 2 && isSelected)
                var isModeFocused by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .onFocusChanged { isModeFocused = it.isFocused }
                        .focusable()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected || isModeFocused || isModeActive) NeutralBtnBgFocused else NeutralBtnBg)
                        .border(
                            width = if (isModeActive || isModeFocused) 2.5.dp else if (isSelected) 2.dp else 1.dp,
                            color = if (isModeActive || isModeFocused) FocusRing else if (isSelected) Color.White else CardBorderUnselected,
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
                            BackgroundMode.OLED -> "OLED"
                        },
                        color = if (isSelected) TextWhite else TextPrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        if (currentBgMode == BackgroundMode.WALLPAPER) {
            StartOptionRow(
                label = if (hasCustomWallpaper) "Wallpaper ativo (Trocar)" else "Escolher Imagem do Dispositivo",
                onClick = onRequestPickWallpaper,
                isExternalFocused = (selectedOptionIndex == 3)
            )
        }

        Text("Intros de Boot & Transições", style = MaterialTheme.typography.titleMedium, color = TextWhite)

        var introsActive by remember { mutableStateOf(IntroManager.isIntroEnabled(context)) }
        val introIdx = if (currentBgMode == BackgroundMode.WALLPAPER) 4 else 3
        StartOptionRow(
            label = if (introsActive) "Intros de Boot dos Consoles: Ativado" else "Intros de Boot dos Consoles: Desativado",
            onClick = {
                val next = !introsActive
                introsActive = next
                IntroManager.setIntroEnabled(context, next)
            },
            isExternalFocused = (selectedOptionIndex == introIdx)
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
    onDismiss: () -> Unit,
    selectedOptionIndex: Int = -1
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
        Text("Provedor Padrão de Artes", style = MaterialTheme.typography.titleMedium, color = TextWhite)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ArtworkProvider.values().forEach { provider ->
                val isSelected = preferredProvider == provider
                val isProvActive = (selectedOptionIndex == 0 && isSelected)
                var isProviderFocused by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .onFocusChanged { isProviderFocused = it.isFocused }
                        .focusable()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected || isProviderFocused || isProvActive) NeutralBtnBgFocused else NeutralBtnBg)
                        .border(
                            width = if (isProvActive || isProviderFocused) 2.5.dp else if (isSelected) 2.dp else 1.dp,
                            color = if (isProvActive || isProviderFocused) FocusRing else if (isSelected) Color.White else CardBorderUnselected,
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
                        style = MaterialTheme.typography.labelMedium
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
            },
            isExternalFocused = (selectedOptionIndex == 1)
        )

        StartOptionRow(
            label = "Configurar Chave iiDB (iiSU Network)",
            onClick = {
                onRequestIidbKey()
                onDismiss()
            },
            isExternalFocused = (selectedOptionIndex == 2)
        )

        Spacer(modifier = Modifier.height(6.dp))

        val isResetActive = (selectedOptionIndex == 3)
        Button(
            onClick = { showConfirmReset = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(
                    width = if (isResetActive) 2.5.dp else 0.dp,
                    color = if (isResetActive) FocusRing else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = DangerBtnBg,
                contentColor = DangerBtnText
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Limpar Cache e Metadados Salvos", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun StartOptionRow(
    label: String,
    onClick: () -> Unit,
    isExternalFocused: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val isHighlighted = isFocused || isExternalFocused

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp) // rule-ok: toque minimo 48dp
            .clip(RoundedCornerShape(12.dp))
            .background(if (isHighlighted) NeutralBtnBgFocused else NeutralBtnBg)
            .border(
                width = if (isHighlighted) 1.5.dp else 1.dp,
                color = if (isHighlighted) FocusRing else CardBorderUnselected,
                shape = RoundedCornerShape(12.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
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
                color = if (isHighlighted) TextWhite else NeutralBtnText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
