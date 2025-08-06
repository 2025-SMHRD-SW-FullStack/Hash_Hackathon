package com.example.talent_link.util

import android.content.Context
import android.util.Log

object TokenManager {
    private const val PREF_NAME = "auth"
    private const val KEY_ACCESS_TOKEN = "accessToken"

    fun saveToken(context: Context, token: String) {
        context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
        Log.d("TokenManager", "✅ 저장된 토큰: $token")
    }

    fun getToken(context: Context): String? {
        val token = context.applicationContext
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS_TOKEN, null)
        Log.d("TokenManager", "📥 불러온 토큰: $token")
        return token
    }
}
