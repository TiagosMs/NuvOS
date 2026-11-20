package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object CoverManager {

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    private fun getCoversDir(context: Context): File {
        val dir = File(context.filesDir, "covers")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getCoverFile(context: Context, id: String): File {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val webpFile = File(getCoversDir(context), "$safeName.webp")
        if (webpFile.exists()) return webpFile
        val pngFile = File(getCoversDir(context), "$safeName.png")
        if (pngFile.exists()) return pngFile
        return webpFile
    }

    fun getCoverPath(context: Context, id: String): String? {
        val file = getCoverFile(context, id)
        return if (file.exists()) file.absolutePath else null
    }

    fun deleteCover(context: Context, id: String): Boolean {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val webpFile = File(getCoversDir(context), "$safeName.webp")
        val pngFile = File(getCoversDir(context), "$safeName.png")
        var deleted = false
        if (webpFile.exists()) deleted = webpFile.delete() || deleted
        if (pngFile.exists()) deleted = pngFile.delete() || deleted
        return deleted
    }

    fun loadLocalCover(context: Context, id: String): Bitmap? {
        val path = getCoverPath(context, id) ?: return null
        return try {
            BitmapFactory.decodeFile(path)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveCover(context: Context, id: String, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val file = File(getCoversDir(context), "$safeName.webp")

        // Redimensiona para proporção ideal de capa (máx 360 x 540) para economizar RAM
        val scaled = if (bitmap.width > 360 || bitmap.height > 540) {
            val scale = (360f / bitmap.width).coerceAtMost(540f / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            bitmap
        }

        try {
            FileOutputStream(file).use { out ->
                val fmt = if (Build.VERSION.SDK_INT >= 30) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                scaled.compress(fmt, 85, out)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun downloadCover(context: Context, id: String, imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", "DeckLauncher/1.0 (Android)")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful && response.body != null) {
                    val bytes = response.body!!.bytes()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        saveCover(context, id, bitmap)
                        return@withContext bitmap
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun searchLibretroCover(context: Context, id: String, cleanTitle: String, systemFolder: String? = null): Bitmap? = withContext(Dispatchers.IO) {
        val systems = if (systemFolder != null) {
            listOf(systemFolder)
        } else {
            listOf(
                "Nintendo_-_Nintendo_DS",
                "Nintendo_-_Nintendo_Switch",
                "Microsoft_-_Xbox",
                "Sony_-_PlayStation_Portable",
                "Nintendo_-_Game_Boy_Advance",
                "Nintendo_-_Nintendo_64",
                "Sony_-_PlayStation",
                "Sony_-_PlayStation_2"
            )
        }

        val variations = listOf(
            cleanTitle,
            "$cleanTitle (USA)",
            "$cleanTitle (World)"
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
