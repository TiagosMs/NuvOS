package com.decklauncher.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.model.AppItem
import com.decklauncher.ui.theme.CardBgDark
import com.decklauncher.ui.theme.TextWhite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridCard(
    app: AppItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onLaunch: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 0.96f,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 160f),
        label = "grid_card_scale"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        animationSpec = spring(dampingRatio = 0.88f, stiffness = 180f),
        label = "grid_border"
    )

    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .scale(scale)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (isSelected) onLaunch() else onSelect()
                },
                onLongClick = {
                    onSelect()
                    onLongClick?.invoke()
                }
            )
            .semantics { contentDescription = app.label },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(shape)
                .border(
                    BorderStroke(borderWidth, if (isSelected) Color.White else Color(0x28FFFFFF)),
                    shape = shape
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFF2D2D2D) else CardBgDark
            ),
            shape = shape
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E)),
                contentAlignment = Alignment.Center
            ) {
                val icon = com.decklauncher.data.rememberAppIcon(app)
                if (icon != null) {
                    Image(
                        bitmap = icon,
                        contentDescription = app.label,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Text(
                        text = app.label.take(1).uppercase(),
                        color = TextWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = app.label,
                color = if (isSelected) TextWhite else Color(0xFFB0B0B0),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}
