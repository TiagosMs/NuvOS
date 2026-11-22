package com.decklauncher.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object Motion {
    const val Fast = 120      // feedback imediato (foco, toque)
    const val Base = 220      // transições de conteúdo
    const val Slow = 420      // troca de fundo/hero

    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)          // entrar: rápido e suave no fim
    val SelectSpring = spring<Float>(dampingRatio = 0.92f, stiffness = Spring.StiffnessMedium)
    val SoftSpring   = spring<Float>(dampingRatio = 0.95f, stiffness = Spring.StiffnessLow)
}
