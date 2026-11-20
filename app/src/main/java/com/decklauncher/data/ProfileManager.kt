package com.decklauncher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class AvatarPreset(
    val id: String,
    val name: String,
    val primaryColor: Long,
    val accentColor: Long,
    val initial: String
)

object ProfileManager {
    private const val PREFS_NAME = "deck_profile_prefs"
    private const val KEY_PROFILE_NAME = "profile_name"
    private const val KEY_AVATAR_ID = "avatar_id"

    const val CUSTOM_AVATAR_ID = "custom_gallery"

    var avatarVersion by mutableIntStateOf(0)
        private set

    val PRESET_AVATARS = listOf(
        AvatarPreset("deck_blue", "Steam Deck", 0xFF1A73E8, 0xFF4285F4, "D"),
        AvatarPreset("cyber_red", "Cyber Neon", 0xFFE50914, 0xFFFF4D4D, "C"),
        AvatarPreset("retro_green", "Retro Matrix", 0xFF10B981, 0xFF34D399, "R"),
        AvatarPreset("stealth_dark", "Stealth Gold", 0xFFF59E0B, 0xFFFBBF24, "S"),
        AvatarPreset("galaxy_purple", "Cosmic Purple", 0xFF8B5CF6, 0xFFA78BFA, "X"),
        AvatarPreset("ember_orange", "Ember Blaze", 0xFFF97316, 0xFFFB923C, "E")
    )

    private var cachedCustomAvatar: Bitmap? = null

    fun getProfileName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PROFILE_NAME, "Tiago") ?: "Tiago"
    }

    fun setProfileName(context: Context, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PROFILE_NAME, name.trim()).apply()
    }

    fun getAvatarId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_AVATAR_ID, "deck_blue") ?: "deck_blue"
    }

    fun setAvatarId(context: Context, avatarId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_AVATAR_ID, avatarId).apply()
        avatarVersion++
    }

    fun getActivePreset(context: Context): AvatarPreset {
        val currentId = getAvatarId(context)
        return PRESET_AVATARS.firstOrNull { it.id == currentId } ?: PRESET_AVATARS.first()
    }

    fun getCustomAvatarFile(context: Context): File {
        return File(context.filesDir, "profile_avatar.webp")
    }

    fun hasCustomAvatar(context: Context): Boolean {
        return getCustomAvatarFile(context).exists()
    }

    fun loadCustomAvatar(context: Context): Bitmap? {
        if (cachedCustomAvatar != null && !cachedCustomAvatar!!.isRecycled) {
            return cachedCustomAvatar
        }
        val file = getCustomAvatarFile(context)
        if (file.exists()) {
            return try {
                val bmp = BitmapFactory.decodeFile(file.absolutePath)
                cachedCustomAvatar = bmp
                bmp
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    fun saveCustomAvatar(context: Context, bitmap: Bitmap): Boolean {
        val size = 384
        val minEdge = bitmap.width.coerceAtMost(bitmap.height)
        val cropX = (bitmap.width - minEdge) / 2
        val cropY = (bitmap.height - minEdge) / 2
        val cropped = Bitmap.createBitmap(bitmap, cropX, cropY, minEdge, minEdge)
        val scaled = if (cropped.width != size || cropped.height != size) {
            Bitmap.createScaledBitmap(cropped, size, size, true)
        } else {
            cropped
        }

        val file = getCustomAvatarFile(context)
        return try {
            FileOutputStream(file).use { out ->
                val fmt = if (Build.VERSION.SDK_INT >= 30) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                scaled.compress(fmt, 95, out)
            }
            cachedCustomAvatar = scaled
            setAvatarId(context, CUSTOM_AVATAR_ID)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun saveCustomAvatarFromUri(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
            val bmp = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bmp != null) {
                return@withContext saveCustomAvatar(context, bmp)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        false
    }

    fun deleteCustomAvatar(context: Context): Boolean {
        cachedCustomAvatar = null
        val f = getCustomAvatarFile(context)
        val deleted = if (f.exists()) f.delete() else false
        setAvatarId(context, "deck_blue")
        return deleted
    }
}
