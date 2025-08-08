package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.locallife.dto.CommentRequest;
import com.talentlink.talentlink.locallife.dto.CommentResponse;
import com.talentlink.talentlink.user.User; // User 임포트 추가
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocalCommentService {
    private final LocalCommentRepository commentRepo;
    private final LocalPostRepository postRepo;

    public List<CommentResponse> getComments(Long postId) {
        return commentRepo.findByPostIdOrderByCreatedAtAsc(postId)
                .stream().map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    // 메서드 시그니처를 User 객체를 받도록 수정
    public CommentResponse addComment(Long postId, CommentRequest req, User user) {
        LocalPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        LocalComment comment = LocalComment.builder()
                .post(post)
                .writerNickname(user.getNickname()) // 인증된 사용자의 닉네임을 사용
                .address(post.getAddress())         // 게시글의 주소를 사용
                .content(req.getContent())
                .build();
        return CommentResponse.from(commentRepo.save(comment));
    }
}