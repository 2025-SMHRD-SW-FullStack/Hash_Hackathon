package com.talentlink.talentlink.locallife;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocalCommentRepository extends JpaRepository<LocalComment, Long> {
    List<LocalComment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
