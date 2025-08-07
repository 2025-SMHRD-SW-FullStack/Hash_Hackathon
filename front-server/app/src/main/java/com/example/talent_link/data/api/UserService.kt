package com.example.talent_link.data.api

import com.example.talent_link.data.model.login.UserResponse
import com.example.talent_link.data.model.mypage.MyPageResponse
import com.example.talent_link.data.model.mypage.UserUpdateRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface UserService {
    @GET("/api/users/mypage")
    suspend fun getMyPageProfile(
//        @Header("Authorization") token: String
    ) : Response<MyPageResponse>

    @PUT("/api/users/me")
    suspend fun updateNickname(
        @Body nicknameRequest: UserUpdateRequest
    ): Response<UserResponse>

    @Multipart
    @POST("/api/users/profile-image")
    suspend fun uploadProfileImage(
        @Part file: MultipartBody.Part
    ): Response<String>

}