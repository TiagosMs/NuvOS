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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.ui.theme.*

@Composable
fun BottomBar(
    isAppsTab: Boolean = false,
    isInsideFolder: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControllerActionPrompt(
                buttonLetter = "A",
                buttonColor = BtnAGreen,
                actionLabel = if (isAppsTab && !isInsideFolder) "Abrir Pasta" else "Jogar"
            )
            ControllerActionPrompt(
                buttonLetter = "X",
                buttonColor = BtnXBlue,
                actionLabel = "Opções"
            )
            ControllerActionPrompt(
                buttonLetter = "Y",
                buttonColor = BtnYYellow,
                actionLabel = "Adicionar Jogos"
            )
            if (isAppsTab && isInsideFolder) {
                ControllerActionPrompt(
                    buttonLetter = "B",
                    buttonColor = Color(0xFFEF4444),
                    actionLabel = "Voltar"
                )
            }
        }
    }
}

@Composable
fun ControllerActionPrompt(
    buttonLetter: String,
    buttonColor: Color,
    actionLabel: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Canvas(modifier = Modifier.size(17.dp)) {
            val radius = size.minDimension / 2f
            drawCircle(
                color = buttonColor,
                radius = radius,
                center = center
            )

            val paint = Paint().apply {
                color = android.graphics.Color.WHITE
                textSize = 9.8.sp.toPx()
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
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
