package com.decklauncher.data

import android.content.Context

enum class LaunchTransitionType(val id: String, val title: String, val description: String) {
    FADE("fade", "Fade Suave (Steam Deck)", "A tela escurece suavemente antes de abrir o jogo"),
    ZOOM("zoom", "Zoom / Expansão (Console)", "Efeito de zoom do jogo preenchendo a tela"),
    INSTANT("instant", "Instantâneo (Sem Animação)", "Corte direto para o jogo sem transições"),
    SLIDE("slide", "Deslizar (Padrão Android)", "Animação padrão do sistema de deslizar janelas")
}

object TransitionManager {
    private const val PREFS_NAME = "deck_transition_prefs"
    private const val KEY_TRANSITION_TYPE = "launch_transition_type"

    fun getLaunchTransition(context: Context): LaunchTransitionType {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_TRANSITION_TYPE, LaunchTransitionType.FADE.id)
        return LaunchTransitionType.values().firstOrNull { it.id == id } ?: LaunchTransitionType.FADE
    }

    fun setLaunchTransition(context: Context, type: LaunchTransitionType) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TRANSITION_TYPE, type.id).apply()
    }
}
