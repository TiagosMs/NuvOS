package com.decklauncher.shortcut

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Build
import android.os.Process
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import org.json.JSONArray
import org.json.JSONObject

data class PinnedShortcutRecord(
    val id: String,
    val packageName: String,
    val label: String,
    val timestamp: Long
)

object ShortcutManager {

    private const val PREFS_NAME = "pinned_shortcuts_prefs"
    private const val KEY_SHORTCUTS = "pinned_shortcuts_list"

    fun getPinnedShortcuts(context: Context): List<PinnedShortcutRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SHORTCUTS, null) ?: return emptyList()
        val list = mutableListOf<PinnedShortcutRecord>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PinnedShortcutRecord(
                        id = obj.getString("id"),
                        packageName = obj.getString("packageName"),
                        label = obj.getString("label"),
                        timestamp = obj.optLong("timestamp", 0L)
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun savePinnedShortcut(context: Context, id: String, packageName: String, label: String) {
        val current = getPinnedShortcuts(context).filter { it.id != id }.toMutableList()
        current.add(PinnedShortcutRecord(id, packageName, label, System.currentTimeMillis()))
        val arr = JSONArray()
        for (item in current) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("packageName", item.packageName)
                put("label", item.label)
                put("timestamp", item.timestamp)
            }
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SHORTCUTS, arr.toString())
            .apply()
    }

    fun removePinnedShortcut(context: Context, id: String) {
        val current = getPinnedShortcuts(context).filter { it.id != id }
        val arr = JSONArray()
        for (item in current) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("packageName", item.packageName)
                put("label", item.label)
                put("timestamp", item.timestamp)
            }
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SHORTCUTS, arr.toString())
            .apply()
    }

    fun launchShortcut(context: Context, record: PinnedShortcutRecord): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                if (launcherApps != null) {
                    launcherApps.startShortcut(
                        record.packageName,
                        record.id,
                        null,
                        null,
                        Process.myUserHandle()
                    )
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
}
