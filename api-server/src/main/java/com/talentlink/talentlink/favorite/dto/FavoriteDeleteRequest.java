package com.talentlink.talentlink.favorite.dto;

import lombok.Getter;

@Getter
public class FavoriteDeleteRequest {
    private String userId;
    private Long sellId; // 판매글 id
    private Long buyId;  // 구매글 id
}