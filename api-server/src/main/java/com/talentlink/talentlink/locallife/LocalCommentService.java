package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.locallife.dto.CommentRequest;
import com.talentlink.talentlink.locallife.dto.CommentResponse;
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

    public CommentResponse addComment(Long postId, CommentRequest req) {
        LocalPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        LocalComment comment = LocalComment.builder()
                .post(post)
                .writerNickname(req.getWriterNickname())
                .address(req.getAddress())
                .content(req.getContent())
                .build();
        return CommentResponse.from(commentRepo.save(comment));
    }
}

