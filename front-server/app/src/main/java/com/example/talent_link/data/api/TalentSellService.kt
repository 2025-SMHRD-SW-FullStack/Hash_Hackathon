package com.example.talent_link.data.api

import com.example.talent_link.data.model.talentsell.TalentSellResponse
import com.example.talent_link.util.ApiUrl
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface TalentSellService {
    @Multipart
    @POST(ApiUrl.TalentSell.UPLOAD)
    suspend fun uploadTalentSell(
//        @Header("Authorization") token: String,
        @Part("request") request: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<TalentSellResponse>
}