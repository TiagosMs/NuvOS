package com.decklauncher.model

import com.decklauncher.data.FolderCategory

enum class AppCategory {
    JOGOS,
    EMULADORES,
    APPS
}

enum class ItemType {
    APP,
    ROM,
    CLOUD,
    SHORTCUT
}

data class AppItem(
    val id: String,
    val label: String,
    val packageName: String,
    val activityName: String? = null,
    val category: AppCategory = AppCategory.APPS,
    val folderCategory: FolderCategory? = null,
    val itemType: ItemType = ItemType.APP,
    val romPath: String? = null,
    val cloudUrl: String? = null,
    val coverPath: String? = null
)
