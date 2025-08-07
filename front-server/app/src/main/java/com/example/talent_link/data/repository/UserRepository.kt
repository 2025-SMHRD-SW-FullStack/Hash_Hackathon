package com.example.talent_link.data.repository

import com.example.talent_link.data.api.UserService
import com.example.talent_link.data.model.login.UserResponse
import com.example.talent_link.data.model.mypage.MyPageResponse
import com.example.talent_link.data.model.mypage.UserUpdateRequest
import okhttp3.MultipartBody
import retrofit2.Response

class UserRepository(private val userService: UserService) {

    suspend fun getUserProfile(): Response<MyPageResponse> {
        return userService.getMyPageProfile()
    }

    suspend fun updateNickname(request: UserUpdateRequest): Response<UserResponse> {
        return userService.updateNickname(request)
    }

    suspend fun uploadProfileImage(filePart: MultipartBody.Part): Response<String> {
        return userService.uploadProfileImage(filePart)
    }
}
