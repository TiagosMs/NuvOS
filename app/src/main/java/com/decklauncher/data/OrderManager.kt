package com.decklauncher.data

import android.content.Context
import com.decklauncher.model.AppItem
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class GameSortMode {
    RECENTS,    // Smart Queue: Mais recentemente jogados primeiro (padrão de console)
    CUSTOM,     // Ordem manual organizada pelo usuário
    NAME_ASC    // Alfabética A-Z
}

object OrderManager {
    private const val PREFS_NAME = "deck_item_order_prefs"
    private const val KEY_PREFIX = "order_"
    private const val KEY_SORT_MODE = "game_sort_mode"
    private const val PREFIX_LAST_PLAYED = "last_played_"
    private const val PREFIX_PLAY_COUNT = "play_count_"
    private const val PREFIX_TOTAL_MINUTES = "total_minutes_"

    fun getSortMode(context: Context): GameSortMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_SORT_MODE, GameSortMode.RECENTS.name)
        return try {
            GameSortMode.valueOf(name ?: GameSortMode.RECENTS.name)
        } catch (_: Exception) {
            GameSortMode.RECENTS
        }
    }

    fun setSortMode(context: Context, mode: GameSortMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SORT_MODE, mode.name).apply()
    }

    fun recordAppLaunch(context: Context, appId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val curCount = prefs.getInt(PREFIX_PLAY_COUNT + appId, 0)
        val curMinutes = prefs.getInt(PREFIX_TOTAL_MINUTES + appId, 0)
        prefs.edit()
            .putLong(PREFIX_LAST_PLAYED + appId, now)
            .putInt(PREFIX_PLAY_COUNT + appId, curCount + 1)
            .putInt(PREFIX_TOTAL_MINUTES + appId, curMinutes + 15) // incrementa estimativa razoavel por sessao
            .apply()
    }

    fun getLastPlayed(context: Context, appId: String): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(PREFIX_LAST_PLAYED + appId, 0L)
    }

    fun getPlayCount(context: Context, appId: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(PREFIX_PLAY_COUNT + appId, 0)
    }

    fun getTotalPlayMinutes(context: Context, appId: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(PREFIX_TOTAL_MINUTES + appId, 0)
    }

    fun formatLastPlayed(context: Context, appId: String): String {
        val time = getLastPlayed(context, appId)
        if (time == 0L) return "Nunca iniciado"
        val diff = System.currentTimeMillis() - time
        return when {
            diff < 5 * 60 * 1000L -> "Jogado agora há pouco"
            diff < 60 * 60 * 1000L -> "Jogado nesta hora"
            diff < 24 * 60 * 60 * 1000L -> "Jogado hoje"
            diff < 48 * 60 * 60 * 1000L -> "Jogado ontem"
            else -> "Jogado em " + SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(time))
        }
    }

    fun formatPlayTime(context: Context, appId: String): String {
        val minutes = getTotalPlayMinutes(context, appId)
        if (minutes < 60) return "${minutes.coerceAtLeast(10)} min"
        val hours = minutes / 60
        val rem = minutes % 60
        return if (rem == 0) "${hours}h" else "${hours}h ${rem}m"
    }

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

    /**
     * Aplica ordenação considerando Smart Queue (Recentes), Custom ou Alfabética.
     */
    fun applyOrder(context: Context, categoryKey: String, items: List<AppItem>): List<AppItem> {
        val mode = getSortMode(context)
        return when (mode) {
            GameSortMode.RECENTS -> {
                // Itens jogados recentemente na frente (ordenados do mais recente para o mais antigo)
                // Itens que nunca foram jogados mantêm a ordem customizada ou alfabética
                val customOrder = getCustomOrder(context, categoryKey)
                val customMap = customOrder.mapIndexed { idx, id -> id to idx }.toMap()

                items.sortedWith(Comparator { a, b ->
                    val lastA = getLastPlayed(context, a.id)
                    val lastB = getLastPlayed(context, b.id)
                    when {
                        lastA > 0L && lastB > 0L -> lastB.compareTo(lastA) // mais recente primeiro
                        lastA > 0L -> -1
                        lastB > 0L -> 1
                        else -> {
                            val idxA = customMap[a.id]
                            val idxB = customMap[b.id]
                            when {
                                idxA != null && idxB != null -> idxA.compareTo(idxB)
                                idxA != null -> -1
                                idxB != null -> 1
                                else -> a.label.compareTo(b.label, ignoreCase = true)
                            }
                        }
                    }
                })
            }
            GameSortMode.CUSTOM -> {
                val order = getCustomOrder(context, categoryKey)
                if (order.isEmpty()) return items
                val orderMap = order.mapIndexed { index, id -> id to index }.toMap()
                items.sortedWith(Comparator { a, b ->
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
            GameSortMode.NAME_ASC -> {
                items.sortedBy { it.label.lowercase(Locale.getDefault()) }
            }
        }
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
