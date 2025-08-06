package com.example.talent_link.data.model.signup

data class SignupRequest(
    val email: String,
    val password: String,
    val nickname: String,
    val confirmPassword: String,
    val profileImageUrl: String? = null
)