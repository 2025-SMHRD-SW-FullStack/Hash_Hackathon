package com.example.talent_link.ui.Home.dto

data class TalentSellResponse(

    val id: Long,
    val title: String,
    val description: String,
    val price: Int,
    val imageUrl: String?,
    val writerNickname: String,
    val createdAt: String // or LocalDateTime, 파싱 필요

)
