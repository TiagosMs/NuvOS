package com.decklauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tokens Oficiais - GUIA_DE_DESIGN_LAUNCHER.md
val ColorBgDeep = Color(0xFF1C212A)
val TabActiveBg = Color(0xFF191919)
val SurfaceItem = Color(0xFF292727)
val ConsoleBg = Color(0xFF2A2A2A)
val ScrollbarThumb = Color(0xFF373737)
val SurfaceCard = Color(0xFF3D3A3A)
val DrawerBg = Color(0xFF484848)
val ColorOverlay = Color(0xFF6A6A6A)

// Tipografia e Elementos Neutros
val TextPrimary = Color(0xFFCBCBCB)
val NeutralHigh = Color(0xFFD9D9D9)
val WhiteFocus = Color(0xFFFFFFFF)
val TextWhite = Color(0xFFFFFFFF)
val GlowWhite = Color(0xFFFFFFFF)
val TabInactiveText = Color(0xFFCBCBCB)

// Gamepad Prompts & Ações
val BtnAGreen = Color(0xFF55A43B)
val BtnBRed = Color(0xFFDA1725)
val BtnXBlue = Color(0xFF039AC9)
val BtnYYellow = Color(0xFFF1D514)
val BtnShoulder = Color(0xFFD6E1F6)
val LbRbPillBg = Color(0xFFD6E1F6)
val LbRbPillText = Color(0xFF1C212A)
val StatusPlaying = Color(0xFF1E7900)
val DotGreen = Color(0xFF55A43B)

// Cards & Bordas
val CardBgDark = Color(0xFF3D3A3A)
val CardBorderUnselected = Color(0x22FFFFFF)
val ScrollbarColor = Color(0xFF373737)

private val DarkColorScheme = darkColorScheme(
    primary = WhiteFocus,
    secondary = BtnAGreen,
    background = ConsoleBg,
    surface = SurfaceCard,
    onPrimary = Color.Black,
    onSecondary = WhiteFocus,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun DeckLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
