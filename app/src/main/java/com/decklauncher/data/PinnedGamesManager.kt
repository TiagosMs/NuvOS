package com.decklauncher.data

import android.content.Context
import android.content.SharedPreferences

object PinnedGamesManager {
    private const val PREFS_NAME = "deck_launcher_pinned_games"
    private const val KEY_PINNED_IDS = "pinned_rom_ids"
    private const val KEY_HIDDEN_IDS = "hidden_from_home_ids"
    private const val KEY_INITIALIZED = "pinned_initialized_v2"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isInitialized(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_INITIALIZED, false)
    }

    fun setInitialized(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_INITIALIZED, true).apply()
    }

    fun getPinnedGameIds(context: Context): Set<String> {
        val prefs = getPrefs(context)
        return prefs.getStringSet(KEY_PINNED_IDS, emptySet()) ?: emptySet()
    }

    fun isPinned(context: Context, gameId: String): Boolean {
        return getPinnedGameIds(context).contains(gameId)
    }

    fun pinGame(context: Context, gameId: String) {
        val prefs = getPrefs(context)
        unhideFromHome(context, gameId)
        val current = prefs.getStringSet(KEY_PINNED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(gameId)
        prefs.edit().putStringSet(KEY_PINNED_IDS, current).apply()
    }

    fun unpinGame(context: Context, gameId: String) {
        val prefs = getPrefs(context)
        val current = prefs.getStringSet(KEY_PINNED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(gameId)
        prefs.edit().putStringSet(KEY_PINNED_IDS, current).apply()
        hideFromHome(context, gameId)
    }

    fun getHiddenFromHomeIds(context: Context): Set<String> {
        val prefs = getPrefs(context)
        return prefs.getStringSet(KEY_HIDDEN_IDS, emptySet()) ?: emptySet()
    }

    fun isHiddenFromHome(context: Context, gameId: String): Boolean {
        return getHiddenFromHomeIds(context).contains(gameId)
    }

    fun hideFromHome(context: Context, gameId: String) {
        val prefs = getPrefs(context)
        val current = prefs.getStringSet(KEY_HIDDEN_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(gameId)
        prefs.edit().putStringSet(KEY_HIDDEN_IDS, current).apply()
    }

    fun unhideFromHome(context: Context, gameId: String) {
        val prefs = getPrefs(context)
        val current = prefs.getStringSet(KEY_HIDDEN_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.remove(gameId)
        prefs.edit().putStringSet(KEY_HIDDEN_IDS, current).apply()
    }

    fun togglePin(context: Context, gameId: String): Boolean {
        return if (isPinned(context, gameId)) {
            unpinGame(context, gameId)
            false
        } else {
            pinGame(context, gameId)
            true
        }
    }
}
