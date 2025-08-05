package com.example.talent_link.util

import android.content.Context

fun saveToken(context: Context, accessToken: String) {
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    prefs.edit().putString("access_token", accessToken).apply()
}

fun getToken(context: Context): String? {
    val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    return prefs.getString("access_token", null)
}
