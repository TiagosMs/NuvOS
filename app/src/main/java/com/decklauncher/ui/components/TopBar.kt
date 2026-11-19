package com.decklauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decklauncher.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TopBar(
    selectedTab: Int,
    isTabAreaFocused: Boolean,
    onTabSelected: (Int) -> Unit,
    batteryPct: Int,
    modifier: Modifier = Modifier
) {
    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(10000)
        }
    }

    val tabs = listOf("Jogos", "Emuladores", "Apps")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left spacer for symmetric visual balance
        Spacer(modifier = Modifier.width(96.dp))

        // Center Tabs: [LB] Jogos Emuladores Apps [RB]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // [LB] Shoulder Button Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(LbRbPillBg)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LB",
                    color = LbRbPillText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Tabs row
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val tabBg by animateColorAsState(
                    targetValue = if (isSelected) TabActiveBg else Color.Transparent,
                    animationSpec = spring(),
                    label = "tab_bg"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(tabBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(index) }
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) TextWhite else TabInactiveText,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // [RB] Shoulder Button Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(LbRbPillBg)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RB",
                    color = LbRbPillText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Right Status: Signal + Battery + Time
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.semantics {
                contentDescription = "Bateria: $batteryPct%, Horário: $currentTime"
            }
        ) {
            SignalBarsIcon(
                modifier = Modifier.size(width = 16.dp, height = 12.dp)
            )

            BatteryIcon(
                percentage = batteryPct,
                modifier = Modifier.size(width = 22.dp, height = 11.dp)
            )

            Text(
                text = currentTime,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SignalBarsIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val totalBars = 4
        val barWidth = size.width / (totalBars * 1.6f)
        val gap = barWidth * 0.6f

        for (i in 0 until totalBars) {
            val h = size.height * ((i + 1f) / totalBars)
            val left = i * (barWidth + gap)
            drawRect(
                color = Color.White,
                topLeft = Offset(left, size.height - h),
                size = Size(barWidth, h)
            )
        }
    }
}

@Composable
fun BatteryIcon(percentage: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val capWidth = 2.dp.toPx()
        val bodyWidth = size.width - capWidth - 1.dp.toPx()
        val bodyHeight = size.height
        val corner = 2.dp.toPx()

        // Battery outer shell
        drawRoundRect(
            color = Color.White,
            topLeft = Offset.Zero,
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 1.3.dp.toPx())
        )

        // Positive terminal cap
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(bodyWidth + 1.dp.toPx(), bodyHeight * 0.25f),
            size = Size(capWidth, bodyHeight * 0.5f),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )

        // Inside fill
        val fillPadding = 2.dp.toPx()
        val maxFillWidth = bodyWidth - fillPadding * 2
        val currentFill = (maxFillWidth * (percentage.coerceIn(0, 100) / 100f)).coerceAtLeast(1f)

        drawRoundRect(
            color = if (percentage <= 20) Color(0xFFEF4444) else Color.White,
            topLeft = Offset(fillPadding, fillPadding),
            size = Size(currentFill, bodyHeight - fillPadding * 2),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
    }
}
