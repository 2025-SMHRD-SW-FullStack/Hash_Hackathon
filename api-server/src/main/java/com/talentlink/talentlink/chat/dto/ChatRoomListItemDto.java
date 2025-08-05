package com.talentlink.talentlink.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatRoomListItemDto {

    private Long roomId;
    private String opponentNickname;
    private String opponentProfileUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;

}
