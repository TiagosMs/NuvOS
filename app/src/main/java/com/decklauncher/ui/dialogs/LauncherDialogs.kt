package com.decklauncher.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.decklauncher.data.*
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import com.decklauncher.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OptionsDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onLaunch: () -> Unit,
    onRequestEditCover: () -> Unit,
    onRequestAddGame: () -> Unit,
    onRequestSteamGridSettings: () -> Unit,
    onRequestIconPackSettings: () -> Unit,
    onRescanRoms: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onUninstallApp: () -> Unit,
    onRemoveItem: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(380.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF222222))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(22.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Text(
                    text = app.label,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (app.itemType) {
                        ItemType.CLOUD -> "Jogo em Nuvem (xCloud)"
                        ItemType.ROM -> "Jogo de Emulador (${app.romPath ?: ""})"
                        ItemType.APP -> "Aplicativo Android"
                    },
                    color = TabInactiveText,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Actions list
                OptionRow(label = "▶  Iniciar", onClick = { onDismiss(); onLaunch() })
                OptionRow(label = "🖼  Editar Capa no SteamGridDB", onClick = { onDismiss(); onRequestEditCover() })
                OptionRow(label = "➕  Adicionar Jogos (Y)", onClick = { onDismiss(); onRequestAddGame() })

                if (app.itemType == ItemType.APP) {
                    OptionRow(label = "ℹ️  Informações do Aplicativo", onClick = { onDismiss(); onOpenAppSettings() })
                    OptionRow(
                        label = "🗑️  Desinstalar Aplicativo",
                        color = Color(0xFFEF4444),
                        onClick = { onDismiss(); onUninstallApp() }
                    )
                } else {
                    OptionRow(
                        label = "❌  Remover da Tela Inicial",
                        color = Color(0xFFEF4444),
                        onClick = { onDismiss(); onRemoveItem() }
                    )
                }

                OptionRow(label = "🎨  Pacote de Ícones (iiSU style 1)", onClick = { onDismiss(); onRequestIconPackSettings() })
                OptionRow(label = "📁  Re-escanear Jogos/ROMs", onClick = { onDismiss(); onRescanRoms() })

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar", color = TextWhite)
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
                statusMessage = "Nenhum jogo encontrado com esse nome."
            }
            isLoading = false
        }
    }

    // Auto-search on open
    LaunchedEffect(Unit) {
        doSearch(searchTitle)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF222222))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🖼 Editar Capa - ${app.label}",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = DotGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = BtnAGreen),
                        shape = RoundedCornerShape(10.dp),
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
                        text = "Toque em uma capa para aplicar:",
                        color = TabInactiveText,
                        fontSize = 12.sp
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(results) { artwork ->
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(135.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp))
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
                                        modifier = Modifier.fillMaxSize().background(Color(0xFF333333)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                } else if (!isLoading) {
                    Text(
                        text = "Nenhuma capa encontrada para '$searchTitle'. Tente outro termo acima.",
                        color = TabInactiveText,
                        fontSize = 12.sp
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancelar", color = TabInactiveText)
                }
            }
        }
    }
}

@Composable
fun AddGameOptionsDialog(
    onDismiss: () -> Unit,
    onAddCloudGame: () -> Unit,
    onRescanRoms: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF222222))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "➕ Adicionar Jogos (Y)",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                OptionRow(
                    label = "☁️  Adicionar Jogo em Nuvem (xCloud)",
                    onClick = { onDismiss(); onAddCloudGame() }
                )

                OptionRow(
                    label = "📁  Escanear Pasta de ROMs (/sdcard/ROMs)",
                    onClick = { onDismiss(); onRescanRoms() }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar", color = TextWhite)
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

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF222222))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "🔑 Chave de API SteamGridDB",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Chave configurada para download de capas oficiais em alta definição (600x900):",
                    color = TabInactiveText,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = currentKey,
                    onValueChange = { currentKey = it },
                    label = { Text("Chave da API") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = BtnAGreen,
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
                        Text("Cancelar", color = TabInactiveText)
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

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(440.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF222222))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "🎨 Pacote de Ícones (iiSU style 1)",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { IconPackManager.openIconPackStudioLink(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "⚡ Abrir iiSU style 1 no Icon Pack Studio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Baixe ou exporte o tema para aplicar neste launcher",
                            color = Color(0xFFE9D5FF),
                            fontSize = 10.sp
                        )
                    }
                }

                HorizontalDivider(color = Color(0x22FFFFFF))

                Text(
                    text = "Pacotes instalados no dispositivo:",
                    color = TabInactiveText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                IconPackOptionRow(
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
                        color = Color(0xFF9CA3AF),
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    for (pack in installedPacks) {
                        IconPackOptionRow(
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
                    Text("Fechar", color = TabInactiveText)
                }
            }
        }
    }
}

@Composable
private fun IconPackOptionRow(
    name: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0x3322C55E) else Color(0xFF2A2A2A))
            .border(
                1.dp,
                if (isSelected) BtnAGreen else Color(0x22FFFFFF),
                RoundedCornerShape(10.dp)
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
                Text(text = subtitle, color = TabInactiveText, fontSize = 11.sp)
            }
            if (isSelected) {
                Text(text = "✓ Ativo", color = BtnAGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddCloudGameDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://www.xbox.com/play/games/") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(400.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF222222))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Adicionar Jogo em Nuvem (xCloud)",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome do Jogo (ex: Forza Horizon 5)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL do Streaming no xCloud") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TabInactiveText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && url.isNotBlank()) {
                                onAdd(title.trim(), url.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BtnAGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Adicionar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    label: String,
    color: Color = TextWhite,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2A2A2A))
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
