package com.talentlink.talentlink.locallife.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    private String content;
    // 로그인 안 붙이면 nickname/address도 받기
    private String writerNickname;
    private String address;
}
