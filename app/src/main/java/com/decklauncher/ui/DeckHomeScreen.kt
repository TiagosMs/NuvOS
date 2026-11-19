package com.decklauncher.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.AppFolder
import com.decklauncher.data.CategoryManager
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.FocusArea
import com.decklauncher.ui.components.*
import com.decklauncher.ui.dialogs.*
import com.decklauncher.ui.theme.*
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
    showOptionsDialog: Boolean,
    showDownloadCoverDialog: Boolean,
    showAddCloudDialog: Boolean,
    showSteamGridDialog: Boolean,
    showIconPackDialog: Boolean,
    onSelectedIndexChange: (Int) -> Unit,
    onTabSelected: (Int) -> Unit,
    onOpenFolder: (AppFolder) -> Unit,
    onCloseFolder: () -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onOpenSettings: (AppItem?) -> Unit,
    onDismissOptions: () -> Unit,
    onDismissDownloadCover: () -> Unit,
    onDismissAddCloud: () -> Unit,
    onDismissSteamGrid: () -> Unit,
    onDismissIconPack: () -> Unit,
    onRequestDownloadCover: (AppItem) -> Unit,
    onRequestAddCloudGame: () -> Unit,
    onRequestSteamGridSettings: () -> Unit,
    onRequestIconPackSettings: () -> Unit,
    onSaveSteamGridKey: (String) -> Unit,
    onSelectIconPack: (String?) -> Unit,
    onSearchLibretroCover: (AppItem, String) -> Unit,
    onDownloadCoverUrl: (AppItem, String) -> Unit,
    onAddCloudGameConfirm: (String, String) -> Unit,
    onRescanRoms: () -> Unit,
    onRemoveItem: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val carouselState = rememberLazyListState()
    val folderRowState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = carouselState)

    // Current category based on tab
    val currentTabCategory = when (selectedTab) {
        0 -> AppCategory.JOGOS
        1 -> AppCategory.EMULADORES
        else -> AppCategory.APPS
    }

    val filteredApps = remember(apps, currentTabCategory) {
        val list = apps.filter { it.category == currentTabCategory }
        if (list.isEmpty() && currentTabCategory != AppCategory.APPS) {
            apps
        } else list
    }

    // Folders for Apps tab
    val folders = remember(apps) {
        CategoryManager.groupIntoFolders(apps)
    }

    // List of items currently focused in Tab 2
    val activeAppItems = remember(activeFolder, filteredApps) {
        if (activeFolder != null) activeFolder.apps else filteredApps
    }

    val currentTotalCount = remember(selectedTab, activeFolder, folders.size, activeAppItems.size) {
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

    // Auto-scroll folders row
    LaunchedEffect(safeIndex, activeFolder) {
        if (selectedTab == 2 && activeFolder == null && folders.isNotEmpty()) {
            coroutineScope.launch {
                folderRowState.animateScrollToItem(safeIndex)
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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top Bar: [LB] Jogos Emuladores Apps [RB] + Signal Battery Clock
            TopBar(
                selectedTab = selectedTab,
                isTabAreaFocused = focusArea == FocusArea.TABS,
                onTabSelected = onTabSelected,
                batteryPct = batteryPct
            )

            // 2. Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (filteredApps.isEmpty() && selectedTab != 2) {
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
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_content_switch"
                    ) { tab ->
                        if (tab == 0 || tab == 1) {
                            // Layout Carousel (Jogos e Emuladores)
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                LazyRow(
                                    state = carouselState,
                                    flingBehavior = snapFlingBehavior,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    contentPadding = PaddingValues(horizontal = 40.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    itemsIndexed(
                                        items = filteredApps,
                                        key = { _, app -> app.id }
                                    ) { index, app ->
                                        GameCard(
                                            app = app,
                                            isSelected = index == safeIndex && focusArea != FocusArea.TABS,
                                            onSelect = { onSelectedIndexChange(index) },
                                            onLaunch = { onLaunchApp(app) }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Tab 2: APPS - Organizados em Pastas por Categorias + Ícones Menores
                            if (activeFolder == null) {
                                // Vista de Pastas (Folders View)
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 40.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "📁 Pastas de Aplicativos (${folders.size} categorias)",
                                            color = TextWhite,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "(A) Abrir Pasta   |   (X) Opções / Ícones",
                                            color = TabInactiveText,
                                            fontSize = 12.sp
                                        )
                                    }

                                    LazyRow(
                                        state = folderRowState,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        itemsIndexed(
                                            items = folders,
                                            key = { _, folder -> folder.id }
                                        ) { index, folder ->
                                            AppFolderCard(
                                                folder = folder,
                                                isSelected = index == safeIndex && focusArea != FocusArea.TABS,
                                                onSelect = { onSelectedIndexChange(index) },
                                                onOpen = { onOpenFolder(folder) }
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Vista dentro da Pasta (Folder Contents View) - Ícones Menores em 8 Colunas
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 40.dp, vertical = 6.dp)
                                ) {
                                    // Folder Header com botão de voltar
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF222222))
                                                .clickable { onCloseFolder() }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = "◀ (B) Voltar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(text = "|", color = Color(0x44FFFFFF))
                                            Text(
                                                text = "${activeFolder.iconEmoji} ${activeFolder.title} (${activeFolder.apps.size})",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "(A) Abrir App   |   (X) Opções",
                                            color = TabInactiveText,
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Compact Grid de 8 Colunas com Ícones Menores
                                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(8),
                                            state = gridState,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight(),
                                            contentPadding = PaddingValues(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            itemsIndexed(
                                                items = activeFolder.apps,
                                                key = { _, app -> app.id }
                                            ) { index, app ->
                                                AppGridCard(
                                                    app = app,
                                                    isSelected = index == safeIndex && focusArea != FocusArea.TABS,
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

            // 3. Bottom Controller Hints: (A) Jogar / Abrir   (X) Opções
            BottomBar()
        }

        // Dialogs
        if (showOptionsDialog) {
            val appToConfigure = currentFocusedApp ?: apps.firstOrNull()
            if (appToConfigure != null) {
                OptionsDialog(
                    app = appToConfigure,
                    onDismiss = onDismissOptions,
                    onLaunch = { onLaunchApp(appToConfigure) },
                    onRequestDownloadCover = { onRequestDownloadCover(appToConfigure) },
                    onRequestAddCloudGame = onRequestAddCloudGame,
                    onRequestSteamGridSettings = onRequestSteamGridSettings,
                    onRequestIconPackSettings = onRequestIconPackSettings,
                    onRescanRoms = onRescanRoms,
                    onOpenAppSettings = { onOpenSettings(appToConfigure) },
                    onRemoveItem = { onRemoveItem(appToConfigure) }
                )
            }
        }

        if (showDownloadCoverDialog && currentFocusedApp != null) {
            DownloadCoverDialog(
                app = currentFocusedApp,
                onDismiss = onDismissDownloadCover,
                onSearchLibretro = { title -> onSearchLibretroCover(currentFocusedApp, title) },
                onDownloadUrl = { url -> onDownloadCoverUrl(currentFocusedApp, url) },
                onRequestSteamGridSettings = onRequestSteamGridSettings
            )
        }

        if (showAddCloudDialog) {
            AddCloudGameDialog(
                onDismiss = onDismissAddCloud,
                onAdd = onAddCloudGameConfirm
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
    }
}
