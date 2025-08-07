package com.talentlink.talentlink.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatReadEventDto {
    private Long roomId;
    private Long userId;
}
