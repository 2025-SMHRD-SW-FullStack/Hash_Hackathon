package com.example.talent_link.Chat

import com.example.talent_link.Chat.dto.ChatMessageDto
import com.example.talent_link.Chat.dto.ChatRoomListItemDto
import com.example.talent_link.Chat.dto.SendMessageRequest
import retrofit2.http.*

interface ChatApi {

    @GET("/api/chat/rooms")
    suspend fun getMyChatRooms(
        @Query("userId") userId: Long,
        @Header("Authorization") token: String
        ): List<ChatRoomListItemDto>

    @GET("/api/chat/rooms/{roomId}/messages")
    suspend fun getRoomMessages(
        @Path("roomId") roomId: Long,
        @Header("Authorization") token: String
        ): List<ChatMessageDto>

    @POST("/api/chat/rooms/{roomId}/send")
    suspend fun sendMessage(
        @Path("roomId") roomId: Long,
        @Body req: SendMessageRequest,
        @Header("Authorization") token: String
    ): ChatMessageDto

    @POST("/api/chat/rooms/{roomId}/read")
    suspend fun markAsRead(
        @Path("roomId") roomId: Long,
        @Query("userId") userId: Long,
        @Header("Authorization") token: String
    )

}