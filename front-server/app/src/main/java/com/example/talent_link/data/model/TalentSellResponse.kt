package com.example.talent_link.data.model

data class TalentSellResponse(
    val id: Long,
    val title: String,
    val description: String,
    val price: Int,
    val imageUrl: String?,
    val createdAt: String
)
