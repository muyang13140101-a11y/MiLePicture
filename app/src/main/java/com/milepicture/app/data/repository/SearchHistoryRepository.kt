package com.milepicture.app.data.repository

import android.content.Context
import android.content.SharedPreferences

class SearchHistoryRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mile_search_history", Context.MODE_PRIVATE)

    fun getSearchHistory(): List<String> {
        val raw = prefs.getString("history_list", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("|||").filter { it.isNotBlank() }
    }

    fun addHistory(query: String) {
        val clean = query.trim()
        if (clean.isBlank() || clean.length < 2) return
        val current = getSearchHistory().toMutableList()
        current.remove(clean)
        current.add(0, clean)
        val trimmed = current.take(15)
        prefs.edit().putString("history_list", trimmed.joinToString("|||")).apply()
    }

    fun removeHistory(query: String) {
        val current = getSearchHistory().toMutableList()
        current.remove(query.trim())
        prefs.edit().putString("history_list", current.joinToString("|||")).apply()
    }

    fun clearHistory() {
        prefs.edit().remove("history_list").apply()
    }
}
