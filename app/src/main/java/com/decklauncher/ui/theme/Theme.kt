package com.decklauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ConsoleBg = Color(0xFF2A2A2A)
val TabActiveBg = Color(0xFF191919)
val TabInactiveText = Color(0xFF9E9E9E)
val TextWhite = Color(0xFFFFFFFF)
val GlowWhite = Color(0xFFFFFFFF)
val DotGreen = Color(0xFF22C55E)

val BtnAGreen = Color(0xFF16A34A)
val BtnXBlue = Color(0xFF0284C7)
val BtnYYellow = Color(0xFFEAB308)

val LbRbPillBg = Color(0xFFE5E7EB)
val LbRbPillText = Color(0xFF000000)

val CardBgDark = Color(0xFF1E1E1E)
val CardBorderUnselected = Color(0x22FFFFFF)
val ScrollbarColor = Color(0xFF383838)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = DotGreen,
    background = ConsoleBg,
    surface = CardBgDark,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun DeckLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
