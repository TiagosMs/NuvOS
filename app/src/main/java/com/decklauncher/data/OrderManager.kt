package com.decklauncher.data

import android.content.Context
import com.decklauncher.model.AppItem
import org.json.JSONArray

object OrderManager {
    private const val PREFS_NAME = "deck_item_order_prefs"
    private const val KEY_PREFIX = "order_"

    fun getCustomOrder(context: Context, categoryKey: String): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PREFIX + categoryKey, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCustomOrder(context: Context, categoryKey: String, itemIds: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        itemIds.forEach { jsonArray.put(it) }
        prefs.edit().putString(KEY_PREFIX + categoryKey, jsonArray.toString()).apply()
    }

    fun applyOrder(context: Context, categoryKey: String, items: List<AppItem>): List<AppItem> {
        val order = getCustomOrder(context, categoryKey)
        if (order.isEmpty()) return items

        val orderMap = order.mapIndexed { index, id -> id to index }.toMap()
        return items.sortedWith(Comparator { a, b ->
            val idxA = orderMap[a.id]
            val idxB = orderMap[b.id]
            when {
                idxA != null && idxB != null -> idxA.compareTo(idxB)
                idxA != null -> -1
                idxB != null -> 1
                else -> 0
            }
        })
    }

    fun swapItems(
        context: Context,
        categoryKey: String,
        currentList: List<AppItem>,
        fromIndex: Int,
        toIndex: Int
    ): List<AppItem> {
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices || fromIndex == toIndex) {
            return currentList
        }

        val mutable = currentList.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)

        val newIds = mutable.map { it.id }
        saveCustomOrder(context, categoryKey, newIds)
        return mutable
    }
}
