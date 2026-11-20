package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.util.LruCache
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil

object LogoManager {

    private val netGate = Semaphore(2)
    private val negativeCache = mutableSetOf<String>()

    private val memoryCache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    private val extrudedCache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    private fun getLogosDir(context: Context): File {
        val dir = File(context.filesDir, "logos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getLogoFile(context: Context, id: String): File {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val webpFile = File(getLogosDir(context), "logo_$safeName.webp")
        if (webpFile.exists()) return webpFile
        val pngFile = File(getLogosDir(context), "logo_$safeName.png")
        if (pngFile.exists()) return pngFile
        return webpFile
    }

    fun loadLocalLogo(context: Context, id: String): Bitmap? {
        val cached = memoryCache.get(id)
        if (cached != null && !cached.isRecycled) return cached

        val file = getLogoFile(context, id)
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

    /**
     * Pré-renderiza a extrusão volumétrica 3D do logo fora da main thread (Fase 4.3).
     * Reduz o desenho de 5 camadas no Compose para 1 único Bitmap desenhado a 60/120 fps.
     */
    fun buildExtrudedLogo(src: Bitmap, layers: Int = 5, stepPx: Float = 1.6f): Bitmap {
        val pad = ceil(layers * stepPx).toInt() + 2
        val out = Bitmap.createBitmap(src.width + pad * 2, src.height + pad * 2, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        val p = Paint(Paint.FILTER_BITMAP_FLAG)
        val dark = 0xFF080B10.toInt()
        val mid = 0xFF4A5568.toInt()
        for (i in layers downTo 1) {
            val blendFactor = i / layers.toFloat()
            p.colorFilter = PorterDuffColorFilter(
                ColorUtils.blendARGB(mid, dark, blendFactor),
                PorterDuff.Mode.SRC_IN
            )
            c.drawBitmap(src, pad + i * stepPx, pad + i * stepPx, p)
        }
        p.colorFilter = null
        c.drawBitmap(src, pad.toFloat(), pad.toFloat(), p)
        return out
    }

    fun getExtrudedLogo(context: Context, id: String, rawLogo: Bitmap): Bitmap {
        val cacheKey = "${id}_${rawLogo.generationId}"
        val cached = extrudedCache.get(cacheKey)
        if (cached != null && !cached.isRecycled) return cached

        // Redimensiona para o tamanho de exibição ideal da UI antes de extrusar
        val maxW = 500
        val maxH = 160
        val scaled = if (rawLogo.width > maxW || rawLogo.height > maxH) {
            val scale = (maxW.toFloat() / rawLogo.width).coerceAtMost(maxH.toFloat() / rawLogo.height)
            Bitmap.createScaledBitmap(rawLogo, (rawLogo.width * scale).toInt().coerceAtLeast(1), (rawLogo.height * scale).toInt().coerceAtLeast(1), true)
        } else {
            rawLogo
        }

        val extruded = buildExtrudedLogo(scaled)
        extrudedCache.put(cacheKey, extruded)
        return extruded
    }

    suspend fun saveLogo(context: Context, id: String, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val safeName = id.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val file = File(getLogosDir(context), "logo_$safeName.webp")
        try {
            FileOutputStream(file).use { out ->
                val fmt = if (Build.VERSION.SDK_INT >= 30) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                bitmap.compress(fmt, 100, out)
            }
            memoryCache.put(id, bitmap)
            extrudedCache.evictAll()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteLogo(context: Context, id: String): Boolean {
        memoryCache.remove(id)
        extrudedCache.evictAll()
        val f = getLogoFile(context, id)
        return if (f.exists()) f.delete() else false
    }

    fun clearExtruded(id: String) {
        extrudedCache.evictAll()
    }

    fun clearAllCaches() {
        memoryCache.evictAll()
        extrudedCache.evictAll()
        negativeCache.clear()
    }
}
