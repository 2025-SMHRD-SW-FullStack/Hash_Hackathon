package com.example.talent_link.data.api

import com.example.talent_link.data.model.mypage.MyPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Query

interface UserService {
    @GET("/api/users/mypage")
    suspend fun getMyPageProfile(
        @Header("Authorization") token: String
    ) : Response<MyPageResponse>

//    suspend fun updateNickname(
//        @Header("Authorization") token: String,
//        @Query("nickname") nickname: String
//    ) : Response<MyPageResponse>
}