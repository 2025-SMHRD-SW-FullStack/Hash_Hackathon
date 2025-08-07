package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.locallife.dto.LocalPostLikeRequest;
import com.talentlink.talentlink.locallife.dto.LocalPostLikeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/localposts/{postId}/likes")
@RequiredArgsConstructor
@Tag(name = "게시글 좋아요 API", description = "로컬라이프 게시글 좋아요 관련 API")
public class LocalPostLikeController {

    private final LocalPostLikeService likeService;

    @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 누릅니다.")
    @PostMapping
    public ResponseEntity<?> like(
            @PathVariable Long postId,
            @RequestBody LocalPostLikeRequest request
    ) {
        likeService.likePost(postId, request.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 좋아요 취소", description = "특정 게시글에 누른 좋아요를 취소합니다.")
    @DeleteMapping
    public ResponseEntity<?> unlike(
            @PathVariable Long postId,
            @RequestBody LocalPostLikeRequest request
    ) {
        likeService.unlikePost(postId, request.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내가 좋아요 했는지 + 좋아요 수 조회", description = "내가 좋아요 했는지 여부와 해당 게시글의 좋아요 개수를 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<LocalPostLikeResponse> isLiked(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        boolean liked = likeService.isLiked(postId, userId);
        long count = likeService.getLikeCount(postId);
        return ResponseEntity.ok(new LocalPostLikeResponse(liked, count));
    }

    @Operation(summary = "좋아요 개수 조회", description = "해당 게시글의 좋아요 개수만 반환합니다.")
    @GetMapping("/count")
    public ResponseEntity<Long> count(@PathVariable Long postId) {
        return ResponseEntity.ok(likeService.getLikeCount(postId));
    }
}
