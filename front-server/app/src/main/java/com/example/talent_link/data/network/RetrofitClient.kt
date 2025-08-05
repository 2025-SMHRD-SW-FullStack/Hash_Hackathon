package com.example.talent_link.data.network

import com.example.talent_link.data.api.AuthService
import com.example.talent_link.data.api.TalentSellService
import com.example.talent_link.util.ApiUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val client = OkHttpClient.Builder().build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiUrl.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val authService: AuthService = retrofit.create(AuthService::class.java)
    val talentSellService: TalentSellService = retrofit.create(TalentSellService::class.java)
}

