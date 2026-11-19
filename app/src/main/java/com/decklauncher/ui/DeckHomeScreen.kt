package com.decklauncher.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    showEditCoverDialog: Boolean,
    showEditHeroDialog: Boolean = false,
    showEditLogoDialog: Boolean = false,
    showSelectIntroDialog: Boolean = false,
    showInitialSetupDialog: Boolean = false,
    showAddCloudDialog: Boolean,
    showAddGameOptionsDialog: Boolean,
    showSteamGridDialog: Boolean,
    showIconPackDialog: Boolean,
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
    onDismissEditCover: () -> Unit,
    onDismissEditHero: () -> Unit = {},
    onDismissEditLogo: () -> Unit = {},
    onDismissSelectIntro: () -> Unit = {},
    onDismissInitialSetup: () -> Unit = {},
    onDismissAddCloud: () -> Unit,
    onDismissAddGameOptions: () -> Unit,
    onDismissSteamGrid: () -> Unit,
    onDismissIconPack: () -> Unit,
    onDismissLauncherSettings: () -> Unit = {},
    onDismissChangeAvatar: () -> Unit = {},
    onDismissEmulatorGames: () -> Unit = {},
    onTogglePinGame: (AppItem) -> Unit = {},
    onLaunchRom: (AppItem) -> Unit = {},
    onRequestEditCover: (AppItem) -> Unit,
    onRequestEditHero: (AppItem) -> Unit = {},
    onRequestEditLogo: (AppItem) -> Unit = {},
    onRequestSelectIntro: (AppItem) -> Unit = {},
    onRequestPickRomFolder: () -> Unit = {},
    onRequestPickDownloadFolder: () -> Unit = {},
    onRequestSetupFolders: () -> Unit = {},
    onCompleteSetup: () -> Unit = {},
    onRequestAddGame: () -> Unit,
    onRequestSteamGridSettings: () -> Unit,
    onRequestIconPackSettings: () -> Unit,
    onRequestAddGameOptions: () -> Unit = {},
    onRequestOpenLauncherSettings: () -> Unit = {},
    onRequestChangeAvatar: () -> Unit = {},
    onRequestReorderMode: () -> Unit = {},
    onAvatarSelected: (String) -> Unit = {},
    onProfileNameUpdated: (String) -> Unit = {},
    onSaveSteamGridKey: (String) -> Unit,
    onSelectIconPack: (String?) -> Unit,
    onApplyCoverUrl: (AppItem, String) -> Unit,
    onApplyHeroUrl: (AppItem, String) -> Unit = { _, _ -> },
    onApplyLogoUrl: (AppItem, String) -> Unit = { _, _ -> },
    onIntroSelected: (AppItem, String?) -> Unit = { _, _ -> },
    onAddCloudGameConfirm: (String, String) -> Unit,
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
    val folderGridState = rememberLazyGridState()
    val gridState = rememberLazyGridState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = carouselState)

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

    // Dynamic 16:9 hero background & 3DS Logo adaptativo com SteamGridDB
    var currentHeroBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentLogoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(currentFocusedApp?.id, selectedTab) {
        if ((selectedTab == 0 || selectedTab == 1) && currentFocusedApp != null) {
            delay(120) // Debounce para navegação rápida
            val target = currentFocusedApp
            coroutineScope.launch {
                val hero = HeroManager.getOrFetchHero(context, target.id, target.label)
                if (currentFocusedApp?.id == target.id) {
                    currentHeroBitmap = hero
                }
            }
            coroutineScope.launch {
                val logo = LogoManager.getOrFetchLogo(context, target.id, target.label)
                if (currentFocusedApp?.id == target.id) {
                    currentLogoBitmap = logo
                }
            }
        } else {
            currentHeroBitmap = null
            currentLogoBitmap = null
        }
    }

    // Auto-scroll Carousel (Jogos / Emuladores)
    LaunchedEffect(safeIndex, selectedTab) {
        if (selectedTab != 2 && filteredApps.isNotEmpty()) {
            coroutineScope.launch {
                carouselState.animateScrollToItem(safeIndex)
            }
        }
    }

    // Auto-scroll inside folder
    LaunchedEffect(safeIndex, activeFolder) {
        if (selectedTab == 2 && activeFolder != null && activeFolder.apps.isNotEmpty()) {
            coroutineScope.launch {
                gridState.animateScrollToItem(safeIndex)
            }
        }
    }

    // Auto-scroll folders grid
    LaunchedEffect(safeIndex, activeFolder) {
        if (selectedTab == 2 && activeFolder == null && folders.isNotEmpty()) {
            coroutineScope.launch {
                folderGridState.animateScrollToItem(safeIndex)
            }
        }
    }

    // Dynamic scrollbar thumb position for Grid
    val scrollFraction by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val visibleItems = layoutInfo.visibleItemsInfo.size
            if (totalItems <= visibleItems || totalItems == 0) {
                0f
            } else {
                val firstIndex = gridState.firstVisibleItemIndex.toFloat()
                (firstIndex / (totalItems - visibleItems).coerceAtLeast(1)).coerceIn(0f, 1f)
            }
        }
    }

    val animatedThumbOffset by animateFloatAsState(
        targetValue = scrollFraction,
        label = "scrollbar_thumb"
    )

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
                // Solução Madura de Console (PlayStation 5 / Steam Deck Cinema Stage):
                // A arte 16:9 fica posicionada no topo/centro com nitidez suave (blur sutil 6dp),
                // com um degradê cinematográfico que desce até o grafite escuro onde fica o carrossel,
                // eliminando corte feio e visual borrado/lavado.
                AnimatedContent(
                    targetState = currentHeroBitmap,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(animationSpec = tween(450, easing = FastOutSlowInEasing))
                    },
                    label = "hero_bg_fade"
                ) { heroBmp ->
                    if (heroBmp != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // 1. Camada de fundo ambiente suave (Bloom)
                            Image(
                                bitmap = heroBmp.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(radius = 32.dp)
                            )

                            // 2. Arte 16:9 Cinema Stage no topo da tela com softening sutil (preserva arte e personagens)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.72f)
                            ) {
                                Image(
                                    bitmap = heroBmp.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.TopCenter,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .blur(radius = 6.dp)
                                )
                            }

                            // 3. Multi-stop Cinema Scrim
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colorStops = arrayOf(
                                                0.00f to Color(0xCC111418),
                                                0.15f to Color(0x66111418),
                                                0.45f to Color(0x33111418),
                                                0.70f to Color(0xEE111418),
                                                1.00f to Color(0xFF111418)
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
                onOpenAddGame = onRequestAddGameOptions
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
                                        accentColor = accentColor
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
                                            isReorderMode = isReorderMode,
                                            accentColor = accentColor
                                        )
                                    }
                                }
                            }
                        } else {
                            // Tab 2: APPS - Organizados em Pastas por Categorias + Ícones Menores
                            if (activeFolder == null) {
                                // Vista de Pastas estilo Mockup: Grid 3x3 na esquerda + Preview na direita
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            start = if (isCompactHeight) 24.dp else 44.dp,
                                            end = if (isCompactHeight) 24.dp else 44.dp,
                                            top = if (isCompactHeight) 2.dp else 8.dp,
                                            bottom = if (isCompactHeight) 4.dp else 10.dp
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. Grid 3x3 de Pastas + Barra de Rolagem (Lado Esquerdo)
                                    Row(
                                        modifier = Modifier
                                            .weight(0.48f)
                                            .fillMaxHeight(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(3),
                                            state = folderGridState,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            contentPadding = PaddingValues(
                                                top = if (isCompactHeight) 4.dp else 8.dp,
                                                bottom = if (isCompactHeight) 4.dp else 8.dp,
                                                start = 2.dp,
                                                end = 8.dp
                                            ),
                                            horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 10.dp else 14.dp),
                                            verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 10.dp else 14.dp)
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

                                        // Scrollbar vertical do Grid de Pastas
                                        val folderScrollFraction = remember {
                                            derivedStateOf {
                                                val totalRows = (folders.size + 2) / 3
                                                val firstRow = folderGridState.firstVisibleItemIndex / 3
                                                if (totalRows <= 3) 0f
                                                else (firstRow.toFloat() / (totalRows - 3).coerceAtLeast(1)).coerceIn(0f, 1f)
                                            }
                                        }
                                        val animatedFolderScroll by animateFloatAsState(
                                            targetValue = folderScrollFraction.value,
                                            label = "folder_scroll"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .fillMaxHeight(0.6f)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(Color(0x28FFFFFF))
                                        ) {
                                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                                val trackHeight = maxHeight
                                                val thumbHeight = (trackHeight * 0.45f).coerceAtLeast(18.dp)
                                                val maxOffset = trackHeight - thumbHeight
                                                val thumbOffset = maxOffset * animatedFolderScroll

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(thumbHeight)
                                                        .offset(y = thumbOffset)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(Color(0x88FFFFFF))
                                                )
                                            }
                                        }
                                    }

                                    // 2. Pré-visualização da Pasta Ativa (Lado Direito do Mockup)
                                    val focusedFolder = folders.getOrNull(safeIndex.coerceIn(0, (folders.size - 1).coerceAtLeast(0)))
                                    Box(
                                        modifier = Modifier
                                            .weight(0.52f)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (focusedFolder != null) {
                                            FolderDetailPreview(folder = focusedFolder)
                                        }
                                    }
                                }
                            } else {
                                // Vista dentro da Pasta (Folder Contents View) - Sem corte no topo, sem textos redundantes
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            horizontal = if (isCompactHeight) 20.dp else 40.dp,
                                            vertical = if (isCompactHeight) 2.dp else 4.dp
                                        )
                                    ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = if (isCompactHeight) 2.dp else 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF222222))
                                                .clickable { onCloseFolder() }
                                                .padding(horizontal = 12.dp, vertical = if (isCompactHeight) 4.dp else 6.dp)
                                        ) {
                                            Text(text = "◀ (B) Voltar", color = Color.White, fontSize = if (isCompactHeight) 11.sp else 12.sp, fontWeight = FontWeight.Bold)
                                            Text(text = "|", color = Color(0x44FFFFFF))
                                            Text(
                                                text = "${activeFolder.title} (${activeFolder.apps.size})",
                                                color = Color.White,
                                                fontSize = if (isCompactHeight) 12.sp else 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Compact Grid de Colunas com Padding superior ampliado (sem corte de topo)
                                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(if (isCompactHeight) 6 else 8),
                                            state = gridState,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            contentPadding = PaddingValues(
                                                top = if (isCompactHeight) 6.dp else 16.dp,
                                                bottom = if (isCompactHeight) 8.dp else 16.dp,
                                                start = 4.dp,
                                                end = 4.dp
                                            ),
                                            horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 8.dp else 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 8.dp else 10.dp)
                                        ) {
                                            itemsIndexed(
                                                items = activeFolder.apps,
                                                key = { _, app -> app.id }
                                            ) { index, app ->
                                                AppGridCard(
                                                    app = app,
                                                    isSelected = index == safeIndex && focusArea == FocusArea.CAROUSEL,
                                                    onSelect = { onSelectedIndexChange(index) },
                                                    onLaunch = { onLaunchApp(app) },
                                                    showLabel = true
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Dynamic Scrollbar
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .fillMaxHeight(0.7f)
                                                .align(Alignment.CenterVertically)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(ScrollbarColor.copy(alpha = 0.35f))
                                        ) {
                                            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                                val trackHeight = maxHeight
                                                val thumbHeight = (trackHeight * 0.35f).coerceAtLeast(20.dp)
                                                val maxOffset = trackHeight - thumbHeight

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(thumbHeight)
                                                        .offset(y = maxOffset * animatedThumbOffset)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(ScrollbarColor)
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

            // 3. Bottom Controller Hints: (A) Jogar/Abrir   (X) Opções   (Y) Adicionar Jogos
            BottomBar(
                isAppsTab = selectedTab == 2,
                isInsideFolder = activeFolder != null,
                isReorderMode = isReorderMode,
                isEmulatorsTab = selectedTab == 1
            )
        }

        // Dialogs
        val targetApp = activeDialogApp ?: currentFocusedApp ?: apps.firstOrNull()

        if (showOptionsDialog && targetApp != null) {
            OptionsDialog(
                app = targetApp,
                onDismiss = onDismissOptions,
                onLaunch = { onLaunchApp(targetApp) },
                onRequestEditCover = { onRequestEditCover(targetApp) },
                onRequestEditHero = { onRequestEditHero(targetApp) },
                onRequestEditLogo = { onRequestEditLogo(targetApp) },
                onRequestSelectIntro = { onRequestSelectIntro(targetApp) },
                onRequestAddGame = onRequestAddGame,
                onRequestSteamGridSettings = onRequestSteamGridSettings,
                onRequestIconPackSettings = onRequestIconPackSettings,
                onRescanRoms = onRescanRoms,
                onOpenAppSettings = { onOpenSettings(targetApp) },
                onUninstallApp = { onUninstallApp(targetApp) },
                onRemoveItem = { onRemoveItem(targetApp) },
                onRequestReorder = onRequestReorderMode
            )
        }

        if (showEditCoverDialog && targetApp != null) {
            EditCoverSteamGridDialog(
                app = targetApp,
                onDismiss = onDismissEditCover,
                onApplyCoverUrl = { url -> onApplyCoverUrl(targetApp, url) }
            )
        }

        if (showEditHeroDialog && targetApp != null) {
            SteamGridHeroSelectDialog(
                app = targetApp,
                onDismiss = onDismissEditHero,
                onApplyHeroUrl = { url -> onApplyHeroUrl(targetApp, url) }
            )
        }

        if (showEditLogoDialog && targetApp != null) {
            SteamGridLogoSelectDialog(
                app = targetApp,
                onDismiss = onDismissEditLogo,
                onApplyLogoUrl = { url -> onApplyLogoUrl(targetApp, url) }
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
                onOpenCloudCatalog = onRequestAddGame,
                onRescanRoms = onRescanRoms
            )
        }

        if (showAddCloudDialog) {
            CloudGameCatalogDialog(
                onDismiss = onDismissAddCloud,
                onSelectGame = onAddCloudGameConfirm
            )
        }

        if (showSteamGridDialog) {
            SteamGridApiKeyDialog(
                onDismiss = onDismissSteamGrid,
                onSaveKey = onSaveSteamGridKey
            )
        }

        if (showIconPackDialog) {
            IconPackDialog(
                onDismiss = onDismissIconPack,
                onSelectPack = onSelectIconPack
            )
        }

        if (showLauncherSettingsDialog) {
            LauncherSettingsDialog(
                onDismiss = onDismissLauncherSettings,
                onRequestChangeAvatar = onRequestChangeAvatar,
                onRequestSteamGridKey = onRequestSteamGridSettings,
                onRequestIconPack = onRequestIconPackSettings,
                onRequestAddCloudGame = onRequestAddGame,
                onRescanRoms = onRescanRoms,
                onRequestReorderMode = onRequestReorderMode,
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
                onLaunchTransitionSelected = onLaunchTransitionSelected
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
                onLaunchEmulator = { onLaunchApp(activeEmulatorApp) },
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
