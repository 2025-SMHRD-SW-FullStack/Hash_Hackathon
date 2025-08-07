package com.example.talent_link.ui.TalentBuy.dto

data class TalentBuyResponse(
    val id: Long,
    val title: String,
    val description: String,
    val budget: Int,
    val imageUrl: String?,
    val deadline: String,
    val writerNickname: String,
    val createdAt: String
)
