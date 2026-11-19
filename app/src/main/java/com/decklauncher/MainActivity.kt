package com.decklauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.decklauncher.data.*
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.FocusArea
import com.decklauncher.model.ItemType
import com.decklauncher.ui.DeckHomeScreen
import com.decklauncher.ui.theme.DeckLauncherTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private var selectedIndexState = mutableIntStateOf(0)
    private var selectedTabState = mutableIntStateOf(0)
    private var focusAreaState = mutableStateOf(FocusArea.CAROUSEL)
    private var appsListState = mutableStateOf<List<AppItem>>(emptyList())
    private var batteryState = mutableIntStateOf(100)
    private var activeFolderState = mutableStateOf<AppFolder?>(null)
    private var isLoadingState = mutableStateOf(true)

    // Dialog & Interaction states
    private var showOptionsDialogState = mutableStateOf(false)
    private var showEditCoverDialogState = mutableStateOf(false)
    private var showAddCloudDialogState = mutableStateOf(false)
    private var showAddGameOptionsDialogState = mutableStateOf(false)
    private var showSteamGridDialogState = mutableStateOf(false)
    private var showIconPackDialogState = mutableStateOf(false)
    private var showLauncherSettingsDialogState = mutableStateOf(false)
    private var showChangeAvatarDialogState = mutableStateOf(false)
    private var isReorderModeState = mutableStateOf(false)
    private var profileNameState = mutableStateOf("Tiago")
    private var avatarIdState = mutableStateOf("deck_blue")
    private var activeDialogAppState = mutableStateOf<AppItem?>(null)
    private var backgroundModeState = mutableStateOf(BackgroundMode.IMMERSIVE)
    private var accentColorLongState = mutableLongStateOf(0xFF1A73E8)
    private var customWallpaperBitmapState = mutableStateOf<Bitmap?>(null)
    private var hasCustomWallpaperState = mutableStateOf(false)
    private var showEditHeroDialogState = mutableStateOf(false)
    private var showEditLogoDialogState = mutableStateOf(false)
    private var showInitialSetupDialogState = mutableStateOf(false)
    private var romFolderNameState = mutableStateOf("")
    private var downloadFolderNameState = mutableStateOf("")
    private var apiKeyTextState = mutableStateOf("")

    private val romFolderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            SetupManager.setRomFolder(this, uri)
            romFolderNameState.value = SetupManager.getRomFolderName(this)
            loadInstalledApps()
            Toast.makeText(this, "Pasta de ISOs configurada!", Toast.LENGTH_SHORT).show()
        }
    }

    private val downloadFolderPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            SetupManager.setDownloadFolder(this, uri)
            downloadFolderNameState.value = SetupManager.getDownloadFolderName(this)
            Toast.makeText(this, "Pasta de downloads configurada!", Toast.LENGTH_SHORT).show()
        }
    }

    private val wallpaperPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val ok = ThemeManager.saveCustomWallpaperFromUri(this@MainActivity, uri)
                if (ok) {
                    val bmp = ThemeManager.loadCustomWallpaper(this@MainActivity)
                    customWallpaperBitmapState.value = bmp
                    hasCustomWallpaperState.value = true
                    backgroundModeState.value = BackgroundMode.WALLPAPER
                    ThemeManager.setBackgroundMode(this@MainActivity, BackgroundMode.WALLPAPER)
                    Toast.makeText(this@MainActivity, "Papel de parede aplicado!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private enum class JoystickDir { NONE, LEFT, RIGHT, UP, DOWN }
    private var currentJoyDir = JoystickDir.NONE
    private var joyRepeatJob: Job? = null
    private var lastNavTime = 0L

    companion object {
        private const val HOLD_THRESHOLD_MS = 1000L
        private const val REPEAT_INTERVAL_MS = 90L
        private const val MIN_TAP_INTERVAL_MS = 180L
        private const val DEADZONE_ACTIVE = 0.55f
        private const val DEADZONE_RELEASE = 0.25f
        private const val GRID_COLUMNS = 8
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                batteryState.intValue = (level * 100) / scale
            }
        }
    }

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadInstalledApps()
        }
    }

    private val steamGridBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val key = intent?.getStringExtra("key")
            if (!key.isNullOrBlank()) {
                SteamGridDbManager.setApiKey(this@MainActivity, key.trim())
                Toast.makeText(this@MainActivity, "Chave SteamGridDB salva!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configura chave de API padrão fornecida pelo usuário
        SteamGridDbManager.setApiKey(this, "1b8937c720a13f8746fedc874c88b46e")

        profileNameState.value = ProfileManager.getProfileName(this)
        avatarIdState.value = ProfileManager.getAvatarId(this)
        backgroundModeState.value = ThemeManager.getBackgroundMode(this)
        accentColorLongState.longValue = ThemeManager.getAccentColorLong(this)
        hasCustomWallpaperState.value = ThemeManager.hasCustomWallpaper(this)
        customWallpaperBitmapState.value = ThemeManager.loadCustomWallpaper(this)
        romFolderNameState.value = SetupManager.getRomFolderName(this)
        downloadFolderNameState.value = SetupManager.getDownloadFolderName(this)
        apiKeyTextState.value = SteamGridDbManager.getApiKey(this)

        if (!SetupManager.isSetupCompleted(this)) {
            showInitialSetupDialogState.value = true
        }

        setupImmersiveMode()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val pkgFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        registerReceiver(packageReceiver, pkgFilter)

        val sgFilter = IntentFilter("com.decklauncher.SET_STEAMGRID_KEY")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(steamGridBroadcastReceiver, sgFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(steamGridBroadcastReceiver, sgFilter)
        }

        setContent {
            DeckLauncherTheme {
                DeckHomeScreen(
                    apps = appsListState.value,
                    selectedIndex = selectedIndexState.intValue,
                    selectedTab = selectedTabState.intValue,
                    focusArea = focusAreaState.value,
                    batteryPct = batteryState.intValue,
                    activeFolder = activeFolderState.value,
                    activeDialogApp = activeDialogAppState.value,
                    isLoading = isLoadingState.value,
                    showOptionsDialog = showOptionsDialogState.value,
                    showEditCoverDialog = showEditCoverDialogState.value,
                    showEditHeroDialog = showEditHeroDialogState.value,
                    showEditLogoDialog = showEditLogoDialogState.value,
                    showInitialSetupDialog = showInitialSetupDialogState.value,
                    showAddCloudDialog = showAddCloudDialogState.value,
                    showAddGameOptionsDialog = showAddGameOptionsDialogState.value,
                    showSteamGridDialog = showSteamGridDialogState.value,
                    showIconPackDialog = showIconPackDialogState.value,
                    showLauncherSettingsDialog = showLauncherSettingsDialogState.value,
                    showChangeAvatarDialog = showChangeAvatarDialogState.value,
                    isReorderMode = isReorderModeState.value,
                    profileName = profileNameState.value,
                    avatarId = avatarIdState.value,
                    currentRomFolder = romFolderNameState.value,
                    currentDownloadFolder = downloadFolderNameState.value,
                    currentApiKey = apiKeyTextState.value,
                    backgroundMode = backgroundModeState.value,
                    accentColorLong = accentColorLongState.longValue,
                    customWallpaperBitmap = customWallpaperBitmapState.value,
                    hasCustomWallpaper = hasCustomWallpaperState.value,
                    onBgModeSelected = { mode ->
                        backgroundModeState.value = mode
                        ThemeManager.setBackgroundMode(this@MainActivity, mode)
                    },
                    onRequestPickWallpaper = {
                        try {
                            wallpaperPickerLauncher.launch("image/*")
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Erro ao abrir seletor: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onAccentColorSelected = { colorVal ->
                        accentColorLongState.longValue = colorVal
                        ThemeManager.setAccentColor(this@MainActivity, colorVal)
                    },
                    onSelectedIndexChange = { selectedIndexState.intValue = it },
                    onTabSelected = {
                        selectedTabState.intValue = it
                        selectedIndexState.intValue = 0
                        activeFolderState.value = null
                        focusAreaState.value = FocusArea.CAROUSEL
                    },
                    onOpenFolder = { folder ->
                        activeFolderState.value = folder
                        selectedIndexState.intValue = 0
                        triggerHaptic()
                    },
                    onCloseFolder = {
                        activeFolderState.value = null
                        selectedIndexState.intValue = 0
                        triggerHaptic()
                    },
                    onLaunchApp = { launchItem(it) },
                    onOpenSettings = { openAppOptions(it) },
                    onUninstallApp = { uninstallApp(it) },
                    onDismissOptions = { showOptionsDialogState.value = false },
                    onDismissEditCover = { showEditCoverDialogState.value = false },
                    onDismissEditHero = { showEditHeroDialogState.value = false },
                    onDismissEditLogo = { showEditLogoDialogState.value = false },
                    onDismissInitialSetup = { showInitialSetupDialogState.value = false },
                    onDismissAddCloud = { showAddCloudDialogState.value = false },
                    onDismissAddGameOptions = { showAddGameOptionsDialogState.value = false },
                    onDismissSteamGrid = { showSteamGridDialogState.value = false },
                    onDismissIconPack = { showIconPackDialogState.value = false },
                    onDismissLauncherSettings = { showLauncherSettingsDialogState.value = false },
                    onDismissChangeAvatar = { showChangeAvatarDialogState.value = false },
                    onRequestEditCover = { item ->
                        activeDialogAppState.value = item
                        showEditCoverDialogState.value = true
                    },
                    onRequestEditHero = { item ->
                        activeDialogAppState.value = item
                        showEditHeroDialogState.value = true
                    },
                    onRequestEditLogo = { item ->
                        activeDialogAppState.value = item
                        showEditLogoDialogState.value = true
                    },
                    onRequestPickRomFolder = {
                        try {
                            romFolderPickerLauncher.launch(null)
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Erro ao abrir seletor: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRequestPickDownloadFolder = {
                        try {
                            downloadFolderPickerLauncher.launch(null)
                        } catch (e: Exception) {
                            Toast.makeText(this@MainActivity, "Erro ao abrir seletor: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRequestSetupFolders = {
                        showInitialSetupDialogState.value = true
                    },
                    onCompleteSetup = {
                        SetupManager.setSetupCompleted(this@MainActivity, true)
                        showInitialSetupDialogState.value = false
                        loadInstalledApps()
                        Toast.makeText(this@MainActivity, "Configuração concluída! Biblioteca atualizada.", Toast.LENGTH_LONG).show()
                    },
                    onRequestAddGame = {
                        showAddCloudDialogState.value = true
                    },
                    onRequestSteamGridSettings = {
                        showSteamGridDialogState.value = true
                    },
                    onRequestIconPackSettings = {
                        showIconPackDialogState.value = true
                    },
                    onRequestOpenLauncherSettings = {
                        showLauncherSettingsDialogState.value = true
                    },
                    onRequestChangeAvatar = {
                        showChangeAvatarDialogState.value = true
                    },
                    onRequestReorderMode = {
                        isReorderModeState.value = true
                        Toast.makeText(this@MainActivity, "Modo Mover: D-Pad ◀ / ▶ troca de posição, (A) fixa", Toast.LENGTH_LONG).show()
                    },
                    onAvatarSelected = { avatarId ->
                        avatarIdState.value = avatarId
                        ProfileManager.setAvatarId(this@MainActivity, avatarId)
                    },
                    onProfileNameUpdated = { name ->
                        profileNameState.value = name
                        ProfileManager.setProfileName(this@MainActivity, name)
                    },
                    onSaveSteamGridKey = { key ->
                        SteamGridDbManager.setApiKey(this, key)
                        Toast.makeText(this, "Chave SteamGridDB salva!", Toast.LENGTH_SHORT).show()
                    },
                    onSelectIconPack = { packPkg ->
                        IconPackManager.setSelectedPack(this, packPkg)
                        loadInstalledApps(forceReloadPack = true)
                        Toast.makeText(this, if (packPkg != null) "Pacote de ícones aplicado!" else "Ícones padrão restaurados", Toast.LENGTH_SHORT).show()
                    },
                    onApplyCoverUrl = { item, url ->
                        downloadCoverFromUrl(item, url)
                    },
                    onApplyHeroUrl = { item, url ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val bmp = SteamGridDbManager.downloadAndApplyHero(this@MainActivity, item.id, url)
                            if (bmp != null) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, "Fundo 16:9 atualizado!", Toast.LENGTH_SHORT).show()
                                    showEditHeroDialogState.value = false
                                    loadInstalledApps()
                                }
                            }
                        }
                    },
                    onApplyLogoUrl = { item, url ->
                        CoroutineScope(Dispatchers.IO).launch {
                            val bmp = SteamGridDbManager.downloadAndApplyLogo(this@MainActivity, item.id, url)
                            if (bmp != null) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@MainActivity, "Logo 3D atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                                    showEditLogoDialogState.value = false
                                    loadInstalledApps()
                                }
                            }
                        }
                    },
                    onAddCloudGameConfirm = { title, url ->
                        val item = CloudGameManager.addCloudGame(this, title, url)
                        showAddCloudDialogState.value = false
                        loadInstalledApps()
                        // Busca e aplica capa do SteamGridDB automaticamente em background
                        CoroutineScope(Dispatchers.IO).launch {
                            val games = SteamGridDbManager.searchGames(this@MainActivity, title)
                            if (games.isNotEmpty()) {
                                val grids = SteamGridDbManager.getGridsForGame(this@MainActivity, games.first().id)
                                if (grids.isNotEmpty()) {
                                    CoverManager.downloadCover(this@MainActivity, item.id, grids.first().url)
                                    loadInstalledApps()
                                }
                            }
                        }
                        Toast.makeText(this, "$title adicionado para Better xCloud!", Toast.LENGTH_SHORT).show()
                    },
                    onRescanRoms = {
                        loadInstalledApps()
                        Toast.makeText(this, "ROMs atualizadas!", Toast.LENGTH_SHORT).show()
                    },
                    onRemoveItem = { item ->
                        if (item.itemType == ItemType.CLOUD) {
                            CloudGameManager.removeCloudGame(this, item.id)
                            loadInstalledApps()
                            Toast.makeText(this, "Jogo em nuvem removido", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        loadInstalledApps()
    }

    override fun onResume() {
        super.onResume()
        setupImmersiveMode()
    }

    override fun onPause() {
        super.onPause()
        joyRepeatJob?.cancel()
        currentJoyDir = JoystickDir.NONE
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        unregisterReceiver(packageReceiver)
        try {
            unregisterReceiver(steamGridBroadcastReceiver)
        } catch (_: Exception) {}
    }

    private fun setupImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun triggerHaptic() {
        try {
            window.decorView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (_: Exception) {}
    }

    private fun getFilteredApps(): List<AppItem> {
        val all = appsListState.value
        val cat = when (selectedTabState.intValue) {
            0 -> AppCategory.JOGOS
            1 -> AppCategory.EMULADORES
            else -> AppCategory.APPS
        }
        val filtered = all.filter { it.category == cat }
        return if (filtered.isEmpty() && cat != AppCategory.APPS) all else filtered
    }

    private fun loadInstalledApps(forceReloadPack: Boolean = false) {
        isLoadingState.value = true
        CoroutineScope(Dispatchers.IO).launch {
            IconPackManager.loadIconPack(this@MainActivity, forceReload = forceReloadPack)

            val pm = packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolved = pm.queryIntentActivities(mainIntent, 0)
            val appItems = resolved
                .filter { it.activityInfo.packageName != packageName }
                .map { info ->
                    val label = info.loadLabel(pm).toString()
                    val pkg = info.activityInfo.packageName
                    val pkgLower = pkg.lowercase()
                    val labelLower = label.lowercase()
                    val appId = "app_$pkgLower"
                    val activityName = info.activityInfo.name

                    // Identifica apps emuladores
                    val isEmulator = pkgLower.contains("retroarch") || pkgLower.contains("dolphin")
                        || pkgLower.contains("ppsspp") || pkgLower.contains("aether") || pkgLower.contains("nether")
                        || pkgLower.contains("yuzu") || pkgLower.contains("citra") || pkgLower.contains("vita3k")
                        || pkgLower.contains("duckstation") || pkgLower.contains("mupen") || pkgLower.contains("snes9x")
                        || pkgLower.contains("myboy") || pkgLower.contains("drastic") || pkgLower.contains("lemuroid")
                        || pkgLower.contains("skyline") || pkgLower.contains("redream") || pkgLower.contains("flycast")
                        || pkgLower.contains("melonds") || pkgLower.contains("hakux") || pkgLower.contains("gamehub")
                        || pkgLower.contains("eden") || labelLower.contains("emulator") || labelLower.contains("emulador")
                        || labelLower.contains("hakux") || labelLower.contains("dolphin")

                    // Identifica jogos Android nativos
                    val isGame = !isEmulator && (
                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && info.activityInfo.applicationInfo.category == ApplicationInfo.CATEGORY_GAME)
                        || pkgLower.contains("minecraft") || pkgLower.contains("game") || pkgLower.contains("roblox")
                        || pkgLower.contains("xbox") || labelLower.contains("minecraft") || labelLower.contains("game")
                        || labelLower.contains("xcloud") || labelLower.contains("moonlight")
                    )

                    // Emuladores vão para Tab 1; Jogos para Tab 0; demais para Tab 2 (Apps)
                    val category = when {
                        isEmulator -> AppCategory.EMULADORES
                        isGame -> AppCategory.JOGOS
                        else -> AppCategory.APPS
                    }

                    val customIconDrawable = IconPackManager.getIcon(this@MainActivity, pkg, activityName)
                    val drawable = customIconDrawable ?: try {
                        info.loadIcon(pm)
                    } catch (e: Exception) {
                        null
                    }

                    val iconBitmap = drawable?.let { drawableToBitmap(it) }
                    val localCover = CoverManager.loadLocalCover(this@MainActivity, appId)

                    val tempItem = AppItem(
                        id = appId,
                        label = label,
                        packageName = pkg,
                        activityName = activityName,
                        category = category,
                        itemType = ItemType.APP,
                        iconBitmap = iconBitmap?.asImageBitmap(),
                        coverBitmap = localCover?.asImageBitmap()
                    )

                    val folderCat = CategoryManager.categorizeApp(tempItem, info.activityInfo.applicationInfo)
                    tempItem.copy(folderCategory = folderCat)
                }

            // Jogos em Nuvem vão para Tab JOGOS
            val cloudGames = CloudGameManager.getCloudGames(this@MainActivity).map { cg ->
                val localCover = CoverManager.loadLocalCover(this@MainActivity, cg.id)
                cg.copy(category = AppCategory.JOGOS, coverBitmap = localCover?.asImageBitmap())
            }

            // ROMs de Emulador vão para Tab JOGOS (conforme solicitado pelo usuário)
            val romGames = RomScanner.scanRoms(this@MainActivity).map { rg ->
                var localCover = CoverManager.loadLocalCover(this@MainActivity, rg.id)
                if (localCover == null) {
                    localCover = CoverManager.searchLibretroCover(this@MainActivity, rg.id, rg.label)
                }
                rg.copy(category = AppCategory.JOGOS, coverBitmap = localCover?.asImageBitmap())
            }

            val allJogos = (appItems + cloudGames + romGames).filter { it.category == AppCategory.JOGOS }.sortedBy { it.label.lowercase() }
            val allEmuladores = appItems.filter { it.category == AppCategory.EMULADORES }.sortedBy { it.label.lowercase() }
            val allApps = appItems.filter { it.category == AppCategory.APPS }.sortedBy { it.label.lowercase() }

            val orderedJogos = OrderManager.applyOrder(this@MainActivity, "jogos", allJogos)
            val orderedEmuladores = OrderManager.applyOrder(this@MainActivity, "emuladores", allEmuladores)
            val orderedApps = OrderManager.applyOrder(this@MainActivity, "apps", allApps)

            val combined = orderedJogos + orderedEmuladores + orderedApps

            withContext(Dispatchers.Main) {
                appsListState.value = combined
                isLoadingState.value = false
            }
        }
    }

    private fun swapSelectedItem(delta: Int) {
        val catKey = when (selectedTabState.intValue) {
            0 -> "jogos"
            1 -> "emuladores"
            else -> "apps"
        }
        val currentList = getFilteredApps()
        val currIdx = selectedIndexState.intValue
        val targetIdx = currIdx + delta
        if (targetIdx in currentList.indices) {
            val newList = OrderManager.swapItems(this, catKey, currentList, currIdx, targetIdx)
            selectedIndexState.intValue = targetIdx

            // Atualiza a lista completa preservando as outras categorias
            val otherItems = appsListState.value.filter {
                when (selectedTabState.intValue) {
                    0 -> it.category != AppCategory.JOGOS
                    1 -> it.category != AppCategory.EMULADORES
                    else -> it.category != AppCategory.APPS
                }
            }
            appsListState.value = when (selectedTabState.intValue) {
                0 -> newList + otherItems
                1 -> otherItems.filter { it.category == AppCategory.JOGOS } + newList + otherItems.filter { it.category == AppCategory.APPS }
                else -> otherItems + newList
            }
            triggerHaptic()
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
            val bmp = drawable.bitmap
            if (bmp.width <= 256 && bmp.height <= 256) {
                return bmp
            }
        }
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 128
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 128
        val targetW = width.coerceIn(96, 192)
        val targetH = height.coerceIn(96, 192)
        val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.isFilterBitmap = true
        drawable.draw(canvas)
        return bitmap
    }

    private fun launchItem(item: AppItem) {
        when (item.itemType) {
            ItemType.CLOUD -> {
                val url = item.cloudUrl
                if (!url.isNullOrBlank()) {
                    var openedInBxc = false
                    try {
                        val bxcIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            setPackage("com.redphx.betterxc")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(bxcIntent)
                        openedInBxc = true
                    } catch (_: Exception) {}

                    if (!openedInBxc) {
                        try {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(browserIntent)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Não foi possível abrir o jogo", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "URL de streaming inválida", Toast.LENGTH_SHORT).show()
                }
            }
            ItemType.ROM -> {
                val ok = RomScanner.launchRom(this, item)
                if (!ok) {
                    Toast.makeText(this, "Instale o emulador para abrir: ${item.label}", Toast.LENGTH_LONG).show()
                }
            }
            ItemType.APP -> {
                val launchIntent = packageManager.getLaunchIntentForPackage(item.packageName)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(this, "Não foi possível iniciar ${item.label}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openAppOptions(app: AppItem?) {
        if (app != null && app.itemType == ItemType.APP) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${app.packageName}")
                }
                startActivity(intent)
                return
            } catch (_: Exception) {}
        }
        startActivity(Intent(Settings.ACTION_SETTINGS))
    }

    private fun uninstallApp(app: AppItem?) {
        if (app != null && app.itemType == ItemType.APP) {
            try {
                val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                    data = Uri.parse("package:${app.packageName}")
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Não foi possível desinstalar ${app.label}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadCoverFromUrl(item: AppItem, url: String) {
        isLoadingState.value = true
        CoroutineScope(Dispatchers.IO).launch {
            val bmp = CoverManager.downloadCover(this@MainActivity, item.id, url)
            withContext(Dispatchers.Main) {
                isLoadingState.value = false
                if (bmp != null) {
                    showEditCoverDialogState.value = false
                    loadInstalledApps()
                    Toast.makeText(this@MainActivity, "Capa atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Falha ao baixar imagem dessa URL.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Suporte Total a Controles Físicos / Gamepads ---

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event)
        }

        // Se algum diálogo estiver aberto, botão B fecha o diálogo
        if (showOptionsDialogState.value || showEditCoverDialogState.value ||
            showEditHeroDialogState.value || showEditLogoDialogState.value ||
            showInitialSetupDialogState.value ||
            showAddCloudDialogState.value || showAddGameOptionsDialogState.value ||
            showSteamGridDialogState.value || showIconPackDialogState.value ||
            showLauncherSettingsDialogState.value || showChangeAvatarDialogState.value) {
            if (event.keyCode == KeyEvent.KEYCODE_BUTTON_B || event.keyCode == KeyEvent.KEYCODE_BACK) {
                showOptionsDialogState.value = false
                showEditCoverDialogState.value = false
                showEditHeroDialogState.value = false
                showEditLogoDialogState.value = false
                showInitialSetupDialogState.value = false
                showAddCloudDialogState.value = false
                showAddGameOptionsDialogState.value = false
                showSteamGridDialogState.value = false
                showIconPackDialogState.value = false
                showLauncherSettingsDialogState.value = false
                showChangeAvatarDialogState.value = false
                return true
            }
        }

        // Se estiver no Modo Reordenar Itens
        if (isReorderModeState.value) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    swapSelectedItem(-1)
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    swapSelectedItem(1)
                    return true
                }
                KeyEvent.KEYCODE_BUTTON_A,
                KeyEvent.KEYCODE_BUTTON_B,
                KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    isReorderModeState.value = false
                    Toast.makeText(this, "Ordem salva com sucesso!", Toast.LENGTH_SHORT).show()
                    triggerHaptic()
                    return true
                }
            }
        }

        val folders = CategoryManager.groupIntoFolders(appsListState.value)
        val filtered = getFilteredApps()

        when (event.keyCode) {
            // EXCLUSIVAMENTE LB e RB trocam de abas:
            KeyEvent.KEYCODE_BUTTON_L1 -> {
                selectedTabState.intValue = (selectedTabState.intValue - 1).coerceAtLeast(0)
                selectedIndexState.intValue = 0
                activeFolderState.value = null
                focusAreaState.value = FocusArea.CAROUSEL
                triggerHaptic()
                return true
            }
            KeyEvent.KEYCODE_BUTTON_R1 -> {
                selectedTabState.intValue = (selectedTabState.intValue + 1).coerceAtMost(2)
                selectedIndexState.intValue = 0
                activeFolderState.value = null
                focusAreaState.value = FocusArea.CAROUSEL
                triggerHaptic()
                return true
            }

            // Botão Y: Abre diretamente o Catálogo de Jogos em Nuvem (Better xCloud) com pesquisa
            KeyEvent.KEYCODE_BUTTON_Y -> {
                showAddCloudDialogState.value = true
                triggerHaptic()
                return true
            }

            // Botão START / F1: Abre Configurações do Launcher
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_F1 -> {
                showLauncherSettingsDialogState.value = true
                triggerHaptic()
                return true
            }

            // Botão SELECT: Abre Opções do app/jogo focado
            KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_MENU -> {
                val current = if (selectedTabState.intValue == 2 && activeFolderState.value != null) {
                    activeFolderState.value?.apps?.getOrNull(selectedIndexState.intValue)
                } else {
                    filtered.getOrNull(selectedIndexState.intValue)
                }
                if (current != null) {
                    activeDialogAppState.value = current
                    showOptionsDialogState.value = true
                }
                triggerHaptic()
                return true
            }

            // D-PAD & Teclas direcionais
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                executeNavigation(JoystickDir.LEFT)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                executeNavigation(JoystickDir.RIGHT)
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                executeNavigation(JoystickDir.UP)
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                executeNavigation(JoystickDir.DOWN)
                return true
            }

            // Botão A / Confirmar: Abrir Pasta ou Iniciar App/Jogo
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (selectedTabState.intValue == 2) {
                    // Aba Apps
                    if (activeFolderState.value == null) {
                        val folder = folders.getOrNull(selectedIndexState.intValue)
                        if (folder != null) {
                            activeFolderState.value = folder
                            selectedIndexState.intValue = 0
                        }
                    } else {
                        val app = activeFolderState.value?.apps?.getOrNull(selectedIndexState.intValue)
                        if (app != null) launchItem(app)
                    }
                } else {
                    // Abas Jogos e Emuladores
                    val current = filtered.getOrNull(selectedIndexState.intValue)
                    if (current != null) launchItem(current)
                }
                triggerHaptic()
                return true
            }

            // Botão B: Voltar da pasta ou voltar da navegação
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_ESCAPE -> {
                if (activeFolderState.value != null) {
                    activeFolderState.value = null
                    selectedIndexState.intValue = 0
                    triggerHaptic()
                    return true
                }
                return true
            }

            // Botão X: Abre menu de Opções (Editar Capa, Informações, etc.)
            KeyEvent.KEYCODE_BUTTON_X -> {
                val current = if (selectedTabState.intValue == 2 && activeFolderState.value != null) {
                    activeFolderState.value?.apps?.getOrNull(selectedIndexState.intValue)
                } else {
                    filtered.getOrNull(selectedIndexState.intValue)
                }
                if (current != null) {
                    activeDialogAppState.value = current
                    showOptionsDialogState.value = true
                } else {
                    val fallback = appsListState.value.firstOrNull()
                    if (fallback != null) {
                        activeDialogAppState.value = fallback
                        showOptionsDialogState.value = true
                    }
                }
                triggerHaptic()
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val axisX = event.getAxisValue(MotionEvent.AXIS_X).takeIf { it != 0f }
            ?: event.getAxisValue(MotionEvent.AXIS_HAT_X)
        val axisY = event.getAxisValue(MotionEvent.AXIS_Y).takeIf { it != 0f }
            ?: event.getAxisValue(MotionEvent.AXIS_HAT_Y)

        val newDir = when {
            axisX > DEADZONE_ACTIVE -> JoystickDir.RIGHT
            axisX < -DEADZONE_ACTIVE -> JoystickDir.LEFT
            axisY > DEADZONE_ACTIVE -> JoystickDir.DOWN
            axisY < -DEADZONE_ACTIVE -> JoystickDir.UP
            kotlin.math.abs(axisX) < DEADZONE_RELEASE && kotlin.math.abs(axisY) < DEADZONE_RELEASE -> JoystickDir.NONE
            else -> currentJoyDir
        }

        if (newDir == JoystickDir.NONE) {
            if (currentJoyDir != JoystickDir.NONE) {
                currentJoyDir = JoystickDir.NONE
                joyRepeatJob?.cancel()
                joyRepeatJob = null
            }
            return super.onGenericMotionEvent(event)
        }

        if (newDir != currentJoyDir) {
            joyRepeatJob?.cancel()
            currentJoyDir = newDir

            executeNavigation(newDir, isAutoRepeat = false)

            joyRepeatJob = CoroutineScope(Dispatchers.Main).launch {
                delay(HOLD_THRESHOLD_MS)
                while (currentJoyDir == newDir) {
                    executeNavigation(newDir, isAutoRepeat = true)
                    delay(REPEAT_INTERVAL_MS)
                }
            }
            return true
        }

        return true
    }

    private fun executeNavigation(dir: JoystickDir, isAutoRepeat: Boolean = false): Boolean {
        val now = SystemClock.uptimeMillis()
        if (!isAutoRepeat && (now - lastNavTime < MIN_TAP_INTERVAL_MS)) {
            return false
        }
        lastNavTime = now

        if (isReorderModeState.value) {
            when (dir) {
                JoystickDir.LEFT -> {
                    swapSelectedItem(-1)
                    return true
                }
                JoystickDir.RIGHT -> {
                    swapSelectedItem(1)
                    return true
                }
                else -> return true
            }
        }

        val isAppsTab = selectedTabState.intValue == 2
        val isInsideFolder = isAppsTab && activeFolderState.value != null
        val folders = CategoryManager.groupIntoFolders(appsListState.value)

        val count = when {
            isInsideFolder -> activeFolderState.value?.apps?.size ?: 0
            isAppsTab -> folders.size
            else -> getFilteredApps().size
        }

        when (dir) {
            // Nota: Esquerda e Direita NÃO trocam abas (exclusivo para LB e RB conforme solicitado)
            JoystickDir.LEFT -> {
                if (count > 0) {
                    selectedIndexState.intValue = (selectedIndexState.intValue - 1).coerceAtLeast(0)
                    triggerHaptic()
                }
            }
            JoystickDir.RIGHT -> {
                if (count > 0) {
                    selectedIndexState.intValue = (selectedIndexState.intValue + 1).coerceAtMost(count - 1)
                    triggerHaptic()
                }
            }
            JoystickDir.UP -> {
                if (isInsideFolder && selectedIndexState.intValue >= GRID_COLUMNS) {
                    selectedIndexState.intValue -= GRID_COLUMNS
                    triggerHaptic()
                }
            }
            JoystickDir.DOWN -> {
                if (isInsideFolder && count > 0) {
                    val next = selectedIndexState.intValue + GRID_COLUMNS
                    if (next < count) {
                        selectedIndexState.intValue = next
                        triggerHaptic()
                    }
                }
            }
            JoystickDir.NONE -> {}
        }
        return true
    }
}
