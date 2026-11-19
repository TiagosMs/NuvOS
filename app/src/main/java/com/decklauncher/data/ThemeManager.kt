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
    SOLID("solid", "Sóbrio (SteamOS Escuro)")
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
        ThemeColorPreset("blue", "Steam Blue", 0xFF1A73E8),
        ThemeColorPreset("red", "Cyber Red", 0xFFE50914),
        ThemeColorPreset("green", "Matrix Green", 0xFF10B981),
        ThemeColorPreset("gold", "Amber Gold", 0xFFF59E0B),
        ThemeColorPreset("purple", "Cosmic Purple", 0xFF8B5CF6),
        ThemeColorPreset("cyan", "Electric Cyan", 0xFF06B6D4),
        ThemeColorPreset("white", "Minimal White", 0xFFFFFFFF)
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
        return prefs.getLong(KEY_ACCENT_COLOR, 0xFF1A73E8)
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
            val bmp = BitmapFactory.decodeFile(file.absolutePath)
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
