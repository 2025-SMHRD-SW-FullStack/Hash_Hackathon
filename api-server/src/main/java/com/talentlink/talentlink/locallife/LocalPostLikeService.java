package com.talentlink.talentlink.locallife;

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
        LocalPost post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        likeRepo.findByPostIdAndUserId(postId, userId)
                .ifPresent(like -> { throw new IllegalStateException("이미 좋아요 함"); });

        LocalPostLike like = LocalPostLike.builder()
                .post(post)
                .user(user)
                .build();
        likeRepo.save(like);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        likeRepo.deleteByPostIdAndUserId(postId, userId);
    }

    public boolean isLiked(Long postId, Long userId) {
        return likeRepo.findByPostIdAndUserId(postId, userId).isPresent();
    }

    public long getLikeCount(Long postId) {
        return likeRepo.countByPostId(postId);
    }
}
