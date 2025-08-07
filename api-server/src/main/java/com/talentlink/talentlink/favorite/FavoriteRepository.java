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
}
