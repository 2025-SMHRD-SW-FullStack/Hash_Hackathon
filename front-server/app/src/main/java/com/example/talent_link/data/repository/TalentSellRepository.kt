package com.example.talent_link.data.repository

import com.example.talent_link.data.api.TalentSellService
import com.example.talent_link.data.model.talentsell.TalentSellResponse
import com.example.talent_link.data.network.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class TalentSellRepository() {

    private val api = RetrofitClient.instance.create(TalentSellService::class.java)

    suspend fun uploadTalentSell(
        request: RequestBody,
        image: MultipartBody.Part?
    ): Response<TalentSellResponse> {
        return api.uploadTalentSell(request, image)
    }

}
