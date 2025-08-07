package com.example.talent_link.ui.TalentBuy

import com.example.talent_link.ui.TalentBuy.dto.TalentBuyResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface TalentBuyService {
    @Multipart
    @POST("/api/talentbuy")
    suspend fun uploadTalentBuy(
        @Header("Authorization") token: String,
        @Part("request") request: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<TalentBuyResponse>
}