package com.example.talent_link.util

import android.util.Base64
import org.json.JSONObject

object JwtUtils {
    fun parseUserIdFromJwt(token: String): Long {
        return try {
            val payload = token.split(".")[1]
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE), Charsets.UTF_8)
            val json = JSONObject(decoded)
            json.optString("sub", "-1").toLongOrNull() ?: -1
        } catch (e: Exception) {
            -1
        }
    }
}
