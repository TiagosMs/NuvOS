package com.decklauncher.data

import android.content.Context
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import org.json.JSONArray
import org.json.JSONObject

data class CloudCatalogItem(
    val title: String,
    val url: String
)

object CloudGameManager {

    private const val PREFS_NAME = "cloud_games_prefs"
    private const val KEY_GAMES = "saved_cloud_games"

    // Catálogo amplo de jogos disponíveis no Xbox Cloud Gaming (xCloud / Better xCloud)
    val XBOX_CLOUD_CATALOG = listOf(
        CloudCatalogItem("Fortnite", "https://www.xbox.com/play/games/fortnite/BT5P2X999VH2"),
        CloudCatalogItem("Forza Horizon 5", "https://www.xbox.com/play/games/forza-horizon-5-standard-edition/9NKX70BBC2TN"),
        CloudCatalogItem("Forza Horizon 4", "https://www.xbox.com/play/games/forza-horizon-4/9PNJXVCV32X2"),
        CloudCatalogItem("Forza Motorsport", "https://www.xbox.com/play/games/forza-motorsport/9NV3088R0L77"),
        CloudCatalogItem("Halo Infinite", "https://www.xbox.com/play/games/halo-infinite/9PP5G1F0C2B6"),
        CloudCatalogItem("Halo: The Master Chief Collection", "https://www.xbox.com/play/games/halo-the-master-chief-collection/9NTM9HZNLSZX"),
        CloudCatalogItem("Starfield", "https://www.xbox.com/play/games/starfield/9NCGNJD8ZPNC"),
        CloudCatalogItem("Minecraft Legends", "https://www.xbox.com/play/games/minecraft-legends/9N42SR714Z77"),
        CloudCatalogItem("Minecraft Dungeons", "https://www.xbox.com/play/games/minecraft-dungeons/9N8NJ74FZ689"),
        CloudCatalogItem("Sea of Thieves", "https://www.xbox.com/play/games/sea-of-thieves-2024-edition/9P2N57MC619K"),
        CloudCatalogItem("Hi-Fi RUSH", "https://www.xbox.com/play/games/hi-fi-rush/9P5081ZCVF3Z"),
        CloudCatalogItem("Gears 5", "https://www.xbox.com/play/games/gears-5/9P2N57MC619K"),
        CloudCatalogItem("Gears of War 4", "https://www.xbox.com/play/games/gears-of-war-4/9NBLGGH4PBRB"),
        CloudCatalogItem("Persona 3 Reload", "https://www.xbox.com/play/games/persona-3-reload/9PFD9S5QFSJ2"),
        CloudCatalogItem("Persona 5 Tactica", "https://www.xbox.com/play/games/persona-5-tactica/9PF2P3L68007"),
        CloudCatalogItem("Palworld", "https://www.xbox.com/play/games/palworld-game-preview/9NKVB3VNXZ21"),
        CloudCatalogItem("Lies of P", "https://www.xbox.com/play/games/lies-of-p/9NDSL5N75ZRF"),
        CloudCatalogItem("Senua's Saga: Hellblade II", "https://www.xbox.com/play/games/senuas-saga-hellblade-ii/9P6ZBCG1Q5P5"),
        CloudCatalogItem("Fallout 4", "https://www.xbox.com/play/games/fallout-4/C3KRDMGZDGZ6"),
        CloudCatalogItem("Fallout 76", "https://www.xbox.com/play/games/fallout-76/C5ZJHZ8HG361"),
        CloudCatalogItem("The Elder Scrolls V: Skyrim", "https://www.xbox.com/play/games/the-elder-scrolls-v-skyrim-special-edition/BQ1W1T1FC14W"),
        CloudCatalogItem("DOOM Eternal", "https://www.xbox.com/play/games/doom-eternal-standard-edition/9P5FRBJZP0TP"),
        CloudCatalogItem("Microsoft Flight Simulator", "https://www.xbox.com/play/games/microsoft-flight-simulator-standard-40th-anniversary-edition/9PDRB3GLLN90"),
        CloudCatalogItem("Hollow Knight", "https://www.xbox.com/play/games/hollow-knight-voidheart-edition/9MW9469V91LM"),
        CloudCatalogItem("Dead Cells", "https://www.xbox.com/play/games/dead-cells/BNXPTWT6MTLQ"),
        CloudCatalogItem("Ori and the Will of the Wisps", "https://www.xbox.com/play/games/ori-and-the-will-of-the-wisps/9N8CD0XZKLP4"),
        CloudCatalogItem("Ori and the Blind Forest", "https://www.xbox.com/play/games/ori-and-the-blind-forest-definitive-edition/BW85KQB8JPXS"),
        CloudCatalogItem("Vampire Survivors", "https://www.xbox.com/play/games/vampire-survivors/9NDPV9ZSTPNC"),
        CloudCatalogItem("Mortal Kombat 11", "https://www.xbox.com/play/games/mortal-kombat-11/BTC0Z0BG2KVV"),
        CloudCatalogItem("Grand Theft Auto V", "https://www.xbox.com/play/games/grand-theft-auto-v/9NXQGZNDST9C"),
        CloudCatalogItem("Assassin's Creed Valhalla", "https://www.xbox.com/play/games/assassins-creed-valhalla/9N91XSTFCHRF"),
        CloudCatalogItem("Assassin's Creed Odyssey", "https://www.xbox.com/play/games/assassins-creed-odyssey/BWVG7HGTCGBC"),
        CloudCatalogItem("No Man's Sky", "https://www.xbox.com/play/games/no-mans-sky/BQVQTL3PCH05"),
        CloudCatalogItem("Dead by Daylight", "https://www.xbox.com/play/games/dead-by-daylight-special-edition/C0N22P73HSFZ"),
        CloudCatalogItem("Rainbow Six Siege", "https://www.xbox.com/play/games/tom-clancys-rainbow-six-siege/C12TJNQ7NLT1"),
        CloudCatalogItem("Far Cry 6", "https://www.xbox.com/play/games/far-cry-6/9P4J2V7606RF"),
        CloudCatalogItem("Deathloop", "https://www.xbox.com/play/games/deathloop/9P509Z5600Z5"),
        CloudCatalogItem("Monster Hunter Rise", "https://www.xbox.com/play/games/monster-hunter-rise/9P4L7T6R0Z5L"),
        CloudCatalogItem("Yakuza: Like a Dragon", "https://www.xbox.com/play/games/yakuza-like-a-dragon/9NK007P477Z5"),
        CloudCatalogItem("Wo Long: Fallen Dynasty", "https://www.xbox.com/play/games/wo-long-fallen-dynasty/9NM491Q90L1B"),
        CloudCatalogItem("Atomic Heart", "https://www.xbox.com/play/games/atomic-heart/9P7J3S9B472L"),
        CloudCatalogItem("Control", "https://www.xbox.com/play/games/control/BZBTFGR25MG2"),
        CloudCatalogItem("Slay the Spire", "https://www.xbox.com/play/games/slay-the-spire/9MVKWZZ2MZBN"),
        CloudCatalogItem("Cities: Skylines", "https://www.xbox.com/play/games/cities-skylines-remastered/9N0P5VLL9L25"),
        CloudCatalogItem("Battlefield 2042", "https://www.xbox.com/play/games/battlefield-2042/9NJT4P9L5X30"),
        CloudCatalogItem("Batman: Arkham Knight", "https://www.xbox.com/play/games/batman-arkham-knight/BQSVBTFGR25M"),
        CloudCatalogItem("Diablo IV", "https://www.xbox.com/play/games/diablo-iv/9N907L3PCH05")
    )

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

    private fun saveCloudGames(context: Context, items: List<AppItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("label", item.label)
                put("packageName", item.packageName)
                put("cloudUrl", item.cloudUrl ?: "")
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_GAMES, array.toString()).apply()
    }
}
