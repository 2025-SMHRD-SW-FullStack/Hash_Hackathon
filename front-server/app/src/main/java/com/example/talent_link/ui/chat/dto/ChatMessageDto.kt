package com.example.talent_link.Chat.dto

import com.google.gson.annotations.SerializedName

data class ChatMessageDto(
    val id: Long? = null,
    val roomId: Long,
    val senderId: Long,
    val senderNickname: String,
    val content: String,
    val sentAt: String? = null,  // ISO 형식 시간 문자열 (서버 반환)
    @SerializedName("read")
    val isRead: Boolean = false
)
