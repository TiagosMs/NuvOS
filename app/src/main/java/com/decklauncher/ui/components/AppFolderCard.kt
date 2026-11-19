package com.decklauncher.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.data.AppFolder
import com.decklauncher.ui.theme.CardBgDark
import com.decklauncher.ui.theme.TabInactiveText
import com.decklauncher.ui.theme.TextWhite

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
        targetValue = if (isSelected) 1.05f else 0.97f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 160f),
        label = "folder_card_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
        label = "folder_border"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
        label = "folder_shadow"
    )

    val shape = RoundedCornerShape(if (isCompactHeight) 16.dp else 22.dp)

    Card(
        modifier = modifier
            .width(if (isCompactHeight) 120.dp else 160.dp)
            .height(if (isCompactHeight) 135.dp else 180.dp)
            .scale(scale)
            .shadow(shadowElevation, shape, spotColor = Color(0x40FFFFFF), ambientColor = Color(0x20FFFFFF))
            .clip(shape)
            .border(
                BorderStroke(borderWidth, if (isSelected) Color.White else Color(0x33FFFFFF)),
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isSelected) onOpen() else onSelect()
            }
            .semantics { contentDescription = "Pasta ${folder.title}" },
        colors = CardDefaults.cardColors(containerColor = CardBgDark),
        shape = shape
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
                .padding(if (isCompactHeight) 10.dp else 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 2x2 Mini Icons Container
            Box(
                modifier = Modifier
                    .size(if (isCompactHeight) 68.dp else 92.dp)
                    .clip(RoundedCornerShape(if (isCompactHeight) 12.dp else 16.dp))
                    .background(Color(0xFF141414))
                    .padding(if (isCompactHeight) 5.dp else 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val previewApps = folder.apps.take(4)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniAppIcon(previewApps.getOrNull(0), isCompactHeight)
                        MiniAppIcon(previewApps.getOrNull(1), isCompactHeight)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniAppIcon(previewApps.getOrNull(2), isCompactHeight)
                        MiniAppIcon(previewApps.getOrNull(3), isCompactHeight)
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Folder Title & Count (Without emojis)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = folder.title,
                    color = TextWhite,
                    fontSize = if (isCompactHeight) 11.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${folder.apps.size} apps",
                    color = TabInactiveText,
                    fontSize = if (isCompactHeight) 9.sp else 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MiniAppIcon(
    app: com.decklauncher.model.AppItem?,
    isCompactHeight: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(if (isCompactHeight) 25.dp else 34.dp)
            .clip(RoundedCornerShape(if (isCompactHeight) 6.dp else 8.dp))
            .background(Color(0xFF262626)),
        contentAlignment = Alignment.Center
    ) {
        if (app?.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isCompactHeight) 1.dp else 2.dp)
                    .clip(RoundedCornerShape(if (isCompactHeight) 4.dp else 6.dp))
            )
        } else if (app != null) {
            Text(
                text = app.label.take(1).uppercase(),
                color = TextWhite,
                fontSize = if (isCompactHeight) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
