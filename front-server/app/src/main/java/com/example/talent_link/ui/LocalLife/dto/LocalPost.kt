package com.example.talent_link.ui.LocalLife.dto

data class LocalPost(
    val id: Long,
    val title: String,
    val content: String,
    val writerNickname: String,
    val address: String,
    val imageUrl: String?,
    val likeCount: Int,
    val createdAt: String,
    val writerId: Long // 👈 writerId 추가
)