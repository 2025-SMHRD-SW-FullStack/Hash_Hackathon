package com.example.talent_link.ui.Home

import com.example.talent_link.ui.Home.dto.TalentBuyResponse
import com.example.talent_link.ui.Home.dto.TalentSellResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("/api/talentsell")
    suspend fun getTalentSellList(
        @Header("Authorization") token: String
    ): List<TalentSellResponse>

    @GET("/api/talentbuy")
    suspend fun getTalentBuyList(
        @Header("Authorization") token: String
    ): List<TalentBuyResponse>

    @GET("/api/talentsell/{id}")
    suspend fun getTalentSellDetail(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): TalentSellResponse

    @GET("/api/talentbuy/{id}")
    suspend fun getTalentBuyDetail(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): TalentBuyResponse

    // 👇 수정 API (Sell)
    @Multipart
    @PUT("/api/talentsell/{id}")
    suspend fun updateTalentSell(
        @Path("id") id: Long,
        @Header("Authorization") token: String,
        @Part("request") request: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<TalentSellResponse>

    // 👇 삭제 API (Sell)
    @DELETE("/api/talentsell/{id}")
    suspend fun deleteTalentSell(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Unit>

    // 👇 수정 API (Buy)
    @Multipart
    @PUT("/api/talentbuy/{id}")
    suspend fun updateTalentBuy(
        @Path("id") id: Long,
        @Header("Authorization") token: String,
        @Part("request") request: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<TalentBuyResponse>

    // 👇 삭제 API (Buy)
    @DELETE("/api/talentbuy/{id}")
    suspend fun deleteTalentBuy(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<Unit>
}