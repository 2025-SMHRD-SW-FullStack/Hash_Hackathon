package com.example.talent_link.data.network

import android.content.Context
import com.example.talent_link.data.api.AuthService
import com.example.talent_link.data.api.TalentSellService
import com.example.talent_link.util.ApiUrl
import com.example.talent_link.util.TokenAuthenticator
import com.example.talent_link.util.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private lateinit var authServiceInstance: AuthService
    private lateinit var talentSellServiceInstance: TalentSellService

    fun init(context: Context) {
        val client = OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(context)) // ✅ 중요
            .addInterceptor { chain ->
                val token = TokenManager.getToken(context)
                val requestBuilder = chain.request().newBuilder()
                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(ApiUrl.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        authServiceInstance = retrofit.create(AuthService::class.java)
        talentSellServiceInstance = retrofit.create(TalentSellService::class.java)
    }

    val authService: AuthService
        get() = authServiceInstance

    val talentSellService: TalentSellService
        get() = talentSellServiceInstance
}
