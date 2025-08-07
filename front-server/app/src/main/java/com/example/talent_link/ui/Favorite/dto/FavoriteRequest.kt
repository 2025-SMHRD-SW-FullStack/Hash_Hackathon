package com.example.talent_link.ui.Favorite.dto

data class FavoriteRequest(
    val id: Long?,             // sell일 때만
    val type: String,          // "buy" or "sell"
    val userId: String,
    val writerNickname: String?,
    val buyId: Long?,          // buy일 때만
    val sellId: Long?          // sell일 때만
)