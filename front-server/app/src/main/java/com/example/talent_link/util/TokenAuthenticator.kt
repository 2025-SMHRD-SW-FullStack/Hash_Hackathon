package com.example.talent_link.util

import android.content.Context
import android.util.Log
import com.example.talent_link.data.api.AuthApi
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(private val context: Context) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("🔄 TokenAuthenticator", "AccessToken 만료 → 재발급 시도")

        // 무한 루프 방지
        if (responseCount(response) >= 2) return null

        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(ApiUrl.BASE_URL) // 🔁 너의 API 주소로 바꿔줘
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val authApi = retrofit.create(AuthApi::class.java)
            val refreshResponse = authApi.refreshToken().execute()

            if (refreshResponse.isSuccessful) {
                val newAccessToken = refreshResponse.body()?.accessToken
                if (!newAccessToken.isNullOrBlank()) {
                    Log.d("🔄 TokenAuthenticator", "재발급 성공 → 새 토큰 저장")
                    TokenManager.saveToken(context, newAccessToken)

                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                }
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "토큰 재발급 실패", e)
        }

        return null
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
