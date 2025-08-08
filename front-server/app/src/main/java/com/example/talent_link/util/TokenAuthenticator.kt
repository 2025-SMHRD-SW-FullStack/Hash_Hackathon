package com.example.talent_link.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.talent_link.NoNavActivity
import com.example.talent_link.data.api.AuthService
import com.example.talent_link.data.model.login.RefreshRequest
import com.example.talent_link.data.network.RetrofitClient
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(private val context: Context) : Authenticator {

    // 토큰 갱신 전용 Retrofit (인터셉터 X)
    private val refreshAuthService: AuthService by lazy {
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiUrl.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        retrofit.create(AuthService::class.java)
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("🔄 TokenAuthenticator", "AccessToken 만료 → 재발급 시도")

        if (responseCount(response) >= 2) {
            Log.e("🔄 TokenAuthenticator", "재시도 횟수 초과, 토큰 갱신 실패로 간주.")
            forceLogout()
            return null
        }

        val refreshToken = TokenManager.getRefreshToken(context)
        if (refreshToken.isNullOrBlank()) {
            Log.e("🔄 TokenAuthenticator", "리프레시 토큰이 없음, 로그인 필요.")
            forceLogout()
            return null
        }

        try {
            val refreshResponse = runBlocking {
                refreshAuthService.refreshToken(RefreshRequest(refreshToken))
            }

            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body()
                val newAccessToken = body?.accessToken
                val newRefreshToken = body?.refreshToken

                if (!newAccessToken.isNullOrBlank()) {
                    Log.d("🔄 TokenAuthenticator", "재발급 성공 → 새 토큰 저장")

                    val savedRefreshToken = newRefreshToken ?: TokenManager.getRefreshToken(context)
                    TokenManager.saveTokens(context, newAccessToken, savedRefreshToken)

                    // ✅ rebuild은 다음 요청부터 반영되므로, 지금은 의미 없음
                    // RetrofitClient.rebuild()

                    // ✅ 새 토큰으로 재요청
                    return response.request.newBuilder()
                        .removeHeader("Authorization")
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                } else {
                    Log.e("TokenAuthenticator", "새 액세스 토큰이 비어있음. 로그인 필요.")
                }
            } else {
                Log.e("TokenAuthenticator", "리프레시 토큰 API 실패: ${refreshResponse.code()} - ${refreshResponse.message()}")
            }
        } catch (e: Exception) {
            Log.e("TokenAuthenticator", "토큰 재발급 중 예외 발생", e)
        }

        forceLogout()
        return null
    }


    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

    private fun forceLogout() {
        Log.e("TokenAuthenticator", "❌ 강제 로그아웃 처리 실행")
        TokenManager.clearTokens(context)

        val appContext = context.applicationContext
        val intent = Intent(appContext, NoNavActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or  // 새 태스크
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or // 기존 태스크 전부 제거
                    Intent.FLAG_ACTIVITY_CLEAR_TOP     // 백스택에 있는 로그인화면까지 전부 제거
        }
        appContext.startActivity(intent)
    }
}
