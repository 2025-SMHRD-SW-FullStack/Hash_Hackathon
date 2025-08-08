package com.example.talent_link.ui.Home.dto

data class TalentBuyResponse(
    val id: Long,
    val title: String,
    val description: String,
    val budget: Int,
    val imageUrl: String?,
    val deadline: String, // or LocalDateTime
    val writerNickname: String,
    val writerId: Long, // ✅ writerId 필드 추가
    val createdAt: String
)