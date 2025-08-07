package com.example.talent_link.ui.Favorite.dto

data class FavoriteResponse(
    val id: Long,
    val userId: String,
    val type: String,
    val title: String,
    val writerNickname: String,
    val sellId: Long? = null,   // 추가!
    val buyId: Long? = null,    // 추가!
    val time: String? = null
)
