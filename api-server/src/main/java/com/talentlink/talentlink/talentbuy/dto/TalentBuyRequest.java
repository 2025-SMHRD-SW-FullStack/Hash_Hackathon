package com.talentlink.talentlink.talentbuy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "재능 구매 등록 요청 DTO")
public class TalentBuyRequest {

    @Schema(description = "제목", example = "로고 디자인 잘하시는 분 구합니다.")
    private String title;

    @Schema(description = "설명", example = "2025년 8월 15일까지 로고 디자인 부탁드립니다.")
    private String description;

    @Schema(description = "희망가격", example = "5000")
    private Integer budget;

    @Schema(description = "카테고리", example = "디자인")
    private String category;

    @Schema(description = "마감일", example = "2025-08-15T23:59:00")
    private LocalDateTime deadline;

}