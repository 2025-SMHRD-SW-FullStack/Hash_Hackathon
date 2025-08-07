package com.example.talent_link.data.api

import com.example.talent_link.util.ApiUrl
import com.example.talent_link.data.model.login.LoginRequest
import com.example.talent_link.data.model.login.LoginResponse
import com.example.talent_link.data.model.login.RefreshRequest
import com.example.talent_link.data.model.login.RefreshResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AuthService {
    // 인증(로그인, 회원가입, 토큰) 관련 API만 다루는 인터페이스

    @Multipart
    @POST("/api/auth/signup")
    suspend fun signup(
        @Part("request") request: RequestBody,
        @Part profileImage: MultipartBody.Part? = null
    ): Response<ResponseBody>

    @POST(ApiUrl.Auth.LOGIN)
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("/api/auth/refresh")
    suspend fun refreshToken(
        @Body refreshRequest: RefreshRequest
    ): Response<RefreshResponse>

}
