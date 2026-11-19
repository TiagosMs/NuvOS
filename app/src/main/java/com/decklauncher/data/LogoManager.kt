package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object LogoManager {

    private val memoryCache = mutableMapOf<String, Bitmap>()

    private fun getLogosDir(context: Context): File {
        val dir = File(context.filesDir, "logos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getLogoFile(context: Context, id: String): File {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return File(getLogosDir(context), "logo_$safeName.png")
    }

    fun loadLocalLogo(context: Context, id: String): Bitmap? {
        val cached = memoryCache[id]
        if (cached != null && !cached.isRecycled) return cached

        val file = getLogoFile(context, id)
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

    suspend fun saveLogo(context: Context, id: String, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val file = getLogoFile(context, id)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            memoryCache[id] = bitmap
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getOrFetchLogo(context: Context, id: String, title: String): Bitmap? = withContext(Dispatchers.IO) {
        // 1. Tenta memória e arquivo local
        val local = loadLocalLogo(context, id)
        if (local != null) return@withContext local

        // 2. Busca logo transparente oficial no SteamGridDB
        val apiKey = SteamGridDbManager.getApiKey(context)
        if (apiKey.isBlank()) return@withContext null

        val cleanTitle = HeroManager.cleanTitleForSearch(title)
        if (cleanTitle.isBlank()) return@withContext null

        try {
            val games = SteamGridDbManager.searchGames(context, cleanTitle)
            if (games.isNotEmpty()) {
                val bestMatch = games.firstOrNull { it.name.equals(cleanTitle, ignoreCase = true) }
                    ?: games.firstOrNull { it.name.contains(cleanTitle, ignoreCase = true) }
                    ?: games.first()

                val logos = SteamGridDbManager.getLogosForGame(context, bestMatch.id)
                if (logos.isNotEmpty()) {
                    val logoUrl = logos.first().url
                    val downloaded = downloadLogoBitmap(logoUrl)
                    if (downloaded != null) {
                        saveLogo(context, id, downloaded)
                        return@withContext downloaded
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        null
    }

    private suspend fun downloadLogoBitmap(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "DeckLauncher/1.0 (Android)")
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                return@withContext BitmapFactory.decodeStream(conn.inputStream)
            }
        } catch (_: Exception) {}
        null
    }
}
