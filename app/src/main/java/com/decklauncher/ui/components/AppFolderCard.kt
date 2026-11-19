package com.decklauncher.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.AppFolder

/**
 * Gráfico vetorial preciso da pasta estilo console / OS moderno,
 * com aba superior chanfrada, cantos arredondados e suporte a sombra projetada.
 */
@Composable
fun FolderIconGraphic(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD6D6D6),
    hasShadow: Boolean = false,
    shadowColor: Color = Color(0x77000000)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val tabW = w * 0.32f
        val slopeW = w * 0.12f
        val tabH = h * 0.16f
        val r = (w * 0.08f).coerceAtMost(h * 0.08f)

        fun drawSingleFolder(offsetX: Float, offsetY: Float, fillColor: Color) {
            val path = Path().apply {
                // Início no topo esquerdo da aba abaixo da curva
                moveTo(offsetX, offsetY + r)
                quadraticBezierTo(offsetX, offsetY, offsetX + r, offsetY)
                // Topo plano da aba
                lineTo(offsetX + tabW - r, offsetY)
                quadraticBezierTo(offsetX + tabW, offsetY, offsetX + tabW + r * 0.4f, offsetY + r * 0.2f)
                // Chanfro diagonal descendo para o corpo principal
                lineTo(offsetX + tabW + slopeW - r * 0.4f, offsetY + tabH - r * 0.2f)
                quadraticBezierTo(offsetX + tabW + slopeW, offsetY + tabH, offsetX + tabW + slopeW + r, offsetY + tabH)
                // Topo do corpo principal à direita
                lineTo(offsetX + w - r, offsetY + tabH)
                quadraticBezierTo(offsetX + w, offsetY + tabH, offsetX + w, offsetY + tabH + r)
                // Borda direita
                lineTo(offsetX + w, offsetY + h - r)
                quadraticBezierTo(offsetX + w, offsetY + h, offsetX + w - r, offsetY + h)
                // Borda inferior
                lineTo(offsetX + r, offsetY + h)
                quadraticBezierTo(offsetX, offsetY + h, offsetX, offsetY + h - r)
                // Borda esquerda
                lineTo(offsetX, offsetY + r)
                close()
            }
            drawPath(path, fillColor)
        }

        if (hasShadow) {
            // Sombra projetada para baixo e à esquerda conforme mockup
            drawSingleFolder(
                offsetX = -w * 0.02f,
                offsetY = h * 0.05f,
                fillColor = shadowColor
            )
        }

        drawSingleFolder(offsetX = 0f, offsetY = 0f, fillColor = color)
    }
}

/**
 * Card da pasta no grid 3x3 à esquerda (Mockup da Aba Apps).
 */
@Composable
fun AppFolderCard(
    folder: AppFolder,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 0.98f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
        label = "folder_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 200f),
        label = "folder_border"
    )

    val shape = RoundedCornerShape(if (isCompactHeight) 16.dp else 22.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.12f)
            .scale(scale)
            .clip(shape)
            .border(
                BorderStroke(borderWidth, if (isSelected) Color.White else Color.Transparent),
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isSelected) onOpen() else onSelect()
            }
            .semantics { contentDescription = "Pasta ${folder.title}" },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF3A3A3A) else Color(0xFF303030)
        ),
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompactHeight) 10.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            FolderIconGraphic(
                modifier = Modifier.size(
                    width = if (isCompactHeight) 44.dp else 60.dp,
                    height = if (isCompactHeight) 32.dp else 44.dp
                ),
                color = Color(0xFFD6D6D6)
            )
        }
    }
}

/**
 * Painel direito do mockup: pré-visualização grande da pasta selecionada.
 */
@Composable
fun FolderDetailPreview(
    folder: AppFolder,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isCompactHeight = configuration.screenHeightDp < 520

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FolderIconGraphic(
            modifier = Modifier.size(
                width = if (isCompactHeight) 170.dp else 240.dp,
                height = if (isCompactHeight) 125.dp else 175.dp
            ),
            color = Color(0xFFD6D6D6),
            hasShadow = true,
            shadowColor = Color(0x88000000)
        )

        Spacer(modifier = Modifier.height(if (isCompactHeight) 14.dp else 22.dp))

        Text(
            text = folder.title,
            color = Color.White,
            fontSize = if (isCompactHeight) 22.sp else 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
