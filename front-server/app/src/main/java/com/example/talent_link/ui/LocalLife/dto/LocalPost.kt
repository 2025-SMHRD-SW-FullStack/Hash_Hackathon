package com.example.talent_link.ui.LocalLife.dto

data class LocalPost(

    val id: Long,
    val title: String,
    val content: String,
    val writerNickname: String,
    val address: String,
    val imageUrl: String?,   // 이미지 연동할 경우
    val likeCount: Int,
    val createdAt: String

)
