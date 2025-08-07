package com.talentlink.talentlink.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    private String token;  // 클라이언트가 보내는 리프레시 토큰
}