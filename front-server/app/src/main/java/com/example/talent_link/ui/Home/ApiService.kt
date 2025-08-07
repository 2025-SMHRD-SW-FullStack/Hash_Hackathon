package com.example.talent_link.ui.Home

import com.example.talent_link.ui.Home.dto.TalentBuyResponse
import com.example.talent_link.ui.Home.dto.TalentSellResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

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
}