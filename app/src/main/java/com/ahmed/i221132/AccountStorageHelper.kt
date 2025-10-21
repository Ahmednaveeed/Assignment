package com.ahmed.i221132

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object AccountStorageHelper {
    private const val PREFS_NAME = "LastAccountPrefs"
    private const val ACCOUNT_KEY = "last_account"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Saves or overwrites the single last account
    fun saveLastAccount(context: Context, accountInfo: SavedAccountInfo) {
        val json = gson.toJson(accountInfo)
        getPrefs(context).edit().putString(ACCOUNT_KEY, json).apply()
    }

    // Loads the single last account, or null if none saved
    fun getLastAccount(context: Context): SavedAccountInfo? {
        val json = getPrefs(context).getString(ACCOUNT_KEY, null)
        return if (json != null) {
            gson.fromJson(json, SavedAccountInfo::class.java)
        } else {
            null
        }
    }
}