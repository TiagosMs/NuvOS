package com.decklauncher.ui.dialogs

import com.decklauncher.data.SoundEffectManager

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.decklauncher.data.*
import com.decklauncher.ui.components.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.decklauncher.data.*
import com.decklauncher.model.AppItem
import com.decklauncher.model.AppCategory
import com.decklauncher.model.ItemType
import com.decklauncher.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Paleta base usando tokens oficiais v1.1
private val SteamDeckBg = ColorBgDeep
private val SteamDeckCardBg = NeutralBtnBg
private val SteamDeckBorder = CardBorderUnselected
private val SteamDeckTextMuted = TextPrimary
private val SteamDeckButtonBlue = NeutralBtnBgFocused

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
    onRequestSteamGridSettings: () -> Unit,
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
        if (app.itemType == ItemType.APP) {
            list.add(DialogActionItem("Informações do Aplicativo") { onOpenAppSettings(); onDismiss() })
            list.add(DialogActionItem("Desinstalar Aplicativo", Color(0xFFEF4444)) { onUninstallApp(); onDismiss() })
        } else {
            list.add(DialogActionItem("Remover da Tela Inicial", Color(0xFFEF4444)) { onRemoveItem(); onDismiss() })
        }
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
                                ItemType.CLOUD -> "Jogo em Nuvem"
                                ItemType.SHORTCUT -> "Atalho de Jogo em Nuvem"
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
    var searchTitle by remember(app.id) { mutableStateOf(app.label) }
    var activeProvider by remember(app.id) { mutableStateOf(ArtworkProviderManager.getPreferredProvider(context)) }
    var isLoading by remember(app.id) { mutableStateOf(false) }
    var results by remember(app.id) { mutableStateOf<List<ArtworkCandidate>>(emptyList()) }
    var statusMessage by remember(app.id) { mutableStateOf<String?>(null) }

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

    LaunchedEffect(app.id) {
        doSearch(searchTitle, activeProvider)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        var isVisible by remember { mutableStateOf(false) }
        var selectedGridIndex by remember { mutableIntStateOf(0) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            isVisible = true
            focusRequester.requestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor)
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_DPAD_LEFT -> {
                                if (results.isNotEmpty()) {
                                    selectedGridIndex = (selectedGridIndex - 1).coerceAtLeast(0)
                                    SoundEffectManager.playNavMove()
                                    true
                                } else false
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (results.isNotEmpty()) {
                                    selectedGridIndex = (selectedGridIndex + 1).coerceAtMost(results.size - 1)
                                    SoundEffectManager.playNavMove()
                                    true
                                } else false
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_UP -> {
                                if (results.isNotEmpty()) {
                                    selectedGridIndex = (selectedGridIndex - 2).coerceAtLeast(0)
                                    SoundEffectManager.playNavMove()
                                    true
                                } else false
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_DOWN -> {
                                if (results.isNotEmpty()) {
                                    selectedGridIndex = (selectedGridIndex + 2).coerceAtMost(results.size - 1)
                                    SoundEffectManager.playNavMove()
                                    true
                                } else false
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                val item = results.getOrNull(selectedGridIndex)
                                if (item != null) {
                                    SoundEffectManager.playConfirm()
                                    onApplyCoverUrl(item.url)
                                    onDismiss()
                                    true
                                } else false
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
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
                        .width(540.dp)
                        .fillMaxHeight(0.92f)
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header com nome do item e fechar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Escolher Capa / Ícone",
                                    color = TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = app.label,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = FocusRing,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(NeutralBtnBg)
                                        .clickable { onDismiss() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    VectorIconClose(tint = TextWhite)
                                }
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
                                        .background(if (isSel) NeutralBtnBgFocused else NeutralBtnBg)
                                        .border(
                                            width = if (isSel) 1.5.dp else 1.dp,
                                            color = if (isSel) FocusRing else CardBorderUnselected,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            activeProvider = prov
                                            doSearch(searchTitle, prov)
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = prov.title,
                                        color = if (isSel) TextWhite else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Barra de busca
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchTitle,
                                onValueChange = { searchTitle = it },
                                label = { Text("Nome do Jogo / App") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = FocusRing,
                                    unfocusedBorderColor = CardBorderUnselected
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            Button(
                                onClick = { doSearch(searchTitle) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeutralBtnBgFocused),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isLoading
                            ) {
                                Text("Buscar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (statusMessage != null) {
                            Text(text = statusMessage!!, color = TextPrimary, fontSize = 12.sp)
                        }

                        // Grade quadrada com os ícones / capas encontrados
                        if (results.isEmpty() && !isLoading) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhuma arte encontrada.\nDigite o nome e toque em Buscar.",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                gridItemsIndexed(results) { index, artwork ->
                                    var isCandidateFocused by remember { mutableStateOf(false) }
                                    val isCandidateActive = isCandidateFocused || (results.indices.contains(selectedGridIndex) && results[selectedGridIndex] == artwork)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.0f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isCandidateActive) NeutralBtnBgFocused else NeutralBtnBg)
                                            .border(
                                                width = if (isCandidateActive) 2.5.dp else 1.dp,
                                                color = if (isCandidateActive) FocusRing else CardBorderUnselected,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .onFocusChanged { isCandidateFocused = it.isFocused }
                                            .focusable()
                                            .clickable {
                                                selectedGridIndex = index
                                                onApplyCoverUrl(artwork.url)
                                                onDismiss()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(artwork.thumbUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = artwork.label,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        if (!artwork.label.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .background(ColorBgDeep.copy(alpha = 0.85f))
                                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = artwork.label,
                                                    color = TextWhite,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
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
        }
    }
}

@Composable
fun AddGameOptionsDialog(
    onDismiss: () -> Unit,
    onRescanRoms: () -> Unit,
    allRoms: List<AppItem> = emptyList(),
    pinnedGameIds: Set<String> = emptySet(),
    onTogglePinGame: (AppItem) -> Unit = {}
) {
    var showRomPicker by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showRomPicker) {
        AddEmulatorGamePickerDialog(
            allRoms = allRoms,
            pinnedGameIds = pinnedGameIds,
            onDismiss = { showRomPicker = false },
            onTogglePin = onTogglePinGame
        )
        return
    }

    if (showAppPicker) {
        val installedApps = remember {
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val list = pm.queryIntentActivities(intent, 0)
            list.mapNotNull { resolveInfo ->
                val pkg = resolveInfo.activityInfo.packageName
                if (pkg == context.packageName) null
                else {
                    val label = resolveInfo.loadLabel(pm).toString()
                    AppItem(
                        id = "app_${pkg.lowercase()}",
                        label = label,
                        packageName = pkg,
                        activityName = resolveInfo.activityInfo.name,
                        category = AppCategory.APPS,
                        itemType = ItemType.APP,
                        coverPath = CoverManager.getCoverPath(context, "app_${pkg.lowercase()}")
                    )
                }
            }.sortedBy { it.label.lowercase() }
        }

        AddEmulatorGamePickerDialog(
            allRoms = installedApps,
            pinnedGameIds = pinnedGameIds,
            title = "Aplicativos Instalados",
            subtitle = "Selecione quais aplicativos fixar na Tela Inicial",
            emptyText = "Nenhum aplicativo encontrado.",
            onDismiss = { showAppPicker = false },
            onTogglePin = onTogglePinGame
        )
        return
    }

    val actions = remember {
        listOf(
            DialogActionItem("Adicionar Jogos de Emuladores à Tela Inicial") {
                showRomPicker = true
            },
            DialogActionItem("Adicionar Aplicativos à Tela Inicial") {
                showAppPicker = true
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
fun AddEmulatorGamePickerDialog(
    allRoms: List<AppItem>,
    pinnedGameIds: Set<String>,
    title: String = "Jogos de Emuladores",
    subtitle: String = "Selecione quais jogos fixar na Tela Inicial",
    emptyText: String = "Nenhuma ROM encontrada no armazenamento.\nColoque seus jogos em /sdcard/Rom.",
    onDismiss: () -> Unit,
    onTogglePin: (AppItem) -> Unit
) {
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
                .background(ScrimColor) // rule-ok: scrim escurecido padrao
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
                                selectedIndex = (selectedIndex + 1).coerceAtMost((allRoms.size - 1).coerceAtLeast(0))
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                val item = allRoms.getOrNull(selectedIndex)
                                if (item != null) onTogglePin(item)
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
                        .width(480.dp)
                        .fillMaxHeight(0.92f)
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = title,
                                    color = TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = subtitle,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NeutralBtnBg)
                                    .border(1.dp, CardBorderUnselected, RoundedCornerShape(12.dp))
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                VectorIconClose(tint = TextWhite)
                            }
                        }

                        HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 2.dp))

                        if (allRoms.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emptyText,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            val listState = rememberLazyListState()
                            LaunchedEffect(selectedIndex) {
                                listState.animateScrollToItem(selectedIndex.coerceAtLeast(0))
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(allRoms) { idx, rom ->
                                    val isFocused = selectedIndex == idx
                                    val isPinned = pinnedGameIds.contains(rom.id)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (isFocused) NeutralBtnBgFocused else NeutralBtnBg)
                                            .border(
                                                width = if (isFocused) 2.dp else 1.dp,
                                                color = if (isFocused) FocusRing else CardBorderUnselected,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { onTogglePin(rom) }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = rom.label,
                                                color = if (isFocused) TextWhite else NeutralBtnText,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = rom.romPath ?: "",
                                                color = TextPrimary.copy(alpha = 0.7f),
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isPinned) NeutralBtnBgFocused else Color(0xFF2A3240))
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isPinned) FocusRing else CardBorderUnselected,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isPinned) "Fixado" else "Adicionar",
                                                color = TextWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
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
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Voltar",
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
fun SteamGridApiKeyDialog(
    onDismiss: () -> Unit,
    onSaveKey: (String) -> Unit
) {
    val context = LocalContext.current
    var currentKey by remember { mutableStateOf(SteamGridDbManager.getApiKey(context)) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
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
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
                                onDismiss()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                SoundEffectManager.playConfirm()
                                onSaveKey(currentKey.trim())
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
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(400.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SteamDeckBg)
                    .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
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
}

@Composable
fun IidbApiKeyDialog(
    onDismiss: () -> Unit,
    onSaveKey: (String) -> Unit
) {
    val context = LocalContext.current
    var currentKey by remember { mutableStateOf(IidbManager.getApiKey(context)) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
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
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
                                onDismiss()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                SoundEffectManager.playConfirm()
                                onSaveKey(currentKey.trim())
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
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(420.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SteamDeckBg)
                    .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
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
    val coroutineScope = rememberCoroutineScope()
    var currentName by remember { mutableStateOf(ProfileManager.getProfileName(context)) }
    var currentAvatarId by remember { mutableStateOf(ProfileManager.getAvatarId(context)) }
    var selectedPresetIndex by remember {
        mutableIntStateOf(ProfileManager.PRESET_AVATARS.indexOfFirst { it.id == currentAvatarId }.coerceAtLeast(0))
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val ok = ProfileManager.saveCustomAvatarFromUri(context, uri)
                if (ok) {
                    currentAvatarId = ProfileManager.CUSTOM_AVATAR_ID
                    onAvatarSelected(ProfileManager.CUSTOM_AVATAR_ID)
                    Toast.makeText(context, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Erro ao carregar imagem da galeria", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val customAvatarBmp = remember(currentAvatarId, ProfileManager.avatarVersion) {
        if (ProfileManager.hasCustomAvatar(context)) ProfileManager.loadCustomAvatar(context) else null
    }
    val isCustomSelected = currentAvatarId == ProfileManager.CUSTOM_AVATAR_ID

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
                                val presets = ProfileManager.PRESET_AVATARS
                                if (selectedPresetIndex > 0) {
                                    selectedPresetIndex--
                                    val preset = presets[selectedPresetIndex]
                                    currentAvatarId = preset.id
                                    ProfileManager.setAvatarId(context, preset.id)
                                    onAvatarSelected(preset.id)
                                    SoundEffectManager.playNavMove()
                                }
                                true
                            }
                            AndroidKeyEvent.KEYCODE_DPAD_RIGHT -> {
                                val presets = ProfileManager.PRESET_AVATARS
                                if (selectedPresetIndex < presets.size - 1) {
                                    selectedPresetIndex++
                                    val preset = presets[selectedPresetIndex]
                                    currentAvatarId = preset.id
                                    ProfileManager.setAvatarId(context, preset.id)
                                    onAvatarSelected(preset.id)
                                    SoundEffectManager.playNavMove()
                                }
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_A,
                            AndroidKeyEvent.KEYCODE_ENTER,
                            AndroidKeyEvent.KEYCODE_DPAD_CENTER -> {
                                SoundEffectManager.playConfirm()
                                onDismiss()
                                true
                            }
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
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
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(420.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SteamDeckBg)
                    .border(1.2.dp, Brush.verticalGradient(listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))), RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
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

                Text("Foto de Perfil Personalizada", color = SteamDeckTextMuted, fontSize = 11.sp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isCustomSelected) Color(0x353B82F6) else SteamDeckCardBg)
                        .border(
                            width = if (isCustomSelected) 2.dp else 1.dp,
                            color = if (isCustomSelected) SteamDeckButtonBlue else Color(0x22FFFFFF),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { galleryLauncher.launch("image/*") }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF27303D)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (customAvatarBmp != null) {
                            Image(
                                bitmap = customAvatarBmp.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("🖼️", fontSize = 18.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCustomSelected && customAvatarBmp != null) "Foto da Galeria Ativa" else "Escolher Foto da Galeria",
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toque para abrir a galeria e cortar imagem",
                            color = SteamDeckTextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = SteamDeckButtonBlue),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Galeria", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text("Ou use um Ícone Predefinido (Use D-Pad):", color = SteamDeckTextMuted, fontSize = 11.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileManager.PRESET_AVATARS.forEachIndexed { index, preset ->
                        val isSelected = currentAvatarId == preset.id || selectedPresetIndex == index
                        Box(
                            modifier = Modifier
                                .size(44.dp)
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
                                    color = if (isSelected) Color.White else Color(0x33FFFFFF),
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedPresetIndex = index
                                    currentAvatarId = preset.id
                                    ProfileManager.setAvatarId(context, preset.id)
                                    onAvatarSelected(preset.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.initial,
                                color = Color.White,
                                fontSize = 16.sp,
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
}

@Composable
fun SteamGridHeroSelectDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onApplyHeroUrl: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchTitle by remember(app.id) { mutableStateOf(HeroManager.cleanTitleForSearch(app.label)) }
    var activeProvider by remember(app.id) { mutableStateOf(ArtworkProviderManager.getPreferredProvider(context)) }
    var results by remember(app.id) { mutableStateOf<List<ArtworkCandidate>>(emptyList()) }
    var isLoading by remember(app.id) { mutableStateOf(false) }
    var statusMessage by remember(app.id) { mutableStateOf<String?>(null) }

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

    LaunchedEffect(app.id) {
        doSearch(searchTitle, activeProvider)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        var isVisible by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            isVisible = true
            focusRequester.requestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor) // rule-ok: scrim escurecido padrao
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
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
                        .width(540.dp)
                        .fillMaxHeight(0.92f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(SteamDeckBg)
                        .border(1.2.dp, CardBorderUnselected, RoundedCornerShape(26.dp))
                        .padding(22.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
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
                            var isCandidateFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(112.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(
                                        width = if (isCandidateFocused) 3.dp else 1.dp,
                                        color = if (isCandidateFocused) Color.White else Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .onFocusChanged { isCandidateFocused = it.isFocused }
                                    .focusable()
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
    var searchTitle by remember(app.id) { mutableStateOf(HeroManager.cleanTitleForSearch(app.label)) }
    var activeProvider by remember(app.id) { mutableStateOf(ArtworkProviderManager.getPreferredProvider(context)) }
    var results by remember(app.id) { mutableStateOf<List<ArtworkCandidate>>(emptyList()) }
    var isLoading by remember(app.id) { mutableStateOf(false) }
    var statusMessage by remember(app.id) { mutableStateOf<String?>(null) }

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

    LaunchedEffect(app.id) {
        doSearch(searchTitle, activeProvider)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        var isVisible by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            isVisible = true
            focusRequester.requestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor) // rule-ok: scrim escurecido padrao
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
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
                        .width(540.dp)
                        .fillMaxHeight(0.92f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(SteamDeckBg)
                        .border(1.2.dp, CardBorderUnselected, RoundedCornerShape(26.dp))
                        .padding(22.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
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
                            var isCandidateFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(95.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF161A20))
                                    .border(
                                        width = if (isCandidateFocused) 3.dp else 1.dp,
                                        color = if (isCandidateFocused) Color.White else Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .onFocusChanged { isCandidateFocused = it.isFocused }
                                    .focusable()
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
        var isVisible by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            isVisible = true
            focusRequester.requestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor) // rule-ok: scrim escurecido padrao
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
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
                        .width(480.dp)
                        .fillMaxHeight(0.92f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(SteamDeckBg)
                        .border(1.2.dp, CardBorderUnselected, RoundedCornerShape(26.dp))
                        .padding(22.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
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
    }
}

@Composable
fun GameMetadataDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onRequestEditCover: () -> Unit,
    onRequestEditHero: () -> Unit,
    onRequestEditLogo: () -> Unit,
    onRequestEditMusic: () -> Unit,
    onRequestReorder: () -> Unit = {},
    onDeleteAll: () -> Unit,
    metadataUpdateCounter: Int = 0
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

    val coverBmp = remember(app.id, metadataUpdateCounter, refreshKey) {
        CoverManager.loadLocalCover(context, app.id)
    }
    val heroBmp = remember(app.id, metadataUpdateCounter, refreshKey) {
        HeroManager.loadLocalHero(context, app.id)
    }
    val logoBmp = remember(app.id, metadataUpdateCounter, refreshKey) {
        LogoManager.loadLocalLogo(context, app.id)
    }
    val soundbiteFile = remember(app.id, metadataUpdateCounter, refreshKey) {
        IidbManager.getSoundbiteFile(context, app.id)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        var isVisible by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            isVisible = true
            focusRequester.requestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor) // rule-ok: scrim escurecido padrao
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
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
                        .width(530.dp)
                        .fillMaxHeight(0.92f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(SteamDeckBg)
                        .border(1.2.dp, CardBorderUnselected, RoundedCornerShape(26.dp))
                        .padding(22.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
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
                            var isIidbFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isIidb) Color(0xFFE2E2E6) else Color.Transparent)
                                    .border(
                                        width = if (isIidbFocused) 2.dp else 0.dp,
                                        color = if (isIidbFocused) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .onFocusChanged { isIidbFocused = it.isFocused }
                                    .focusable()
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
                            var isSgdbFocused by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isSgdb) Color(0xFFE2E2E6) else Color.Transparent)
                                    .border(
                                        width = if (isSgdbFocused) 2.dp else 0.dp,
                                        color = if (isSgdbFocused) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .onFocusChanged { isSgdbFocused = it.isFocused }
                                    .focusable()
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
                                    width = if (isIconFocused) 2.5.dp else 0.dp,
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
                                        width = if (isBannerFocused) 2.5.dp else 0.dp,
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
                                            width = if (isMusicFocused) 2.5.dp else 0.dp,
                                            color = if (isMusicFocused) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .onFocusChanged { isMusicFocused = it.isFocused }
                                        .focusable()
                                        .clickable {
                                            onRequestEditMusic()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        MetadataDiscIcon(isPlaying = isPlayingMusic)
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
                                            width = if (isLogoFocused) 2.5.dp else 0.dp,
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

                    // 3. Bottom Buttons: "Mover Posição" e "Deletar tudo"
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var isMoveFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF2563EB))
                                .border(
                                    width = if (isMoveFocused) 2.5.dp else 0.dp,
                                    color = if (isMoveFocused) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .onFocusChanged { isMoveFocused = it.isFocused }
                                .focusable()
                                .clickable {
                                    onDismiss()
                                    onRequestReorder()
                                }
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Mover Posição",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        var isDeleteFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFDC2626))
                                .border(
                                    width = if (isDeleteFocused) 2.5.dp else 0.dp,
                                    color = if (isDeleteFocused) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .onFocusChanged { isDeleteFocused = it.isFocused }
                                .focusable()
                                .clickable {
                                    onDeleteAll()
                                    refreshKey++
                                    isPlayingMusic = false
                                }
                                .padding(horizontal = 18.dp),
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
    }
}

@Composable
fun SelectMusicDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onApplySoundbiteUrl: (String) -> Unit,
    onRemoveSoundbite: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchTitle by remember(app.id) { mutableStateOf(HeroManager.cleanTitleForSearch(app.label)) }
    var results by remember(app.id) { mutableStateOf<List<IidbArtwork>>(emptyList()) }
    var isLoading by remember(app.id) { mutableStateOf(false) }
    var previewPlayingUrl by remember(app.id) { mutableStateOf<String?>(null) }
    val previewPlayer = remember { android.media.MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            try {
                if (previewPlayer.isPlaying) previewPlayer.stop()
                previewPlayer.release()
            } catch (_: Exception) {}
        }
    }

    fun doSearch(term: String) {
        coroutineScope.launch {
            isLoading = true
            results = emptyList()
            try {
                results = IidbManager.searchSoundbites(context, term)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoading = false
        }
    }

    LaunchedEffect(app.id) {
        doSearch(searchTitle)
    }

    val currentSoundbiteFile = remember(app.id) { IidbManager.getSoundbiteFile(context, app.id) }
    val hasLocalSoundbite = currentSoundbiteFile.exists() && currentSoundbiteFile.length() > 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        EnableWindowBlur()
        var isVisible by remember { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            isVisible = true
            focusRequester.requestFocus()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor) // rule-ok: scrim escurecido padrao
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            AndroidKeyEvent.KEYCODE_BUTTON_B,
                            AndroidKeyEvent.KEYCODE_BACK,
                            AndroidKeyEvent.KEYCODE_ESCAPE -> {
                                SoundEffectManager.playMenuClose()
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
                        .width(520.dp)
                        .fillMaxHeight(0.92f)
                        .clip(RoundedCornerShape(26.dp))
                        .background(SteamDeckBg)
                        .border(1.2.dp, CardBorderUnselected, RoundedCornerShape(26.dp))
                        .padding(22.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Música Tema - ${app.label}",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "iiDB (iiSU Network)",
                                color = SteamDeckTextMuted,
                                fontSize = 12.sp
                            )
                        }
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color(0xFF60A5FA),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Search row
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

                    // Results
                    if (results.isNotEmpty()) {
                        Text(
                            text = "Selecione uma faixa para ouvir ou aplicar:",
                            color = SteamDeckTextMuted,
                            fontSize = 12.sp
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(results) { track ->
                                val isPreviewing = previewPlayingUrl == track.url
                                var isTrackFocused by remember { mutableStateOf(false) }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isPreviewing) Color(0xFF1E3A8A) else SteamDeckCardBg)
                                        .border(
                                            width = if (isTrackFocused) 2.5.dp else 1.dp,
                                            color = if (isTrackFocused) Color.White else Color(0x22FFFFFF),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .onFocusChanged { isTrackFocused = it.isFocused }
                                        .focusable()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        MetadataDiscIcon(isPlaying = isPreviewing, modifier = Modifier.size(32.dp))
                                        Column {
                                            Text(
                                                text = track.displayName ?: "Tema de Áudio",
                                                color = TextWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (track.durationMs != null && track.durationMs > 0) {
                                                val secs = (track.durationMs / 1000) % 60
                                                val mins = (track.durationMs / 1000) / 60
                                                Text(
                                                    text = String.format("%d:%02d", mins, secs),
                                                    color = SteamDeckTextMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Preview button
                                        Button(
                                            onClick = {
                                                if (isPreviewing) {
                                                    try {
                                                        if (previewPlayer.isPlaying) previewPlayer.stop()
                                                        previewPlayer.reset()
                                                    } catch (_: Exception) {}
                                                    previewPlayingUrl = null
                                                } else {
                                                    try {
                                                        if (previewPlayer.isPlaying) previewPlayer.stop()
                                                        previewPlayer.reset()
                                                        previewPlayer.setDataSource(track.url)
                                                        previewPlayer.isLooping = false
                                                        previewPlayer.setOnPreparedListener { it.start() }
                                                        previewPlayer.setOnCompletionListener { previewPlayingUrl = null }
                                                        previewPlayer.prepareAsync()
                                                        previewPlayingUrl = track.url
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Erro ao reproduzir prévia", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isPreviewing) Color(0xFFEF4444) else Color(0xFF3B82F6)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(
                                                text = if (isPreviewing) "Parar" else "Ouvir",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Apply button
                                        Button(
                                            onClick = {
                                                try {
                                                    if (previewPlayer.isPlaying) previewPlayer.stop()
                                                } catch (_: Exception) {}
                                                onApplySoundbiteUrl(track.url)
                                                onDismiss()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(
                                                text = "Aplicar",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!isLoading) {
                        Text(
                            text = "Nenhuma música encontrada no iiDB para \"$searchTitle\".",
                            color = SteamDeckTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Bottom actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (hasLocalSoundbite) {
                            TextButton(
                                onClick = {
                                    onRemoveSoundbite()
                                    onDismiss()
                                }
                            ) {
                                Text("Remover Música Atual", color = Color(0xFFEF4444), fontSize = 12.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        TextButton(onClick = onDismiss) {
                            Text("Fechar", color = SteamDeckTextMuted, fontSize = 13.sp)
                        }
                    }
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
