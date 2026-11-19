package com.decklauncher.data

import android.content.Context
import androidx.compose.ui.graphics.Color

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

    val PRESET_AVATARS = listOf(
        AvatarPreset("deck_blue", "Steam Deck", 0xFF1A73E8, 0xFF4285F4, "D"),
        AvatarPreset("cyber_red", "Cyber Neon", 0xFFE50914, 0xFFFF4D4D, "C"),
        AvatarPreset("retro_green", "Retro Matrix", 0xFF10B981, 0xFF34D399, "R"),
        AvatarPreset("stealth_dark", "Stealth Gold", 0xFFF59E0B, 0xFFFBBF24, "S"),
        AvatarPreset("galaxy_purple", "Cosmic Purple", 0xFF8B5CF6, 0xFFA78BFA, "X"),
        AvatarPreset("ember_orange", "Ember Blaze", 0xFFF97316, 0xFFFB923C, "E")
    )

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
    }

    fun getActivePreset(context: Context): AvatarPreset {
        val currentId = getAvatarId(context)
        return PRESET_AVATARS.firstOrNull { it.id == currentId } ?: PRESET_AVATARS.first()
    }
}
