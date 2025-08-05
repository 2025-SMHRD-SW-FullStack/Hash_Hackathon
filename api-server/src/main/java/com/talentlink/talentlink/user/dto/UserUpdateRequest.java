package com.talentlink.talentlink.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원정보 수정 요청 DTO")
public class UserUpdateRequest {

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

}
