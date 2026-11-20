package com.decklauncher.data

import android.content.Context

enum class ArtworkProvider(val title: String, val shortName: String, val description: String) {
    STEAMGRID_DB(
        title = "SteamGridDB",
        shortName = "SteamGridDB",
        description = "Capas comunitárias 600x900, fundos 16:9 e logos"
    ),
    IIDB(
        title = "iiDB (iiSU Network)",
        shortName = "iiDB",
        description = "Capas oficiais, fundos 16:9, logos 3D e áudio tema"
    )
}

object ArtworkProviderManager {

    private const val PREFS_NAME = "artwork_provider_prefs"
    private const val KEY_PROVIDER = "preferred_artwork_provider"

    fun getPreferredProvider(context: Context): ArtworkProvider {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_PROVIDER, ArtworkProvider.STEAMGRID_DB.name)
        return try {
            ArtworkProvider.valueOf(name ?: ArtworkProvider.STEAMGRID_DB.name)
        } catch (_: Exception) {
            ArtworkProvider.STEAMGRID_DB
        }
    }

    fun setPreferredProvider(context: Context, provider: ArtworkProvider) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PROVIDER, provider.name).apply()
    }
}
