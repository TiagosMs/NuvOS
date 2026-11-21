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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.decklauncher.ui.theme.TextWhite

@Composable
fun VectorIconBooks(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()

        // Livro 1
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.15f, h * 0.2f),
            size = Size(w * 0.28f, h * 0.65f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )
        // Livro 2
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.48f, h * 0.12f),
            size = Size(w * 0.35f, h * 0.73f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )
        // Linhas de lombada
        drawLine(tint, Offset(w * 0.2f, h * 0.35f), Offset(w * 0.38f, h * 0.35f), strokeWidth = stroke * 0.8f)
        drawLine(tint, Offset(w * 0.55f, h * 0.3f), Offset(w * 0.76f, h * 0.3f), strokeWidth = stroke * 0.8f)
    }
}

@Composable
fun VectorIconPalette(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.12f)
            cubicTo(w * 0.85f, h * 0.12f, w * 0.92f, h * 0.45f, w * 0.78f, h * 0.72f)
            cubicTo(w * 0.72f, h * 0.84f, w * 0.55f, h * 0.88f, w * 0.45f, h * 0.82f)
            cubicTo(w * 0.38f, h * 0.78f, w * 0.32f, h * 0.82f, w * 0.25f, h * 0.86f)
            cubicTo(w * 0.12f, h * 0.8f, w * 0.08f, h * 0.55f, w * 0.15f, h * 0.35f)
            cubicTo(w * 0.22f, h * 0.16f, w * 0.35f, h * 0.12f, w * 0.5f, h * 0.12f)
            close()
        }
        drawPath(path, color = tint, style = Stroke(width = stroke))

        // Ponto de paleta
        drawCircle(tint, radius = stroke * 1.2f, center = Offset(w * 0.35f, h * 0.35f))
        drawCircle(tint, radius = stroke * 1.2f, center = Offset(w * 0.55f, h * 0.3f))
        drawCircle(tint, radius = stroke * 1.2f, center = Offset(w * 0.7f, h * 0.45f))
    }
}

@Composable
fun VectorIconDatabase(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()

        // Top cylinder ellipse
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.15f, h * 0.12f),
            size = Size(w * 0.7f, h * 0.24f),
            style = Stroke(width = stroke)
        )
        // Middle arc
        val midArc = Path().apply {
            moveTo(w * 0.15f, h * 0.48f)
            cubicTo(w * 0.15f, h * 0.62f, w * 0.85f, h * 0.62f, w * 0.85f, h * 0.48f)
        }
        drawPath(midArc, color = tint, style = Stroke(width = stroke))

        // Bottom arc
        val botArc = Path().apply {
            moveTo(w * 0.15f, h * 0.76f)
            cubicTo(w * 0.15f, h * 0.9f, w * 0.85f, h * 0.9f, w * 0.85f, h * 0.76f)
        }
        drawPath(botArc, color = tint, style = Stroke(width = stroke))

        // Sides
        drawLine(tint, Offset(w * 0.15f, h * 0.24f), Offset(w * 0.15f, h * 0.76f), strokeWidth = stroke)
        drawLine(tint, Offset(w * 0.85f, h * 0.24f), Offset(w * 0.85f, h * 0.76f), strokeWidth = stroke)
    }
}

@Composable
fun VectorIconGamepad(
    modifier: Modifier = Modifier.size(24.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()

        // Gamepad body outline
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.12f, h * 0.26f),
            size = Size(w * 0.76f, h * 0.5f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
            style = Stroke(width = stroke)
        )

        // D-Pad cross
        drawLine(tint, Offset(w * 0.25f, h * 0.51f), Offset(w * 0.39f, h * 0.51f), strokeWidth = stroke * 1.5f)
        drawLine(tint, Offset(w * 0.32f, h * 0.44f), Offset(w * 0.32f, h * 0.58f), strokeWidth = stroke * 1.5f)

        // Action buttons
        drawCircle(tint, radius = stroke, center = Offset(w * 0.65f, h * 0.45f))
        drawCircle(tint, radius = stroke, center = Offset(w * 0.75f, h * 0.53f))
    }
}

@Composable
fun VectorIconGear(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width * 0.32f
        val stroke = 1.8.dp.toPx()

        drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = Stroke(width = stroke))
        drawCircle(color = tint, radius = r * 0.45f, center = Offset(cx, cy), style = Stroke(width = stroke))

        val teeth = 6
        for (i in 0 until teeth) {
            val angle = Math.toRadians((i * (360.0 / teeth)))
            val cos = Math.cos(angle).toFloat()
            val sin = Math.sin(angle).toFloat()
            drawLine(
                color = tint,
                start = Offset(cx + cos * (r * 0.85f), cy + sin * (r * 0.85f)),
                end = Offset(cx + cos * (r * 1.35f), cy + sin * (r * 1.35f)),
                strokeWidth = stroke * 1.6f
            )
        }
    }
}

@Composable
fun VectorIconClose(
    modifier: Modifier = Modifier.size(16.dp),
    tint: Color = TextWhite
) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val pad = size.width * 0.2f
        drawLine(tint, Offset(pad, pad), Offset(size.width - pad, size.height - pad), strokeWidth = stroke)
        drawLine(tint, Offset(size.width - pad, pad), Offset(pad, size.height - pad), strokeWidth = stroke)
    }
}
