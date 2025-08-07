package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.common.FileService; // ✅ FileService 임포트
import com.talentlink.talentlink.locallife.dto.LocalPostRequest;
import com.talentlink.talentlink.locallife.dto.LocalPostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // ✅ MultipartFile 임포트

import java.util.List;

@RestController
@RequestMapping("/api/localposts")
@RequiredArgsConstructor
@Tag(name = "로컬라이프 게시글 API", description = "로컬라이프 게시글 CRUD 관련 API")
public class LocalPostController {

    private final LocalPostService localPostService;
    private final FileService fileService; // ✅ FileService 주입

    @Operation(summary = "게시글 생성", description = "로컬라이프 게시글을 작성합니다.")
    @PostMapping
    public ResponseEntity<LocalPostResponse> createPost(
            @RequestPart("request") LocalPostRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileService.upload(image); // 이미지 업로드 후 URL 반환
        }

        LocalPostResponse saved = localPostService.createPost(request, imageUrl);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "게시글 전체 목록 조회", description = "모든 로컬라이프 게시글을 조회합니다.")
    @GetMapping
    public List<LocalPostResponse> getAllPosts() {
        return localPostService.getAllPosts();
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세정보를 조회합니다.")
    @GetMapping("/{id}")
    public LocalPostResponse getPost(@PathVariable Long id) {
        return localPostService.getPost(id);
    }
}