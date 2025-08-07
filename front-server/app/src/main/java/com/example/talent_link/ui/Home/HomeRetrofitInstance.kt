package com.example.talent_link.ui.Home

object HomeRetrofitInstance {
    val api: ApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8099/") // 서버 주소에 맞게 수정
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}