package com.example.talent_link.data.model.login

data class LoginResponse(
    val accessToken: String,
    val user: UserResponse
)