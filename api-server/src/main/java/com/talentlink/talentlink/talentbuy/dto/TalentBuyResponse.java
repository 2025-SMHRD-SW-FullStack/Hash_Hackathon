package com.talentlink.talentlink.talentbuy.dto;

import com.talentlink.talentlink.talentbuy.TalentBuy;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "재능 구매 응답 DTO")
public class TalentBuyResponse {

    @Schema(description = "글 ID", example = "1")
    private Long id;

    @Schema(description = "제목", example = "로고 디자인 잘하시는 분 구합니다.")
    private String title;

    @Schema(description = "설명", example = "2025년 8월 15일까지 로고 디자인 부탁드립니다.")
    private String description;

    @Schema(description = "희망가격", example = "5000")
    private Integer budget;

    @Schema(description = "상품 이미지", type = "string", format = "binary")
    private String imageUrl;

    @Schema(description = "마감일", example = "2025-08-15T23:59:00")
    private LocalDateTime deadline;

    @Schema(description = "작성자 닉네임", example = "designerKim")
    private String writerNickname;

    // ✅ writerId 필드 추가
    @Schema(description = "작성자 ID", example = "42")
    private Long writerId;

    @Schema(description = "작성일", example = "2025-07-24T18:21:01")
    private LocalDateTime createdAt;

    public static TalentBuyResponse from(TalentBuy buy) {
        return TalentBuyResponse.builder()
                .id(buy.getId())
                .title(buy.getTitle())
                .description(buy.getDescription())
                .budget(buy.getBudget())
                .imageUrl(buy.getImageUrl())
                .deadline(buy.getDeadline())
                .writerNickname(buy.getUser().getNickname())
                .writerId(buy.getUser().getId()) // ✅ 작성자 ID 추가
                .createdAt(buy.getCreatedAt())
                .build();
    }
}