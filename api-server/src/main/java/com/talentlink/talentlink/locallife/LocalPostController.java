package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.common.FileService;
import com.talentlink.talentlink.locallife.dto.LocalPostRequest;
import com.talentlink.talentlink.locallife.dto.LocalPostResponse;
import com.talentlink.talentlink.user.User; // 👈 User 임포트
import com.talentlink.talentlink.user.UserService; // 👈 UserService 임포트
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter; // 👈 Parameter 임포트
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 👈 SecurityRequirement 임포트
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 👈 @AuthenticationPrincipal 임포트
import org.springframework.security.core.userdetails.UserDetails; // 👈 UserDetails 임포트
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map; // 👈 Map 임포트

@RestController
@RequestMapping("/api/localposts")
@RequiredArgsConstructor
@Tag(name = "로컬라이프 게시글 API", description = "로컬라이프 게시글 CRUD 관련 API")
public class LocalPostController {

    private final LocalPostService localPostService;
    private final FileService fileService;
    private final UserService userService; // 👈 UserService 주입

    @SecurityRequirement(name = "bearerAuth") // 👈 Swagger 인증 추가
    @Operation(summary = "게시글 생성", description = "로컬라이프 게시글을 작성합니다.")
    @PostMapping
    public ResponseEntity<LocalPostResponse> createPost(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails, // 👈 userDetails 추가
            @RequestPart("request") LocalPostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest httpServletRequest
    ) {
        User user = userService.getUserFromPrincipal(userDetails); // 👈 작성자 정보 가져오기
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            String relativeUrl = fileService.upload(image);
            String scheme = httpServletRequest.getScheme();
            String serverName = httpServletRequest.getServerName();
            int serverPort = httpServletRequest.getServerPort();
            String baseUrl = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
            imageUrl = baseUrl + relativeUrl;
        }

        LocalPostResponse saved = localPostService.createPost(request, imageUrl, user); // 👈 user 정보 전달
        return ResponseEntity.ok(saved);
    }

    // 👇 게시글 수정 API 추가
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", description = "ID로 특정 게시글을 수정합니다.")
    public ResponseEntity<LocalPostResponse> updatePost(
            @Parameter(description = "수정할 게시글 ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("request") LocalPostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            HttpServletRequest httpServletRequest
    ) {
        User user = userService.getUserFromPrincipal(userDetails);
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            String relativeUrl = fileService.upload(image);
            // ... (기존과 동일하게 절대 URL 생성)
            String scheme = httpServletRequest.getScheme();
            String serverName = httpServletRequest.getServerName();
            int serverPort = httpServletRequest.getServerPort();
            String baseUrl = scheme + "://" + serverName + ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);
            imageUrl = baseUrl + relativeUrl;
        }
        LocalPostResponse updated = localPostService.updatePost(id, request, imageUrl, user);
        return ResponseEntity.ok(updated);
    }

    // 👇 게시글 삭제 API 추가
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", description = "ID로 특정 게시글을 삭제합니다.")
    public ResponseEntity<Map<String, String>> deletePost(
            @Parameter(description = "삭제할 게시글 ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getUserFromPrincipal(userDetails);
        localPostService.deletePost(id, user);
        return ResponseEntity.ok(Map.of("message", "삭제 완료"));
    }


    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "게시글 전체 목록 조회", description = "모든 로컬라이프 게시글을 조회합니다.")
    @GetMapping
    public List<LocalPostResponse> getAllPosts() {
        return localPostService.getAllPosts();
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세정보를 조회합니다.")
    @GetMapping("/{id}")
    public LocalPostResponse getPost(@PathVariable Long id) {
        return localPostService.getPost(id);
    }
}