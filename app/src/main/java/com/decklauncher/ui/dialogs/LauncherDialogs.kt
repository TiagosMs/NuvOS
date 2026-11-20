package com.decklauncher.ui.dialogs

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.KeyEvent as AndroidKeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.decklauncher.data.*
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import com.decklauncher.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Paleta Frosted Glass (Vidro translúcido desfocado profundo)
private val SteamDeckBg = Color(0xC025282F) // ~75% opacidade para efeito de vidro fosco profundo
private val SteamDeckCardBg = Color(0x99181A1F) // ~60% opacidade para pílulas de vidro fosco
private val SteamDeckBorder = Color(0x28FFFFFF)
private val SteamDeckTextMuted = Color(0xFFD6D6D6)
private val SteamDeckButtonBlue = Color(0xFF1A73E8)

@Composable
fun EnableWindowBlur(blurRadius: Int = 100) {
    val view = LocalView.current
    SideEffect {
        val window = (view.parent as? DialogWindowProvider)?.window
        if (window != null) {
            window.setDimAmount(0.70f)
        }
    }
}

data class ArtworkCandidate(
    val id: Long,
    val url: String,
    val thumbUrl: String,
    val width: Int = 0,
    val height: Int = 0,
    val label: String? = null
)

private data class DialogActionItem(
    val label: String,
    val color: Color = TextWhite,
    val onClick: () -> Unit
)

@Composable
fun OptionsDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onRequestManageMetadata: () -> Unit = {},
    onRequestEditCover: () -> Unit,
    onRequestEditHero: () -> Unit = {},
    onRequestEditLogo: () -> Unit = {},
    onRequestSelectIntro: () -> Unit = {},
    onRequestFetchIidb: () -> Unit = {},
    onRequestAddGame: () -> Unit,
    onRequestSteamGridSettings: () -> Unit,
    onRequestIconPackSettings: () -> Unit,
    onRescanRoms: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onUninstallApp: () -> Unit,
    onRemoveItem: () -> Unit,
    onRequestReorder: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferredProvider = remember { ArtworkProviderManager.getPreferredProvider(context) }
    val actions = remember(app, preferredProvider) {
        val list = mutableListOf<DialogActionItem>()
        list.add(DialogActionItem("Iniciar") { onLaunch(); onDismiss() })
        list.add(DialogActionItem("Personalizar Metadados (Capa, Banner, Logo, Som)", Color(0xFF818CF8)) { onRequestManageMetadata(); onDismiss() })
        list.add(DialogActionItem("Mover Posição / Trocar de Lugar") { onRequestReorder(); onDismiss() })
        list.add(DialogActionItem("Baixar Tudo do iiDB (Capa, Hero, Logo, Áudio)", Color(0xFF60A5FA)) { onRequestFetchIidb(); onDismiss() })
        list.add(DialogActionItem("Editar Capa (${preferredProvider.shortName})") { onRequestEditCover(); onDismiss() })
        list.add(DialogActionItem("Editar Fundo / Hero (${preferredProvider.shortName})") { onRequestEditHero(); onDismiss() })
        list.add(DialogActionItem("Editar Logo 3D (${preferredProvider.shortName})") { onRequestEditLogo(); onDismiss() })
        list.add(DialogActionItem("Trocar Intro do Console") { onRequestSelectIntro(); onDismiss() })
        list.add(DialogActionItem("Adicionar Jogos em Nuvem (Better xCloud)") { onRequestAddGame(); onDismiss() })
        if (app.itemType == ItemType.APP) {
            list.add(DialogActionItem("Informações do Aplicativo") { onOpenAppSettings(); onDismiss() })
            list.add(DialogActionItem("Desinstalar Aplicativo", Color(0xFFEF4444)) { onUninstallApp(); onDismiss() })
        } else {
            list.add(DialogActionItem("Remover da Tela Inicial", Color(0xFFEF4444)) { onRemoveItem(); onDismiss() })
        }
        list.add(DialogActionItem("Pacote de Ícones") { onRequestIconPackSettings(); onDismiss() })
        list.add(DialogActionItem("Re-escanear Armazenamento") { onRescanRoms(); onDismiss() })
        list.add(DialogActionItem("Fechar") { onDismiss() })
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
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(180)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 20.dp, bottom = 20.dp)
                        .width(390.dp)
                        .fillMaxHeight(0.90f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(SteamDeckBg)
                        .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(26.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Header sóbrio Steam Deck
                        Text(
                            text = app.label,
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (app.itemType) {
                                ItemType.CLOUD -> "Jogo em Nuvem (Better xCloud)"
                                ItemType.ROM -> "Jogo de Emulador (${app.romPath ?: ""})"
                                ItemType.APP -> "Aplicativo Android"
                            },
                            color = SteamDeckTextMuted,
                            fontSize = 12.sp
                        )

                        HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                        // Actions list com foco nítido e seleção por controle
                        actions.forEachIndexed { idx, item ->
                            SteamDeckOptionRow(
                                label = item.label,
                                color = item.color,
                                isExternalFocused = selectedIndex == idx,
                                onClick = item.onClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditCoverSteamGridDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onApplyCoverUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchTitle by remember { mutableStateOf(app.label) }
    var activeProvider by remember { mutableStateOf(ArtworkProviderManager.getPreferredProvider(context)) }
    var isLoading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<ArtworkCandidate>>(emptyList()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun doSearch(term: String, provider: ArtworkProvider = activeProvider) {
        coroutineScope.launch {
            isLoading = true
            results = emptyList()
            if (provider == ArtworkProvider.STEAMGRID_DB) {
                statusMessage = "Buscando capas verticais no SteamGridDB..."
                val games = SteamGridDbManager.searchGames(context, term)
                if (games.isNotEmpty()) {
                    val game = games.first()
                    val grids = SteamGridDbManager.getGridsForGame(context, game.id)
                    results = grids.map {
                        ArtworkCandidate(
                            id = it.id,
                            url = it.url,
                            thumbUrl = it.thumbUrl,
                            width = it.width,
                            height = it.height,
                            label = it.style
                        )
                    }
                    statusMessage = if (grids.isNotEmpty()) "${grids.size} capas encontradas no SteamGridDB para '${game.name}'" else "Nenhuma capa no SteamGridDB para '${game.name}'"
                } else {
                    statusMessage = "Nenhum jogo encontrado no SteamGridDB."
                }
            } else {
                statusMessage = "Buscando capas oficiais no iiDB..."
                if (!IidbManager.hasApiKey(context)) {
                    statusMessage = "Configure sua Chave iiDB nas Configurações primeiro."
                    isLoading = false
                    return@launch
                }
                val games = IidbManager.searchGames(context, term)
                if (games.isNotEmpty()) {
                    val game = games.first()
                    val gallery = IidbManager.getGalleryForParent(context, game.id, "boxart")
                    val list = gallery.map {
                        ArtworkCandidate(
                            id = it.id,
                            url = it.url,
                            thumbUrl = it.thumbnailUrl ?: it.url,
                            label = it.displayName ?: "Box Art"
                        )
                    }.toMutableList()
                    if (list.isEmpty()) {
                        val assets = IidbManager.getArtworkForParent(context, game.id)
                        val boxartUrl = assets?.boxartUrl
                        if (!boxartUrl.isNullOrBlank()) {
                            list.add(
                                ArtworkCandidate(
                                    id = game.id,
                                    url = boxartUrl,
                                    thumbUrl = boxartUrl,
                                    label = "Box Art Oficial"
                                )
                            )
                        }
                    }
                    results = list
                    statusMessage = if (list.isNotEmpty()) "${list.size} capas encontradas no iiDB para '${game.name}'" else "Nenhuma capa no iiDB para '${game.name}'"
                } else {
                    statusMessage = "Nenhum jogo encontrado no iiDB."
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        doSearch(searchTitle, activeProvider)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SteamDeckBg)
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar Capa - ${app.label}",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = DotGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Seletor de Database / Provedor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArtworkProvider.values().forEach { prov ->
                        val isSel = activeProvider == prov
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSel) SteamDeckButtonBlue.copy(alpha = 0.35f) else SteamDeckCardBg)
                                .border(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) SteamDeckButtonBlue else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    activeProvider = prov
                                    doSearch(searchTitle, prov)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prov.title,
                                color = if (isSel) TextWhite else SteamDeckTextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchTitle,
                        onValueChange = { searchTitle = it },
                        label = { Text("Nome do Jogo") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = { doSearch(searchTitle) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27303D)),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isLoading
                    ) {
                        Text("Buscar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (statusMessage != null) {
                    Text(text = statusMessage!!, color = Color(0xFF22C55E), fontSize = 12.sp)
                }

                if (results.isNotEmpty()) {
                    Text(
                        text = "Selecione uma capa vertical:",
                        color = SteamDeckTextMuted,
                        fontSize = 12.sp
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(results) { artwork ->
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(135.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                                    .clickable {
                                        onApplyCoverUrl(artwork.url)
                                        onDismiss()
                                    }
                            ) {
                                var thumbBmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                                LaunchedEffect(artwork.thumbUrl) {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val url = java.net.URL(artwork.thumbUrl)
                                            val stream = url.openStream()
                                            thumbBmp = android.graphics.BitmapFactory.decodeStream(stream)
                                            stream.close()
                                        } catch (_: Exception) {}
                                    }
                                }

                                if (thumbBmp != null) {
                                    Image(
                                        bitmap = thumbBmp!!.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Color(0xFF27303D)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancelar", color = SteamDeckTextMuted)
                }
            }
        }
    }
}

@Composable
fun AddGameOptionsDialog(
    onDismiss: () -> Unit,
    onOpenCloudCatalog: () -> Unit,
    onRescanRoms: () -> Unit
) {
    val actions = remember {
        listOf(
            DialogActionItem("Catálogo Xbox Cloud (Better xCloud)") {
                onOpenCloudCatalog()
                onDismiss()
            },
            DialogActionItem("Escanear Pasta de ROMs (/sdcard/ROMs)") {
                onDismiss()
                onRescanRoms()
            },
            DialogActionItem("Fechar") {
                onDismiss()
            }
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    var isVisible by remember { mutableStateOf(false) }

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
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(180)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 20.dp, bottom = 20.dp)
                        .width(390.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(SteamDeckBg)
                        .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(26.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Adicionar Jogos",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selecione o tipo de jogo para adicionar:",
                            color = SteamDeckTextMuted,
                            fontSize = 12.sp
                        )

                        HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                        actions.forEachIndexed { idx, item ->
                            SteamDeckOptionRow(
                                label = item.label,
                                color = item.color,
                                isExternalFocused = selectedIndex == idx,
                                onClick = item.onClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmulatorGamesDialog(
    emulatorApp: AppItem,
    allRoms: List<AppItem>,
    pinnedGameIds: Set<String>,
    onDismiss: () -> Unit,
    onLaunchEmulator: () -> Unit,
    onLaunchRom: (AppItem) -> Unit,
    onTogglePinGame: (AppItem) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val emulatorRoms = remember(emulatorApp, allRoms) {
        RomScanner.getRomsForEmulator(emulatorApp.packageName, allRoms)
    }

    var selectedIndex by remember { mutableIntStateOf(if (emulatorRoms.isNotEmpty()) 1 else 0) }
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(selectedIndex) {
        val target = ((selectedIndex - 1).coerceAtLeast(0) * 68)
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
                                selectedIndex = (selectedIndex + 1).coerceAtMost(emulatorRoms.size)
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                if (selectedIndex == 0) {
                                    onLaunchEmulator()
                                    onDismiss()
                                } else {
                                    val rom = emulatorRoms.getOrNull(selectedIndex - 1)
                                    if (rom != null) {
                                        onLaunchRom(rom)
                                        onDismiss()
                                    }
                                }
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_X,
                            AndroidKeyEvent.KEYCODE_BUTTON_Y -> {
                                if (selectedIndex > 0) {
                                    val rom = emulatorRoms.getOrNull(selectedIndex - 1)
                                    if (rom != null) {
                                        onTogglePinGame(rom)
                                    }
                                }
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
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(180)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 20.dp, bottom = 20.dp)
                        .width(460.dp)
                        .fillMaxHeight(0.92f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(SteamDeckBg)
                        .border(
                            1.2.dp,
                            Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))),
                            RoundedCornerShape(26.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val emuIcon = com.decklauncher.data.rememberAppIcon(emulatorApp)
                            if (emuIcon != null) {
                                Image(
                                    bitmap = emuIcon,
                                    contentDescription = emulatorApp.label,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF333333)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎮", fontSize = 22.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = emulatorApp.label,
                                    color = TextWhite,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${emulatorRoms.size} jogos encontrados",
                                    color = SteamDeckTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Option 0: Abrir App do Emulador
                        SteamDeckOptionRow(
                            label = "Abrir App do Emulador",
                            color = Color(0xFF60A5FA),
                            isExternalFocused = selectedIndex == 0,
                            onClick = {
                                onLaunchEmulator()
                                onDismiss()
                            }
                        )

                        HorizontalDivider(
                            color = Color(0x22FFFFFF),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        if (emulatorRoms.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Nenhum jogo encontrado",
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Coloque seus arquivos na pasta de ROMs ou configure pelo menu de opções.",
                                    color = SteamDeckTextMuted,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(scrollState),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                emulatorRoms.forEachIndexed { idx, rom ->
                                    val isRowFocused = selectedIndex == idx + 1
                                    val isPinned = pinnedGameIds.contains(rom.id)

                                    EmulatorRomItemRow(
                                        rom = rom,
                                        isFocused = isRowFocused,
                                        isPinned = isPinned,
                                        onLaunch = {
                                            onLaunchRom(rom)
                                            onDismiss()
                                        },
                                        onTogglePin = {
                                            onTogglePinGame(rom)
                                        }
                                    )
                                }
                            }
                        }

                        // Controller Hints at bottom of Drawer
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x30000000))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎮 (A) Jogar  •  (X) Fixar na Home",
                                color = Color(0xFFD6D6D6),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "(B) Voltar",
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmulatorRomItemRow(
    rom: AppItem,
    isFocused: Boolean,
    isPinned: Boolean,
    onLaunch: () -> Unit,
    onTogglePin: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.02f else 1.0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "rom_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.5.dp else 1.dp,
        label = "rom_border_width"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x1AFFFFFF),
        label = "rom_border_color"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0x45FFFFFF) else SteamDeckCardBg,
        label = "rom_bg_color"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(18.dp))
            .clickable { onLaunch() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (rom.coverPath != null) {
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(java.io.File(rom.coverPath))
                        .size(100, 150)
                        .crossfade(false)
                        .build(),
                    contentDescription = rom.label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF262626)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎮", fontSize = 20.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rom.label,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val ext = rom.romPath?.substringAfterLast('.', "")?.uppercase() ?: "ROM"
                    Text(
                        text = ext,
                        color = Color(0xFFAAAAAA),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isPinned) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF059669))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Na Tela Inicial ✓",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isPinned) Color(0x35EF4444) else Color(0x3510B981))
                    .border(1.dp, if (isPinned) Color(0x80EF4444) else Color(0x8010B981), RoundedCornerShape(12.dp))
                    .clickable { onTogglePin() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isPinned) "Desfixar" else "+ Fixar",
                    color = if (isPinned) Color(0xFFFCA5A5) else Color(0xFF6EE7B7),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CloudGameCatalogDialog(
    onDismiss: () -> Unit,
    onSelectGame: (String, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val catalog = remember { CloudGameManager.XBOX_CLOUD_CATALOG }

    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            catalog
        } else {
            catalog.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .width(480.dp)
                .height(520.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SteamDeckBg)
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Catálogo Xbox Cloud Gaming",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecione um jogo para iniciar diretamente no Better xCloud:",
                    color = SteamDeckTextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Pesquisar jogo no catálogo") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredList) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SteamDeckCardBg)
                                .clickable {
                                    onSelectGame(item.title, item.url)
                                    onDismiss()
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = item.title,
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Xbox Cloud • Better xCloud",
                                        color = SteamDeckTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "Adicionar",
                                    color = BtnAGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Nenhum jogo encontrado para '$searchQuery'",
                                        color = SteamDeckTextMuted,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            val customUrl = "https://www.xbox.com/play/games/" + searchQuery.lowercase().replace(" ", "-")
                                            onSelectGame(searchQuery.trim(), customUrl)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BtnAGreen),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text("Adicionar '$searchQuery' mesmo assim", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = SteamDeckTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun SteamGridApiKeyDialog(
    onDismiss: () -> Unit,
    onSaveKey: (String) -> Unit
) {
    val context = LocalContext.current
    var currentKey by remember { mutableStateOf(SteamGridDbManager.getApiKey(context)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SteamDeckBg)
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Chave de API SteamGridDB",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Chave para download de capas oficiais em alta definição (600x900):",
                    color = SteamDeckTextMuted,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = currentKey,
                    onValueChange = { currentKey = it },
                    label = { Text("Chave da API") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = SteamDeckTextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveKey(currentKey.trim())
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BtnAGreen),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun IidbApiKeyDialog(
    onDismiss: () -> Unit,
    onSaveKey: (String) -> Unit
) {
    val context = LocalContext.current
    var currentKey by remember { mutableStateOf(IidbManager.getApiKey(context)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SteamDeckBg)
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Chave de API iiDB (iiSU Network)",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Gere sua chave pessoal em iisu.network para baixar capas oficiais, fundos 16:9, logos 3D e áudios tema.",
                    color = SteamDeckTextMuted,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = currentKey,
                    onValueChange = { currentKey = it },
                    label = { Text("Chave de API iiDB") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = SteamDeckTextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSaveKey(currentKey.trim())
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BtnAGreen),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Salvar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun IconPackDialog(
    onDismiss: () -> Unit,
    onSelectPack: (String?) -> Unit
) {
    val context = LocalContext.current
    val installedPacks = remember { IconPackManager.getInstalledIconPacks(context) }
    val currentPack = remember { IconPackManager.getSelectedPack(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SteamDeckBg)
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pacote de Ícones",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { IconPackManager.openIconPackStudioLink(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27303D)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Abrir iiSU style 1 no Icon Pack Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Baixe ou exporte o tema para aplicar neste launcher",
                            color = SteamDeckTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                HorizontalDivider(color = Color(0x22FFFFFF))

                Text(
                    text = "Pacotes instalados no dispositivo:",
                    color = SteamDeckTextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                SteamDeckIconPackRow(
                    name = "Padrão do Sistema",
                    subtitle = "Ícones originais dos aplicativos",
                    isSelected = currentPack == null,
                    onClick = {
                        onSelectPack(null)
                        onDismiss()
                    }
                )

                if (installedPacks.isEmpty()) {
                    Text(
                        text = "Nenhum pacote de ícones APK detectado ainda.\nAbra o link acima no Icon Pack Studio e clique em 'Exportar' para instalar.",
                        color = SteamDeckTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    for (pack in installedPacks) {
                        SteamDeckIconPackRow(
                            name = pack.name,
                            subtitle = if (pack.isIconPackStudio) "Icon Pack Studio (iiSU style)" else pack.packageName,
                            isSelected = currentPack == pack.packageName,
                            onClick = {
                                onSelectPack(pack.packageName)
                                onDismiss()
                            }
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Fechar", color = SteamDeckTextMuted)
                }
            }
        }
    }
}

@Composable
fun SteamDeckOptionRow(
    label: String,
    color: Color = TextWhite,
    isExternalFocused: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hasFocus by remember { mutableStateOf(false) }
    val isFocused = hasFocus || isExternalFocused

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.025f else 1.0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "option_scale"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.5.dp else 1.dp,
        label = "option_border_width"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color(0x1AFFFFFF),
        label = "option_border_color"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0x45FFFFFF) else SteamDeckCardBg,
        label = "option_bg_color"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .onFocusChanged { hasFocus = it.isFocused }
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (isFocused) Color.White else color,
                fontSize = 13.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold
            )
            if (isFocused) {
                // Indicador nítido de cursor focado
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun SteamDeckIconPackRow(
    name: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0x3322C55E) else SteamDeckCardBg)
            .border(
                1.dp,
                if (isSelected) BtnAGreen else Color(0x22FFFFFF),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = name, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = SteamDeckTextMuted, fontSize = 11.sp)
            }
            if (isSelected) {
                Text(text = "Ativo", color = BtnAGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LauncherSettingsDialog(
    onDismiss: () -> Unit,
    onRequestChangeAvatar: () -> Unit,
    onRequestSteamGridKey: () -> Unit,
    onRequestIidbKey: () -> Unit = {},
    onArtworkProviderSelected: (ArtworkProvider) -> Unit = {},
    onRequestIconPack: () -> Unit,
    onRequestAddCloudGame: () -> Unit,
    onRescanRoms: () -> Unit,
    onRequestReorderMode: () -> Unit,
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
    onLaunchTransitionSelected: (LaunchTransitionType) -> Unit = {}
) {
    val context = LocalContext.current
    val activePreset = remember(avatarId) {
        ProfileManager.PRESET_AVATARS.firstOrNull { it.id == avatarId } ?: ProfileManager.PRESET_AVATARS.first()
    }
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f)
                ) + fadeIn(animationSpec = tween(200)),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(180)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 20.dp, bottom = 20.dp)
                        .width(440.dp)
                        .fillMaxHeight(0.92f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(SteamDeckBg)
                        .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(26.dp))
                        .padding(22.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                // Seção Perfil
                Text(
                    text = "Perfil",
                    color = Color(0xFFD6D6D6),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                var isProfileFocused by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isProfileFocused = it.isFocused }
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isProfileFocused) Color(0x45FFFFFF) else SteamDeckCardBg)
                        .border(
                            width = if (isProfileFocused) 2.5.dp else 1.dp,
                            color = if (isProfileFocused) Color.White else Color(0x1AFFFFFF),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            onRequestChangeAvatar()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(activePreset.primaryColor),
                                            Color(activePreset.accentColor)
                                        )
                                    )
                                )
                                .border(1.5.dp, Color(0x66FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activePreset.initial,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column {
                            Text(
                                text = profileName,
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@Without_",
                                color = Color(0xFFAAAAAA),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "Trocar",
                        color = Color(0xFFCCCCCC),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Seção Biblioteca e Navegação
                Text(
                    text = "Biblioteca e Navegação",
                    color = Color(0xFFD6D6D6),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                SteamDeckOptionRow(
                    label = "Mover e Trocar Ordem dos Itens",
                    onClick = {
                        onRequestReorderMode()
                        onDismiss()
                    }
                )

                SteamDeckOptionRow(
                    label = "Adicionar Jogos em Nuvem (Better xCloud)",
                    onClick = {
                        onRequestAddCloudGame()
                        onDismiss()
                    }
                )

                SteamDeckOptionRow(
                    label = "Re-escanear ROMs e Emuladores",
                    onClick = {
                        onRescanRoms()
                        onDismiss()
                    }
                )

                SteamDeckOptionRow(
                    label = "Configurar Pastas de ISOs & Inicialização",
                    onClick = {
                        onRequestSetupFolders()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "APARÊNCIA & PERSONALIZAÇÃO",
                    color = Color(0xFF6B7D8E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                // Modo de Fundo
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Plano de Fundo", color = SteamDeckTextMuted, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BackgroundMode.values().forEach { mode ->
                            val isSelected = currentBgMode == mode
                            val accentCol = Color(currentAccentColor)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) accentCol.copy(alpha = 0.25f) else SteamDeckCardBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) accentCol else Color(0x22FFFFFF),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { onBgModeSelected(mode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (mode) {
                                        BackgroundMode.IMMERSIVE -> "Imersivo"
                                        BackgroundMode.WALLPAPER -> "Wallpaper"
                                        BackgroundMode.SOLID -> "Sóbrio"
                                    },
                                    color = if (isSelected) TextWhite else SteamDeckTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (currentBgMode == BackgroundMode.WALLPAPER) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SteamDeckCardBg)
                                .clickable { onRequestPickWallpaper() }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Escolher Imagem do Dispositivo",
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (hasCustomWallpaper) "Wallpaper ativo do armazenamento ✓" else "Nenhum arquivo selecionado",
                                    color = if (hasCustomWallpaper) Color(0xFF10B981) else SteamDeckTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "Selecionar",
                                color = Color(currentAccentColor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Cor do Tema (Accent Color)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("cor de destaque", color = Color(0xFFD6D6D6), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeManager.COLOR_PRESETS.forEach { preset ->
                            val isSelected = currentAccentColor == preset.colorValue
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.colorValue))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onAccentColorSelected(preset.colorValue) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (preset.colorValue == 0xFFFFFFFF) Color.Black else Color.White)
                                    )
                                }
                            }
                        }
                    }
                }

                // Intros dos Consoles ao Iniciar
                var introsActive by remember { mutableStateOf(IntroManager.isIntroEnabled(context)) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SteamDeckCardBg)
                        .clickable {
                            val newActive = !introsActive
                            introsActive = newActive
                            IntroManager.setIntroEnabled(context, newActive)
                        }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Intros de Boot dos Consoles",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (introsActive) "Vídeo oficial e áudio ao iniciar o jogo" else "Desativado (início imediato)",
                            color = if (introsActive) Color(0xFF10B981) else SteamDeckTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = if (introsActive) "Ativado" else "Desativado",
                        color = if (introsActive) Color(0xFF10B981) else SteamDeckTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Transição ao Abrir Jogos
                Text(
                    text = "TRANSIÇÃO AO ABRIR JOGOS",
                    color = Color(0xFF6B7D8E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LaunchTransitionType.values().forEach { trans ->
                        val isSelected = currentLaunchTransition == trans
                        val accentCol = Color(currentAccentColor)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) accentCol.copy(alpha = 0.22f) else SteamDeckCardBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) accentCol else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onLaunchTransitionSelected(trans) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = trans.title,
                                    color = TextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = trans.description,
                                    color = if (isSelected) Color(0xFF93C5FD) else SteamDeckTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Text(
                                    text = "Ativo",
                                    color = accentCol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "DATABASE DE ARTES & SISTEMA",
                    color = Color(0xFF6B7D8E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                // Provedor Padrão de Artes
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Provedor Padrão de Artes", color = SteamDeckTextMuted, fontSize = 11.sp)
                    var preferredProvider by remember { mutableStateOf(ArtworkProviderManager.getPreferredProvider(context)) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ArtworkProvider.values().forEach { provider ->
                            val isSelected = preferredProvider == provider
                            val accentCol = Color(currentAccentColor)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) accentCol.copy(alpha = 0.25f) else SteamDeckCardBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) accentCol else Color(0x22FFFFFF),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        preferredProvider = provider
                                        ArtworkProviderManager.setPreferredProvider(context, provider)
                                        onArtworkProviderSelected(provider)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = provider.title,
                                    color = if (isSelected) TextWhite else SteamDeckTextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                    Text(
                        text = when (preferredProvider) {
                            ArtworkProvider.STEAMGRID_DB -> "SteamGridDB ativo: foco em capas verticais 600x900 da comunidade."
                            ArtworkProvider.IIDB -> "iiDB ativo: banco oficial com capas, banners 16:9, logos 3D e áudios tema."
                        },
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                SteamDeckOptionRow(
                    label = "Configurar Chave de API SteamGridDB",
                    onClick = {
                        onRequestSteamGridKey()
                        onDismiss()
                    }
                )

                SteamDeckOptionRow(
                    label = "Configurar Chave iiDB (iiSU Network)",
                    onClick = {
                        onRequestIidbKey()
                        onDismiss()
                    }
                )

                var musicActive by remember { mutableStateOf(IidbManager.isThemeMusicEnabled(context)) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SteamDeckCardBg)
                        .clickable {
                            val newActive = !musicActive
                            musicActive = newActive
                            IidbManager.setThemeMusicEnabled(context, newActive)
                            if (!newActive) {
                                ThemeMusicManager.stopTheme()
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Música Tema ao Focar Jogo (iiDB)",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (musicActive) "Toca áudio oficial do jogo no carrossel" else "Desativado",
                            color = if (musicActive) Color(0xFF10B981) else SteamDeckTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = if (musicActive) "Ativado" else "Desativado",
                        color = if (musicActive) Color(0xFF10B981) else SteamDeckTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                SteamDeckOptionRow(
                    label = "Selecionar Pacote de Ícones",
                    onClick = {
                        onRequestIconPack()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                var isCloseFocused by remember { mutableStateOf(false) }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isCloseFocused) Color(0xFF384353) else Color(0xFF27303D)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(if (isCloseFocused) 2.5.dp else 0.dp, Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isCloseFocused = it.isFocused }
                ) {
                    Text("Fechar", color = TextWhite, fontSize = 13.sp, fontWeight = if (isCloseFocused) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
            }
        }
    }
}

@Composable
fun ChangeAvatarDialog(
    onDismiss: () -> Unit,
    onAvatarSelected: (String) -> Unit,
    onNameUpdated: (String) -> Unit
) {
    val context = LocalContext.current
    var currentName by remember { mutableStateOf(ProfileManager.getProfileName(context)) }
    var currentAvatarId by remember { mutableStateOf(ProfileManager.getAvatarId(context)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SteamDeckBg)
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Personalizar Perfil",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nome de Perfil", color = SteamDeckTextMuted, fontSize = 11.sp)
                    TextField(
                        value = currentName,
                        onValueChange = {
                            currentName = it
                            ProfileManager.setProfileName(context, it)
                            onNameUpdated(it)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SteamDeckCardBg,
                            unfocusedContainerColor = SteamDeckCardBg,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            cursorColor = SteamDeckButtonBlue,
                            focusedIndicatorColor = SteamDeckButtonBlue,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Text("Escolher Ícone de Perfil", color = SteamDeckTextMuted, fontSize = 11.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileManager.PRESET_AVATARS.forEach { preset ->
                        val isSelected = currentAvatarId == preset.id
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(preset.primaryColor),
                                            Color(preset.accentColor)
                                        )
                                    )
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color(0x44FFFFFF),
                                    shape = CircleShape
                                )
                                .clickable {
                                    currentAvatarId = preset.id
                                    ProfileManager.setAvatarId(context, preset.id)
                                    onAvatarSelected(preset.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.initial,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SteamDeckButtonBlue),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar e Fechar", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SteamGridHeroSelectDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onApplyHeroUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchTitle by remember { mutableStateOf(HeroManager.cleanTitleForSearch(app.label)) }
    var activeProvider by remember { mutableStateOf(ArtworkProviderManager.getPreferredProvider(context)) }
    var results by remember { mutableStateOf<List<ArtworkCandidate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun doSearch(term: String, provider: ArtworkProvider = activeProvider) {
        coroutineScope.launch {
            isLoading = true
            results = emptyList()
            if (provider == ArtworkProvider.STEAMGRID_DB) {
                statusMessage = "Buscando fundos 16:9 no SteamGridDB..."
                val games = SteamGridDbManager.searchGames(context, term)
                if (games.isNotEmpty()) {
                    val game = games.first()
                    val heroes = SteamGridDbManager.getHeroesForGame(context, game.id)
                    results = heroes.map {
                        ArtworkCandidate(
                            id = it.id,
                            url = it.url,
                            thumbUrl = it.thumbUrl,
                            width = it.width,
                            height = it.height,
                            label = it.style
                        )
                    }
                    statusMessage = if (heroes.isNotEmpty()) "${heroes.size} fundos 16:9 encontrados no SteamGridDB para '${game.name}'" else "Nenhum fundo 16:9 no SteamGridDB para '${game.name}'"
                } else {
                    statusMessage = "Nenhum jogo encontrado no SteamGridDB."
                }
            } else {
                statusMessage = "Buscando fundos 16:9 no iiDB..."
                if (!IidbManager.hasApiKey(context)) {
                    statusMessage = "Configure sua Chave iiDB nas Configurações primeiro."
                    isLoading = false
                    return@launch
                }
                val games = IidbManager.searchGames(context, term)
                if (games.isNotEmpty()) {
                    val game = games.first()
                    val banners = IidbManager.getGalleryForParent(context, game.id, "banner")
                    val heroes = IidbManager.getGalleryForParent(context, game.id, "hero")
                    val gallery = (banners + heroes).distinctBy { it.id }
                    val list = gallery.map {
                        ArtworkCandidate(
                            id = it.id,
                            url = it.url,
                            thumbUrl = it.thumbnailUrl ?: it.url,
                            label = it.displayName ?: "Fundo 16:9 Oficial"
                        )
                    }.toMutableList()
                    if (list.isEmpty()) {
                        val assets = IidbManager.getArtworkForParent(context, game.id)
                        val bannerUrl = assets?.bannerUrl
                        if (!bannerUrl.isNullOrBlank()) {
                            list.add(
                                ArtworkCandidate(
                                    id = game.id,
                                    url = bannerUrl,
                                    thumbUrl = bannerUrl,
                                    label = "Banner Oficial"
                                )
                            )
                        }
                    }
                    results = list
                    statusMessage = if (list.isNotEmpty()) "${list.size} fundos encontrados no iiDB para '${game.name}'" else "Nenhum fundo no iiDB para '${game.name}'"
                } else {
                    statusMessage = "Nenhum jogo encontrado no iiDB."
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        doSearch(searchTitle, activeProvider)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .width(620.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SteamDeckBg)
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selecionar Fundo 16:9 - ${app.label}",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = BtnAGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Seletor de Database / Provedor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArtworkProvider.values().forEach { prov ->
                        val isSel = activeProvider == prov
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSel) SteamDeckButtonBlue.copy(alpha = 0.35f) else SteamDeckCardBg)
                                .border(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) SteamDeckButtonBlue else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    activeProvider = prov
                                    doSearch(searchTitle, prov)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prov.title,
                                color = if (isSel) TextWhite else SteamDeckTextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchTitle,
                        onValueChange = { searchTitle = it },
                        label = { Text("Nome do Jogo") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = { doSearch(searchTitle) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27303D)),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isLoading
                    ) {
                        Text("Buscar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (statusMessage != null) {
                    Text(text = statusMessage!!, color = Color(0xFF22C55E), fontSize = 12.sp)
                }

                if (results.isNotEmpty()) {
                    Text(
                        text = "Selecione o plano de fundo desejado:",
                        color = SteamDeckTextMuted,
                        fontSize = 12.sp
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(results) { artwork ->
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(112.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                                    .clickable {
                                        onApplyHeroUrl(artwork.url)
                                        onDismiss()
                                    }
                            ) {
                                var thumbBmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                                LaunchedEffect(artwork.thumbUrl) {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val url = java.net.URL(artwork.thumbUrl)
                                            val stream = url.openStream()
                                            thumbBmp = android.graphics.BitmapFactory.decodeStream(stream)
                                            stream.close()
                                        } catch (_: Exception) {}
                                    }
                                }

                                if (thumbBmp != null) {
                                    Image(
                                        bitmap = thumbBmp!!.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF222933)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = SteamDeckButtonBlue,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = SteamDeckTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun SteamGridLogoSelectDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onApplyLogoUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchTitle by remember { mutableStateOf(HeroManager.cleanTitleForSearch(app.label)) }
    var activeProvider by remember { mutableStateOf(ArtworkProviderManager.getPreferredProvider(context)) }
    var results by remember { mutableStateOf<List<ArtworkCandidate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun doSearch(term: String, provider: ArtworkProvider = activeProvider) {
        coroutineScope.launch {
            isLoading = true
            results = emptyList()
            if (provider == ArtworkProvider.STEAMGRID_DB) {
                statusMessage = "Buscando logos transparentes no SteamGridDB..."
                val games = SteamGridDbManager.searchGames(context, term)
                if (games.isNotEmpty()) {
                    val game = games.first()
                    val logos = SteamGridDbManager.getLogosForGame(context, game.id)
                    results = logos.map {
                        ArtworkCandidate(
                            id = it.id,
                            url = it.url,
                            thumbUrl = it.thumbUrl,
                            width = it.width,
                            height = it.height,
                            label = it.style
                        )
                    }
                    statusMessage = if (logos.isNotEmpty()) "${logos.size} logos encontradas no SteamGridDB para '${game.name}'" else "Nenhuma logo no SteamGridDB para '${game.name}'"
                } else {
                    statusMessage = "Nenhum jogo encontrado no SteamGridDB."
                }
            } else {
                statusMessage = "Buscando logos oficiais no iiDB..."
                if (!IidbManager.hasApiKey(context)) {
                    statusMessage = "Configure sua Chave iiDB nas Configurações primeiro."
                    isLoading = false
                    return@launch
                }
                val games = IidbManager.searchGames(context, term)
                if (games.isNotEmpty()) {
                    val game = games.first()
                    val logos = IidbManager.getGalleryForParent(context, game.id, "logo")
                    val icons = IidbManager.getGalleryForParent(context, game.id, "icon")
                    val gallery = (logos + icons).distinctBy { it.id }
                    val list = gallery.map {
                        ArtworkCandidate(
                            id = it.id,
                            url = it.url,
                            thumbUrl = it.thumbnailUrl ?: it.url,
                            label = it.displayName ?: "Logo Oficial"
                        )
                    }.toMutableList()
                    if (list.isEmpty()) {
                        val assets = IidbManager.getArtworkForParent(context, game.id)
                        val logoUrl = assets?.logoUrl ?: assets?.iconUrl
                        if (!logoUrl.isNullOrBlank()) {
                            list.add(
                                ArtworkCandidate(
                                    id = game.id,
                                    url = logoUrl,
                                    thumbUrl = logoUrl,
                                    label = "Logo Oficial"
                                )
                            )
                        }
                    }
                    results = list
                    statusMessage = if (list.isNotEmpty()) "${list.size} logos encontradas no iiDB para '${game.name}'" else "Nenhuma logo no iiDB para '${game.name}'"
                } else {
                    statusMessage = "Nenhum jogo encontrado no iiDB."
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        doSearch(searchTitle, activeProvider)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .width(620.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SteamDeckBg)
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selecionar Logo 3D - ${app.label}",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = BtnAGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Seletor de Database / Provedor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ArtworkProvider.values().forEach { prov ->
                        val isSel = activeProvider == prov
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSel) SteamDeckButtonBlue.copy(alpha = 0.35f) else SteamDeckCardBg)
                                .border(
                                    width = if (isSel) 1.5.dp else 1.dp,
                                    color = if (isSel) SteamDeckButtonBlue else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    activeProvider = prov
                                    doSearch(searchTitle, prov)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prov.title,
                                color = if (isSel) TextWhite else SteamDeckTextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchTitle,
                        onValueChange = { searchTitle = it },
                        label = { Text("Nome do Jogo") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = { doSearch(searchTitle) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27303D)),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isLoading
                    ) {
                        Text("Buscar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (statusMessage != null) {
                    Text(text = statusMessage!!, color = Color(0xFF22C55E), fontSize = 12.sp)
                }

                if (results.isNotEmpty()) {
                    Text(
                        text = "Selecione a logo transparente oficial:",
                        color = SteamDeckTextMuted,
                        fontSize = 12.sp
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(results) { artwork ->
                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(95.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF161A20))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                                    .clickable {
                                        onApplyLogoUrl(artwork.url)
                                        onDismiss()
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                var thumbBmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                                LaunchedEffect(artwork.thumbUrl) {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val url = java.net.URL(artwork.thumbUrl)
                                            val stream = url.openStream()
                                            thumbBmp = android.graphics.BitmapFactory.decodeStream(stream)
                                            stream.close()
                                        } catch (_: Exception) {}
                                    }
                                }

                                if (thumbBmp != null) {
                                    Image(
                                        bitmap = thumbBmp!!.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    CircularProgressIndicator(
                                        color = SteamDeckButtonBlue,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = SteamDeckTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun InitialSetupDialog(
    currentRomFolder: String,
    currentDownloadFolder: String,
    currentApiKey: String,
    onRequestPickRomFolder: () -> Unit,
    onRequestPickDownloadFolder: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onCompleteSetup: () -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyText by remember { mutableStateOf(currentApiKey) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SteamDeckBg)
                .border(1.5.dp, Color(0xFF2563EB), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Configuração Inicial do Launcher",
                    color = TextWhite,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Defina suas pastas de jogos e chave de API para escanear títulos e capas automaticamente:",
                    color = SteamDeckTextMuted,
                    fontSize = 12.sp
                )

                HorizontalDivider(color = Color(0x22FFFFFF))

                // 1. Pasta das ISOs / ROMs
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "1. PASTA DE ISOS E ROMS",
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SteamDeckCardBg)
                            .border(1.dp, SteamDeckBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentRomFolder,
                            color = TextWhite,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Button(
                            onClick = onRequestPickRomFolder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Selecionar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. Pasta de Download / Salvar Jogos
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "2. PASTA PARA SALVAR JOGOS E DOWNLOADS",
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(SteamDeckCardBg)
                            .border(1.dp, SteamDeckBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentDownloadFolder,
                            color = TextWhite,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Button(
                            onClick = onRequestPickDownloadFolder,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Selecionar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 3. Chave de API SteamGridDB
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "3. CHAVE DE API STEAMGRIDDB",
                        color = Color(0xFF93C5FD),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        label = { Text("Chave da API") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Concluir e Iniciar
                Button(
                    onClick = {
                        onSaveApiKey(apiKeyText.trim())
                        onCompleteSetup()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Concluir e Iniciar Launcher",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SelectConsoleIntroDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onIntroSelected: (String?) -> Unit
) {
    val context = LocalContext.current
    var selectedId by remember { mutableStateOf(IntroManager.getCustomIntroId(context, app.id)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .width(440.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SteamDeckBg)
                .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Intro de Boot - ${app.label}",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecione o console cuja animação de boot com som tocará ao abrir este jogo:",
                    color = SteamDeckTextMuted,
                    fontSize = 12.sp
                )

                HorizontalDivider(color = Color(0x22FFFFFF))

                // Opção Automática (Padrão)
                val isAuto = selectedId == null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isAuto) Color(0xFF1E3A8A) else SteamDeckCardBg)
                        .clickable {
                            selectedId = null
                            IntroManager.setCustomIntroId(context, app.id, null)
                            onIntroSelected(null)
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Automático (Detectar pelo Console/ROM)",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val isDolphin = app.packageName.lowercase().contains("dolphin") || app.label.lowercase().contains("dolphin")
                        val detected = IntroManager.detectIntroForApp(context, app, isLaunching = false)
                        val displayDetected = if (isDolphin) "Wii / GameCube (Alternando a cada boot)" else (detected?.displayName ?: "Nenhuma")
                        Text(
                            text = "Atual: $displayDetected",
                            color = SteamDeckTextMuted,
                            fontSize = 11.sp
                        )
                    }
                    if (isAuto) {
                        Text("Ativo", color = Color(0xFF60A5FA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IntroManager.ALL_INTROS.forEach { intro ->
                    val isSelected = selectedId == intro.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF1E3A8A) else SteamDeckCardBg)
                            .clickable {
                                selectedId = intro.id
                                IntroManager.setCustomIntroId(context, app.id, intro.id)
                                onIntroSelected(intro.id)
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = intro.displayName,
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Text("Ativo", color = Color(0xFF60A5FA), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fechar", color = SteamDeckTextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun GameMetadataDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onRequestEditCover: () -> Unit,
    onRequestEditHero: () -> Unit,
    onRequestEditLogo: () -> Unit,
    onDeleteAll: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var activeProvider by remember {
        mutableStateOf(ArtworkProviderManager.getPreferredProvider(context))
    }
    var refreshKey by remember { mutableIntStateOf(0) }
    var isPlayingMusic by remember {
        mutableStateOf(ThemeMusicManager.isPlayingForApp(app.id))
    }
    var isDownloadingMusic by remember { mutableStateOf(false) }

    val coverBmp = remember(app.id, refreshKey) {
        CoverManager.loadLocalCover(context, app.id)
    }
    val heroBmp = remember(app.id, refreshKey) {
        HeroManager.loadLocalHero(context, app.id)
    }
    val logoBmp = remember(app.id, refreshKey) {
        LogoManager.loadLocalLogo(context, app.id)
    }
    val soundbiteFile = remember(app.id, refreshKey) {
        IidbManager.getSoundbiteFile(context, app.id)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(530.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF323337))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Previne dismiss ao tocar dentro do card
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. Top Provider Switcher (Capsule)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(Color(0xFF1E1E22))
                            .padding(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // IIDB Tab
                            val isIidb = activeProvider == ArtworkProvider.IIDB
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isIidb) Color(0xFFE2E2E6) else Color.Transparent)
                                    .clickable {
                                        activeProvider = ArtworkProvider.IIDB
                                        ArtworkProviderManager.setPreferredProvider(context, ArtworkProvider.IIDB)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "IIDB",
                                    color = if (isIidb) Color(0xFF1E1E22) else Color(0xFF9E9EA4),
                                    fontSize = 13.sp,
                                    fontWeight = if (isIidb) FontWeight.Bold else FontWeight.Medium
                                )
                            }

                            // SteamgridDB Tab
                            val isSgdb = activeProvider == ArtworkProvider.STEAMGRID_DB
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isSgdb) Color(0xFFE2E2E6) else Color.Transparent)
                                    .clickable {
                                        activeProvider = ArtworkProvider.STEAMGRID_DB
                                        ArtworkProviderManager.setPreferredProvider(context, ArtworkProvider.STEAMGRID_DB)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SteamgridDB",
                                    color = if (isSgdb) Color(0xFF1E1E22) else Color(0xFF9E9EA4),
                                    fontSize = 13.sp,
                                    fontWeight = if (isSgdb) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Main Content Grid (Two Columns)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Left Column: Icone (Cover)
                        var isIconFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .width(140.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF222226))
                                .border(
                                    width = if (isIconFocused) 2.dp else 0.dp,
                                    color = if (isIconFocused) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .onFocusChanged { isIconFocused = it.isFocused }
                                .focusable()
                                .clickable {
                                    onRequestEditCover()
                                }
                        ) {
                            if (coverBmp != null) {
                                Image(
                                    bitmap = coverBmp.asImageBitmap(),
                                    contentDescription = "Icone",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = app.label.take(2).uppercase(),
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Bottom Scrim & Label
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Color(0x99000000),
                                                Color(0xEE000000)
                                            )
                                        )
                                    )
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Icone",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Right Column: Banner on top, Musica + Logo 3D below
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top: Banner (Hero)
                            var isBannerFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF222226))
                                    .border(
                                        width = if (isBannerFocused) 2.dp else 0.dp,
                                        color = if (isBannerFocused) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .onFocusChanged { isBannerFocused = it.isFocused }
                                    .focusable()
                                    .clickable {
                                        onRequestEditHero()
                                    }
                            ) {
                                if (heroBmp != null) {
                                    Image(
                                        bitmap = heroBmp.asImageBitmap(),
                                        contentDescription = "Banner",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Sem banner",
                                            color = Color.White.copy(alpha = 0.35f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Bottom Scrim & Label
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    Color(0x99000000),
                                                    Color(0xEE000000)
                                                )
                                            )
                                        )
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Banner",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Bottom Row: Musica + Logo 3D
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Musica Card (Square)
                                var isMusicFocused by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF222226))
                                        .border(
                                            width = if (isMusicFocused) 2.dp else 0.dp,
                                            color = if (isMusicFocused) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .onFocusChanged { isMusicFocused = it.isFocused }
                                        .focusable()
                                        .clickable {
                                            if (soundbiteFile.exists() && soundbiteFile.length() > 0) {
                                                if (isPlayingMusic) {
                                                    ThemeMusicManager.stopTheme()
                                                    isPlayingMusic = false
                                                } else {
                                                    ThemeMusicManager.playTheme(context, app.id, app.label)
                                                    isPlayingMusic = true
                                                }
                                            } else {
                                                if (!IidbManager.hasApiKey(context)) {
                                                    Toast.makeText(context, "Configure a chave iiDB para baixar temas!", Toast.LENGTH_SHORT).show()
                                                } else if (!isDownloadingMusic) {
                                                    isDownloadingMusic = true
                                                    Toast.makeText(context, "Buscando áudio tema no iiDB...", Toast.LENGTH_SHORT).show()
                                                    coroutineScope.launch {
                                                        val ok = IidbManager.fetchSoundbiteOnly(context, app.id, app.label)
                                                        isDownloadingMusic = false
                                                        refreshKey++
                                                        if (ok) {
                                                            Toast.makeText(context, "Música baixada do iiDB!", Toast.LENGTH_SHORT).show()
                                                            ThemeMusicManager.playTheme(context, app.id, app.label)
                                                            isPlayingMusic = true
                                                        } else {
                                                            Toast.makeText(context, "Nenhuma música encontrada no iiDB.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        if (isDownloadingMusic) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(28.dp),
                                                color = Color.White,
                                                strokeWidth = 2.5.dp
                                            )
                                        } else {
                                            MetadataDiscIcon(isPlaying = isPlayingMusic)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Musica",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Logo 3D Card
                                var isLogoFocused by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF222226))
                                        .border(
                                            width = if (isLogoFocused) 2.dp else 0.dp,
                                            color = if (isLogoFocused) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .onFocusChanged { isLogoFocused = it.isFocused }
                                        .focusable()
                                        .clickable {
                                            onRequestEditLogo()
                                        }
                                ) {
                                    if (logoBmp != null) {
                                        Image(
                                            bitmap = logoBmp.asImageBitmap(),
                                            contentDescription = "Logo 3D",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 50.dp)
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                .align(Alignment.Center)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.Center),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Sem logo 3D",
                                                color = Color.White.copy(alpha = 0.35f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Logo 3D",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Bottom Button: "Deletar tudo" (Red Capsule)
                    var isDeleteFocused by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .width(190.dp)
                            .height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFDC2626))
                            .border(
                                width = if (isDeleteFocused) 2.dp else 0.dp,
                                color = if (isDeleteFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .onFocusChanged { isDeleteFocused = it.isFocused }
                            .focusable()
                            .clickable {
                                onDeleteAll()
                                refreshKey++
                                isPlayingMusic = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Deletar tudo",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataDiscIcon(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation = if (isPlaying) {
        val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "disc_rotation"
        ).value
    } else 0f

    Canvas(
        modifier = modifier
            .size(38.dp)
            .graphicsLayer(rotationZ = rotation)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f
        val innerHoleRadius = outerRadius * 0.28f
        val grooveRadius = outerRadius * 0.65f

        // Outer white disc
        drawCircle(
            color = Color(0xFFE2E2E6),
            radius = outerRadius,
            center = center
        )
        // Subtle vinyl groove line
        drawCircle(
            color = Color(0xFFB0B0B6),
            radius = grooveRadius,
            center = center,
            style = Stroke(width = 1.2f)
        )
        // Center hole
        drawCircle(
            color = Color(0xFF222226),
            radius = innerHoleRadius,
            center = center
        )
    }
}
