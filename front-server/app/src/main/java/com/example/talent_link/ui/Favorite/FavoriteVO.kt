package com.example.talent_link.ui.Favorite

data class FavoriteVO(
    val img: Int,
    val title: String,
    val local: String,
    val price: String,
    var favorite: Boolean = true
)
