package com.decklauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tokens Oficiais - GUIA_DE_DESIGN_LAUNCHER.md & D5 v1.1
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

// Gamepad Prompts & Ações (D5: Botões e glifos em cinza do tema)
val NeutralBtnBg = Color(0xFF2A323D)
val NeutralBtnBgFocused = Color(0xFF3B4654)
val NeutralBtnText = Color(0xFFE2E8F0)
val NeutralGlyphBg = Color(0xFF424E5E)
val FocusRing = Color(0xFFFFFFFF)
val ScrimColor = Color(0x99000000)
val DangerBtnBg = Color(0xFF3B2A2D)
val DangerBtnText = Color(0xFFFCA5A5)

// Cores Legadas compatíveis
val BtnAGreen = Color(0xFF424E5E)
val BtnBRed = Color(0xFF424E5E)
val BtnXBlue = Color(0xFF424E5E)
val BtnYYellow = Color(0xFF424E5E)
val BtnShoulder = Color(0xFF424E5E)
val LbRbPillBg = Color(0xFF2A323D)
val LbRbPillText = Color(0xFFE2E8F0)
val StatusPlaying = Color(0xFF10B981)
val DotGreen = Color(0xFF10B981)

// Cards & Bordas
val CardBgDark = Color(0xFF3D3A3A)
val CardBorderUnselected = Color(0x22FFFFFF)
val ScrollbarColor = Color(0xFF373737)

private val DarkColorScheme = darkColorScheme(
    primary = WhiteFocus,
    secondary = NeutralBtnBgFocused,
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
        typography = DeckTypography,
        content = content
    )
}
