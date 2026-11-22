package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.decklauncher.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * Adaptive Aura Manager (Feature 4 Next-Gen):
 * Extrai a cor cromática dominante da capa/arte de cada jogo para criar iluminação ambiente OLED no topo.
 */
object AdaptiveAuraManager {
    private val colorCache = LruCache<String, Color>(64)

    fun getCachedAuraColor(appId: String?): Color? {
        if (appId == null) return null
        return colorCache.get(appId)
    }

    suspend fun getAuraColor(context: Context, app: AppItem?, fallbackAccent: Color): Color {
        if (app == null) return fallbackAccent
        val cached = colorCache.get(app.id)
        if (cached != null) return cached

        return withContext(Dispatchers.IO) {
            try {
                val coverFile = app.coverPath?.let { File(it) }?.takeIf { it.exists() }
                val targetFile = coverFile ?: HeroManager.getHeroFile(context, app.id).takeIf { it.exists() }

                if (targetFile != null) {
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = 8 // decodifica micro miniatura para zero consumo de memoria
                    }
                    val bmp = BitmapFactory.decodeFile(targetFile.absolutePath, opts)
                    if (bmp != null) {
                        val extracted = extractDominantVibrantColor(bmp)
                        bmp.recycle()
                        if (extracted != null) {
                            colorCache.put(app.id, extracted)
                            return@withContext extracted
                        }
                    }
                }
            } catch (_: Exception) {}
            colorCache.put(app.id, fallbackAccent)
            fallbackAccent
        }
    }

    private fun extractDominantVibrantColor(bitmap: Bitmap): Color? {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 0 || height <= 0) return null

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var bestColor: Color? = null
        var maxScore = -1f

        // Amostra a cada 4 pixels para máxima velocidade
        val step = max(1, pixels.size / 64)
        for (i in pixels.indices step step) {
            val c = pixels[i]
            val r = (c shr 16 and 0xFF) / 255f
            val g = (c shr 8 and 0xFF) / 255f
            val b = (c and 0xFF) / 255f

            val maxC = max(r, max(g, b))
            val minC = min(r, min(g, b))
            val delta = maxC - minC
            val saturation = if (maxC > 0f) delta / maxC else 0f
            val brightness = maxC

            // Prioriza cores saturadas vibrantes que não sejam nem brancas nem pretas absolutas
            if (saturation in 0.35f..0.95f && brightness in 0.30f..0.88f) {
                val score = saturation * 1.5f + brightness
                if (score > maxScore) {
                    maxScore = score
                    bestColor = Color(r, g, b, 1.0f)
                }
            }
        }

        return bestColor
    }
}
