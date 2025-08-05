package com.example.talent_link.data.repository

import android.content.Context
import android.net.Uri
import com.example.talent_link.data.api.AuthService
import com.example.talent_link.data.model.login.LoginRequest
import com.example.talent_link.data.model.login.LoginResponse
import com.example.talent_link.data.model.signup.createSignupRequestBody
import com.example.talent_link.data.model.signup.prepareFilePart
import com.example.talent_link.data.network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Response

class AuthRepository(private val context: Context) {

    private val authService = RetrofitClient.authService

    suspend fun signup(
        email: String,
        password: String,
        confirmPassword: String,
        nickname: String,
        profileUri: Uri?
    ): Response<ResponseBody> {
        val requestBody = createSignupRequestBody(email, password, confirmPassword, nickname)
        val imagePart = profileUri?.let { prepareFilePart(it, context) }

        return authService.signup(requestBody, imagePart)
    }

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(email, password)
        return authService.login(request)
    }
}

