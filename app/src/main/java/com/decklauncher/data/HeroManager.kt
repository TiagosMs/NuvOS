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

    fun cleanTitleForSearch(rawTitle: String): String {
        var t = rawTitle
        // Remove extensões de arquivo comuns (.nds, .gba, .sfc, .nes, .iso, etc.)
        t = t.replace(Regex("\\.[a-zA-Z0-9]{2,4}$"), "")
        // Remove tags de região/versão entre parênteses ou colchetes: (USA), (Europe), [!], (v1.0), etc.
        t = t.replace(Regex("\\s*[\\[\\(].*?[\\]\\)]"), "")
        // Casos especiais conhecidos no launcher
        val lower = t.trim().lowercase()
        if (lower.contains("better xcloud") || lower.contains("betterxc")) return "Xbox Cloud Gaming"
        if (lower.contains("hakux")) return "Xbox"
        if (lower.contains("cooking mama")) return "Cooking Mama"
        if (lower.contains("forza horizon 5")) return "Forza Horizon 5"
        if (lower.contains("forza motorsport")) return "Forza Motorsport"
        if (lower.contains("halo infinite")) return "Halo Infinite"
        if (lower.contains("persona 5")) return "Persona 5 Tactica"
        if (lower.contains("minecraft")) return "Minecraft"
        if (lower.contains("korg ds-10")) return "KORG DS-10"
        if (lower.contains("moonlight")) return "Moonlight"
        if (lower.contains("rhythm heaven")) return "Rhythm Heaven"
        return t.trim()
    }

    suspend fun getOrFetchHero(context: Context, id: String, title: String): Bitmap? = withContext(Dispatchers.IO) {
        // 1. Tenta carregar do cache local
        val local = loadLocalHero(context, id)
        if (local != null) return@withContext local

        // 2. Busca arte 16:9 de herói no SteamGridDB com título limpo
        val apiKey = SteamGridDbManager.getApiKey(context)
        if (apiKey.isBlank()) return@withContext null

        val cleanTitle = cleanTitleForSearch(title)
        if (cleanTitle.isBlank()) return@withContext null

        try {
            val games = SteamGridDbManager.searchGames(context, cleanTitle)
            if (games.isNotEmpty()) {
                // Encontra a melhor correspondência em vez de pegar cegamente o primeiro
                val bestMatch = games.firstOrNull { it.name.equals(cleanTitle, ignoreCase = true) }
                    ?: games.firstOrNull { it.name.contains(cleanTitle, ignoreCase = true) }
                    ?: games.first()

                val heroUrl = fetchHeroArtworkUrl(apiKey, bestMatch.id)
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
