package com.decklauncher.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.decklauncher.data.*
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import com.decklauncher.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Paleta sóbria Steam Deck / SteamOS
private val SteamDeckBg = Color(0xFF1B2026)
private val SteamDeckCardBg = Color(0xFF222933)
private val SteamDeckBorder = Color(0x33FFFFFF)
private val SteamDeckTextMuted = Color(0xFF8A96A0)
private val SteamDeckButtonBlue = Color(0xFF1A73E8)

@Composable
fun OptionsDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onRequestEditCover: () -> Unit,
    onRequestEditHero: () -> Unit = {},
    onRequestAddGame: () -> Unit,
    onRequestSteamGridSettings: () -> Unit,
    onRequestIconPackSettings: () -> Unit,
    onRescanRoms: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onUninstallApp: () -> Unit,
    onRemoveItem: () -> Unit,
    onRequestReorder: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SteamDeckBg)
                .border(1.dp, SteamDeckBorder, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Column(
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

                // Actions list - Sóbrio, sem emojis
                SteamDeckOptionRow(label = "Iniciar", onClick = { onLaunch(); onDismiss() })
                SteamDeckOptionRow(label = "Mover Posição / Trocar de Lugar", onClick = { onRequestReorder(); onDismiss() })
                SteamDeckOptionRow(label = "Editar Capa (SteamGridDB)", onClick = { onRequestEditCover(); onDismiss() })
                SteamDeckOptionRow(label = "Editar Fundo / Hero (SteamGridDB)", onClick = { onRequestEditHero(); onDismiss() })
                SteamDeckOptionRow(label = "Adicionar Jogos em Nuvem (Better xCloud)", onClick = { onRequestAddGame(); onDismiss() })

                if (app.itemType == ItemType.APP) {
                    SteamDeckOptionRow(label = "Informações do Aplicativo", onClick = { onOpenAppSettings(); onDismiss() })
                    SteamDeckOptionRow(
                        label = "Desinstalar Aplicativo",
                        color = Color(0xFFEF4444),
                        onClick = { onUninstallApp(); onDismiss() }
                    )
                } else {
                    SteamDeckOptionRow(
                        label = "Remover da Tela Inicial",
                        color = Color(0xFFEF4444),
                        onClick = { onRemoveItem(); onDismiss() }
                    )
                }

                SteamDeckOptionRow(label = "Pacote de Ícones", onClick = { onRequestIconPackSettings(); onDismiss() })
                SteamDeckOptionRow(label = "Re-escanear Armazenamento", onClick = { onRescanRoms(); onDismiss() })

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27303D)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar", color = TextWhite, fontSize = 13.sp)
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
    var isLoading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<SteamGridArtwork>>(emptyList()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun doSearch(term: String) {
        coroutineScope.launch {
            isLoading = true
            statusMessage = "Buscando capas verticais no SteamGridDB..."
            val games = SteamGridDbManager.searchGames(context, term)
            if (games.isNotEmpty()) {
                val game = games.first()
                val grids = SteamGridDbManager.getGridsForGame(context, game.id)
                results = grids
                statusMessage = if (grids.isNotEmpty()) "${grids.size} capas encontradas para '${game.name}'" else "Nenhuma capa 600x900 para '${game.name}'"
            } else {
                results = emptyList()
                statusMessage = "Nenhum jogo encontrado com esse termo."
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        doSearch(searchTitle)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SteamDeckBg)
                .border(1.dp, SteamDeckBorder, RoundedCornerShape(12.dp))
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
                        shape = RoundedCornerShape(8.dp),
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SteamDeckBg)
                .border(1.dp, SteamDeckBorder, RoundedCornerShape(12.dp))
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

                SteamDeckOptionRow(
                    label = "Catálogo Xbox Cloud (Better xCloud)",
                    onClick = {
                        onOpenCloudCatalog()
                        onDismiss()
                    }
                )

                SteamDeckOptionRow(
                    label = "Escanear Pasta de ROMs (/sdcard/ROMs)",
                    onClick = { onDismiss(); onRescanRoms() }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27303D)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar", color = TextWhite)
                }
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
        Box(
            modifier = Modifier
                .width(480.dp)
                .height(520.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SteamDeckBg)
                .border(1.dp, SteamDeckBorder, RoundedCornerShape(12.dp))
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
                                .clip(RoundedCornerShape(8.dp))
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
                                        shape = RoundedCornerShape(8.dp)
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
        Box(
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SteamDeckBg)
                .border(1.dp, SteamDeckBorder, RoundedCornerShape(12.dp))
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
                        shape = RoundedCornerShape(8.dp)
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
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SteamDeckBg)
                .border(1.dp, SteamDeckBorder, RoundedCornerShape(12.dp))
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
                    shape = RoundedCornerShape(8.dp),
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
private fun SteamDeckOptionRow(
    label: String,
    color: Color = TextWhite,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SteamDeckCardBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
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
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0x3322C55E) else SteamDeckCardBg)
            .border(
                1.dp,
                if (isSelected) BtnAGreen else Color(0x22FFFFFF),
                RoundedCornerShape(8.dp)
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
    onAccentColorSelected: (Long) -> Unit = {}
) {
    val activePreset = remember(avatarId) {
        ProfileManager.PRESET_AVATARS.firstOrNull { it.id == avatarId } ?: ProfileManager.PRESET_AVATARS.first()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(440.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SteamDeckBg)
                .border(1.dp, SteamDeckBorder, RoundedCornerShape(12.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Configurações do Launcher",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Personalização, Sistema e Biblioteca",
                    color = SteamDeckTextMuted,
                    fontSize = 12.sp
                )

                HorizontalDivider(color = Color(0x22FFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "PERFIL",
                    color = Color(0xFF6B7D8E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SteamDeckCardBg)
                        .clickable {
                            onRequestChangeAvatar()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
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
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Column {
                            Text(
                                text = profileName,
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ícone: ${activePreset.name}",
                                color = SteamDeckTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "Trocar",
                        color = SteamDeckButtonBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "BIBLIOTECA & NAVEGAÇÃO",
                    color = Color(0xFF6B7D8E),
                    fontSize = 11.sp,
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) accentCol.copy(alpha = 0.25f) else SteamDeckCardBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) accentCol else Color(0x22FFFFFF),
                                        shape = RoundedCornerShape(8.dp)
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
                                .clip(RoundedCornerShape(8.dp))
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
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Cor de Destaque", color = SteamDeckTextMuted, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeManager.COLOR_PRESETS.forEach { preset ->
                            val isSelected = currentAccentColor == preset.colorValue
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.colorValue))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color(0x33FFFFFF),
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "INTEGRAÇÕES & SISTEMA",
                    color = Color(0xFF6B7D8E),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                SteamDeckOptionRow(
                    label = "Configurar Chave de API SteamGridDB",
                    onClick = {
                        onRequestSteamGridKey()
                        onDismiss()
                    }
                )

                SteamDeckOptionRow(
                    label = "Selecionar Pacote de Ícones",
                    onClick = {
                        onRequestIconPack()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27303D)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar", color = TextWhite, fontSize = 13.sp)
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
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SteamDeckBg)
                .border(1.dp, SteamDeckBorder, RoundedCornerShape(12.dp))
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
                        shape = RoundedCornerShape(8.dp),
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
                    shape = RoundedCornerShape(8.dp),
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
    var results by remember { mutableStateOf<List<SteamGridArtwork>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun doSearch(term: String) {
        coroutineScope.launch {
            isLoading = true
            statusMessage = "Buscando fundos 16:9 no SteamGridDB..."
            val games = SteamGridDbManager.searchGames(context, term)
            if (games.isNotEmpty()) {
                val game = games.first()
                val heroes = SteamGridDbManager.getHeroesForGame(context, game.id)
                results = heroes
                statusMessage = if (heroes.isNotEmpty()) "${heroes.size} fundos 16:9 encontrados para '${game.name}'" else "Nenhum fundo 16:9 para '${game.name}'"
            } else {
                results = emptyList()
                statusMessage = "Nenhum jogo encontrado com esse termo."
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        doSearch(searchTitle)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .width(620.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SteamDeckBg)
                .border(1.dp, SteamDeckBorder, RoundedCornerShape(12.dp))
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
                        shape = RoundedCornerShape(8.dp),
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
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
                .clip(RoundedCornerShape(14.dp))
                .background(SteamDeckBg)
                .border(1.5.dp, Color(0xFF2563EB), RoundedCornerShape(14.dp))
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(SteamDeckCardBg)
                            .border(1.dp, SteamDeckBorder, RoundedCornerShape(8.dp))
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(SteamDeckCardBg)
                            .border(1.dp, SteamDeckBorder, RoundedCornerShape(8.dp))
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
                    shape = RoundedCornerShape(8.dp)
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
