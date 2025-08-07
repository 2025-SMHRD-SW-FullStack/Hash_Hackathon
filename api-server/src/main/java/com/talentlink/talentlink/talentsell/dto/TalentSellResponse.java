package com.talentlink.talentlink.talentsell.dto;

import com.talentlink.talentlink.talentsell.TalentSell;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "재능 판매 응답 DTO")
public class TalentSellResponse {

    @Schema(description = "글 ID", example = "1")
    private Long id;

    @Schema(description = "제목", example = "로고 디자인 해드립니다")
    private String title;

    @Schema(description = "설명", example = "간단한 포토샵 작업 포함")
    private String description;

    @Schema(description = "가격", example = "50000")
    private Integer price;

    @Schema(description = "상품 이미지", type = "string", format = "binary")
    private String imageUrl;

    @Schema(description = "작성자 닉네임", example = "designerKim")
    private String writerNickname;

    @Schema(description = "작성자 ID", example = "42")
    private Long writerId;

    @Schema(description = "작성일", example = "2025-07-24T18:21:01")
    private LocalDateTime createdAt;

    public static TalentSellResponse from(TalentSell sell) {
        return TalentSellResponse.builder()
                .id(sell.getId())
                .title(sell.getTitle())
                .description(sell.getDescription())
                .price(sell.getPrice())
                .imageUrl(sell.getImageUrl())
                .writerNickname(sell.getUser().getNickname())
                .writerId(sell.getUser().getId())
                .createdAt(sell.getCreatedAt())
                .build();
    }
}
