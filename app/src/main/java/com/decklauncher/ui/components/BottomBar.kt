package com.decklauncher.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
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
                    text = "◀ D-Pad ▶ Mover",
                    color = Color(0xFFF59E0B),
                    fontSize = if (isCompactHeight) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
                ControllerActionPrompt(
                    buttonLetter = "A",
                    buttonColor = BtnAGreen,
                    actionLabel = "Fixar / Salvar",
                    isCompactHeight = isCompactHeight
                )
                ControllerActionPrompt(
                    buttonLetter = "B",
                    buttonColor = Color(0xFFEF4444),
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
                    buttonLetter = "A",
                    buttonColor = BtnAGreen,
                    actionLabel = if (isAppsTab && !isInsideFolder) "Abrir Pasta" else if (isEmulatorsTab) "Ver Jogos" else "Jogar",
                    isCompactHeight = isCompactHeight
                )
                ControllerActionPrompt(
                    buttonLetter = "X",
                    buttonColor = BtnXBlue,
                    actionLabel = "Opções",
                    isCompactHeight = isCompactHeight
                )
                ControllerActionPrompt(
                    buttonLetter = "Y",
                    buttonColor = BtnYYellow,
                    actionLabel = "Adicionar Jogos",
                    isCompactHeight = isCompactHeight
                )
                if (isAppsTab && isInsideFolder) {
                    ControllerActionPrompt(
                        buttonLetter = "B",
                        buttonColor = Color(0xFFEF4444),
                        actionLabel = "Voltar",
                        isCompactHeight = isCompactHeight
                    )
                }
            }
        }
    }
}

@Composable
fun ControllerActionPrompt(
    buttonLetter: String,
    buttonColor: Color,
    actionLabel: String,
    isCompactHeight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (isCompactHeight) 5.dp else 7.dp)
    ) {
        Canvas(modifier = Modifier.size(if (isCompactHeight) 14.dp else 17.dp)) {
            val radius = size.minDimension / 2f
            drawCircle(
                color = buttonColor,
                radius = radius,
                center = center
            )

            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = (if (isCompactHeight) 8.2.sp else 9.8.sp).toPx()
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val yOffset = center.y - ((paint.descent() + paint.ascent()) / 2f)
            drawContext.canvas.nativeCanvas.drawText(buttonLetter, center.x, yOffset, paint)
        }

        Text(
            text = actionLabel,
            color = TextWhite,
            fontSize = if (isCompactHeight) 10.sp else 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
