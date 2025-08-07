package com.example.talent_link.util

import android.content.Context
import android.util.Log
import com.example.talent_link.data.model.login.RefreshRequest
import com.example.talent_link.data.network.RetrofitClient
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(private val context: Context) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("🔄 TokenAuthenticator", "AccessToken 만료 → 재발급 시도")

        // 무한 루프 방지
        if (responseCount(response) >= 2) return null

        val refreshToken = TokenManager.getRefreshToken(context) ?: return null


        try {
            val authApi = RetrofitClient.authService

            // 동기 호출로 리프레시 토큰 API 실행
            val refreshResponse = runBlocking {
                authApi.refreshToken(RefreshRequest(refreshToken))
            }

            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body()
                val newAccessToken = body?.accessToken

                if (!newAccessToken.isNullOrBlank()) {
                    Log.d("🔄 TokenAuthenticator", "재발급 성공 → 새 토큰 저장")
                    // 토큰 저장 (리프레시 토큰은 기존 값 유지)
                    TokenManager.saveTokens(context, newAccessToken, null)

                    // 요청에 새 토큰 넣어서 재시도
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    Log.e("TokenAuthenticator", "새 액세스 토큰이 비어있음")
                }
            } else {
                Log.e("TokenAuthenticator", "리프레시 토큰 API 실패: ${refreshResponse.code()}")
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "토큰 재발급 실패", e)
        }

        return null
    }

    private fun responseCount(response: Response): Int {
        // 응답 재시도 횟수 계산 (무한 루프 방지용)
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
