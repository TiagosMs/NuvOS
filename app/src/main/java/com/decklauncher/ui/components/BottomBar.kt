package com.decklauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onOpenDetailsAction: () -> Unit = {},
    onQuickAccessAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (isCompactHeight) 20.dp else 32.dp,
                vertical = if (isCompactHeight) 6.dp else 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isReorderMode) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 12.dp else 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mover Itens",
                    color = TextWhite,
                    fontSize = if (isCompactHeight) 11.sp else 12.sp,
                    fontWeight = FontWeight.Medium
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
                horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 10.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ControllerActionPrompt(
                    buttonLetter = "A",
                    actionLabel = if (isAppsTab && !isInsideFolder) "Abrir" else if (isEmulatorsTab) "Iniciar" else "Jogar",
                    isCompactHeight = isCompactHeight,
                    onClick = onPrimaryAction
                )
                if (!isAppsTab) {
                    ControllerActionPrompt(
                        buttonLetter = "▼",
                        actionLabel = "Detalhes",
                        isCompactHeight = isCompactHeight,
                        onClick = onOpenDetailsAction
                    )
                }
                ControllerActionPrompt(
                    buttonLetter = "SELECT",
                    actionLabel = "Opções",
                    isCompactHeight = isCompactHeight,
                    onClick = onOptionsAction
                )
                if (!isAppsTab) {
                    ControllerActionPrompt(
                        buttonLetter = "Y",
                        actionLabel = "Adicionar",
                        isCompactHeight = isCompactHeight,
                        onClick = onAddGameAction
                    )
                }
                if (isAppsTab && isInsideFolder) {
                    ControllerActionPrompt(
                        buttonLetter = "B",
                        actionLabel = "Voltar",
                        isCompactHeight = isCompactHeight,
                        onClick = onBackAction
                    )
                }
            }

            // Acesso Rápido no canto direito da barra inferior
            ControllerActionPrompt(
                buttonLetter = "R3",
                actionLabel = "Menu Rápido",
                isCompactHeight = isCompactHeight,
                onClick = onQuickAccessAction
            )
        }
    }
}

@Composable
fun ControllerActionPrompt(
    buttonLetter: String? = null,
    actionLabel: String,
    isCompactHeight: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp) // rule-ok: toque minimo 48dp
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button) { onClick() }
                } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 6.dp else 8.dp)
    ) {
        if (!buttonLetter.isNullOrBlank()) {
            val isSelect = buttonLetter.equals("SELECT", ignoreCase = true)
            val badgeHeight = if (isCompactHeight) 18.dp else 20.dp

            if (isSelect) {
                Box(
                    modifier = Modifier
                        .height(badgeHeight)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonLetter,
                        color = TextWhite,
                        fontSize = if (isCompactHeight) 8.sp else 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(badgeHeight)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x35FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonLetter,
                        color = TextWhite,
                        fontSize = if (isCompactHeight) 9.sp else 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Text(
            text = actionLabel,
            color = TextPrimary,
            fontSize = if (isCompactHeight) 11.sp else 13.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
