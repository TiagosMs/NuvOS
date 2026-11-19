package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object HeroManager {

    private val memoryCache = mutableMapOf<String, Bitmap>()

    private fun getHeroesDir(context: Context): File {
        val dir = File(context.filesDir, "heroes")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getHeroFile(context: Context, id: String): File {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return File(getHeroesDir(context), "hero_$safeName.png")
    }

    fun loadLocalHero(context: Context, id: String): Bitmap? {
        val cached = memoryCache[id]
        if (cached != null && !cached.isRecycled) return cached

        val file = getHeroFile(context, id)
        if (file.exists()) {
            return try {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                if (bmp != null) memoryCache[id] = bmp
                bmp
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    suspend fun saveHero(context: Context, id: String, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val file = getHeroFile(context, id)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
            }
            memoryCache[id] = bitmap
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getOrFetchHero(context: Context, id: String, title: String): Bitmap? = withContext(Dispatchers.IO) {
        // 1. Tenta carregar do cache local
        val local = loadLocalHero(context, id)
        if (local != null) return@withContext local

        // 2. Busca arte 16:9 de herói no SteamGridDB
        val apiKey = SteamGridDbManager.getApiKey(context)
        if (apiKey.isBlank()) return@withContext null

        try {
            val games = SteamGridDbManager.searchGames(context, title)
            if (games.isNotEmpty()) {
                val gameId = games.first().id
                val heroUrl = fetchHeroArtworkUrl(apiKey, gameId)
                if (!heroUrl.isNullOrBlank()) {
                    val downloaded = downloadHeroBitmap(heroUrl)
                    if (downloaded != null) {
                        saveHero(context, id, downloaded)
                        return@withContext downloaded
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Fallback: Se não encontrar hero 16:9, usa a capa local
        val cover = CoverManager.loadLocalCover(context, id)
        if (cover != null) {
            memoryCache[id] = cover
            return@withContext cover
        }

        null
    }

    private suspend fun fetchHeroArtworkUrl(apiKey: String, gameId: Long): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.steamgriddb.com/api/v2/heroes/game/$gameId")
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
                    val data = root.optJSONArray("data")
                    if (data != null && data.length() > 0) {
                        val first = data.getJSONObject(0)
                        return@withContext first.getString("url")
                    }
                }
            }
        } catch (_: Exception) {}
        null
    }

    private suspend fun downloadHeroBitmap(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "DeckLauncher/1.0 (Android)")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val stream = conn.inputStream
                // Downsample para economizar memória mantendo alta qualidade de fundo desfocado
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                val bmp = BitmapFactory.decodeStream(stream, null, options)
                stream.close()
                return@withContext bmp
            }
        } catch (_: Exception) {}
        null
    }
}
