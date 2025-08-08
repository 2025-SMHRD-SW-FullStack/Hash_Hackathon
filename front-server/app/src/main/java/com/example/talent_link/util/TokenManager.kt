package com.example.talent_link.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object TokenManager {
    private const val PREF_NAME = "auth"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(context: Context, accessToken: String?, refreshToken: String?) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        accessToken?.let { editor.putString(KEY_ACCESS_TOKEN, it) }
        refreshToken?.let { editor.putString(KEY_REFRESH_TOKEN, it) }
        editor.apply()
    }

    fun getAccessToken(context: Context): String? =
        getPrefs(context).getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(context: Context): String? =
        getPrefs(context).getString(KEY_REFRESH_TOKEN, null)

    fun clearTokens(context: Context) {
        getPrefs(context).edit().clear().apply()
        Log.d("🧹 TokenManager", "🚫 모든 토큰 삭제됨")
    }
}
