package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalPostLikeRepository extends JpaRepository<LocalPostLike, Long> {
    Optional<LocalPostLike> findByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
