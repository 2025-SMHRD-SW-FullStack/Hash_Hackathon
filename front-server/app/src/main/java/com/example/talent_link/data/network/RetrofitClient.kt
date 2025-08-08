package com.example.talent_link.data.network

import android.content.Context
import android.util.Log
import com.example.talent_link.data.api.AuthService
import com.example.talent_link.data.api.TalentSellService
import com.example.talent_link.data.api.UserService
import com.example.talent_link.ui.TalentBuy.TalentBuyService
import com.example.talent_link.util.ApiUrl
import com.example.talent_link.util.TokenAuthenticator
import com.example.talent_link.util.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {
    private lateinit var authServiceInstance: AuthService
    private lateinit var talentSellServiceInstance: TalentSellService
    private lateinit var talentBuyServiceInstance: TalentBuyService
    private lateinit var userServiceInstance: UserService

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext // 재사용 가능하도록 보관

        val client = OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(context))
            .addInterceptor { chain ->
                val currentToken = TokenManager.getAccessToken(appContext!!)  // ✅ 무조건 최신 토큰 참조
                val requestBuilder = chain.request().newBuilder()
                if (!currentToken.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $currentToken")
                }
                chain.proceed(requestBuilder.build())
            }

            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiUrl.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        authServiceInstance = retrofit.create(AuthService::class.java)
        talentSellServiceInstance = retrofit.create(TalentSellService::class.java)
        talentBuyServiceInstance = retrofit.create(TalentBuyService::class.java)
        userServiceInstance = retrofit.create(UserService::class.java)
    }

    // ✅ 새 토큰 저장 후 강제 재빌드 (access/refresh 바뀌었을 경우)
    fun rebuild() {
        Log.d("🧪 RetrofitClient", "✅ RetrofitClient.rebuild() 호출됨")

        appContext?.let {
            init(it) // 기존 init 호출하면 모든 구성 요소 자동 재생성됨
        } ?: throw IllegalStateException("RetrofitClient is not initialized. Call init(context) first.")
    }

    val authService: AuthService get() = authServiceInstance
    val talentSellService: TalentSellService get() = talentSellServiceInstance
    val talentBuyService: TalentBuyService get() = talentBuyServiceInstance
    val userService: UserService get() = userServiceInstance
}
