package com.decklauncher.data

import android.content.Context
import android.util.Log
import com.decklauncher.model.AppCategory
import com.decklauncher.model.AppItem
import com.decklauncher.model.ItemType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object AppCacheManager {
    private const val TAG = "AppCacheManager"
    private const val CACHE_FILE_NAME = "apps_cache.json"

    fun loadCachedApps(context: Context): List<AppItem> {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val content = file.readText()
            val array = JSONArray(content)
            val list = ArrayList<AppItem>(array.length())
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val catStr = obj.optString("category", AppCategory.APPS.name)
                val category = try { AppCategory.valueOf(catStr) } catch (_: Exception) { AppCategory.APPS }

                val folderCatStr = if (obj.has("folderCategory")) obj.optString("folderCategory") else null
                val folderCat = if (!folderCatStr.isNullOrEmpty() && folderCatStr != "null") {
                    try { FolderCategory.valueOf(folderCatStr) } catch (_: Exception) { null }
                } else null

                val itemTypeStr = obj.optString("itemType", ItemType.APP.name)
                val itemType = try { ItemType.valueOf(itemTypeStr) } catch (_: Exception) { ItemType.APP }

                list.add(
                    AppItem(
                        id = obj.getString("id"),
                        label = obj.getString("label"),
                        packageName = obj.getString("packageName"),
                        activityName = if (obj.has("activityName")) obj.optString("activityName").takeIf { it.isNotEmpty() && it != "null" } else null,
                        category = category,
                        folderCategory = folderCat,
                        itemType = itemType,
                        romPath = if (obj.has("romPath")) obj.optString("romPath").takeIf { it.isNotEmpty() && it != "null" } else null,
                        cloudUrl = if (obj.has("cloudUrl")) obj.optString("cloudUrl").takeIf { it.isNotEmpty() && it != "null" } else null,
                        coverPath = if (obj.has("coverPath")) obj.optString("coverPath").takeIf { it.isNotEmpty() && it != "null" } else null
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error loading cached apps", e)
            emptyList()
        }
    }

    fun saveCachedApps(context: Context, apps: List<AppItem>) {
        try {
            val array = JSONArray()
            for (app in apps) {
                val obj = JSONObject()
                obj.put("id", app.id)
                obj.put("label", app.label)
                obj.put("packageName", app.packageName)
                if (app.activityName != null) obj.put("activityName", app.activityName)
                obj.put("category", app.category.name)
                if (app.folderCategory != null) obj.put("folderCategory", app.folderCategory.name)
                obj.put("itemType", app.itemType.name)
                if (app.romPath != null) obj.put("romPath", app.romPath)
                if (app.cloudUrl != null) obj.put("cloudUrl", app.cloudUrl)
                if (app.coverPath != null) obj.put("coverPath", app.coverPath)
                array.put(obj)
            }
            val file = File(context.filesDir, CACHE_FILE_NAME)
            file.writeText(array.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cached apps", e)
        }
    }
}
