package com.talentlink.talentlink.locallife.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // (선택) 기본 생성자 추가 – 직렬화/역직렬화에 필요할 때가 많아요
public class LocalPostRequest {
    private String title;
    private String content;
    private String writerNickname;
    private String address;
    private String imageUrl; // ← 이미지 URL도 포함(프론트에서 string으로 넘기는 경우)

    // 필요하면 생성자/빌더 등 추가 가능
}
