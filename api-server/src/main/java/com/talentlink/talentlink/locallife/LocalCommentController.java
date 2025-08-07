package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.locallife.dto.CommentRequest;
import com.talentlink.talentlink.locallife.dto.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "댓글 API", description = "게시글에 대한 댓글 관련 API")
public class LocalCommentController {
    private final LocalCommentService commentService;

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 모든 댓글을 조회합니다.")
    @GetMapping
    public List<CommentResponse> getAll(@PathVariable Long postId) {
        return commentService.getComments(postId);
    }

    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
    @PostMapping
    public CommentResponse add(@PathVariable Long postId, @RequestBody CommentRequest req) {
        return commentService.addComment(postId, req);
    }
}
