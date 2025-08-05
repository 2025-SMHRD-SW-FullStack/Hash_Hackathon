package com.example.talent_link.data.api

import com.example.talent_link.util.ApiUrl
import com.example.talent_link.data.model.login.LoginRequest
import com.example.talent_link.data.model.login.LoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AuthService {

    @Multipart
    @POST(ApiUrl.Auth.SIGNUP)
    suspend fun signup(
        @Part("request") request: RequestBody,
        @Part profileImage: MultipartBody.Part?
    ): Response<ResponseBody>


    @POST(ApiUrl.Auth.LOGIN)
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>
}
