package com.example.talent_link.data.repository

import android.content.Context
import android.net.Uri
import com.example.talent_link.data.api.AuthService
import com.example.talent_link.data.model.signup.createSignupRequestBody
import com.example.talent_link.data.model.signup.prepareFilePart
import com.example.talent_link.data.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Response

class AuthRepository(private val context: Context) {

    private val api = RetrofitClient.instance.create(AuthService::class.java)

    suspend fun signup(email: String, password: String, nickname: String, profileUri: Uri?): Response<ResponseBody> {
        val requestBody = createSignupRequestBody(email, password, nickname)
        val imagePart = profileUri?.let { prepareFilePart(it, context) }

        return api.signup(requestBody, imagePart)
    }
}