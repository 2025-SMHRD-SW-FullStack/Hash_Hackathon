package com.talentlink.talentlink.favorite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {

    private Long id;
    private String userId;
    private String type;
    private Long sellId;   // 판매글일 때 해당 게시글 id
    private Long buyId;    // 구매글일 때 해당 게시글 id
    private String title;
    private String writerNickname;
    private LocalDateTime time;

}
