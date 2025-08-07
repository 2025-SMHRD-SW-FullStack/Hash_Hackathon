package com.example.talent_link.ui.Home.dto

data class HomePostUiModel(

    val id: Long,
    val type: PostType,  // SELL, BUY
    val title: String,
    val content: String,
    val priceOrBudget: Int,
    val imageUrl: String?,
    val writerNickname: String,
    var createdAt: String,
    var isFavorite: Boolean = false

)

enum class PostType { SELL, BUY }


// DTO → UiModel 변환 함수
fun TalentSellResponse.toUiModel() = HomePostUiModel(
    id = id,
    type = PostType.SELL,
    title = title,
    content = description,
    priceOrBudget = price,
    imageUrl = imageUrl,
    writerNickname = writerNickname,
    createdAt = createdAt
)

fun TalentBuyResponse.toUiModel() = HomePostUiModel(
    id = id,
    type = PostType.BUY,
    title = title,
    content = description,
    priceOrBudget = budget,
    imageUrl = imageUrl,
    writerNickname = writerNickname,
    createdAt = createdAt
)