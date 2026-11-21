package com.decklauncher.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.ui.theme.*

@Composable
fun BottomBar(
    isAppsTab: Boolean = false,
    isInsideFolder: Boolean = false,
    isReorderMode: Boolean = false,
    isEmulatorsTab: Boolean = false,
    onPrimaryAction: () -> Unit = {},
    onOptionsAction: () -> Unit = {},
    onAddGameAction: () -> Unit = {},
    onBackAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isCompactHeight) 20.dp else 32.dp,
                vertical = if (isCompactHeight) 6.dp else 18.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        if (isReorderMode) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 14.dp else 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mover Itens",
                    color = TextWhite,
                    fontSize = if (isCompactHeight) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                ControllerActionPrompt(
                    actionLabel = "Fixar / Salvar",
                    isCompactHeight = isCompactHeight
                )
                ControllerActionPrompt(
                    actionLabel = "Concluir",
                    isCompactHeight = isCompactHeight
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 14.dp else 22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControllerActionPrompt(
                    actionLabel = if (isAppsTab && !isInsideFolder) "Abrir" else if (isEmulatorsTab) "Ver Jogos" else "Jogar",
                    isCompactHeight = isCompactHeight,
                    onClick = onPrimaryAction
                )
                ControllerActionPrompt(
                    actionLabel = "Opções",
                    isCompactHeight = isCompactHeight,
                    onClick = onOptionsAction
                )
                if (!isAppsTab) {
                    ControllerActionPrompt(
                        actionLabel = "Adicionar Jogos",
                        isCompactHeight = isCompactHeight,
                        onClick = onAddGameAction
                    )
                }
                if (isAppsTab && isInsideFolder) {
                    ControllerActionPrompt(
                        actionLabel = "Voltar",
                        isCompactHeight = isCompactHeight,
                        onClick = onBackAction
                    )
                }
            }
        }
    }
}

@Composable
fun ControllerActionPrompt(
    actionLabel: String,
    isCompactHeight: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp) // rule-ok: toque minimo 48dp
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button) { onClick() }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = actionLabel,
            color = TextPrimary,
            fontSize = if (isCompactHeight) 11.sp else 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
