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
import androidx.compose.ui.graphics.Color
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
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 0.98f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 160f),
        label = "folder_card_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
        label = "folder_border"
    )

    val shape = RoundedCornerShape(22.dp)

    Card(
        modifier = modifier
            .width(160.dp)
            .height(180.dp)
            .scale(scale)
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
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 2x2 Mini Icons Container
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141414))
                    .padding(8.dp),
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
                        MiniAppIcon(previewApps.getOrNull(0))
                        MiniAppIcon(previewApps.getOrNull(1))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MiniAppIcon(previewApps.getOrNull(2))
                        MiniAppIcon(previewApps.getOrNull(3))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Folder Title & Count (Without emojis)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = folder.title,
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${folder.apps.size} apps",
                    color = TabInactiveText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MiniAppIcon(app: com.decklauncher.model.AppItem?) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF262626)),
        contentAlignment = Alignment.Center
    ) {
        if (app?.iconBitmap != null) {
            Image(
                bitmap = app.iconBitmap,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else if (app != null) {
            Text(
                text = app.label.take(1).uppercase(),
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
