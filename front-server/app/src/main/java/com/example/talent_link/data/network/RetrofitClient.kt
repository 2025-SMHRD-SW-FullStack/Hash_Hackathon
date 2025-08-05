package com.example.talent_link.data.network

import com.example.talent_link.util.ApiUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiUrl.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}
