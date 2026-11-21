package com.decklauncher

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.ComponentName
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
import android.os.StrictMode
import android.os.SystemClock
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.decklauncher.data.*
import com.decklauncher.shortcut.ShortcutManager
import com.decklauncher.ui.wizard.SetupWizardScreen
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {

    private var selectedIndexState = mutableIntStateOf(0)
    private var selectedTabState = mutableIntStateOf(0)
    private var focusAreaState = mutableStateOf(FocusArea.CAROUSEL)
    private var topBarIndexState = mutableIntStateOf(0) // 0: Perfil, 1: Add Game (+)
    private var appsListState = mutableStateOf<List<AppItem>>(emptyList())
    private var batteryState = mutableIntStateOf(100)
    private var activeFolderState = mutableStateOf<AppFolder?>(null)
    private var isLoadingState = mutableStateOf(true)

    // Dialog & Interaction states
    private var showOptionsDialogState = mutableStateOf(false)
    private var showGameMetadataDialogState = mutableStateOf(false)
    private var showSelectMusicDialogState = mutableStateOf(false)
    private var showEditCoverDialogState = mutableStateOf(false)
    private var showAddGameOptionsDialogState = mutableStateOf(false)
    private var showSteamGridDialogState = mutableStateOf(false)
    private var showLauncherSettingsDialogState = mutableStateOf(false)
    private var showChangeAvatarDialogState = mutableStateOf(false)
    private var isReorderModeState = mutableStateOf(false)
    private var profileNameState = mutableStateOf("Tiago")
    private var avatarIdState = mutableStateOf("deck_blue")
    private var activeDialogAppState = mutableStateOf<AppItem?>(null)
    private var backgroundModeState = mutableStateOf(BackgroundMode.IMMERSIVE)
    private var metadataUpdateCounter = mutableIntStateOf(0)
    private var buttonALongPressJob: Job? = null
    private var isButtonALongPressed = false

    private val isAnyDialogOpen: Boolean
        get() = showOptionsDialogState.value || showGameMetadataDialogState.value ||
            showSelectMusicDialogState.value ||
            showEditCoverDialogState.value || showEditHeroDialogState.value ||
            showEditLogoDialogState.value || showSelectIntroDialogState.value ||
            showInitialSetupDialogState.value ||
            showAddGameOptionsDialogState.value || showSteamGridDialogState.value ||
            showLauncherSettingsDialogState.value ||
            showChangeAvatarDialogState.value || showEmulatorGamesDialogState.value ||
            showIidbKeyDialogState.value

    private fun closeAllDialogs() {
        showOptionsDialogState.value = false
        showGameMetadataDialogState.value = false
        showSelectMusicDialogState.value = false
        showEditCoverDialogState.value = false
        showEditHeroDialogState.value = false
        showEditLogoDialogState.value = false
        showSelectIntroDialogState.value = false
        showInitialSetupDialogState.value = false
        showAddGameOptionsDialogState.value = false
        showSteamGridDialogState.value = false
        showIidbKeyDialogState.value = false
        showLauncherSettingsDialogState.value = false
        showChangeAvatarDialogState.value = false
        showEmulatorGamesDialogState.value = false
        activeEmulatorAppState.value = null
        activeDialogAppState.value = null
    }
    private var showIidbKeyDialogState = mutableStateOf(false)
    private var showEmulatorGamesDialogState = mutableStateOf(false)
    private var activeEmulatorAppState = mutableStateOf<AppItem?>(null)
    private var allScannedRomsState = mutableStateOf<List<AppItem>>(emptyList())
    private var pinnedGameIdsState = mutableStateOf<Set<String>>(emptySet())
    private var accentColorLongState = mutableLongStateOf(0xFF1A73E8)
    private var customWallpaperBitmapState = mutableStateOf<Bitmap?>(null)
    private var hasCustomWallpaperState = mutableStateOf(false)
    private var showEditHeroDialogState = mutableStateOf(false)
    private var showEditLogoDialogState = mutableStateOf(false)
    private var showSelectIntroDialogState = mutableStateOf(false)
    private var showInitialSetupDialogState = mutableStateOf(false)
    private var showWizardState = mutableStateOf(false)
    private var activeIntroPlayingState = mutableStateOf<ConsoleIntro?>(null)
    private var pendingLaunchAppState = mutableStateOf<AppItem?>(null)
    private var launchTransitionState = mutableStateOf(LaunchTransitionType.FADE)
    private var isLaunchingFadeState = mutableStateOf(false)
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
            lifecycleScope.launch(Dispatchers.Main) {
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

    private var lastFolderIndex = 0

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

        // Permite compartilhamento de URIs de arquivos com emuladores sem acionar FileUriExposedException
        val vmPolicy = StrictMode.VmPolicy.Builder().build()
        StrictMode.setVmPolicy(vmPolicy)

        profileNameState.value = ProfileManager.getProfileName(this)
        avatarIdState.value = ProfileManager.getAvatarId(this)
        backgroundModeState.value = ThemeManager.getBackgroundMode(this)
        accentColorLongState.longValue = ThemeManager.getAccentColorLong(this)
        hasCustomWallpaperState.value = ThemeManager.hasCustomWallpaper(this)
        if (hasCustomWallpaperState.value) {
            lifecycleScope.launch(Dispatchers.IO) {
                val bmp = ThemeManager.loadCustomWallpaper(this@MainActivity)
                withContext(Dispatchers.Main) {
                    customWallpaperBitmapState.value = bmp
                }
            }
        }
        romFolderNameState.value = SetupManager.getRomFolderName(this)
        downloadFolderNameState.value = SetupManager.getDownloadFolderName(this)
        apiKeyTextState.value = SteamGridDbManager.getApiKey(this)
        launchTransitionState.value = TransitionManager.getLaunchTransition(this)

        if (intent.getBooleanExtra("CLEAR_ALL_METADATA", false)) {
            clearAllAppsMetadata()
        }

        if (!SetupManager.isSetupCompleted(this)) {
            val hasExistingData = SetupManager.getRomFolderUri(this) != null || SteamGridDbManager.hasApiKey(this)
            if (!hasExistingData) {
                showWizardState.value = true
            } else {
                SetupManager.setSetupCompleted(this, true)
            }
        }

        setupImmersiveMode()
        requestHighestRefreshRate()

        // Migração v1.1: limpar preferência legada de pacote de ícones
        getSharedPreferences("iconpack_prefs", Context.MODE_PRIVATE).edit().clear().apply() // rule-ok: limpeza de migracao legada

        setContent {
            DeckLauncherTheme {
                if (showWizardState.value) {
                    SetupWizardScreen(
                        onComplete = {
                            showWizardState.value = false
                            loadInstalledApps()
                            Toast.makeText(this@MainActivity, "Configuração concluída! Biblioteca atualizada.", Toast.LENGTH_LONG).show()
                        },
                        onRequestPickRomFolder = {
                            try {
                                romFolderPickerLauncher.launch(null)
                            } catch (e: Exception) {
                                Toast.makeText(this@MainActivity, "Erro ao abrir seletor: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                } else {
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
                    showGameMetadataDialog = showGameMetadataDialogState.value,
                    showSelectMusicDialog = showSelectMusicDialogState.value,
                    showEditCoverDialog = showEditCoverDialogState.value,
                    showEditHeroDialog = showEditHeroDialogState.value,
                    showEditLogoDialog = showEditLogoDialogState.value,
                    showSelectIntroDialog = showSelectIntroDialogState.value,
                    showInitialSetupDialog = showInitialSetupDialogState.value,
                    activeIntroPlaying = activeIntroPlayingState.value,
                    onIntroFinished = {
                        val item = pendingLaunchAppState.value
                        pendingLaunchAppState.value = null
                        isLaunchingFadeState.value = true
                        activeIntroPlayingState.value = null
                        if (item != null) {
                            launchItem(item)
                        }
                    },
                    showAddGameOptionsDialog = showAddGameOptionsDialogState.value,
                    showSteamGridDialog = showSteamGridDialogState.value,
                    showIidbKeyDialog = showIidbKeyDialogState.value,
                    onDismissIidbKey = { showIidbKeyDialogState.value = false },
                    onRequestIidbKey = { showIidbKeyDialogState.value = true },
                    onSaveIidbKey = { key ->
                        IidbManager.setApiKey(this@MainActivity, key)
                        Toast.makeText(this@MainActivity, "Chave iiDB salva!", Toast.LENGTH_SHORT).show()
                    },
                    onRequestFetchIidb = { item -> fetchIidbAssets(item) },
                    showLauncherSettingsDialog = showLauncherSettingsDialogState.value,
                    showChangeAvatarDialog = showChangeAvatarDialogState.value,
                    showEmulatorGamesDialog = showEmulatorGamesDialogState.value,
                    activeEmulatorApp = activeEmulatorAppState.value,
                    allScannedRoms = allScannedRomsState.value,
                    pinnedGameIds = pinnedGameIdsState.value,
                    isReorderMode = isReorderModeState.value,
                    topBarIndex = topBarIndexState.intValue,
                    profileName = profileNameState.value,
                    avatarId = avatarIdState.value,
                    currentRomFolder = romFolderNameState.value,
                    currentDownloadFolder = downloadFolderNameState.value,
                    currentApiKey = apiKeyTextState.value,
                    backgroundMode = backgroundModeState.value,
                    accentColorLong = accentColorLongState.longValue,
                    customWallpaperBitmap = customWallpaperBitmapState.value,
                    hasCustomWallpaper = hasCustomWallpaperState.value,
                    currentLaunchTransition = launchTransitionState.value,
                    onLaunchTransitionSelected = { trans ->
                        launchTransitionState.value = trans
                        TransitionManager.setLaunchTransition(this@MainActivity, trans)
                    },
                    isLaunchingFade = isLaunchingFadeState.value,
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
                        lastFolderIndex = selectedIndexState.intValue
                        activeFolderState.value = folder
                        selectedIndexState.intValue = 0
                        triggerHaptic()
                    },
                    onCloseFolder = {
                        activeFolderState.value = null
                        selectedIndexState.intValue = lastFolderIndex
                        triggerHaptic()
                    },
                    onLaunchApp = { app ->
                        when (val route = com.decklauncher.model.LaunchRoute.resolve(app, isFromEmulatorDialog = false)) {
                            is com.decklauncher.model.LaunchRoute.OpenEmulatorDialog -> {
                                activeEmulatorAppState.value = route.item
                                showEmulatorGamesDialogState.value = true
                            }
                            is com.decklauncher.model.LaunchRoute.LaunchNow -> {
                                requestLaunchItem(route.item)
                            }
                        }
                    },
                    onOpenSettings = { openAppOptions(it) },
                    onUninstallApp = { uninstallApp(it) },
                    onDismissOptions = {
                        showOptionsDialogState.value = false
                        activeDialogAppState.value = null
                    },
                    onDismissGameMetadata = {
                        showGameMetadataDialogState.value = false
                        activeDialogAppState.value = null
                    },
                    onDismissSelectMusic = { showSelectMusicDialogState.value = false },
                    onDismissEditCover = { showEditCoverDialogState.value = false },
                    onDismissEditHero = { showEditHeroDialogState.value = false },
                    onDismissEditLogo = { showEditLogoDialogState.value = false },
                    onDismissSelectIntro = { showSelectIntroDialogState.value = false },
                    onDismissInitialSetup = { showInitialSetupDialogState.value = false },
                    onDismissAddGameOptions = { showAddGameOptionsDialogState.value = false },
                    onDismissSteamGrid = { showSteamGridDialogState.value = false },
                    onDismissLauncherSettings = { showLauncherSettingsDialogState.value = false },
                    onDismissChangeAvatar = { showChangeAvatarDialogState.value = false },
                    onDismissEmulatorGames = {
                        showEmulatorGamesDialogState.value = false
                        activeEmulatorAppState.value = null
                    },
                    onTogglePinGame = { game ->
                        val pinned = PinnedGamesManager.togglePin(this@MainActivity, game.id)
                        pinnedGameIdsState.value = PinnedGamesManager.getPinnedGameIds(this@MainActivity)
                        loadInstalledApps()
                        Toast.makeText(
                            this@MainActivity,
                            if (pinned) "${game.label} adicionado à Tela Inicial" else "${game.label} removido da Tela Inicial",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onLaunchRom = { requestLaunchItem(it) },
                    onLaunchEmulatorDirectly = { requestLaunchItem(it) },
                    onRequestManageMetadata = { item ->
                        activeDialogAppState.value = item
                        showGameMetadataDialogState.value = true
                    },
                    onRequestEditMusic = { item ->
                        activeDialogAppState.value = item
                        showSelectMusicDialogState.value = true
                    },
                    onDeleteAllGameMetadata = { item ->
                        deleteAllGameMetadata(item)
                    },
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
                    onRequestSelectIntro = { item ->
                        activeDialogAppState.value = item
                        showSelectIntroDialogState.value = true
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
                    onRequestRestartWizard = {
                        showWizardState.value = true
                    },
                    onCompleteSetup = {
                        SetupManager.setSetupCompleted(this@MainActivity, true)
                        showInitialSetupDialogState.value = false
                        loadInstalledApps()
                        Toast.makeText(this@MainActivity, "Configuração concluída! Biblioteca atualizada.", Toast.LENGTH_LONG).show()
                    },
                    onRequestAddGameOptions = {
                        showAddGameOptionsDialogState.value = true
                    },
                    onRequestSteamGridSettings = {
                        showSteamGridDialogState.value = true
                    },
                    onRequestOpenLauncherSettings = {
                        showLauncherSettingsDialogState.value = true
                    },
                    onRequestChangeAvatar = {
                        showChangeAvatarDialogState.value = true
                    },
                    onRequestReorderMode = {
                        isReorderModeState.value = true
                        Toast.makeText(this@MainActivity, "Modo Mover: D-Pad troca de posição", Toast.LENGTH_LONG).show()
                    },
                    onAvatarSelected = { avatarId ->
                        avatarIdState.value = avatarId
                        ProfileManager.setAvatarId(this@MainActivity, avatarId)
                    },
                    onProfileNameUpdated = { name ->
                        profileNameState.value = name
                        ProfileManager.setProfileName(this@MainActivity, name)
                    },
                    onArtworkProviderSelected = { provider ->
                        ArtworkProviderManager.setPreferredProvider(this@MainActivity, provider)
                        Toast.makeText(this@MainActivity, "Provedor padrão: ${provider.title}", Toast.LENGTH_SHORT).show()
                    },
                    onSaveSteamGridKey = { key ->
                        SteamGridDbManager.setApiKey(this, key)
                        Toast.makeText(this, "Chave SteamGridDB salva!", Toast.LENGTH_SHORT).show()
                    },
                    onClearAllMetadata = {
                        clearAllAppsMetadata()
                    },
                    onApplyCoverUrl = { item, url ->
                        downloadCoverFromUrl(item, url)
                    },
                    onApplyHeroUrl = { item, url ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val bmp = SteamGridDbManager.downloadAndApplyHero(this@MainActivity, item.id, url)
                            if (bmp != null) {
                                withContext(Dispatchers.Main) {
                                    metadataUpdateCounter.intValue++
                                    Toast.makeText(this@MainActivity, "Fundo 16:9 atualizado!", Toast.LENGTH_SHORT).show()
                                    showEditHeroDialogState.value = false
                                    loadInstalledApps()
                                }
                            }
                        }
                    },
                    onApplyLogoUrl = { item, url ->
                        lifecycleScope.launch(Dispatchers.IO) {
                            val bmp = SteamGridDbManager.downloadAndApplyLogo(this@MainActivity, item.id, url)
                            if (bmp != null) {
                                withContext(Dispatchers.Main) {
                                    metadataUpdateCounter.intValue++
                                    Toast.makeText(this@MainActivity, "Logo 3D atualizada com sucesso!", Toast.LENGTH_SHORT).show()
                                    showEditLogoDialogState.value = false
                                    loadInstalledApps()
                                }
                            }
                        }
                    },
                    onApplySoundbiteUrl = { item, url ->
                        downloadSoundbiteFromUrl(item, url)
                    },
                    onRemoveSoundbite = { item ->
                        removeSoundbite(item)
                    },
                    metadataUpdateCounter = metadataUpdateCounter.intValue,
                    onIntroSelected = { item, introId ->
                        IntroManager.setCustomIntroId(this@MainActivity, item.id, introId)
                        Toast.makeText(this@MainActivity, "Intro do console atualizada!", Toast.LENGTH_SHORT).show()
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
                        } else if (item.itemType == ItemType.SHORTCUT) {
                            ShortcutManager.removePinnedShortcut(this, item.id)
                            loadInstalledApps()
                            Toast.makeText(this, "Atalho removido da Tela Inicial", Toast.LENGTH_SHORT).show()
                        } else if (item.itemType == ItemType.ROM) {
                            PinnedGamesManager.unpinGame(this, item.id)
                            pinnedGameIdsState.value = PinnedGamesManager.getPinnedGameIds(this)
                            loadInstalledApps()
                            Toast.makeText(this, "${item.label} removido da Tela Inicial", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                }
            }
        }

        loadInstalledApps()
    }

    override fun onStart() {
        super.onStart()
        try {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val pkgFilter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            registerReceiver(packageReceiver, pkgFilter)
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        activeIntroPlayingState.value = null
        isLaunchingFadeState.value = false
        setupImmersiveMode()
        requestHighestRefreshRate()

        // Migração v1.1: limpar preferência legada de pacote de ícones
        getSharedPreferences("iconpack_prefs", Context.MODE_PRIVATE).edit().clear().apply() // rule-ok: limpeza de migracao legada
        ThemeMusicManager.resumeTheme()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("CLEAR_ALL_METADATA", false)) {
            clearAllAppsMetadata()
        }
        selectedTabState.intValue = 0
        selectedIndexState.intValue = 0
        activeFolderState.value = null
        focusAreaState.value = FocusArea.CAROUSEL
        closeAllDialogs()
    }

    override fun onPause() {
        super.onPause()
        joyRepeatJob?.cancel()
        currentJoyDir = JoystickDir.NONE
        ThemeMusicManager.pauseTheme()
    }

    override fun onStop() {
        super.onStop()
        activeIntroPlayingState.value = null
        ThemeMusicManager.stopTheme()
        try {
            unregisterReceiver(batteryReceiver)
            unregisterReceiver(packageReceiver)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        ThemeMusicManager.stopTheme()
    }

    @Suppress("DEPRECATION")
    private fun requestHighestRefreshRate() {
        try {
            val d = windowManager.defaultDisplay ?: return
            val cur = d.mode ?: return
            val best = d.supportedModes
                .filter { it.physicalWidth == cur.physicalWidth && it.physicalHeight == cur.physicalHeight }
                .maxByOrNull { it.refreshRate } ?: return
            window.attributes = window.attributes.apply { preferredDisplayModeId = best.modeId }
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

    private fun loadInstalledApps() {
        isLoadingState.value = true
        lifecycleScope.launch(Dispatchers.IO) {

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

                    // Identifica apps emuladores de forma abrangente e unificada
                    val isEmulator = IntroManager.isEmulator(pkg, label)

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

                    val coverPath = CoverManager.getCoverPath(this@MainActivity, appId)

                    val tempItem = AppItem(
                        id = appId,
                        label = label,
                        packageName = pkg,
                        activityName = activityName,
                        category = category,
                        itemType = ItemType.APP,
                        coverPath = coverPath
                    )

                    val folderCat = CategoryManager.categorizeApp(tempItem, info.activityInfo.applicationInfo)
                    tempItem.copy(folderCategory = folderCat)
                }

            // Jogos em Nuvem vão para Tab JOGOS
            val cloudGames = CloudGameManager.getCloudGames(this@MainActivity).map { cg ->
                cg.copy(category = AppCategory.JOGOS, coverPath = CoverManager.getCoverPath(this@MainActivity, cg.id))
            }

            // Atalhos Fixados de Nuvem (PinItemRequest / Better xCloud / GeForce Now / etc.)
            val pinnedShortcuts = ShortcutManager.getPinnedShortcuts(this@MainActivity).map { sc ->
                AppItem(
                    id = sc.id,
                    label = sc.label,
                    packageName = sc.packageName,
                    activityName = "",
                    category = AppCategory.JOGOS,
                    itemType = ItemType.SHORTCUT,
                    coverPath = CoverManager.getCoverPath(this@MainActivity, sc.id)
                )
            }

            // ROMs de Emulador: escaneia todos
            val romGames = RomScanner.scanRoms(this@MainActivity).map { rg ->
                rg.copy(category = AppCategory.JOGOS, coverPath = CoverManager.getCoverPath(this@MainActivity, rg.id))
            }

            // Apenas as ROMs selecionadas/fixadas pelo usuário vão para a Tela Inicial (Tab Jogos)
            val pinnedIds = PinnedGamesManager.getPinnedGameIds(this@MainActivity)
            val pinnedRomGames = romGames.filter { pinnedIds.contains(it.id) }

            val allJogos = (appItems.filter { it.category == AppCategory.JOGOS } + cloudGames + pinnedShortcuts + pinnedRomGames).sortedBy { it.label.lowercase() }
            val allEmuladores = appItems.filter { it.category == AppCategory.EMULADORES }.sortedBy { it.label.lowercase() }
            val allApps = appItems.filter { it.category == AppCategory.APPS }.sortedBy { it.label.lowercase() }

            val orderedJogos = OrderManager.applyOrder(this@MainActivity, "jogos", allJogos)
            val orderedEmuladores = OrderManager.applyOrder(this@MainActivity, "emuladores", allEmuladores)
            val orderedApps = OrderManager.applyOrder(this@MainActivity, "apps", allApps)

            val combined = orderedJogos + orderedEmuladores + orderedApps

            withContext(Dispatchers.Main) {
                appsListState.value = combined
                allScannedRomsState.value = romGames
                pinnedGameIdsState.value = pinnedIds
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

    private fun getLaunchBundle(): Bundle? {
        val trans = launchTransitionState.value
        return when (trans) {
            LaunchTransitionType.FADE -> {
                ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
            }
            LaunchTransitionType.ZOOM -> {
                val decor = window?.decorView
                if (decor != null && decor.width > 0 && decor.height > 0) {
                    ActivityOptions.makeScaleUpAnimation(decor, decor.width / 2, decor.height / 2, 0, 0).toBundle()
                } else {
                    ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
                }
            }
            LaunchTransitionType.INSTANT -> {
                ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
            }
            LaunchTransitionType.SLIDE -> null
        }
    }

    private fun applyPostLaunchTransition() {
        val trans = launchTransitionState.value
        when (trans) {
            LaunchTransitionType.FADE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
            LaunchTransitionType.INSTANT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(0, 0)
                }
            }
            LaunchTransitionType.ZOOM -> {}
            LaunchTransitionType.SLIDE -> {}
        }
    }

    private fun requestLaunchItem(item: AppItem) {
        ThemeMusicManager.stopTheme()
        val intro = IntroManager.detectIntroForApp(this, item, isLaunching = true)
        if (intro != null && intro.assetFileName != null) {
            activeIntroPlayingState.value = intro
            pendingLaunchAppState.value = item
        } else {
            val trans = launchTransitionState.value
            if (trans == LaunchTransitionType.FADE) {
                isLaunchingFadeState.value = true
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(220)
                    launchItem(item)
                }
            } else {
                launchItem(item)
            }
        }
    }

    private fun launchItem(item: AppItem) {
        val bundle = getLaunchBundle()
        when (item.itemType) {
            ItemType.CLOUD -> {
                val url = item.cloudUrl
                if (!url.isNullOrBlank()) {
                    try {
                        val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        if (bundle != null) startActivity(launchIntent, bundle) else startActivity(launchIntent)
                        applyPostLaunchTransition()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Não foi possível abrir o jogo na nuvem", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "URL de streaming inválida", Toast.LENGTH_SHORT).show()
                }
            }
            ItemType.SHORTCUT -> {
                val record = ShortcutManager.getPinnedShortcuts(this).firstOrNull { it.id == item.id }
                val ok = if (record != null) {
                    ShortcutManager.launchShortcut(this, record)
                } else false
                if (ok) {
                    applyPostLaunchTransition()
                } else {
                    Toast.makeText(this, "Não foi possível abrir o atalho de ${item.label}", Toast.LENGTH_SHORT).show()
                }
            }
            ItemType.ROM -> {
                val ok = RomScanner.launchRom(this, item, bundle)
                if (ok) {
                    applyPostLaunchTransition()
                } else {
                    Toast.makeText(this, "Instale o emulador para abrir: ${item.label}", Toast.LENGTH_LONG).show()
                }
            }
            ItemType.APP -> {
                var launchIntent = packageManager.getLaunchIntentForPackage(item.packageName)
                if (launchIntent == null) {
                    try {
                        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_LAUNCHER)
                            setPackage(item.packageName)
                        }
                        val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
                        val resolveInfo = resolveInfos.firstOrNull()
                        if (resolveInfo != null) {
                            launchIntent = Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_LAUNCHER)
                                component = ComponentName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("LauncherLaunch", "Falha ao consultar intent principal para ${item.packageName}", e)
                    }
                }
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        if (bundle != null) startActivity(launchIntent, bundle) else startActivity(launchIntent)
                        applyPostLaunchTransition()
                    } catch (e: Exception) {
                        android.util.Log.e("LauncherLaunch", "Erro ao executar startActivity para ${item.packageName}", e)
                        Toast.makeText(this, "Erro ao abrir ${item.label} (${item.packageName})", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    android.util.Log.e("LauncherLaunch", "Nenhuma atividade de lançamento encontrada para pacote: ${item.packageName}")
                    Toast.makeText(this, "Não foi possível iniciar ${item.label} (${item.packageName})", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openAppOptions(app: AppItem?) {
        ThemeMusicManager.pauseTheme()
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

    private fun fetchIidbAssets(item: AppItem) {
        if (!IidbManager.hasApiKey(this)) {
            Toast.makeText(this, "Configure sua Chave iiDB nas Configurações primeiro!", Toast.LENGTH_LONG).show()
            showIidbKeyDialogState.value = true
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Buscando artes e áudio no iiDB...", Toast.LENGTH_SHORT).show()
            }
            val ok = IidbManager.fetchAndApplyAllAssets(this@MainActivity, item.id, item.label)
            withContext(Dispatchers.Main) {
                if (ok) {
                    Toast.makeText(this@MainActivity, "Artes e música do iiDB aplicadas!", Toast.LENGTH_SHORT).show()
                    loadInstalledApps()
                    ThemeMusicManager.playTheme(this@MainActivity, item.id, item.label)
                } else {
                    Toast.makeText(this@MainActivity, "Nenhum resultado encontrado no iiDB para \"${item.label}\"", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deleteAllGameMetadata(item: AppItem) {
        CoverManager.deleteCover(this, item.id)
        HeroManager.deleteHero(this, item.id)
        LogoManager.deleteLogo(this, item.id)
        IidbManager.deleteSoundbite(this, item.id)
        ThemeMusicManager.stopTheme()
        metadataUpdateCounter.intValue++
        Toast.makeText(this, "Metadados de \"${item.label}\" restaurados!", Toast.LENGTH_SHORT).show()
        loadInstalledApps()
    }

    private fun clearAllAppsMetadata() {
        try {
            File(filesDir, "covers").deleteRecursively()
            File(filesDir, "heroes").deleteRecursively()
            File(filesDir, "logos").deleteRecursively()
            File(filesDir, "soundbites").deleteRecursively()
            File(cacheDir, "image_cache").deleteRecursively()
            File(filesDir, "covers").mkdirs()
            File(filesDir, "heroes").mkdirs()
            File(filesDir, "logos").mkdirs()
            File(filesDir, "soundbites").mkdirs()
        } catch (_: Exception) {}
        HeroManager.clearAllCaches()
        LogoManager.clearAllCaches()
        ThemeMusicManager.stopTheme()
        metadataUpdateCounter.intValue++
        loadInstalledApps()
        Toast.makeText(this, "Todos os metadados foram excluídos!", Toast.LENGTH_SHORT).show()
    }

    private fun downloadSoundbiteFromUrl(item: AppItem, url: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val f = IidbManager.downloadSoundbite(this@MainActivity, item.id, url, force = true)
            withContext(Dispatchers.Main) {
                if (f != null && f.exists()) {
                    ThemeMusicManager.playTheme(this@MainActivity, item.id, item.label)
                    metadataUpdateCounter.intValue++
                    Toast.makeText(this@MainActivity, "Música tema atualizada!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Erro ao baixar música tema.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun removeSoundbite(item: AppItem) {
        IidbManager.deleteSoundbite(this, item.id)
        ThemeMusicManager.stopTheme()
        metadataUpdateCounter.intValue++
        Toast.makeText(this, "Música tema removida!", Toast.LENGTH_SHORT).show()
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
        lifecycleScope.launch(Dispatchers.IO) {
            val bmp = CoverManager.downloadCover(this@MainActivity, item.id, url)
            withContext(Dispatchers.Main) {
                isLoadingState.value = false
                if (bmp != null) {
                    metadataUpdateCounter.intValue++
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
        // Se uma intro de boot estiver em execução, qualquer botão de ação ou retorno pula a intro
        if (activeIntroPlayingState.value != null && event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_BUTTON_A,
                KeyEvent.KEYCODE_BUTTON_B,
                KeyEvent.KEYCODE_BUTTON_X,
                KeyEvent.KEYCODE_BUTTON_Y,
                KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_ESCAPE,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_NUMPAD_ENTER,
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    val item = pendingLaunchAppState.value
                    pendingLaunchAppState.value = null
                    isLaunchingFadeState.value = true
                    activeIntroPlayingState.value = null
                    if (item != null) {
                        launchItem(item)
                    }
                    return true
                }
            }
            return true
        }

        // Se algum diálogo estiver aberto, mapeia DPAD e ações com foco completo
        if (isAnyDialogOpen) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (event.keyCode == KeyEvent.KEYCODE_BUTTON_B || event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    closeAllDialogs()
                    triggerHaptic()
                    return true
                }
            }
            if (event.keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                val centerEvent = KeyEvent(
                    event.downTime, event.eventTime, event.action,
                    KeyEvent.KEYCODE_DPAD_CENTER, event.repeatCount,
                    event.metaState, event.deviceId, event.scanCode,
                    event.flags, event.source
                )
                return super.dispatchKeyEvent(centerEvent)
            }
            if (event.keyCode in listOf(
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_TAB
            )) {
                return super.dispatchKeyEvent(event)
            }
            return true
        }

        // Se estiver no Modo Reordenar Itens
        if (isReorderModeState.value && event.action == KeyEvent.ACTION_DOWN) {
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

        // O clique no botão A abre/inicia imediatamente no ACTION_UP sem abrir menu ao segurar (Regra 1: Apenas START abre menu)
        if (focusAreaState.value == FocusArea.CAROUSEL) {
            if (event.keyCode in listOf(KeyEvent.KEYCODE_BUTTON_A, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER)) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    return true
                } else if (event.action == KeyEvent.ACTION_UP) {
                    handleConfirmPress()
                    return true
                }
            }
        }

        if (event.action != KeyEvent.ACTION_DOWN) {
            return super.dispatchKeyEvent(event)
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

            // Botão Y: No interior de pastas abre opções do app focado, na tela inicial abre opções de jogos
            KeyEvent.KEYCODE_BUTTON_Y -> {
                if (selectedTabState.intValue == 2 && activeFolderState.value != null) {
                    val app = activeFolderState.value?.apps?.getOrNull(selectedIndexState.intValue)
                    if (app != null) {
                        activeDialogAppState.value = app
                        showOptionsDialogState.value = true
                        triggerHaptic()
                        return true
                    }
                }
                showAddGameOptionsDialogState.value = true
                triggerHaptic()
                return true
            }

            // Botão START / F1: Abre SEMPRE o menu principal do launcher (Start Menu)
            KeyEvent.KEYCODE_BUTTON_START,
            KeyEvent.KEYCODE_F1 -> {
                showLauncherSettingsDialogState.value = true
                triggerHaptic()
                return true
            }

            // Botão SELECT / X / MENU: Abre Opções do app/jogo focado
            KeyEvent.KEYCODE_BUTTON_X,
            KeyEvent.KEYCODE_X,
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
                if (focusAreaState.value == FocusArea.TOP_BAR) {
                    topBarIndexState.intValue = (topBarIndexState.intValue - 1).coerceAtLeast(0)
                    triggerHaptic()
                    return true
                }
                executeNavigation(JoystickDir.LEFT)
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (focusAreaState.value == FocusArea.TOP_BAR) {
                    topBarIndexState.intValue = (topBarIndexState.intValue + 1).coerceAtMost(1)
                    triggerHaptic()
                    return true
                }
                executeNavigation(JoystickDir.RIGHT)
                return true
            }
            KeyEvent.KEYCODE_DPAD_UP -> {
                if (focusAreaState.value == FocusArea.CAROUSEL) {
                    focusAreaState.value = FocusArea.TOP_BAR
                    topBarIndexState.intValue = 0
                    triggerHaptic()
                    return true
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                if (focusAreaState.value == FocusArea.TOP_BAR) {
                    focusAreaState.value = FocusArea.CAROUSEL
                    triggerHaptic()
                    return true
                }
                executeNavigation(JoystickDir.DOWN)
                return true
            }

            // Botão A / Confirmar: Abrir Pasta, Iniciar App/Jogo ou Acionar Ação da TopBar
            KeyEvent.KEYCODE_BUTTON_A,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                handleConfirmPress()
                return true
            }

            // Botão B: Voltar da TopBar, da pasta ou voltar da navegação
            KeyEvent.KEYCODE_BUTTON_B,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_ESCAPE -> {
                if (focusAreaState.value == FocusArea.TOP_BAR) {
                    focusAreaState.value = FocusArea.CAROUSEL
                    triggerHaptic()
                    return true
                }
                if (activeFolderState.value != null) {
                    activeFolderState.value = null
                    selectedIndexState.intValue = lastFolderIndex
                    triggerHaptic()
                    return true
                }
                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.source and InputDevice.SOURCE_JOYSTICK != InputDevice.SOURCE_JOYSTICK ||
            event.action != MotionEvent.ACTION_MOVE) return super.onGenericMotionEvent(event)

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

            if (isAnyDialogOpen) {
                val keycode = when (newDir) {
                    JoystickDir.UP -> KeyEvent.KEYCODE_DPAD_UP
                    JoystickDir.DOWN -> KeyEvent.KEYCODE_DPAD_DOWN
                    JoystickDir.LEFT -> KeyEvent.KEYCODE_DPAD_LEFT
                    JoystickDir.RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT
                    JoystickDir.NONE -> 0
                }
                if (keycode != 0) {
                    val now = SystemClock.uptimeMillis()
                    val down = KeyEvent(now, now, KeyEvent.ACTION_DOWN, keycode, 0)
                    val up = KeyEvent(now, now, KeyEvent.ACTION_UP, keycode, 0)
                    dispatchKeyEvent(down)
                    dispatchKeyEvent(up)
                }
                return true
            }

            // 1. Executa 1 passo único com debounce anti-rebote
            executeNavigation(newDir, isAutoRepeat = false)

            // 2. Repetição acelerada (Fase 3.3): delay 350ms, intervalo 120ms acelerando até 45ms
            joyRepeatJob = lifecycleScope.launch(Dispatchers.Main) {
                delay(350)
                var interval = 120L
                while (currentJoyDir == newDir && isActive) {
                    executeNavigation(newDir, isAutoRepeat = true)
                    delay(interval)
                    interval = (interval - 10).coerceAtLeast(45)
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

        if (isAnyDialogOpen) return false

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
                if (focusAreaState.value == FocusArea.TOP_BAR) {
                    topBarIndexState.intValue = (topBarIndexState.intValue - 1).coerceAtLeast(0)
                    triggerHaptic()
                } else if (count > 0) {
                    selectedIndexState.intValue = (selectedIndexState.intValue - 1).coerceAtLeast(0)
                    triggerHaptic()
                }
            }
            JoystickDir.RIGHT -> {
                if (focusAreaState.value == FocusArea.TOP_BAR) {
                    topBarIndexState.intValue = (topBarIndexState.intValue + 1).coerceAtMost(1)
                    triggerHaptic()
                } else if (count > 0) {
                    selectedIndexState.intValue = (selectedIndexState.intValue + 1).coerceAtMost(count - 1)
                    triggerHaptic()
                }
            }
            JoystickDir.UP -> {
                if (focusAreaState.value == FocusArea.CAROUSEL) {
                    focusAreaState.value = FocusArea.TOP_BAR
                    topBarIndexState.intValue = 0
                    triggerHaptic()
                }
            }
            JoystickDir.DOWN -> {
                if (focusAreaState.value == FocusArea.TOP_BAR) {
                    focusAreaState.value = FocusArea.CAROUSEL
                    triggerHaptic()
                }
            }
            JoystickDir.NONE -> {}
        }
        return true
    }

    private fun handleConfirmPress() {
        if (focusAreaState.value == FocusArea.TOP_BAR) {
            when (topBarIndexState.intValue) {
                0 -> showLauncherSettingsDialogState.value = true
                1 -> showAddGameOptionsDialogState.value = true
            }
            triggerHaptic()
            return
        }
        val folders = CategoryManager.groupIntoFolders(appsListState.value)
        val filtered = getFilteredApps()
        if (selectedTabState.intValue == 2) {
            // Aba Apps
            if (activeFolderState.value == null) {
                val folder = folders.getOrNull(selectedIndexState.intValue)
                if (folder != null) {
                    lastFolderIndex = selectedIndexState.intValue
                    activeFolderState.value = folder
                    selectedIndexState.intValue = 0
                }
            } else {
                val app = activeFolderState.value?.apps?.getOrNull(selectedIndexState.intValue)
                if (app != null) requestLaunchItem(app)
            }
        } else {
            // Abas Jogos e Emuladores
            val current = filtered.getOrNull(selectedIndexState.intValue)
            if (current != null) {
                if (current.category == AppCategory.EMULADORES && current.itemType == ItemType.APP) {
                    activeEmulatorAppState.value = current
                    showEmulatorGamesDialogState.value = true
                } else {
                    requestLaunchItem(current)
                }
            }
        }
        triggerHaptic()
    }
}
