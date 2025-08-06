package com.example.talent_link.data.model.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val accessToken: String,

    @SerializedName("user")
    val user: UserResponse
)
