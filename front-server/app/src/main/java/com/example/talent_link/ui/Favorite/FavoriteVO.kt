package com.example.talent_link.ui.Favorite

data class FavoriteVO(
    val id: Long,            // 서버 아이디
    val img: Int,            // 타입에 따라 매핑
    val title: String,
    val local: String,
    val price: String,
    var favorite: Boolean = true,
    val type: String,        // 서버 type 정보
    val sellId: Long?,
    val buyId: Long?
)