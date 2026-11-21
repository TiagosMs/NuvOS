package com.decklauncher.ui.wizard

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.*
import com.decklauncher.ui.dialogs.ConfirmActionDialog
import com.decklauncher.ui.theme.*

@Composable
fun SetupWizardScreen(
    onComplete: () -> Unit,
    onRequestPickRomFolder: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(SetupManager.getSavedSetupStep(context).coerceIn(0, 5)) }

    fun goToStep(step: Int) {
        val s = step.coerceIn(0, 5)
        currentStep = s
        SetupManager.saveSetupStep(context, s)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBgDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Nome e Indicador de Progresso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NuvOS",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configuração Inicial • Passo ${currentStep + 1} de 6",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }

                // Progress Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..5) {
                        val active = i == currentStep
                        val passed = i < currentStep
                        Box(
                            modifier = Modifier
                                .size(if (active) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        active -> FocusRing
                                        passed -> NeutralBtnBorder
                                        else -> NeutralGlyphBg
                                    }
                                )
                        )
                    }
                }
            }

            // Conteúdo Central
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn(tween(200)))
                                .togetherWith(slideOutHorizontally { width -> -width } + fadeOut(tween(150)))
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn(tween(200)))
                                .togetherWith(slideOutHorizontally { width -> width } + fadeOut(tween(150)))
                        }
                    },
                    label = "WizardStepTransition"
                ) { step ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(ColorSurfaceLow)
                            .border(1.2.dp, CardBorderUnselected, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when (step) {
                            0 -> StepWelcome { goToStep(1) }
                            1 -> StepProfile(context)
                            2 -> StepDefaultLauncher(context)
                            3 -> StepRomFolder(context, onRequestPickRomFolder)
                            4 -> StepArtworkDatabases(context)
                            5 -> StepFinish(context)
                        }
                    }
                }
            }

            // Footer: Controles Voltar / Pular / Avançar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    WizardButton(
                        text = "← Voltar",
                        onClick = { goToStep(currentStep - 1) },
                        isPrimary = false
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (currentStep < 5) {
                        WizardButton(
                            text = "Pular",
                            onClick = {
                                if (currentStep == 4) {
                                    goToStep(5)
                                } else {
                                    goToStep(currentStep + 1)
                                }
                            },
                            isPrimary = false
                        )
                        WizardButton(
                            text = "Avançar →",
                            onClick = { goToStep(currentStep + 1) },
                            isPrimary = true
                        )
                    } else {
                        WizardButton(
                            text = "Concluir e Iniciar",
                            onClick = {
                                SetupManager.setSetupCompleted(context, true)
                                onComplete()
                            },
                            isPrimary = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepWelcome(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Bem-vindo ao NuvOS",
            style = MaterialTheme.typography.displayMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "O seu launcher completo para jogos Android, emuladores retro e títulos em nuvem no formato console.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Vamos guiar você em 5 passos rápidos para configurar pastas, perfil e atalhos. Você pode pular qualquer etapa e alterar tudo depois nas configurações.",
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralBtnText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StepProfile(context: Context) {
    var profileName by remember { mutableStateOf(ProfileManager.getProfileName(context)) }
    var avatarId by remember { mutableStateOf(ProfileManager.getAvatarId(context)) }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Perfil do Jogador",
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Escolha seu nome e o estilo de avatar exibidos na tela inicial.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )

        OutlinedTextField(
            value = profileName,
            onValueChange = {
                profileName = it
                ProfileManager.setProfileName(context, it)
            },
            label = { Text("Nome do Jogador", color = TextPrimary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = FocusRing,
                unfocusedBorderColor = NeutralBtnBorder
            ),
            modifier = Modifier.fillMaxWidth(0.6f)
        )

        Text(
            text = "Avatares Predefinidos",
            style = MaterialTheme.typography.titleMedium,
            color = TextWhite
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileManager.PRESET_AVATARS.forEach { preset ->
                val isSelected = avatarId == preset.id
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(preset.primaryColor)) // rule-ok: cor do preset de avatar
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) FocusRing else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            avatarId = preset.id
                            ProfileManager.setAvatarId(context, preset.id)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.initial,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StepDefaultLauncher(context: Context) {
    val activity = context as? Activity
    var requested by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Launcher Padrão do Sistema",
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Por que isso é necessário?\nO Android exige que o app seja o launcher padrão para receber e fixar atalhos de jogos em nuvem enviados por navegadores e apps de streaming (Better xCloud, GeForce NOW, etc.).",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        WizardButton(
            text = if (requested) "Solicitação enviada (verificar)" else "Definir como Launcher Padrão",
            onClick = {
                requested = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && activity != null) {
                    val roleManager = activity.getSystemService(RoleManager::class.java)
                    if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                        activity.startActivity(intent)
                        return@WizardButton
                    }
                }
                try {
                    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, "Abra Configurações do Android › Apps padrão › Início", Toast.LENGTH_LONG).show()
                }
            },
            isPrimary = true
        )
    }
}

@Composable
private fun StepRomFolder(
    context: Context,
    onRequestPickRomFolder: () -> Unit
) {
    val currentPath = SetupManager.getRomFolderName(context)

    Column(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pasta de Jogos Retro (ROMs)",
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Selecione o diretório onde suas ROMs e ISOs estão guardadas. O NuvOS escaneará títulos automaticamente de acordo com as extensões suportadas pelos seus emuladores instalados.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(16.dp))
                .background(ColorSurfaceMid)
                .border(1.dp, CardBorderUnselected, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentPath,
                color = TextWhite,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        WizardButton(
            text = "Selecionar Pasta de ROMs (SAF)",
            onClick = onRequestPickRomFolder,
            isPrimary = true
        )
    }
}

@Composable
private fun StepArtworkDatabases(context: Context) {
    var sgdbKey by remember { mutableStateOf(SteamGridDbManager.getApiKey(context)) }
    var iidbKey by remember { mutableStateOf(IidbManager.getApiKey(context)) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Bancos de Dados de Artes (Opcional)",
            style = MaterialTheme.typography.headlineMedium,
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Chaves de API para baixar capas em alta resolução, logos 3D e banners oficiais. Os campos são mascarados e nunca são logados.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = sgdbKey,
            onValueChange = {
                sgdbKey = it
                SteamGridDbManager.setApiKey(context, it)
            },
            label = { Text("Chave SteamGridDB (steamgriddb.com/api)", color = TextPrimary) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = FocusRing,
                unfocusedBorderColor = NeutralBtnBorder
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(0.75f)
        )

        OutlinedTextField(
            value = iidbKey,
            onValueChange = {
                iidbKey = it
                IidbManager.setApiKey(context, it)
            },
            label = { Text("Chave iiDB iiSU Network", color = TextPrimary) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedBorderColor = FocusRing,
                unfocusedBorderColor = NeutralBtnBorder
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier.fillMaxWidth(0.75f)
        )
    }
}

@Composable
private fun StepFinish(context: Context) {
    val romFolder = SetupManager.getRomFolderName(context)
    val hasSgdb = SteamGridDbManager.hasApiKey(context)
    val hasIidb = IidbManager.hasApiKey(context)
    val profileName = ProfileManager.getProfileName(context)

    Column(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Tudo Pronto!",
            style = MaterialTheme.typography.headlineLarge,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Resumo da sua configuração:",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .clip(RoundedCornerShape(16.dp))
                .background(ColorSurfaceMid)
                .border(1.dp, CardBorderUnselected, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("👤 Jogador: $profileName", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
            Text("📂 ROMs: $romFolder", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
            Text("🎨 SteamGridDB: ${if (hasSgdb) "Configurado" else "Padrão"}", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
            Text("🎶 iiDB: ${if (hasIidb) "Configurado" else "Não configurado"}", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
        }

        Text(
            text = "Ao tocar em Concluir, o NuvOS iniciará o escaneamento dos seus jogos e aplicativos instalados.",
            style = MaterialTheme.typography.bodyMedium,
            color = NeutralBtnText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WizardButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .heightIn(min = 48.dp) // rule-ok: alvo de toque minimo 48dp
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    isFocused -> NeutralBtnBgFocused
                    isPrimary -> ColorSurfaceHigh
                    else -> NeutralBtnBg
                }
            )
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusRing else NeutralBtnBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isFocused || isPrimary) TextWhite else NeutralBtnText,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
