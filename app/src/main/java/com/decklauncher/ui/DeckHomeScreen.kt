package com.decklauncher.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.decklauncher.data.*
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.FocusArea
import com.decklauncher.ui.components.*
import com.decklauncher.ui.dialogs.*
import com.decklauncher.ui.theme.*
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeckHomeScreen(
    apps: List<AppItem>,
    selectedIndex: Int,
    selectedTab: Int,
    focusArea: FocusArea,
    batteryPct: Int,
    activeFolder: AppFolder?,
    activeDialogApp: AppItem?,
    isLoading: Boolean,
    showOptionsDialog: Boolean,
    showGameMetadataDialog: Boolean = false,
    showSelectMusicDialog: Boolean = false,
    showEditCoverDialog: Boolean,
    showEditHeroDialog: Boolean = false,
    showEditLogoDialog: Boolean = false,
    showSelectIntroDialog: Boolean = false,
    showInitialSetupDialog: Boolean = false,
    showAddGameOptionsDialog: Boolean,
    showSteamGridDialog: Boolean,
    showLauncherSettingsDialog: Boolean = false,
    showChangeAvatarDialog: Boolean = false,
    showEmulatorGamesDialog: Boolean = false,
    activeEmulatorApp: AppItem? = null,
    allScannedRoms: List<AppItem> = emptyList(),
    pinnedGameIds: Set<String> = emptySet(),
    isReorderMode: Boolean = false,
    topBarIndex: Int = 0,
    profileName: String = "Tiago",
    avatarId: String = "deck_blue",
    activeIntroPlaying: ConsoleIntro? = null,
    onIntroFinished: () -> Unit = {},
    currentRomFolder: String = "",
    currentDownloadFolder: String = "",
    currentApiKey: String = "",
    backgroundMode: BackgroundMode = BackgroundMode.IMMERSIVE,
    accentColorLong: Long = 0xFF1A73E8L,
    customWallpaperBitmap: Bitmap? = null,
    hasCustomWallpaper: Boolean = false,
    currentLaunchTransition: LaunchTransitionType = LaunchTransitionType.FADE,
    onLaunchTransitionSelected: (LaunchTransitionType) -> Unit = {},
    isLaunchingFade: Boolean = false,
    onBgModeSelected: (BackgroundMode) -> Unit = {},
    onRequestPickWallpaper: () -> Unit = {},
    onAccentColorSelected: (Long) -> Unit = {},
    onSelectedIndexChange: (Int) -> Unit,
    onTabSelected: (Int) -> Unit,
    onOpenFolder: (AppFolder) -> Unit,
    onCloseFolder: () -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onOpenSettings: (AppItem?) -> Unit,
    onUninstallApp: (AppItem) -> Unit,
    onDismissOptions: () -> Unit,
    onDismissGameMetadata: () -> Unit = {},
    onDismissSelectMusic: () -> Unit = {},
    onDismissEditCover: () -> Unit,
    onDismissEditHero: () -> Unit = {},
    onDismissEditLogo: () -> Unit = {},
    onDismissSelectIntro: () -> Unit = {},
    onDismissInitialSetup: () -> Unit = {},
    onDismissAddGameOptions: () -> Unit,
    onDismissSteamGrid: () -> Unit,
    onDismissLauncherSettings: () -> Unit = {},
    onDismissChangeAvatar: () -> Unit = {},
    onDismissEmulatorGames: () -> Unit = {},
    showIidbKeyDialog: Boolean = false,
    onDismissIidbKey: () -> Unit = {},
    onRequestIidbKey: () -> Unit = {},
    onRequestFetchIidb: (AppItem) -> Unit = {},
    onSaveIidbKey: (String) -> Unit = {},
    onTogglePinGame: (AppItem) -> Unit = {},
    onLaunchRom: (AppItem) -> Unit = {},
    onLaunchEmulatorDirectly: (AppItem) -> Unit = {},
    onRequestManageMetadata: (AppItem) -> Unit = {},
    onRequestEditMusic: (AppItem) -> Unit = {},
    onDeleteAllGameMetadata: (AppItem) -> Unit = {},
    onRequestEditCover: (AppItem) -> Unit,
    onRequestEditHero: (AppItem) -> Unit = {},
    onRequestEditLogo: (AppItem) -> Unit = {},
    onRequestSelectIntro: (AppItem) -> Unit = {},
    onRequestPickRomFolder: () -> Unit = {},
    onRequestPickDownloadFolder: () -> Unit = {},
    onRequestSetupFolders: () -> Unit = {},
    onRequestRestartWizard: () -> Unit = {},
    onCompleteSetup: () -> Unit = {},
    onRequestSteamGridSettings: () -> Unit,
    onRequestAddGameOptions: () -> Unit = {},
    onRequestOpenLauncherSettings: () -> Unit = {},
    onRequestOpenSystemSettings: () -> Unit = {},
    onRequestChangeAvatar: () -> Unit = {},
    onRequestReorderMode: () -> Unit = {},
    onAvatarSelected: (String) -> Unit = {},
    onProfileNameUpdated: (String) -> Unit = {},
    onArtworkProviderSelected: (ArtworkProvider) -> Unit = {},
    onSaveSteamGridKey: (String) -> Unit,
    onApplyCoverUrl: (AppItem, String) -> Unit,
    onApplyHeroUrl: (AppItem, String) -> Unit = { _, _ -> },
    onApplyLogoUrl: (AppItem, String) -> Unit = { _, _ -> },
    onIntroSelected: (AppItem, String?) -> Unit = { _, _ -> },
    onApplySoundbiteUrl: (AppItem, String) -> Unit = { _, _ -> },
    onRemoveSoundbite: (AppItem) -> Unit = {},
    onClearAllMetadata: () -> Unit = {},
    metadataUpdateCounter: Int = 0,
    onRescanRoms: () -> Unit,
    onRemoveItem: (AppItem) -> Unit,
    showGameHubSheet: Boolean = false,
    onDismissGameHubSheet: () -> Unit = {},
    showQuickAccessDrawer: Boolean = false,
    onDismissQuickAccessDrawer: () -> Unit = {},
    onJumpToLetter: (Char) -> Unit = {},
    onRequestOpenDetails: () -> Unit = {},
    onRequestOpenQuickAccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520
    val accentColor = remember(accentColorLong) { Color(accentColorLong) }
    val coroutineScope = rememberCoroutineScope()
    val carouselState = rememberLazyListState()
    val folderRowState = rememberLazyListState()
    val folderCarouselState = rememberLazyListState()
    val folderAppsCarouselState = rememberLazyListState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = carouselState)
    val folderSnapFling = rememberSnapFlingBehavior(lazyListState = folderCarouselState)
    val folderAppsSnapFling = rememberSnapFlingBehavior(lazyListState = folderAppsCarouselState)

    // Current category based on tab:
    // Tab 0: Jogos (Android Games, Cloud Games, ROMs)
    // Tab 1: Emuladores (ONLY emulator apps)
    // Tab 2: Apps (Android apps in folders)
    val filteredApps = remember(apps, selectedTab) {
        when (selectedTab) {
            0 -> apps.filter { it.category == AppCategory.JOGOS }
            1 -> apps.filter { it.category == AppCategory.EMULADORES }
            else -> apps.filter { it.category == AppCategory.APPS }
        }
    }

    // Folders for Apps tab
    val folders = remember(apps) {
        CategoryManager.groupIntoFolders(apps)
    }

    val currentTotalCount = remember(selectedTab, activeFolder, folders.size, filteredApps.size) {
        if (selectedTab == 2) {
            if (activeFolder == null) folders.size else activeFolder.apps.size
        } else {
            filteredApps.size
        }
    }

    val safeIndex = remember(selectedIndex, currentTotalCount) {
        selectedIndex.coerceIn(0, (currentTotalCount - 1).coerceAtLeast(0))
    }

    val currentFocusedApp = remember(selectedTab, activeFolder, safeIndex, filteredApps) {
        if (selectedTab == 2) {
            if (activeFolder != null) activeFolder.apps.getOrNull(safeIndex) else null
        } else {
            filteredApps.getOrNull(safeIndex)
        }
    }

    // Dynamic 3DS Logo com produceState anti-race e debounce estrito por appId (Fase 2.7)
    val currentLogoState by produceState<Pair<String, Bitmap?>?>(
        initialValue = null,
        key1 = currentFocusedApp?.id,
        key2 = selectedTab,
        key3 = metadataUpdateCounter
    ) {
        val app = currentFocusedApp
        if ((selectedTab == 0 || selectedTab == 1) && app != null) {
            val targetId = app.id
            val local = withContext(Dispatchers.IO) {
                LogoManager.loadLocalLogo(context, targetId)
            }
            value = Pair(targetId, local)
        } else {
            value = null
        }
    }

    val currentLogoBitmap = if (currentLogoState?.first == currentFocusedApp?.id) currentLogoState?.second else null

    // Theme Music playback com debounce de 350ms (iiDB soundbite loop)
    LaunchedEffect(currentFocusedApp?.id, selectedTab) {
        val app = currentFocusedApp
        if ((selectedTab == 0 || selectedTab == 1) && app != null) {
            delay(350)
            ThemeMusicManager.playTheme(context, app.id, app.label)
        } else {
            ThemeMusicManager.stopTheme()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ThemeMusicManager.stopTheme()
        }
    }

    val heroFile = remember(currentFocusedApp?.id, metadataUpdateCounter) {
        currentFocusedApp?.let { HeroManager.getHeroFile(context, it.id).takeIf { f -> f.exists() } }
    }
    val blurFile = remember(currentFocusedApp?.id, metadataUpdateCounter) {
        currentFocusedApp?.let { HeroManager.getHeroBlurFile(context, it.id).takeIf { f -> f.exists() } }
    }

    // Auto-scroll Carousel (Jogos / Emuladores) com centralização (Fase 4.6)
    LaunchedEffect(safeIndex, selectedTab) {
        if (selectedTab != 2 && filteredApps.isNotEmpty()) {
            val info = carouselState.layoutInfo
            val itemW = info.visibleItemsInfo.firstOrNull()?.size ?: 0
            val offset = if (itemW > 0) -(info.viewportSize.width - itemW) / 2 else 0
            carouselState.animateScrollToItem(safeIndex, offset)
        }
    }

    // Auto-scroll folder carousel with horizontal centering
    LaunchedEffect(safeIndex, activeFolder, selectedTab) {
        if (selectedTab == 2 && activeFolder == null && folders.isNotEmpty()) {
            val info = folderCarouselState.layoutInfo
            val itemW = info.visibleItemsInfo.firstOrNull()?.size ?: 0
            val offset = if (itemW > 0) -(info.viewportSize.width - itemW) / 2 else 0
            folderCarouselState.animateScrollToItem(safeIndex, offset)
        }
    }

    // Auto-scroll inside folder apps carousel with horizontal centering
    LaunchedEffect(safeIndex, activeFolder, selectedTab) {
        if (selectedTab == 2 && activeFolder != null && activeFolder.apps.isNotEmpty()) {
            val info = folderAppsCarouselState.layoutInfo
            val itemW = info.visibleItemsInfo.firstOrNull()?.size ?: 0
            val offset = if (itemW > 0) -(info.viewportSize.width - itemW) / 2 else 0
            folderAppsCarouselState.animateScrollToItem(safeIndex, offset)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ConsoleBg)
    ) {
        // Renderização dinâmica de fundo de acordo com o modo escolhido pelo usuário
        when (backgroundMode) {
            BackgroundMode.WALLPAPER -> {
                if (customWallpaperBitmap != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = customWallpaperBitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(12.dp) // rule-ok: blur gaussiano de fundo solicitado pelo usuario
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x88000000),
                                            Color(0xAA12161C),
                                            Color(0xFA12161C)
                                        )
                                    )
                                )
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ConsoleBg)
                    )
                }
            }
            BackgroundMode.SOLID -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E252E),
                                    Color(0xFF161A20),
                                    Color(0xFF111418)
                                )
                            )
                        )
                )
            }
            BackgroundMode.OLED -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }
            BackgroundMode.IMMERSIVE -> {
                // Fundo Imersivo otimizado (Fase 4.4):
                // Crossfade leve com hero pré-renderizado e blurFile reduzido
                Crossfade(
                    targetState = heroFile,
                    animationSpec = tween(Motion.Slow),
                    label = "hero_bg_fade"
                ) { file ->
                    if (file != null) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (blurFile != null) {
                                AsyncImage(
                                    model = blurFile,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    filterQuality = FilterQuality.Low,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .blur(14.dp) // rule-ok: blur gaussiano de fundo solicitado pelo usuario
                                )
                            }
                            AsyncImage(
                                model = file,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.TopCenter,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawWithCache {
                                        val brush = Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.00f to Color(0x80111418),
                                                0.25f to Color(0x55111418),
                                                0.50f to Color(0x75111418),
                                                0.68f to Color(0xAA111418),
                                                0.82f to Color(0xDD111418),
                                                1.00f to Color(0xFF111418)
                                            )
                                        )
                                        onDrawBehind { drawRect(brush) }
                                    }
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ConsoleBg)
                        )
                    }
                }
            }
        }

        // Adaptive Aura OLED Lighting (Feature 4 Next-Gen):
        // Iluminação sutil e suave na atmosfera superior baseada na cor da arte do jogo
        var auraColor by remember { mutableStateOf<Color>(accentColor) }
        LaunchedEffect(currentFocusedApp?.id) {
            val extracted = AdaptiveAuraManager.getAuraColor(context, currentFocusedApp, accentColor)
            auraColor = extracted
        }
        val animatedAuraColor by animateColorAsState(
            targetValue = auraColor,
            animationSpec = tween(500),
            label = "adaptive_aura"
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val brush = Brush.radialGradient(
                        colors = listOf(
                            animatedAuraColor.copy(alpha = 0.20f),
                            animatedAuraColor.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, -size.height * 0.08f),
                        radius = size.width * 0.65f
                    )
                    onDrawBehind {
                        drawRect(brush)
                    }
                }
        )

        // Depth Recede Animation quando Quick Access Drawer está aberto
        val contentScale by animateFloatAsState(
            targetValue = if (showQuickAccessDrawer) 0.965f else 1.0f,
            animationSpec = Motion.ConsoleDrawerSpring,
            label = "home_content_scale"
        )
        val contentDimAlpha by animateFloatAsState(
            targetValue = if (showQuickAccessDrawer) 0.35f else 0.0f,
            animationSpec = tween(Motion.Base),
            label = "home_dim_alpha"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = contentScale
                    scaleY = contentScale
                }
        ) {
            // 1. Top Bar: [LB] Jogos Emuladores Apps [RB] + Perfil (canto superior esquerdo) + Signal Battery Clock
            TopBar(
                selectedTab = selectedTab,
                isTabAreaFocused = focusArea == FocusArea.TABS,
                onTabSelected = onTabSelected,
                batteryPct = batteryPct,
                profileName = profileName,
                avatarId = avatarId,
                accentColor = accentColor,
                isTopBarFocused = focusArea == FocusArea.TOP_BAR,
                topBarIndex = topBarIndex,
                onOpenProfileSettings = onRequestOpenLauncherSettings,
                onOpenAddGame = onRequestAddGameOptions,
                onOpenStartMenu = onRequestOpenSystemSettings
            )

            // 2. Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                color = DotGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Carregando...",
                                color = TabInactiveText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (filteredApps.isEmpty() && selectedTab != 2) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum item nesta categoria",
                            color = TabInactiveText,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(
                                    animationSpec = tween(450, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() }
                                ) + fadeIn(animationSpec = tween(380)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(450, easing = FastOutSlowInEasing),
                                            targetOffsetX = { fullWidth -> (-fullWidth * 0.12f).toInt() }
                                        ) + fadeOut(animationSpec = tween(280))
                                    )
                            } else {
                                (slideInHorizontally(
                                    animationSpec = tween(450, easing = FastOutSlowInEasing),
                                    initialOffsetX = { fullWidth -> (-fullWidth * 0.12f).toInt() }
                                ) + fadeIn(animationSpec = tween(380)))
                                    .togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(450, easing = FastOutSlowInEasing),
                                            targetOffsetX = { fullWidth -> (fullWidth * 0.12f).toInt() }
                                        ) + fadeOut(animationSpec = tween(280))
                                    )
                            }
                        },
                        label = "tab_content_switch"
                    ) { tab ->
                        if (tab == 0 || tab == 1) {
                            // Layout Carousel (Jogos e Emuladores)
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                // Logo 3D animada estilo Nintendo 3DS no centro da tela
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(
                                            top = if (isCompactHeight) 2.dp else 10.dp,
                                            bottom = if (isCompactHeight) 2.dp else 4.dp
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GameLogo3D(
                                        currentApp = currentFocusedApp,
                                        logoBitmap = currentLogoBitmap,
                                        accentColor = accentColor,
                                        metadataUpdateCounter = metadataUpdateCounter
                                    )
                                }

                                LazyRow(
                                    state = carouselState,
                                    flingBehavior = snapFlingBehavior,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = if (isCompactHeight) 4.dp else 12.dp),
                                    contentPadding = PaddingValues(horizontal = if (isCompactHeight) 24.dp else 40.dp),
                                    horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 12.dp else 16.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    itemsIndexed(
                                        items = filteredApps,
                                        key = { _, app -> app.id }
                                    ) { index, app ->
                                            GameCard(
                                                app = app,
                                                isSelected = index == safeIndex && focusArea == FocusArea.CAROUSEL,
                                                onSelect = { onSelectedIndexChange(index) },
                                                onLaunch = { onLaunchApp(app) },
                                                onLongClick = {
                                                    onSelectedIndexChange(index)
                                                    onOpenSettings(app)
                                                },
                                                isReorderMode = isReorderMode,
                                                accentColor = accentColor
                                            )
                                    }
                                }
                            }
                        } else {
                            // Tab 2: APPS - Organizados em Pastas por Categorias
                            if (activeFolder == null) {
                                // Vista de Pastas: 3D Preview em cima + Carrossel Horizontal embaixo
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    val focusedFolder = folders.getOrNull(safeIndex.coerceIn(0, (folders.size - 1).coerceAtLeast(0)))
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(
                                                top = if (isCompactHeight) 2.dp else 10.dp,
                                                bottom = if (isCompactHeight) 2.dp else 4.dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (focusedFolder != null) {
                                            FolderDetailPreview(
                                                folder = focusedFolder,
                                                accentColor = accentColor
                                            )
                                        }
                                    }

                                    LazyRow(
                                        state = folderCarouselState,
                                        flingBehavior = folderSnapFling,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = if (isCompactHeight) 4.dp else 12.dp),
                                        contentPadding = PaddingValues(horizontal = if (isCompactHeight) 24.dp else 40.dp),
                                        horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 12.dp else 16.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        itemsIndexed(
                                            items = folders,
                                            key = { _, folder -> folder.id }
                                        ) { index, folder ->
                                            AppFolderCard(
                                                folder = folder,
                                                isSelected = index == safeIndex && focusArea == FocusArea.CAROUSEL,
                                                onSelect = { onSelectedIndexChange(index) },
                                                onOpen = { onOpenFolder(folder) }
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Vista dentro da Pasta (Folder Contents View) - Console Showcase em cima + Carrossel embaixo
                                // Sem corte no topo, sem textos redundantes (inscrição "volta | sistema" completamente removida)
                                val focusedApp = activeFolder.apps.getOrNull(safeIndex.coerceIn(0, (activeFolder.apps.size - 1).coerceAtLeast(0)))
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    // Showcase de topo do App em destaque com ações [A] Iniciar, [X] Opções, [B] Voltar
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(
                                                top = if (isCompactHeight) 2.dp else 10.dp,
                                                bottom = if (isCompactHeight) 2.dp else 4.dp
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (focusedApp != null) {
                                            FolderAppShowcase(
                                                app = focusedApp,
                                                folderTitle = activeFolder.title,
                                                currentIndex = safeIndex,
                                                totalApps = activeFolder.apps.size,
                                                accentColor = accentColor,
                                                onLaunch = { onLaunchApp(focusedApp) },
                                                onManage = { onRequestManageMetadata(focusedApp) },
                                                onBack = { onCloseFolder() }
                                            )
                                        }
                                    }

                                    // Carrossel horizontal de aplicativos com ícones 100% preenchidos de ponta a ponta
                                    LazyRow(
                                        state = folderAppsCarouselState,
                                        flingBehavior = folderAppsSnapFling,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = if (isCompactHeight) 4.dp else 12.dp),
                                        contentPadding = PaddingValues(horizontal = if (isCompactHeight) 24.dp else 40.dp),
                                        horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 10.dp else 14.dp),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        itemsIndexed(
                                            items = activeFolder.apps,
                                            key = { _, app -> app.id }
                                        ) { index, app ->
                                            ConsoleAppTile(
                                                app = app,
                                                isSelected = index == safeIndex && focusArea == FocusArea.CAROUSEL,
                                                onSelect = { onSelectedIndexChange(index) },
                                                onLaunch = { onLaunchApp(app) },
                                                onLongClick = {
                                                    onSelectedIndexChange(index)
                                                    onRequestManageMetadata(app)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Bottom Controller Hints e Botões Tocáveis (T4.2)
            BottomBar(
                isAppsTab = selectedTab == 2,
                isInsideFolder = activeFolder != null,
                isReorderMode = isReorderMode,
                isEmulatorsTab = selectedTab == 1,
                onPrimaryAction = {
                    val app = filteredApps.getOrNull(safeIndex)
                    if (app != null) {
                        onLaunchApp(app)
                    }
                },
                onOptionsAction = {
                    val app = filteredApps.getOrNull(safeIndex)
                    if (app != null) {
                        onOpenSettings(app)
                    }
                },
                onAddGameAction = onRequestAddGameOptions,
                onBackAction = {
                    if (activeFolder != null) onCloseFolder()
                },
                onOpenDetailsAction = {
                    if (selectedTab != 2 && currentFocusedApp != null) {
                        onRequestOpenDetails()
                    }
                },
                onQuickAccessAction = onRequestOpenQuickAccess
            )
        }

        // Dialogs
        val targetApp = activeDialogApp ?: currentFocusedApp

        if (showOptionsDialog && targetApp != null) {
            val appItem = targetApp
            ItemMenuDialog(
                app = appItem,
                onDismiss = onDismissOptions,
                onLaunch = { onLaunchApp(appItem) },
                onRequestManageMetadata = { onRequestManageMetadata(appItem) },
                onRequestEditCover = { onRequestEditCover(appItem) },
                onRequestEditHero = { onRequestEditHero(appItem) },
                onRequestEditLogo = { onRequestEditLogo(appItem) },
                onRequestSelectIntro = { onRequestSelectIntro(appItem) },
                onRequestFetchIidb = { onRequestFetchIidb(appItem) },
                onOpenAppSettings = { onOpenSettings(appItem) },
                onUninstallApp = { onUninstallApp(appItem) },
                onRemoveItem = { onRemoveItem(appItem) },
                onRequestReorder = onRequestReorderMode
            )
        }

        if (showGameMetadataDialog && targetApp != null) {
            val appItem = targetApp
            GameMetadataDialog(
                app = appItem,
                onDismiss = onDismissGameMetadata,
                onRequestEditCover = { onRequestEditCover(appItem) },
                onRequestEditHero = { onRequestEditHero(appItem) },
                onRequestEditLogo = { onRequestEditLogo(appItem) },
                onRequestEditMusic = { onRequestEditMusic(appItem) },
                onRequestReorder = onRequestReorderMode,
                onDeleteAll = { onDeleteAllGameMetadata(appItem) },
                metadataUpdateCounter = metadataUpdateCounter
            )
        }

        if (showSelectMusicDialog && targetApp != null) {
            val appItem = targetApp
            SelectMusicDialog(
                app = appItem,
                onDismiss = onDismissSelectMusic,
                onApplySoundbiteUrl = { url -> onApplySoundbiteUrl(appItem, url) },
                onRemoveSoundbite = { onRemoveSoundbite(appItem) }
            )
        }

        if (showEditCoverDialog && targetApp != null) {
            val appItem = targetApp
            EditCoverSteamGridDialog(
                app = appItem,
                onDismiss = onDismissEditCover,
                onApplyCoverUrl = { url -> onApplyCoverUrl(appItem, url) }
            )
        }

        if (showEditHeroDialog && targetApp != null) {
            val appItem = targetApp
            SteamGridHeroSelectDialog(
                app = appItem,
                onDismiss = onDismissEditHero,
                onApplyHeroUrl = { url -> onApplyHeroUrl(appItem, url) }
            )
        }

        if (showEditLogoDialog && targetApp != null) {
            val appItem = targetApp
            SteamGridLogoSelectDialog(
                app = appItem,
                onDismiss = onDismissEditLogo,
                onApplyLogoUrl = { url -> onApplyLogoUrl(appItem, url) }
            )
        }

        if (showSelectIntroDialog && targetApp != null) {
            SelectConsoleIntroDialog(
                app = targetApp,
                onDismiss = onDismissSelectIntro,
                onIntroSelected = { introId -> onIntroSelected(targetApp, introId) }
            )
        }

        if (showAddGameOptionsDialog) {
            AddGameOptionsDialog(
                onDismiss = onDismissAddGameOptions,
                onRescanRoms = onRescanRoms,
                allRoms = allScannedRoms,
                pinnedGameIds = pinnedGameIds,
                onTogglePinGame = onTogglePinGame
            )
        }

        if (showSteamGridDialog) {
            SteamGridApiKeyDialog(
                onDismiss = onDismissSteamGrid,
                onSaveKey = onSaveSteamGridKey
            )
        }

        if (showIidbKeyDialog) {
            IidbApiKeyDialog(
                onDismiss = onDismissIidbKey,
                onSaveKey = onSaveIidbKey
            )
        }

        if (showLauncherSettingsDialog) {
            StartMenuDialog(
                onDismiss = onDismissLauncherSettings,
                onRequestChangeAvatar = onRequestChangeAvatar,
                onRequestSteamGridKey = onRequestSteamGridSettings,
                onRequestIidbKey = onRequestIidbKey,
                onArtworkProviderSelected = onArtworkProviderSelected,
                onRescanRoms = onRescanRoms,
                onRequestSetupFolders = onRequestSetupFolders,
                onRequestRestartWizard = onRequestRestartWizard,
                profileName = profileName,
                avatarId = avatarId,
                currentBgMode = backgroundMode,
                onBgModeSelected = onBgModeSelected,
                onRequestPickWallpaper = onRequestPickWallpaper,
                hasCustomWallpaper = hasCustomWallpaper,
                currentAccentColor = accentColorLong,
                onAccentColorSelected = onAccentColorSelected,
                currentLaunchTransition = currentLaunchTransition,
                onLaunchTransitionSelected = onLaunchTransitionSelected,
                onClearAllMetadata = onClearAllMetadata
            )
        }

        if (showInitialSetupDialog) {
            InitialSetupDialog(
                currentRomFolder = currentRomFolder,
                currentDownloadFolder = currentDownloadFolder,
                currentApiKey = currentApiKey,
                onRequestPickRomFolder = onRequestPickRomFolder,
                onRequestPickDownloadFolder = onRequestPickDownloadFolder,
                onSaveApiKey = onSaveSteamGridKey,
                onCompleteSetup = onCompleteSetup,
                onDismiss = onDismissInitialSetup
            )
        }

        if (showChangeAvatarDialog) {
            ChangeAvatarDialog(
                onDismiss = onDismissChangeAvatar,
                onAvatarSelected = onAvatarSelected,
                onNameUpdated = onProfileNameUpdated
            )
        }

        if (showEmulatorGamesDialog && activeEmulatorApp != null) {
            com.decklauncher.ui.dialogs.EmulatorGamesDrawer(
                emulatorApp = activeEmulatorApp,
                allRoms = allScannedRoms,
                pinnedGameIds = pinnedGameIds,
                onDismiss = onDismissEmulatorGames,
                onLaunchEmulator = { onLaunchEmulatorDirectly(activeEmulatorApp) },
                onLaunchRom = onLaunchRom,
                onTogglePinGame = onTogglePinGame,
                onRequestChangeArtwork = { rom ->
                    onDismissEmulatorGames()
                    onRequestEditCover(rom)
                }
            )
        }

        // Fullscreen Console Boot Intro Player com fade suave
        if (activeIntroPlaying != null) {
            BootIntroPlayer(
                intro = activeIntroPlaying,
                onIntroFinished = onIntroFinished
            )
        }

        // Mantém a tela preta sólida e absoluta durante a transição para o emulador/jogo (sem deslizar)
        if (isLaunchingFade) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // Scrim escurecedor com profundidade ao abrir gaveta de acesso rápido
        if (contentDimAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = contentDimAlpha))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismissQuickAccessDrawer() }
            )
        }

        // Game Hub Sheet (Feature 2 Next-Gen)
        if (showGameHubSheet && currentFocusedApp != null) {
            val app = currentFocusedApp
            GameHubSheet(
                app = app,
                isVisible = showGameHubSheet,
                onDismiss = onDismissGameHubSheet,
                onLaunch = { onLaunchApp(app) },
                onOptions = { onOpenSettings(app) },
                onManageMetadata = { onRequestManageMetadata(app) },
                accentColor = accentColor,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Quick Access Drawer (Feature 3 Next-Gen)
        if (showQuickAccessDrawer) {
            QuickAccessDrawer(
                isVisible = showQuickAccessDrawer,
                onDismiss = onDismissQuickAccessDrawer,
                batteryPct = batteryPct,
                currentTime = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                onJumpToLetter = onJumpToLetter,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
