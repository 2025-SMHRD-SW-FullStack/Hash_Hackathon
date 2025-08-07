package com.example.talent_link.ui.Favorite.dto

data class FavoriteDeleteRequest(
    val userId: String,
    val sellId: Long? = null,
    val buyId: Long? = null
)
