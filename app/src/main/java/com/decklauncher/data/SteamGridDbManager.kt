package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class SteamGridGame(
    val id: Long,
    val name: String,
    val releaseDate: Long? = null
)

data class SteamGridArtwork(
    val id: Long,
    val url: String,
    val thumbUrl: String,
    val width: Int,
    val height: Int,
    val style: String
)

object SteamGridDbManager {

    private const val PREFS_NAME = "steamgriddb_prefs"
    private const val KEY_API = "api_key"
    private const val BASE_URL = "https://www.steamgriddb.com/api/v2"

    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_API, null)?.trim()
        if (!saved.isNullOrBlank()) return saved
        return com.decklauncher.BuildConfig.SGDB_API_KEY.trim()
    }

    fun setApiKey(context: Context, apiKey: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (apiKey.isNullOrBlank()) {
                remove(KEY_API)
            } else {
                putString(KEY_API, apiKey.trim())
            }
            apply()
        }
    }

    fun hasApiKey(context: Context): Boolean {
        return getApiKey(context).isNotBlank()
    }

    suspend fun searchGames(context: Context, query: String): List<SteamGridGame> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        if (query.isBlank() || apiKey.isBlank()) return@withContext emptyList()

        try {
            val enc = URLEncoder.encode(query.trim(), "UTF-8").replace("+", "%20")
            val url = URL("$BASE_URL/search/autocomplete/$enc")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("User-Agent", "DeckLauncher/1.0 (Android)")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.optJSONArray("data") ?: return@withContext emptyList()
                    val result = mutableListOf<SteamGridGame>()
                    for (i in 0 until data.length()) {
                        val item = data.getJSONObject(i)
                        result.add(
                            SteamGridGame(
                                id = item.getLong("id"),
                                name = item.getString("name"),
                                releaseDate = if (item.has("release_date") && !item.isNull("release_date")) item.getLong("release_date") else null
                            )
                        )
                    }
                    return@withContext result
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyList()
    }

    suspend fun getGridsForGame(context: Context, gameId: Long): List<SteamGridArtwork> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        if (apiKey.isBlank()) return@withContext emptyList()

        val dimensions = listOf("600x900", "any")
        for (dim in dimensions) {
            try {
                val dimParam = if (dim == "any") "" else "?dimensions=$dim"
                val url = URL("$BASE_URL/grids/game/$gameId$dimParam")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    setRequestProperty("User-Agent", "DeckLauncher/1.0 (Android)")
                }

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(jsonStr)
                    if (root.optBoolean("success", false)) {
                        val data = root.optJSONArray("data") ?: continue
                        val list = mutableListOf<SteamGridArtwork>()
                        for (i in 0 until data.length()) {
                            val obj = data.getJSONObject(i)
                            list.add(
                                SteamGridArtwork(
                                    id = obj.getLong("id"),
                                    url = obj.getString("url"),
                                    thumbUrl = obj.optString("thumb", obj.getString("url")),
                                    width = obj.optInt("width", 600),
                                    height = obj.optInt("height", 900),
                                    style = obj.optString("style", "alternate")
                                )
                            )
                        }
                        if (list.isNotEmpty()) {
                            return@withContext list
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        emptyList()
    }

    suspend fun downloadAndApplyGrid(context: Context, appId: String, artworkUrl: String): Bitmap? {
        return CoverManager.downloadCover(context, appId, artworkUrl)
    }

    suspend fun getHeroesForGame(context: Context, gameId: Long): List<SteamGridArtwork> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        if (apiKey.isBlank()) return@withContext emptyList()

        try {
            val url = URL("$BASE_URL/heroes/game/$gameId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("User-Agent", "DeckLauncher/1.0 (Android)")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.optJSONArray("data") ?: return@withContext emptyList()
                    val list = mutableListOf<SteamGridArtwork>()
                    for (i in 0 until data.length()) {
                        val obj = data.getJSONObject(i)
                        list.add(
                            SteamGridArtwork(
                                id = obj.getLong("id"),
                                url = obj.getString("url"),
                                thumbUrl = obj.optString("thumb", obj.getString("url")),
                                width = obj.optInt("width", 1920),
                                height = obj.optInt("height", 1080),
                                style = obj.optString("style", "alternate")
                            )
                        )
                    }
                    return@withContext list
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyList()
    }

    suspend fun getLogosForGame(context: Context, gameId: Long): List<SteamGridArtwork> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        if (apiKey.isBlank()) return@withContext emptyList()

        try {
            val url = URL("$BASE_URL/logos/game/$gameId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("User-Agent", "DeckLauncher/1.0 (Android)")
            }

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                if (root.optBoolean("success", false)) {
                    val data = root.optJSONArray("data") ?: return@withContext emptyList()
                    val list = mutableListOf<SteamGridArtwork>()
                    for (i in 0 until data.length()) {
                        val obj = data.getJSONObject(i)
                        list.add(
                            SteamGridArtwork(
                                id = obj.getLong("id"),
                                url = obj.getString("url"),
                                thumbUrl = obj.optString("thumb", obj.getString("url")),
                                width = obj.optInt("width", 800),
                                height = obj.optInt("height", 400),
                                style = obj.optString("style", "official")
                            )
                        )
                    }
                    return@withContext list
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        emptyList()
    }

    suspend fun downloadAndApplyHero(context: Context, appId: String, heroUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(heroUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "DeckLauncher/1.0 (Android)")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val bmp = android.graphics.BitmapFactory.decodeStream(conn.inputStream)
                if (bmp != null) {
                    HeroManager.saveHero(context, appId, bmp)
                    return@withContext bmp
                }
            }
        } catch (_: Exception) {}
        null
    }

    suspend fun downloadAndApplyLogo(context: Context, appId: String, logoUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(logoUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "DeckLauncher/1.0 (Android)")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val bmp = android.graphics.BitmapFactory.decodeStream(conn.inputStream)
                if (bmp != null) {
                    LogoManager.saveLogo(context, appId, bmp)
                    return@withContext bmp
                }
            }
        } catch (_: Exception) {}
        null
    }
}
