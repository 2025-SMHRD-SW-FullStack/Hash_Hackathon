package com.example.talent_link.ui.Chat.dto

data class CreateChatRoomRequest(
    val myUserId: Long,
    val opponentUserId: Long
)
