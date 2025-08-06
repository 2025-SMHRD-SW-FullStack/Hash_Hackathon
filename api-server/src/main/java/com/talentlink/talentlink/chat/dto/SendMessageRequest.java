package com.talentlink.talentlink.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMessageRequest {
    private Long senderId;
    private String content;
}
