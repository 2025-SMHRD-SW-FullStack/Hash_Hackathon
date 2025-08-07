package com.example.talent_link.util

import android.content.Context

object IdManager {
    fun saveUserId(context: Context, userId: Long) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().putLong("user_id", userId).apply()
    }

    fun getUserId(context: Context): Long {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        // 기본값 -1, 저장된 값 없으면 -1 반환
        return prefs.getLong("user_id", -1L)
    }

    fun saveNickname(context: Context, nickname: String) {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        prefs.edit().putString("nickname", nickname).apply()
    }
    fun getNickname(context: Context): String? {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("nickname", null)
    }

}