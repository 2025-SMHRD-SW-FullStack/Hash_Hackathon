package com.example.talent_link.ui.TalentBuy.dto

import android.content.Context
import com.example.talent_link.data.network.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class TalentBuyRepository(private val context: Context) {

    private val api = RetrofitClient.talentBuyService

    suspend fun uploadTalentBuy(
        request: RequestBody,
        image: MultipartBody.Part?
    ): Response<TalentBuyResponse> {
        val token = com.example.talent_link.util.TokenManager.getAccessToken(context) ?: ""
        return api.uploadTalentBuy("Bearer $token", request, image)
    }
}