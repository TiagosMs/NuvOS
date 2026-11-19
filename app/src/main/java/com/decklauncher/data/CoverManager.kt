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
import java.net.URLEncoder

object CoverManager {

    private fun getCoversDir(context: Context): File {
        val dir = File(context.filesDir, "covers")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getCoverFile(context: Context, id: String): File {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return File(getCoversDir(context), "$safeName.png")
    }

    fun loadLocalCover(context: Context, id: String): Bitmap? {
        val file = getCoverFile(context, id)
        return if (file.exists()) {
            try {
                BitmapFactory.decodeFile(file.absolutePath)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    suspend fun saveCover(context: Context, id: String, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val file = getCoverFile(context, id)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun downloadCover(context: Context, id: String, imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "DeckLauncher/1.0 (Android)")
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val input = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(input)
                input.close()
                if (bitmap != null) {
                    saveCover(context, id, bitmap)
                    return@withContext bitmap
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun searchLibretroCover(context: Context, id: String, cleanTitle: String): Bitmap? = withContext(Dispatchers.IO) {
        val systems = listOf(
            "Nintendo_-_Nintendo_DS",
            "Nintendo_-_Nintendo_Switch",
            "Microsoft_-_Xbox",
            "Sony_-_PlayStation_Portable",
            "Nintendo_-_Game_Boy_Advance",
            "Nintendo_-_Nintendo_64",
            "Sony_-_PlayStation",
            "Sony_-_PlayStation_2"
        )

        val encodedTitle = URLEncoder.encode(cleanTitle, "UTF-8").replace("+", "%20")
        val variations = listOf(
            cleanTitle,
            "$cleanTitle (USA)",
            "$cleanTitle (US)",
            "$cleanTitle (World)",
            "$cleanTitle (Europe)"
        )

        for (system in systems) {
            for (titleVar in variations) {
                val enc = URLEncoder.encode(titleVar, "UTF-8").replace("+", "%20")
                val url = "https://raw.githubusercontent.com/libretro-thumbnails/$system/master/Named_Boxarts/$enc.png"
                val bmp = downloadCover(context, id, url)
                if (bmp != null) return@withContext bmp
            }
        }
        null
    }
}
