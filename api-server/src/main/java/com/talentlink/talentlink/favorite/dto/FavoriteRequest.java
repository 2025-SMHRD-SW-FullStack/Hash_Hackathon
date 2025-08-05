package com.talentlink.talentlink.favorite.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FavoriteRequest {

    private Long id;
    private String type;
    private String userId;
    private String writerNickname;
    private Long buyId;
    private Long sellId;
    private LocalDateTime time;
}
