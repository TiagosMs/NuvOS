package com.decklauncher.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
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
    onCompleteSetup: () -> Unit = {},
    onRequestSteamGridSettings: () -> Unit,
    onRequestAddGameOptions: () -> Unit = {},
    onRequestOpenLauncherSettings: () -> Unit = {},
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
                            modifier = Modifier.fillMaxSize()
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
            BackgroundMode.IMMERSIVE -> {
                // Fundo Imersivo otimizado (Fase 4.4):
                // Crossfade leve com hero pré-renderizado e blurFile reduzido (sem blurs ao vivo em tela cheia)
                Crossfade(
                    targetState = heroFile,
                    animationSpec = tween(Motion.Slow),
                    label = "hero_bg_fade"
                ) { file ->
                    if (file != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (blurFile != null) {
                                AsyncImage(
                                    model = blurFile,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    filterQuality = FilterQuality.Low,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            AsyncImage(
                                model = file,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.TopCenter,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.72f)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .drawWithCache {
                                        val brush = Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.00f to Color(0xCC111418),
                                                0.15f to Color(0x66111418),
                                                0.45f to Color(0x33111418),
                                                0.70f to Color(0xEE111418),
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

        Column(
            modifier = Modifier.fillMaxSize()
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
                onOpenStartMenu = onRequestOpenLauncherSettings
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
                }
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
                onRescanRoms = onRescanRoms
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
            EmulatorGamesDialog(
                emulatorApp = activeEmulatorApp,
                allRoms = allScannedRoms,
                pinnedGameIds = pinnedGameIds,
                onDismiss = onDismissEmulatorGames,
                onLaunchEmulator = { onLaunchEmulatorDirectly(activeEmulatorApp) },
                onLaunchRom = onLaunchRom,
                onTogglePinGame = onTogglePinGame
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
    }
}
