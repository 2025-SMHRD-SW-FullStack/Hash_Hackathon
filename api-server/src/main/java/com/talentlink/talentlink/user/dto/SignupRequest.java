package com.talentlink.talentlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청 DTO")
public class SignupRequest {

    @Schema(description = "이메일(ID 역할)", example = "test@example.com")
    private String email;

    @Schema(description = "비밀번호", example = "password123")
    private String password;

    @Schema(description = "비밀번호 확인", example = "password123")
    private String confirmPassword;

    @Schema(description = "닉네임", example = "hong123")
    private String nickname;

    @Schema(description = "프사 URL은 null로 보내도 됨", example = "https://cdn.example.com/default.png")
    private String profileImageUrl;
}
