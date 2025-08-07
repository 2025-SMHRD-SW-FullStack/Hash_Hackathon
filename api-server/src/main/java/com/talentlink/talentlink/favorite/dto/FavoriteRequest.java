package com.talentlink.talentlink.favorite.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FavoriteRequest {
    private Long id;  // 판매글일 때만
    private String type;  // "buy" or "sell"
    private String userId;
    private String writerNickname;
    private Long buyId;   // 구매글일 때만
    private Long sellId;  // 판매글일 때만
}

