package com.talentlink.talentlink.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteDeleteRequest {
    private String userId;
    private Long buyId;  // 구매글일 때만
    private Long sellId; // 판매글일 때만
}
