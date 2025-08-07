package com.example.talent_link.ui.LocalLife

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LocalLifeRetrofitInstance {
    val api: LocalLifeApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8099/") // 실제 서버 주소로!
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocalLifeApi::class.java)
    }
}