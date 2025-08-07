package com.example.talent_link.ui.Home

import com.example.talent_link.ui.Home.dto.TalentBuyResponse
import com.example.talent_link.ui.Home.dto.TalentSellResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("/api/talentsell")
    suspend fun getTalentSellList(
        @Header("Authorization") token: String
    ): List<TalentSellResponse>

    @GET("/api/talentbuy")
    suspend fun getTalentBuyList(
        @Header("Authorization") token: String
    ): List<TalentBuyResponse>
}