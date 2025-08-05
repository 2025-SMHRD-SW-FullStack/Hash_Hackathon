package com.talentlink.talentlink.talentbuy;

import com.talentlink.talentlink.common.FileService;
import com.talentlink.talentlink.talentbuy.dto.TalentBuyRequest;
import com.talentlink.talentlink.talentbuy.dto.TalentBuyResponse;
import com.talentlink.talentlink.talentsell.TalentSell;
import com.talentlink.talentlink.talentsell.dto.TalentSellRequest;
import com.talentlink.talentlink.talentsell.dto.TalentSellResponse;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/talentbuy")
@RequiredArgsConstructor
@Tag(name = "재능 구매", description = "재능 구매 등록/조회 API")
public class TalentBuyController {

    private final TalentBuyService talentBuyService;
    private final UserService userService;
    private final FileService fileService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @Operation(summary = "재능 구매 등록", description = "재능 구매 글을 작성합니다.")
    public ResponseEntity<TalentSellResponse> createTalentSell(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("request") TalentBuyRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        User user = userService.getUserFromPrincipal(userDetails);
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            imageUrl = fileService.upload(image);
        }

        TalentBuy saved = talentBuyService.register(request, imageUrl, user);
        return ResponseEntity.ok(TalentBuyResponse.from(saved));
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
