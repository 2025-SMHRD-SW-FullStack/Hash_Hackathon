package com.example.talent_link.data.repository

import android.content.Context
import com.example.talent_link.data.model.talentsell.TalentSellResponse
import com.example.talent_link.data.network.RetrofitClient
import com.example.talent_link.util.getToken
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class TalentSellRepository(private val context: Context) {

    private val api = RetrofitClient.talentSellService

    suspend fun uploadTalentSell(
        request: RequestBody,
        image: MultipartBody.Part?
    ): Response<TalentSellResponse> {
        val token = getToken(context) ?: ""
        return api.uploadTalentSell("Bearer $token", request, image)
    }
}
