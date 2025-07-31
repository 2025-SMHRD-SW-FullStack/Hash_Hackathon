package com.talentlink.talentlink.talentsell;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TalentSellRepository extends JpaRepository<TalentSell, Long> {
    List<TalentSell> findAllByOrderByCreatedAtDesc();
}
