package com.talentlink.talentlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "로그인 응답 DTO")
public class LoginResponse {

    @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String accessToken;

    @Schema(description = "JWT 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsIn...")
    private String refreshToken;

    @Schema(description = "사용자 정보")
    private UserResponse user;

}

