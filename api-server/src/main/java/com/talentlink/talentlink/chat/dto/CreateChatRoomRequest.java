package com.talentlink.talentlink.chat.dto;

import lombok.Data;

@Data
public class CreateChatRoomRequest {
    private Long myUserId;
    private Long opponentUserId;
}
