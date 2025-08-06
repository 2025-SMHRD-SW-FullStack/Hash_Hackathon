package com.example.talent_link.data.api

import com.example.talent_link.data.model.login.LoginResponse
import retrofit2.Call
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/refresh")
    fun refreshToken(): Call<LoginResponse>
}
