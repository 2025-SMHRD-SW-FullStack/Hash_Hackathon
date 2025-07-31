package com.talentlink.talentlink.talentbuy;

import com.talentlink.talentlink.talentbuy.dto.TalentBuyRequest;
import com.talentlink.talentlink.talentbuy.dto.TalentBuyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/talentbuy")
@RequiredArgsConstructor
@Tag(name = "재능 구매", description = "재능 구매 등록/조회 API")
public class TalentBuyController {

    private final TalentBuyService talentBuyService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "재능 구매 등록", description = "사용자가 구매하고 싶은 재능을 글로 등록합니다.")
    @PostMapping
    public ResponseEntity<TalentBuyResponse> register(
            @RequestBody TalentBuyRequest dto,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        return ResponseEntity.ok(talentBuyService.register(dto, userId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "재능 구매 전체 조회", description = "등록된 재능 구매글 리스트를 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<List<TalentBuyResponse>> getAll() {
        return ResponseEntity.ok(talentBuyService.getList());
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "재능 구매 단건 조회", description = "ID로 단일 재능 구매 글을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<TalentBuyResponse> getOne(
            @Parameter(description = "글 ID", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(talentBuyService.getDetail(id));
    }
}
