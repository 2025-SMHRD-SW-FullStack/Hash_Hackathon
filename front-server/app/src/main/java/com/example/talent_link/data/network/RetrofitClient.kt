package com.example.talent_link.data.network

import android.content.Context
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
    private lateinit var userServiceInstance : UserService

    fun init(context: Context) {
        val client = OkHttpClient.Builder()
            .authenticator(TokenAuthenticator(context))
            .addInterceptor { chain ->
                val token = TokenManager.getAccessToken(context)
                val requestBuilder = chain.request().newBuilder()
                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
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

    val authService: AuthService
        get() = authServiceInstance

    val talentSellService: TalentSellService
        get() = talentSellServiceInstance

    val talentBuyService: TalentBuyService
        get() = talentBuyServiceInstance

    val userService : UserService
        get() = userServiceInstance
}