package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalPostRepository  extends JpaRepository<LocalPost, Long> {
    // User 객체를 받아 삭제하는 메서드를 추가합니다. (네 파일 모두 동일하게 추가)
    void deleteByUser(User user);
}
