package com.talentlink.talentlink.talentsell.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Schema(description = "재능 판매 등록 요청 DTO")
public class TalentSellRequest {

    @Schema(description = "제목", example = "로고 디자인 해드립니다.")
    private String title;

    @Schema(description = "설명", example = "간단한 포토샵 작업 포함, 3일 내 완료 가능.")
    private String description;

    @Schema(description = "가격", example = "5000")
    private Integer price;

    @Schema(description = "상품 이미지", type = "string", format = "binary")
    private MultipartFile image;
}
