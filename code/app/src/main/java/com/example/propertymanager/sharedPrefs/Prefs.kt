package com.example.propertymanager.sharedPrefs

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val PREFS_NAME = "PropertyManagerPrefs"
    private const val KEY_REMEMBER_ME = "remember_me"
    private const val KEY_USERNAME = "username"

    fun setRememberMe(context: Context, remember: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_REMEMBER_ME, remember).apply()
    }

    fun getRememberMe(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_REMEMBER_ME, false)
    }

    fun saveUsername(context: Context, username: String) {
        getPrefs(context).edit().putString(KEY_USERNAME, username).apply()
    }

    fun getSavedUsername(context: Context): String? {
        return getPrefs(context).getString(KEY_USERNAME, null)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
