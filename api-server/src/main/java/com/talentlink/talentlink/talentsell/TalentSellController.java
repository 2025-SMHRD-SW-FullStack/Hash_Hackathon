package com.talentlink.talentlink.talentsell;

import com.talentlink.talentlink.common.FileService;
import com.talentlink.talentlink.talentsell.dto.TalentSellRequest;
import com.talentlink.talentlink.talentsell.dto.TalentSellResponse;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/talentsell")
@RequiredArgsConstructor
@Tag(name = "재능 판매", description = "재능 판매 등록/조회/수정/삭제 API")
public class TalentSellController {

    private final TalentSellService talentSellService;
    private final UserService userService;
    private final FileService fileService;

    // ... (기존 createTalentSell, getAll, getOne 메서드는 생략) ...
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @Operation(summary = "재능 판매 등록", description = "재능 판매 글을 작성합니다.")
    public ResponseEntity<TalentSellResponse> createTalentSell(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("request") TalentSellRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest httpServletRequest
    ) {
        User user = userService.getUserFromPrincipal(userDetails);
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            String relativeUrl = fileService.upload(image);
            String scheme = httpServletRequest.getScheme();
            String serverName = httpServletRequest.getServerName();
            int serverPort = httpServletRequest.getServerPort();
            String baseUrl = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
            imageUrl = baseUrl + relativeUrl;
        }

        TalentSell saved = talentSellService.register(request, imageUrl, user);
        return ResponseEntity.ok(TalentSellResponse.from(saved));
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

    // ✅ 수정 API 추가
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @Operation(summary = "재능 판매 수정", description = "재능 판매 글을 수정합니다.")
    public ResponseEntity<TalentSellResponse> updateTalentSell(
            @Parameter(description = "글 ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("request") TalentSellRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest httpServletRequest
    ) {
        User user = userService.getUserFromPrincipal(userDetails);
        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            String relativeUrl = fileService.upload(image);
            String scheme = httpServletRequest.getScheme();
            String serverName = httpServletRequest.getServerName();
            int serverPort = httpServletRequest.getServerPort();
            String baseUrl = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
            imageUrl = baseUrl + relativeUrl;
        }

        TalentSell updated = talentSellService.update(id, request, imageUrl, user);
        return ResponseEntity.ok(TalentSellResponse.from(updated));
    }

    // ✅ 삭제 API 추가
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @Operation(summary = "재능 판매 삭제", description = "재능 판매 글을 삭제합니다.")
    public ResponseEntity<Map<String, String>> deleteTalentSell(
            @Parameter(description = "글 ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUserFromPrincipal(userDetails);
        talentSellService.delete(id, user);
        return ResponseEntity.ok(Map.of("message", "삭제 완료"));
    }
}