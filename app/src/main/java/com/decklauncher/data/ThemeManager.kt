package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class BackgroundMode(val id: String, val label: String) {
    IMMERSIVE("immersive", "Imersivo (Arte do Jogo)"),
    WALLPAPER("wallpaper", "Papel de Parede Estático"),
    SOLID("solid", "Sóbrio (SteamOS Escuro)"),
    OLED("oled", "Preto Puro OLED")
}

data class ThemeColorPreset(
    val id: String,
    val name: String,
    val colorValue: Long
)

object ThemeManager {
    private const val PREFS_NAME = "deck_theme_prefs"
    private const val KEY_BG_MODE = "bg_mode"
    private const val KEY_ACCENT_COLOR = "accent_color"

    val COLOR_PRESETS = listOf(
        ThemeColorPreset("red", "Red Classic", 0xFFFF0000),
        ThemeColorPreset("yellow", "Yellow Bright", 0xFFFFE200),
        ThemeColorPreset("blue", "Electric Blue", 0xFF000FFF),
        ThemeColorPreset("pink", "Hot Pink", 0xFFFF0077),
        ThemeColorPreset("white", "Pure White", 0xFFFFFFFF),
        ThemeColorPreset("black", "Deep Black", 0xFF000000),
        ThemeColorPreset("purple", "Soft Violet", 0xFF977CFF),
        ThemeColorPreset("teal", "Jade Green", 0xFF429A73),
        ThemeColorPreset("forest", "Olive / Forest Green", 0xFF627B56),
        ThemeColorPreset("crimson", "Crimson Red", 0xFFE00000),
        ThemeColorPreset("silver", "Slate Gray", 0xFFD9D9D9)
    )

    private var cachedWallpaper: Bitmap? = null

    fun getBackgroundMode(context: Context): BackgroundMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_BG_MODE, BackgroundMode.IMMERSIVE.id) ?: BackgroundMode.IMMERSIVE.id
        return BackgroundMode.values().firstOrNull { it.id == id } ?: BackgroundMode.IMMERSIVE
    }

    fun setBackgroundMode(context: Context, mode: BackgroundMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BG_MODE, mode.id).apply()
    }

    fun getAccentColorLong(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_ACCENT_COLOR, 0xFF627B56)
    }

    fun getAccentColor(context: Context): Color {
        return Color(getAccentColorLong(context))
    }

    fun setAccentColor(context: Context, colorValue: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_ACCENT_COLOR, colorValue).apply()
    }

    private fun getWallpaperFile(context: Context): File {
        return File(context.filesDir, "custom_wallpaper.png")
    }

    fun hasCustomWallpaper(context: Context): Boolean {
        return getWallpaperFile(context).exists()
    }

    fun loadCustomWallpaper(context: Context): Bitmap? {
        if (cachedWallpaper != null && !cachedWallpaper!!.isRecycled) return cachedWallpaper
        val file = getWallpaperFile(context)
        if (!file.exists()) return null
        return try {
            val boundsOnly = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOnly)
            val dm = context.resources.displayMetrics
            val maxDim = maxOf(dm.widthPixels, dm.heightPixels).coerceAtLeast(1080)
            var sample = 1
            while (boundsOnly.outWidth / (sample * 2) >= maxDim && boundsOnly.outHeight / (sample * 2) >= maxDim) {
                sample *= 2
            }
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val bmp = BitmapFactory.decodeFile(file.absolutePath, opts)
            cachedWallpaper = bmp
            bmp
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveCustomWallpaperFromUri(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
            val bmp = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bmp != null) {
                val file = getWallpaperFile(context)
                FileOutputStream(file).use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }
                cachedWallpaper = bmp
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        false
    }
}
