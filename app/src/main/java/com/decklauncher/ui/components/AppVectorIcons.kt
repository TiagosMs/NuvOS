package com.decklauncher.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.decklauncher.ui.theme.TextWhite

/**
 * Ícone Gamepad Ergonômico de Console (Unslop v1.1):
 * Traço uniforme de 1.5dp, silhueta com pegas anatômicas inferiores, D-pad cruzado e botões frontais.
 */
@Composable
fun VectorIconGamepad(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val s = 1.5.dp.toPx()
        val w = size.width
        val h = size.height

        // Corpo anatômico do controle com pegas inferiores
        val bodyPath = Path().apply {
            moveTo(w * 0.28f, h * 0.28f)
            lineTo(w * 0.72f, h * 0.28f)
            cubicTo(w * 0.88f, h * 0.28f, w * 0.94f, h * 0.44f, w * 0.90f, h * 0.68f)
            cubicTo(w * 0.86f, h * 0.84f, w * 0.76f, h * 0.86f, w * 0.70f, h * 0.74f)
            lineTo(w * 0.62f, h * 0.58f)
            lineTo(w * 0.38f, h * 0.58f)
            lineTo(w * 0.30f, h * 0.74f)
            cubicTo(w * 0.24f, h * 0.86f, w * 0.14f, h * 0.84f, w * 0.10f, h * 0.68f)
            cubicTo(w * 0.06f, h * 0.44f, w * 0.12f, h * 0.28f, w * 0.28f, h * 0.28f)
            close()
        }
        drawPath(
            path = bodyPath,
            color = tint,
            style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // D-Pad minimalista em cruz com traço canônico
        val dpadCenter = Offset(w * 0.30f, h * 0.44f)
        val arm = w * 0.07f
        drawLine(tint, Offset(dpadCenter.x - arm, dpadCenter.y), Offset(dpadCenter.x + arm, dpadCenter.y), strokeWidth = s, cap = StrokeCap.Round)
        drawLine(tint, Offset(dpadCenter.x, dpadCenter.y - arm), Offset(dpadCenter.x, dpadCenter.y + arm), strokeWidth = s, cap = StrokeCap.Round)

        // Botões frontais X/Y/A/B (4 micro-glifos proporcionais)
        val btnCenter = Offset(w * 0.70f, h * 0.44f)
        val offset = w * 0.06f
        drawCircle(tint, radius = s * 0.75f, center = Offset(btnCenter.x, btnCenter.y - offset))
        drawCircle(tint, radius = s * 0.75f, center = Offset(btnCenter.x, btnCenter.y + offset))
        drawCircle(tint, radius = s * 0.75f, center = Offset(btnCenter.x - offset, btnCenter.y))
        drawCircle(tint, radius = s * 0.75f, center = Offset(btnCenter.x + offset, btnCenter.y))
    }
}

/**
 * Ícone de Biblioteca / Jogos (Unslop v1.1):
 * Três volumes limpos com um ligeiramente inclinado para transmitir dinamismo visual.
 */
@Composable
fun VectorIconBooks(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val s = 1.5.dp.toPx()
        val w = size.width
        val h = size.height

        // Livro 1 (vertical)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.15f, h * 0.20f),
            size = Size(w * 0.20f, h * 0.65f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Livro 2 (vertical)
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.40f, h * 0.15f),
            size = Size(w * 0.20f, h * 0.70f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Livro 3 (inclinado dinâmico)
        val book3 = Path().apply {
            moveTo(w * 0.65f, h * 0.85f)
            lineTo(w * 0.78f, h * 0.28f)
            lineTo(w * 0.94f, h * 0.32f)
            lineTo(w * 0.82f, h * 0.85f)
            close()
        }
        drawPath(book3, color = tint, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Linhas de lombada
        drawLine(tint, Offset(w * 0.18f, h * 0.35f), Offset(w * 0.32f, h * 0.35f), strokeWidth = s, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.43f, h * 0.30f), Offset(w * 0.57f, h * 0.30f), strokeWidth = s, cap = StrokeCap.Round)
    }
}

/**
 * Ícone Paleta de Cores / Aparência (Unslop v1.1):
 * Silhueta contínua com orifício ergonômico de polegar e gotas de tinta limpas.
 */
@Composable
fun VectorIconPalette(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val s = 1.5.dp.toPx()
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.50f, h * 0.12f)
            cubicTo(w * 0.82f, h * 0.12f, w * 0.90f, h * 0.40f, w * 0.82f, h * 0.68f)
            cubicTo(w * 0.78f, h * 0.82f, w * 0.60f, h * 0.88f, w * 0.48f, h * 0.82f)
            cubicTo(w * 0.40f, h * 0.78f, w * 0.32f, h * 0.80f, w * 0.24f, h * 0.86f)
            cubicTo(w * 0.12f, h * 0.80f, w * 0.10f, h * 0.52f, w * 0.16f, h * 0.34f)
            cubicTo(w * 0.22f, h * 0.16f, w * 0.36f, h * 0.12f, w * 0.50f, h * 0.12f)
            close()
        }
        drawPath(path, color = tint, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Orifício de apoio do dedão
        drawCircle(color = tint, radius = s * 1.5f, center = Offset(w * 0.68f, h * 0.66f), style = Stroke(width = s))

        // Gotas de tinta na borda
        drawCircle(tint, radius = s * 0.9f, center = Offset(w * 0.34f, h * 0.34f))
        drawCircle(tint, radius = s * 0.9f, center = Offset(w * 0.52f, h * 0.26f))
        drawCircle(tint, radius = s * 0.9f, center = Offset(w * 0.70f, h * 0.38f))
    }
}

/**
 * Ícone Banco de Dados / Metadados (Unslop v1.1):
 * Cilindros geométricos tangentes sem descontinuidades de traço.
 */
@Composable
fun VectorIconDatabase(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val s = 1.5.dp.toPx()
        val w = size.width
        val h = size.height

        val left = w * 0.18f
        val right = w * 0.82f
        val cylinderW = right - left

        // Elipse de topo
        drawOval(
            color = tint,
            topLeft = Offset(left, h * 0.12f),
            size = Size(cylinderW, h * 0.22f),
            style = Stroke(width = s)
        )

        // Arco do meio
        val midArc = Path().apply {
            moveTo(left, h * 0.48f)
            cubicTo(left, h * 0.60f, right, h * 0.60f, right, h * 0.48f)
        }
        drawPath(midArc, color = tint, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Arco da base
        val botArc = Path().apply {
            moveTo(left, h * 0.74f)
            cubicTo(left, h * 0.86f, right, h * 0.86f, right, h * 0.74f)
        }
        drawPath(botArc, color = tint, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Laterais conectando os eixos tangentes
        drawLine(tint, Offset(left, h * 0.23f), Offset(left, h * 0.74f), strokeWidth = s, cap = StrokeCap.Round)
        drawLine(tint, Offset(right, h * 0.23f), Offset(right, h * 0.74f), strokeWidth = s, cap = StrokeCap.Round)
    }
}

/**
 * Ícone Engrenagem Mecânica de Precisão (Unslop v1.1):
 * Dentes trapezoidais contínuos calculados parametricamente e eixo interno vazado.
 */
@Composable
fun VectorIconGear(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val s = 1.5.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rOut = size.width * 0.44f
        val rIn = size.width * 0.33f
        val rHole = size.width * 0.16f

        val teeth = 8
        val gearPath = Path()
        for (i in 0 until teeth) {
            val a0 = (i * 2 * Math.PI / teeth).toFloat()
            val a1 = a0 + (0.32f * 2 * Math.PI / teeth).toFloat()
            val a2 = a0 + (0.52f * 2 * Math.PI / teeth).toFloat()
            val a3 = a0 + (0.84f * 2 * Math.PI / teeth).toFloat()

            val p0 = Offset(cx + Math.cos(a0.toDouble()).toFloat() * rIn, cy + Math.sin(a0.toDouble()).toFloat() * rIn)
            val p1 = Offset(cx + Math.cos(a1.toDouble()).toFloat() * rOut, cy + Math.sin(a1.toDouble()).toFloat() * rOut)
            val p2 = Offset(cx + Math.cos(a2.toDouble()).toFloat() * rOut, cy + Math.sin(a2.toDouble()).toFloat() * rOut)
            val p3 = Offset(cx + Math.cos(a3.toDouble()).toFloat() * rIn, cy + Math.sin(a3.toDouble()).toFloat() * rIn)

            if (i == 0) gearPath.moveTo(p0.x, p0.y) else gearPath.lineTo(p0.x, p0.y)
            gearPath.lineTo(p1.x, p1.y)
            gearPath.lineTo(p2.x, p2.y)
            gearPath.lineTo(p3.x, p3.y)
        }
        gearPath.close()

        drawPath(gearPath, color = tint, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color = tint, radius = rHole, center = Offset(cx, cy), style = Stroke(width = s))
    }
}

/**
 * Ícone Fechar / Dispensar (Unslop v1.1):
 * Traço canônico e alinhamento diagonal balanceado.
 */
@Composable
fun VectorIconClose(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val s = 1.5.dp.toPx()
        val pad = size.width * 0.22f
        drawLine(tint, Offset(pad, pad), Offset(size.width - pad, size.height - pad), strokeWidth = s, cap = StrokeCap.Round)
        drawLine(tint, Offset(size.width - pad, pad), Offset(pad, size.height - pad), strokeWidth = s, cap = StrokeCap.Round)
    }
}
