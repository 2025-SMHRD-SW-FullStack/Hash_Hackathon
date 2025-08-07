package com.example.talent_link.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object TokenManager {
    private const val PREF_NAME = "auth"
    private const val KEY_ACCESS_TOKEN = "accessToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"

    private fun getPrefs(context: Context) : SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(context: Context, accessToken: String?, refreshToken: String?) {
        val editor = getPrefs(context).edit()
        accessToken?.let {
            Log.d("TokenManager", "📦 저장된 토큰: $it")
            editor.putString(KEY_ACCESS_TOKEN, it)
        }
        refreshToken?.let {
            Log.d("TokenManager", "📦 저장된 리프레시 토큰: $it")
            editor.putString(KEY_REFRESH_TOKEN, it)
        }
        editor.apply()
    }


    fun getAccessToken(context: Context): String? {
        val token = getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
        Log.d("TokenManager", "📥 불러온 액세스 토큰: $token")
        return token
    }


    fun getRefreshToken(context: Context): String? {
        val refreshToken = getPrefs(context).getString(KEY_REFRESH_TOKEN, null)
        Log.d("TokenManager", "📥 불러온 리프레시 토큰: $refreshToken")
        return refreshToken
    }

    fun clearTokens(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

}
