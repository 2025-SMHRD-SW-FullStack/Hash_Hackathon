package com.example.talent_link.util

import android.content.Context
import android.util.Log
import com.example.talent_link.data.api.AuthService
import com.example.talent_link.data.model.login.RefreshRequest
import com.example.talent_link.util.ApiUrl
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(private val context: Context) : Authenticator {

    // 토큰 갱신 API 호출을 위한 별도의 AuthService 인스턴스
    // 이 클라이언트는 인증자나 액세스 토큰 인터셉터를 포함하지 않습니다.
    private val refreshAuthService: AuthService by lazy {
        val client = OkHttpClient.Builder().build() // ⭐ 중요: 인터셉터/인증자 없음
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiUrl.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        retrofit.create(AuthService::class.java)
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("🔄 TokenAuthenticator", "AccessToken 만료 → 재발급 시도")

        // 무한 루프 방지: 이미 두 번 이상 재시도된 요청이면 null 반환
        if (responseCount(response) >= 2) {
            Log.e("🔄 TokenAuthenticator", "재시도 횟수 초과, 토큰 갱신 실패로 간주.")
            // 모든 토큰을 지우고 로그아웃 처리 (필요에 따라 로그인 화면으로 전환 로직 추가)
            TokenManager.clearTokens(context)
            return null
        }

        val refreshToken = TokenManager.getRefreshToken(context)
        if (refreshToken.isNullOrBlank()) {
            Log.e("🔄 TokenAuthenticator", "리프레시 토큰이 없음, 로그인 필요.")
            // 리프레시 토큰이 없으면 로그아웃 처리
            TokenManager.clearTokens(context)
            return null
        }

        try {
            // ⭐ 중요: 별도로 생성된 refreshAuthService를 사용하여 토큰 갱신 API 호출
            val refreshResponse = runBlocking {
                refreshAuthService.refreshToken(RefreshRequest(refreshToken))
            }

            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body()
                val newAccessToken = body?.accessToken
                val newRefreshToken = body?.refreshToken // 서버가 새로운 리프레시 토큰도 준다면 사용

                if (!newAccessToken.isNullOrBlank()) {
                    Log.d("🔄 TokenAuthenticator", "재발급 성공 → 새 토큰 저장")
                    // 새 액세스 토큰과 (선택적으로) 새 리프레시 토큰 저장
                    TokenManager.saveTokens(context, newAccessToken, newRefreshToken)

                    // 원래 요청에 새 액세스 토큰을 넣어서 재시도
                    return response.request.newBuilder()
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

        // 토큰 갱신 실패 시 모든 토큰을 지우고 로그인 화면으로 이동하도록 유도
        TokenManager.clearTokens(context)
        return null
    }

    // 응답 재시도 횟수 계산 (무한 루프 방지용)
    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}