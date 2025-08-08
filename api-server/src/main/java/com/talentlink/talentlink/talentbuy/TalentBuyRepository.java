package com.talentlink.talentlink.talentbuy;

import com.talentlink.talentlink.talentsell.TalentSell;
import com.talentlink.talentlink.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalentBuyRepository extends JpaRepository<TalentBuy, Long> {
    List<TalentBuy> findAllByOrderByCreatedAtDesc();
    // User 객체를 받아 삭제하는 메서드를 추가합니다. (네 파일 모두 동일하게 추가)
    void deleteByUser(User user);
    List<TalentBuy> findByUser(User user);
}

