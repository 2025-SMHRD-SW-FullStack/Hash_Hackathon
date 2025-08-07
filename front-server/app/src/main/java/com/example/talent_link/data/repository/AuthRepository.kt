package com.example.talent_link.data.repository

import android.content.Context
import android.net.Uri
import com.example.talent_link.data.api.AuthService
import com.example.talent_link.data.model.login.LoginRequest
import com.example.talent_link.data.model.login.LoginResponse
import com.example.talent_link.data.model.login.RefreshRequest
import com.example.talent_link.data.model.login.RefreshResponse
import com.example.talent_link.data.model.signup.createSignupRequestBody
import com.example.talent_link.data.model.signup.prepareFilePart
import com.example.talent_link.data.network.RetrofitClient
import com.example.talent_link.util.TokenManager
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

    suspend fun refreshToken(refreshToken: String): Response<RefreshResponse> {
        val request = RefreshRequest(token = refreshToken)
        return authService.refreshToken(request)
    }

    suspend fun refreshAndSaveToken(refreshToken: String, context: Context): Boolean {
        return try {
            val response = refreshToken(refreshToken)
            if (response.isSuccessful) {
                val body = response.body()
                val access = body?.accessToken
                val refresh = body?.refreshToken
                if (!access.isNullOrEmpty() && !refresh.isNullOrEmpty()) {
                    TokenManager.saveTokens(context, access, refresh)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }


}

