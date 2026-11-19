package com.decklauncher.data

import android.content.Context
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import org.json.JSONArray
import org.json.JSONObject

object CloudGameManager {

    private const val PREFS_NAME = "cloud_games_prefs"
    private const val KEY_GAMES = "saved_cloud_games"

    // Default presets
    private val DEFAULT_PRESETS = listOf(
        AppItem(
            id = "cloud_fortnite",
            label = "Fortnite",
            packageName = "com.microsoft.xcloud.fortnite",
            category = AppCategory.JOGOS,
            itemType = ItemType.CLOUD,
            cloudUrl = "https://www.xbox.com/play/games/fortnite/BT5P2X999VH2"
        ),
        AppItem(
            id = "cloud_forza5",
            label = "Forza Horizon 5",
            packageName = "com.microsoft.xcloud.forza5",
            category = AppCategory.JOGOS,
            itemType = ItemType.CLOUD,
            cloudUrl = "https://www.xbox.com/play/games/forza-horizon-5-standard-edition/9NKX70BBC2TN"
        ),
        AppItem(
            id = "cloud_halo",
            label = "Halo Infinite",
            packageName = "com.microsoft.xcloud.halo",
            category = AppCategory.JOGOS,
            itemType = ItemType.CLOUD,
            cloudUrl = "https://www.xbox.com/play/games/halo-infinite/9PP5G1F0C2B6"
        ),
        AppItem(
            id = "cloud_starfield",
            label = "Starfield",
            packageName = "com.microsoft.xcloud.starfield",
            category = AppCategory.JOGOS,
            itemType = ItemType.CLOUD,
            cloudUrl = "https://www.xbox.com/play/games/starfield/9NCGNJD8ZPNC"
        )
    )

    fun getCloudGames(context: Context): List<AppItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_GAMES, null)

        if (jsonStr == null) {
            // First run: save and return defaults
            saveCloudGames(context, DEFAULT_PRESETS)
            return DEFAULT_PRESETS
        }

        val list = mutableListOf<AppItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AppItem(
                        id = obj.getString("id"),
                        label = obj.getString("label"),
                        packageName = obj.optString("packageName", "cloud.${obj.getString("id")}"),
                        category = AppCategory.JOGOS,
                        itemType = ItemType.CLOUD,
                        cloudUrl = obj.getString("cloudUrl")
                    )
                )
            }
        } catch (e: Exception) {
            return DEFAULT_PRESETS
        }
        return list
    }

    fun addCloudGame(context: Context, title: String, url: String): AppItem {
        val current = getCloudGames(context).toMutableList()
        val safeId = "cloud_" + System.currentTimeMillis()
        val item = AppItem(
            id = safeId,
            label = title,
            packageName = "cloud.$safeId",
            category = AppCategory.JOGOS,
            itemType = ItemType.CLOUD,
            cloudUrl = url
        )
        current.add(item)
        saveCloudGames(context, current)
        return item
    }

    fun removeCloudGame(context: Context, id: String) {
        val current = getCloudGames(context).filter { it.id != id }
        saveCloudGames(context, current)
    }

    private fun saveCloudGames(context: Context, games: List<AppItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        for (g in games) {
            val obj = JSONObject().apply {
                put("id", g.id)
                put("label", g.label)
                put("packageName", g.packageName)
                put("cloudUrl", g.cloudUrl)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_GAMES, array.toString()).apply()
    }
}
