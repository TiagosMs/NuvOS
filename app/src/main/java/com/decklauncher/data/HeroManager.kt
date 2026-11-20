package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object HeroManager {

    private val netGate = Semaphore(2)
    private val negativeCache = mutableSetOf<String>()

    private val memoryCache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    private fun getHeroesDir(context: Context): File {
        val dir = File(context.filesDir, "heroes")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getHeroFile(context: Context, id: String): File {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val webpFile = File(getHeroesDir(context), "hero_$safeName.webp")
        if (webpFile.exists()) return webpFile
        val pngFile = File(getHeroesDir(context), "hero_$safeName.png")
        if (pngFile.exists()) return pngFile
        return webpFile
    }

    fun getHeroBlurFile(context: Context, id: String): File {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val webpFile = File(getHeroesDir(context), "blur_$safeName.webp")
        if (webpFile.exists()) return webpFile
        val pngFile = File(getHeroesDir(context), "blur_$safeName.png")
        if (pngFile.exists()) return pngFile
        return webpFile
    }

    fun loadLocalHero(context: Context, id: String): Bitmap? {
        val cached = memoryCache.get(id)
        if (cached != null && !cached.isRecycled) return cached

        val file = getHeroFile(context, id)
        if (file.exists()) {
            return try {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                if (bmp != null) memoryCache.put(id, bmp)
                bmp
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    suspend fun saveHero(context: Context, id: String, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val heroFile = File(getHeroesDir(context), "hero_$safeName.webp")
        val blurFile = File(getHeroesDir(context), "blur_$safeName.webp")

        // 1. Redimensiona o Hero principal para máx 1280px mantendo 16:9
        val scaledHero = if (bitmap.width > 1280) {
            val ratio = 1280f / bitmap.width
            Bitmap.createScaledBitmap(bitmap, 1280, (bitmap.height * ratio).toInt().coerceAtLeast(1), true)
        } else {
            bitmap
        }

        // 2. Pré-renderiza a versão de blur reduzida (1/16 do tamanho, ~80x45) para economizar GPU
        val blurWidth = (scaledHero.width / 16).coerceAtLeast(64)
        val blurHeight = (scaledHero.height / 16).coerceAtLeast(36)
        val blurBitmap = Bitmap.createScaledBitmap(scaledHero, blurWidth, blurHeight, true)

        try {
            val fmt = if (Build.VERSION.SDK_INT >= 30) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }

            FileOutputStream(heroFile).use { out ->
                scaledHero.compress(fmt, 85, out)
            }

            FileOutputStream(blurFile).use { out ->
                blurBitmap.compress(fmt, 70, out)
            }

            memoryCache.put(id, scaledHero)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun cleanTitleForSearch(rawTitle: String): String {
        var t = rawTitle
        t = t.replace(Regex("\\.[a-zA-Z0-9]{2,4}$"), "")
        t = t.replace(Regex("\\s*[\\[\\(].*?[\\]\\)]"), "")
        val lower = t.trim().lowercase()
        if (lower.contains("better xcloud") || lower.contains("betterxc")) return "Xbox Cloud Gaming"
        if (lower.contains("hakux")) return "Xbox"
        if (lower.contains("cooking mama")) return "Cooking Mama"
        if (lower.contains("forza horizon 5")) return "Forza Horizon 5"
        if (lower.contains("forza motorsport")) return "Forza Motorsport"
        if (lower.contains("halo infinite")) return "Halo Infinite"
        if (lower.contains("persona 5")) return "Persona 5"
        if (lower.contains("minecraft")) return "Minecraft"
        if (lower.contains("korg ds-10")) return "KORG DS-10"
        if (lower.contains("moonlight")) return "Moonlight"
        if (lower.contains("rhythm heaven")) return "Rhythm Heaven"
        return t.trim()
    }

    fun deleteHero(context: Context, id: String): Boolean {
        memoryCache.remove(id)
        val f1 = getHeroFile(context, id)
        val f2 = getHeroBlurFile(context, id)
        var deleted = false
        if (f1.exists()) deleted = f1.delete() || deleted
        if (f2.exists()) deleted = f2.delete() || deleted
        return deleted
    }

    fun clearAllCaches() {
        memoryCache.evictAll()
        negativeCache.clear()
    }

    suspend fun getOrFetchHero(context: Context, id: String, title: String): Bitmap? = withContext(Dispatchers.IO) {
        val local = loadLocalHero(context, id)
        if (local != null) return@withContext local

        if (negativeCache.contains(id)) return@withContext null

        val cleanTitle = cleanTitleForSearch(title)
        if (cleanTitle.isBlank()) return@withContext null

        val preferredProvider = ArtworkProviderManager.getPreferredProvider(context)
        val sgdbKey = SteamGridDbManager.getApiKey(context)
        val iidbKey = IidbManager.getApiKey(context)

        if (sgdbKey.isBlank() && iidbKey.isBlank()) return@withContext null

        netGate.withPermit {
            // Se o preferido for iiDB, tenta iiDB primeiro
            if (preferredProvider == ArtworkProvider.IIDB && iidbKey.isNotBlank()) {
                val downloaded = fetchHeroFromIidb(context, id, cleanTitle)
                if (downloaded != null) return@withContext downloaded
            }

            // Tenta SteamGridDB
            if (sgdbKey.isNotBlank()) {
                val downloaded = fetchHeroFromSgdb(context, id, cleanTitle, sgdbKey)
                if (downloaded != null) return@withContext downloaded
            }

            // Fallback para iiDB
            if (preferredProvider == ArtworkProvider.STEAMGRID_DB && iidbKey.isNotBlank()) {
                val downloaded = fetchHeroFromIidb(context, id, cleanTitle)
                if (downloaded != null) return@withContext downloaded
            }

            val cover = CoverManager.loadLocalCover(context, id)
            if (cover != null) {
                memoryCache.put(id, cover)
                return@withContext cover
            }

            negativeCache.add(id)
            null
        }
    }

    private suspend fun fetchHeroFromIidb(context: Context, id: String, cleanTitle: String): Bitmap? {
        try {
            val games = IidbManager.searchGames(context, cleanTitle)
            if (games.isNotEmpty()) {
                val game = games.first()
                val assets = IidbManager.getArtworkForParent(context, game.id)
                val heroUrl = assets?.bannerUrl
                if (!heroUrl.isNullOrBlank()) {
                    val downloaded = downloadHeroBitmap(heroUrl)
                    if (downloaded != null) {
                        saveHero(context, id, downloaded)
                        return downloaded
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private suspend fun fetchHeroFromSgdb(context: Context, id: String, cleanTitle: String, apiKey: String): Bitmap? {
        try {
            val games = SteamGridDbManager.searchGames(context, cleanTitle)
            if (games.isNotEmpty()) {
                val bestMatch = games.firstOrNull { it.name.equals(cleanTitle, ignoreCase = true) }
                    ?: games.firstOrNull { it.name.contains(cleanTitle, ignoreCase = true) }
                    ?: games.first()

                val heroUrl = fetchHeroArtworkUrl(apiKey, bestMatch.id)
                if (!heroUrl.isNullOrBlank()) {
                    val downloaded = downloadHeroBitmap(heroUrl)
                    if (downloaded != null) {
                        saveHero(context, id, downloaded)
                        return downloaded
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private suspend fun fetchHeroArtworkUrl(apiKey: String, gameId: Long): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://www.steamgriddb.com/api/v2/heroes/game/$gameId")
                .header("Authorization", "Bearer $apiKey")
                .header("User-Agent", "DeckLauncher/1.0 (Android)")
                .build()

            CoverManager.httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.body != null) {
                    val jsonStr = response.body!!.string()
                    val root = JSONObject(jsonStr)
                    if (root.optBoolean("success", false)) {
                        val data = root.optJSONArray("data")
                        if (data != null && data.length() > 0) {
                            return@withContext data.getJSONObject(0).getString("url")
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        null
    }

    private suspend fun downloadHeroBitmap(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", "DeckLauncher/1.0 (Android)")
                .build()

            CoverManager.httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.body != null) {
                    val bytes = response.body!!.bytes()
                    val options = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
                }
            }
        } catch (_: Exception) {}
        null
    }
}
