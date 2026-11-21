package com.decklauncher.model

import com.decklauncher.model.AppItem

sealed interface Overlay {
    data object None : Overlay
    data class ItemMenu(val app: AppItem) : Overlay
    data object StartMenu : Overlay
    data class EmulatorGames(val app: AppItem) : Overlay
    data class EditCover(val app: AppItem) : Overlay
    data class EditHero(val app: AppItem) : Overlay
    data class EditLogo(val app: AppItem) : Overlay
    data class ManageMetadata(val app: AppItem) : Overlay
    data class SelectMusic(val app: AppItem) : Overlay
    data class SelectIntro(val app: AppItem) : Overlay
    data object AddGameOptions : Overlay
    data object SteamGridApiKey : Overlay
    data object IidbApiKey : Overlay
    data object ChangeAvatar : Overlay
    data object InitialSetup : Overlay
    data class ConfirmAction(
        val title: String,
        val message: String,
        val confirmLabel: String = "Confirmar",
        val onConfirm: () -> Unit
    ) : Overlay
}
