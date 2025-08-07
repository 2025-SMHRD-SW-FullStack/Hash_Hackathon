package com.talentlink.talentlink.locallife.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocalPostRequest {
    private String title;
    private String content;
    private String writerNickname;
    private String address;
    // private String imageUrl; // ← 이 줄을 삭제하거나 주석 처리합니다.
}