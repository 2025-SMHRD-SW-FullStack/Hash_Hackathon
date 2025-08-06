package com.example.talent_link.Chat.dto

data class ChatRoomListItemDto(
    val roomId: Long,
    val opponentNickname: String,
    val opponentProfileImageUrl: String?,
    val lastMessage: String,
    val lastMessageAt: String?,
    val unreadCount: Long
)
