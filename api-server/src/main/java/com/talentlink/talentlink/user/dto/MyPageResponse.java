package com.talentlink.talentlink.user.dto;

import com.talentlink.talentlink.auth.AuthProvider;
import com.talentlink.talentlink.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "마이페이지 응답 DTO")
public class MyPageResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "test@example.com")
    private String email;

    @Schema(description = "닉네임", example = "유준선짱")
    private String nickname;

    @Schema(description = "소셜 로그인 여부", example = "true")
    private boolean isSocialUser;

    @Schema(description = "가입 일시", example = "2024-01-01T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "프로필 사진 주소", example = "http://10.0.2.2:8099/images/69e29981-e5bc-4d94-b5d7-29b792dc1be8_upload_1754493761400.jpg")
    private String profileImageUrl;

    public MyPageResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.isSocialUser = user.getProvider() != null && user.getProvider() != AuthProvider.LOCAL;
        this.createdAt = user.getCreatedAt();
        this.profileImageUrl = user.getProfileImageUrl();
    }
}
