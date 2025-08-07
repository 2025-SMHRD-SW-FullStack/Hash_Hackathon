package com.example.talent_link.ui.Favorite

object FavoriteRetrofitInstance {
    val api: FavoriteApi by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8099/") // 실제 서버주소로 변경
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(FavoriteApi::class.java)
    }
}