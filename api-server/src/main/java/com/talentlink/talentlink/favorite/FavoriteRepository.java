package com.talentlink.talentlink.favorite;

import com.talentlink.talentlink.talentbuy.TalentBuy;
import com.talentlink.talentlink.talentsell.TalentSell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite,Long> {
    List<Favorite> findByUserId(String userId);

    Optional<Favorite> findByUserIdAndSellId(String userId, TalentSell sellId);
    Optional<Favorite> findByUserIdAndBuyId(String userId, TalentBuy buyId);

    // Long 타입의 userId로 삭제하는 메서드를 추가합니다.
    void deleteByUserId(String userId);
    // 특정 판매글들을 참조하는 모든 관심 목록 삭제
    void deleteBySellIdIn(List<TalentSell> sells);
    // 특정 구매글들을 참조하는 모든 관심 목록 삭제
    void deleteByBuyIdIn(List<TalentBuy> buys);
}
