package com.talentlink.talentlink.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    private Long id;           // 즐겨찾기 PK
    private String userId;
    private String type;       // "buy" or "sell"
    private Long buyId;        // 구매글 PK (null 가능)
    private Long sellId;       // 판매글 PK (null 가능)
    private String title;
    private String writerNickname;
    private LocalDateTime time;
}

