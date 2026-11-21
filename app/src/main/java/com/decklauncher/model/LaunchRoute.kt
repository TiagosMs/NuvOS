package com.decklauncher.model

sealed interface LaunchRoute {
    data class OpenEmulatorDialog(val item: AppItem) : LaunchRoute
    data class LaunchNow(val item: AppItem) : LaunchRoute

    companion object {
        fun resolve(item: AppItem, isFromEmulatorDialog: Boolean = false): LaunchRoute {
            if (isFromEmulatorDialog) {
                return LaunchNow(item)
            }
            return if (item.category == AppCategory.EMULADORES && item.itemType == ItemType.APP) {
                OpenEmulatorDialog(item)
            } else {
                LaunchNow(item)
            }
        }
    }
}
