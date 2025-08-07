package com.example.talent_link.ui.Favorite

data class FavoriteVO(
    val id: Long,            // 서버 아이디
    val imageUrl: String?,   // 이미지 URL
    val title: String,
    val location: String,    // 지역
    val price: String,       // 가격
    var isFavorite: Boolean = true,
    val type: String,        // "sell" or "buy"
    val sellId: Long?,
    val buyId: Long?
)