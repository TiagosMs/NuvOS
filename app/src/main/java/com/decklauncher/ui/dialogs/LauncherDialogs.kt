package com.decklauncher.ui.dialogs

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
    onRequestDownloadCover: () -> Unit,
    onRequestAddCloudGame: () -> Unit,
    onRequestSteamGridSettings: () -> Unit,
    onRequestIconPackSettings: () -> Unit,
    onRescanRoms: () -> Unit,
    onOpenAppSettings: () -> Unit,
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
                OptionRow(label = "🖼  Baixar Capa da Internet / SteamGridDB", onClick = { onDismiss(); onRequestDownloadCover() })
                OptionRow(label = "🔑  Configurar API SteamGridDB", onClick = { onDismiss(); onRequestSteamGridSettings() })
                OptionRow(label = "🎨  Pacote de Ícones (iiSU style 1)", onClick = { onDismiss(); onRequestIconPackSettings() })
                OptionRow(label = "☁  Adicionar Jogo em Nuvem", onClick = { onDismiss(); onRequestAddCloudGame() })
                OptionRow(label = "📁  Re-escanear Jogos/ROMs", onClick = { onDismiss(); onRescanRoms() })

                if (app.itemType == ItemType.APP) {
                    OptionRow(label = "⚙  Informações do Aplicativo", onClick = { onDismiss(); onOpenAppSettings() })
                } else {
                    OptionRow(
                        label = "❌  Remover da Tela Inicial",
                        color = Color(0xFFEF4444),
                        onClick = { onDismiss(); onRemoveItem() }
                    )
                }

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
    var currentKey by remember { mutableStateOf(SteamGridDbManager.getApiKey(context) ?: "") }

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
                    text = "Acesse steamgriddb.com/profile/preferences/api para gerar sua chave gratuita e cole-a abaixo:",
                    color = TabInactiveText,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = currentKey,
                    onValueChange = { currentKey = it },
                    label = { Text("Chave da API") },
                    placeholder = { Text("Cole aqui seu Bearer token / API key") },
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentKey.isNotBlank()) {
                        TextButton(
                            onClick = {
                                SteamGridDbManager.setApiKey(context, null)
                                currentKey = ""
                                onDismiss()
                            }
                        ) {
                            Text("Remover Chave", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar", color = TabInactiveText)
                        }
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

                // Highlight Button: Open iiSU style 1 link
                Button(
                    onClick = {
                        IconPackManager.openIconPackStudioLink(context)
                    },
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

                // Default system icons option
                IconPackOptionRow(
                    name = "Padrão do Sistema",
                    subtitle = "Ícones originais dos aplicativos",
                    isSelected = currentPack == null,
                    onClick = {
                        onSelectPack(null)
                        onDismiss()
                    }
                )

                // Installed Icon packs
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
fun DownloadCoverDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onSearchLibretro: (String) -> Unit,
    onDownloadUrl: (String) -> Unit,
    onRequestSteamGridSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val hasSteamKey = remember { SteamGridDbManager.hasApiKey(context) }

    var searchTitle by remember { mutableStateOf(app.label) }
    var customUrl by remember { mutableStateOf("") }
    var isSearchingSteamGrid by remember { mutableStateOf(false) }
    var steamGridResults by remember { mutableStateOf<List<SteamGridArtwork>>(emptyList()) }
    var searchStatus by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(480.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF222222))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🖼 Baixar Capa - ${app.label}",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                // Search field
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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // SteamGridDB search action
                if (hasSteamKey) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSearchingSteamGrid = true
                                searchStatus = "Pesquisando no SteamGridDB..."
                                val games = SteamGridDbManager.searchGames(context, searchTitle)
                                if (games.isNotEmpty()) {
                                    val topGame = games.first()
                                    val grids = SteamGridDbManager.getGridsForGame(context, topGame.id)
                                    steamGridResults = grids
                                    searchStatus = if (grids.isNotEmpty()) "${grids.size} capas encontradas para '${topGame.name}':" else "Nenhuma grade 600x900 para '${topGame.name}'"
                                } else {
                                    steamGridResults = emptyList()
                                    searchStatus = "Nenhum jogo encontrado no SteamGridDB."
                                }
                                isSearchingSteamGrid = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BtnAGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSearchingSteamGrid
                    ) {
                        Text(
                            text = if (isSearchingSteamGrid) "Buscando..." else "🔍 Buscar Capas no SteamGridDB (HD)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💡 Configure sua API SteamGridDB para capas 600x900",
                                color = TabInactiveText,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = onRequestSteamGridSettings) {
                                Text("Inserir Chave", color = BtnAGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // SteamGridDB Search Results Thumbnails
                if (searchStatus != null) {
                    Text(text = searchStatus!!, color = Color(0xFF22C55E), fontSize = 12.sp)
                }

                if (steamGridResults.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(steamGridResults) { artwork ->
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(8.dp))
                                    .clickable {
                                        onDownloadUrl(artwork.url)
                                    }
                            ) {
                                // Async thumbnail download preview
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
                                            .background(Color(0xFF333333)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Carregando...", color = TabInactiveText, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0x22FFFFFF))

                // Libretro Fallback button
                Button(
                    onClick = { onSearchLibretro(searchTitle) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Buscar no Banco Libretro (Grátis)", color = Color.White)
                }

                // Direct URL
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("Ou cole URL direta da imagem (PNG/JPG)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0x44FFFFFF)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (customUrl.isNotBlank()) {
                    Button(
                        onClick = { onDownloadUrl(customUrl.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = BtnXBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Baixar da URL", color = Color.White, fontWeight = FontWeight.Bold)
                    }
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
