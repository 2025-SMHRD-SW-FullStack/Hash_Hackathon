package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.exception.UserNotFoundException;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalPostLikeService {

    private final LocalPostLikeRepository likeRepo;
    private final LocalPostRepository postRepo;
    private final UserRepository userRepo;

    @Transactional
    public void likePost(Long postId, Long userId) {
        // 게시글이 존재하는지 확인하고, 없으면 명확한 에러 메시지를 보냅니다.
        LocalPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        // 사용자가 존재하는지 확인하고, 없으면 UserNotFoundException을 발생시킵니다.
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        // 이미 '좋아요'를 눌렀는지 확인합니다.
        likeRepo.findByPostIdAndUserId(postId, userId)
                .ifPresent(like -> { throw new IllegalStateException("User has already liked this post"); });

        LocalPostLike like = LocalPostLike.builder()
                .post(post)
                .user(user)
                .build();
        likeRepo.save(like);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        // '좋아요' 취소 시에도 게시글과 사용자가 존재하는지 확인하여 안정성을 높입니다.
        if (!postRepo.existsById(postId)) {
            throw new IllegalArgumentException("Post not found with id: " + postId);
        }
        if (!userRepo.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
        likeRepo.deleteByPostIdAndUserId(postId, userId);
    }

    public boolean isLiked(Long postId, Long userId) {
        return likeRepo.findByPostIdAndUserId(postId, userId).isPresent();
    }

    public long getLikeCount(Long postId) {
        return likeRepo.countByPostId(postId);
    }
}