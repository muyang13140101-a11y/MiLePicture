package com.milepicture.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.milepicture.app.data.model.UnifiedImage

class FavoritesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("milepicture_favorites_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val keyFavorites = "saved_favorites_list"

    fun loadFavorites(): List<UnifiedImage> {
        val json = prefs.getString(keyFavorites, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<UnifiedImage>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveFavorites(list: List<UnifiedImage>) {
        val json = gson.toJson(list)
        prefs.edit().putString(keyFavorites, json).apply()
    }
}
