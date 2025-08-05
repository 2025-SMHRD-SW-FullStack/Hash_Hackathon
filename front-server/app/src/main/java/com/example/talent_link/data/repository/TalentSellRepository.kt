package com.example.talent_link.data.repository

import com.example.talent_link.data.api.TalentSellService
import com.example.talent_link.data.model.TalentSellRequest
import com.example.talent_link.data.model.TalentSellResponse
import com.example.talent_link.data.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class TalentSellRepository() {

    private val api = RetrofitClient.instance.create(TalentSellService::class.java)

    suspend fun uploadTalentSell(
        request: RequestBody,
        image: MultipartBody.Part?
    ): Response<TalentSellResponse> {
        return api.uploadTalentSell(request, image)
    }

}
