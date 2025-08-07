package com.example.talent_link.data.model.login

import com.google.gson.annotations.SerializedName

data class RefreshResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("user")
    val user: UserResponse
)

