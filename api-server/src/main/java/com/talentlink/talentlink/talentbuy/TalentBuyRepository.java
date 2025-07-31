package com.talentlink.talentlink.talentbuy;

import com.talentlink.talentlink.talentsell.TalentSell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalentBuyRepository extends JpaRepository<TalentBuy, Long> {
    List<TalentBuy> findAllByOrderByCreatedAtDesc();
}

