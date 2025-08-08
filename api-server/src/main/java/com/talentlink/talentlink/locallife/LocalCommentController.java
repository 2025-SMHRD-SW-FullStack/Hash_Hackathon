package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.locallife.dto.CommentRequest;
import com.talentlink.talentlink.locallife.dto.CommentResponse;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "댓글 API", description = "게시글에 대한 댓글 관련 API")
public class LocalCommentController {
    private final LocalCommentService commentService;
    private final UserService userService; // UserService 주입

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 모든 댓글을 조회합니다.")
    @GetMapping
    public List<CommentResponse> getAll(@PathVariable Long postId) {
        return commentService.getComments(postId);
    }

    @Operation(summary = "댓글 작성", description = "특정 게시글에 댓글을 작성합니다.")
    @PostMapping
    public CommentResponse add(
            @PathVariable Long postId,
            @RequestBody CommentRequest req,
            @AuthenticationPrincipal UserDetails userDetails // 인증 정보 받기
    ) {
        User user = userService.getUserFromPrincipal(userDetails); // UserDetails에서 User 객체 조회
        return commentService.addComment(postId, req, user); // 서비스에 User 객체 전달
    }
}