package com.decklauncher.ui.dialogs

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.decklauncher.data.*
import com.decklauncher.model.AppItem
import com.decklauncher.ui.components.VectorIconClose
import com.decklauncher.ui.components.VectorIconGamepad
import com.decklauncher.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun EmulatorGamesDrawer(
    emulatorApp: AppItem,
    allRoms: List<AppItem>,
    pinnedGameIds: Set<String>,
    onDismiss: () -> Unit,
    onLaunchEmulator: () -> Unit,
    onLaunchRom: (AppItem) -> Unit,
    onTogglePinGame: (AppItem) -> Unit,
    onRequestChangeArtwork: ((AppItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) }

    val emulatorRoms = remember(emulatorApp, allRoms) {
        RomScanner.getRomsForEmulator(emulatorApp.packageName, allRoms)
    }

    // Auto-fetch iiDB covers para jogos que ainda não têm capa local
    LaunchedEffect(emulatorApp.packageName, emulatorRoms.size) {
        isVisible = true
        if (IidbManager.hasApiKey(context)) {
            scope.launch(Dispatchers.IO) {
                for (rom in emulatorRoms) {
                    val cover = CoverManager.getCoverPath(context, rom.id)
                    if (cover == null) {
                        try {
                            val assets = IidbManager.getArtworkForParent(context, rom.id.hashCode().toLong())
                            if (assets?.boxartUrl != null) {
                                CoverManager.downloadCover(context, rom.id, assets.boxartUrl)
                            } else {
                                val search = IidbManager.searchGames(context, rom.label)
                                val first = search.firstOrNull()
                                if (first != null && first.boxartUrl != null) {
                                    CoverManager.downloadCover(context, rom.id, first.boxartUrl)
                                }
                            }
                        } catch (_: Exception) {}
                    }
                }
            }
        }
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor) // rule-ok: scrim padrao escurecido
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            NativeKeyEvent.KEYCODE_DPAD_UP -> {
                                selectedIndex = (selectedIndex - 3).coerceAtLeast(0)
                                true
                            }
                            NativeKeyEvent.KEYCODE_DPAD_DOWN -> {
                                selectedIndex = (selectedIndex + 3).coerceAtMost(emulatorRoms.size)
                                true
                            }
                            NativeKeyEvent.KEYCODE_DPAD_LEFT -> {
                                selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                                true
                            }
                            NativeKeyEvent.KEYCODE_DPAD_RIGHT -> {
                                selectedIndex = (selectedIndex + 1).coerceAtMost(emulatorRoms.size)
                                true
                            }
                            NativeKeyEvent.KEYCODE_BUTTON_A,
                            NativeKeyEvent.KEYCODE_ENTER,
                            NativeKeyEvent.KEYCODE_DPAD_CENTER -> {
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
                            NativeKeyEvent.KEYCODE_BUTTON_X,
                            NativeKeyEvent.KEYCODE_BUTTON_Y -> {
                                if (selectedIndex > 0) {
                                    val rom = emulatorRoms.getOrNull(selectedIndex - 1)
                                    if (rom != null) {
                                        onTogglePinGame(rom)
                                    }
                                }
                                true
                            }
                            NativeKeyEvent.KEYCODE_BUTTON_B,
                            NativeKeyEvent.KEYCODE_BACK,
                            NativeKeyEvent.KEYCODE_ESCAPE -> {
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
                        // Header com ícone do emulador e fechar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                val emuIcon = rememberAppIcon(emulatorApp)
                                if (emuIcon != null) {
                                    Image(
                                        bitmap = emuIcon,
                                        contentDescription = emulatorApp.label,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(NeutralBtnBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        VectorIconGamepad(tint = TextWhite)
                                    }
                                }

                                Column {
                                    Text(
                                        text = emulatorApp.label,
                                        color = TextWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${emulatorRoms.size} jogos encontrados",
                                        color = TextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
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

                        // Botão principal: Abrir App do Emulador
                        var isEmuBtnFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedIndex == 0 || isEmuBtnFocused) NeutralBtnBgFocused else NeutralBtnBg)
                                .border(
                                    width = if (selectedIndex == 0 || isEmuBtnFocused) 2.dp else 1.dp,
                                    color = if (selectedIndex == 0 || isEmuBtnFocused) FocusRing else CardBorderUnselected,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .onFocusChanged { isEmuBtnFocused = it.isFocused }
                                .clickable(role = Role.Button) {
                                    onLaunchEmulator()
                                    onDismiss()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Abrir App do Emulador",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Grade deslizante de Jogos com Capas/Ícones
                        if (emulatorRoms.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Nenhum jogo encontrado para este emulador.\nColoque suas ROMs na pasta configurada.",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(emulatorRoms) { index, rom ->
                                    val isFocused = selectedIndex == index + 1
                                    val isPinned = pinnedGameIds.contains(rom.id)

                                    RomGridCardItem(
                                        rom = rom,
                                        isFocused = isFocused,
                                        isPinned = isPinned,
                                        onClick = {
                                            onLaunchRom(rom)
                                            onDismiss()
                                        },
                                        onLongClick = {
                                            onTogglePinGame(rom)
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
}

@Composable
fun RomGridCardItem(
    rom: AppItem,
    isFocused: Boolean,
    isPinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val coverPath = remember(rom.id, rom.coverPath) {
        CoverManager.getCoverPath(context, rom.id) ?: rom.coverPath
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .scale(if (isFocused) 1.04f else 1.0f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isFocused) NeutralBtnBgFocused else NeutralBtnBg)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusRing else CardBorderUnselected,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (coverPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(coverPath))
                    .crossfade(false)
                    .build(),
                contentDescription = rom.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                VectorIconGamepad(
                    modifier = Modifier.size(36.dp),
                    tint = TextPrimary.copy(alpha = 0.5f)
                )
            }
        }

        // Rodapé com o nome do jogo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ColorBgDeep.copy(alpha = 0.88f))
                .padding(horizontal = 6.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rom.label,
                color = TextWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
