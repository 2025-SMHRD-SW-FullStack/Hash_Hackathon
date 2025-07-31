package com.talentlink.talentlink.talentsell;

import com.talentlink.talentlink.talentsell.dto.TalentSellRequest;
import com.talentlink.talentlink.talentsell.dto.TalentSellResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/talentsell")
@RequiredArgsConstructor
@Tag(name = "재능 판매", description = "재능 판매 등록/조회 API")
public class TalentSellController {

    private final TalentSellService talentSellService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "재능 판매 등록", description = "사용자가 자신의 재능을 판매 글로 등록합니다.")
    @PostMapping
    public ResponseEntity<TalentSellResponse> register(
            @RequestBody TalentSellRequest dto,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        return ResponseEntity.ok(talentSellService.register(dto, userId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "재능 판매 전체 조회", description = "등록된 재능 판매 리스트를 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<List<TalentSellResponse>> getAll() {
        return ResponseEntity.ok(talentSellService.getList());
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "재능 판매 단건 조회", description = "ID로 단일 재능 판매 글을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<TalentSellResponse> getOne(
            @Parameter(description = "글 ID", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(talentSellService.getDetail(id));
    }
}
