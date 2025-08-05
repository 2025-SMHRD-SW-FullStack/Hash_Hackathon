package com.talentlink.talentlink.favorite;

import com.talentlink.talentlink.favorite.dto.FavoriteRequest;
import com.talentlink.talentlink.favorite.dto.FavoriteResponse;
import com.talentlink.talentlink.talentbuy.TalentBuy;
import com.talentlink.talentlink.talentbuy.TalentBuyRepository;
import com.talentlink.talentlink.talentsell.TalentSell;
import com.talentlink.talentlink.talentsell.TalentSellRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private TalentBuyRepository talentBuyRepository;

    @Autowired
    private TalentSellRepository talentSellRepository;

    @Transactional
    public List<FavoriteResponse> getAll(String userId){
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
                .map(fav -> {
                    FavoriteResponse dto = new FavoriteResponse();
                    dto.setId(fav.getId());
                    dto.setUserId(fav.getUserId());

                    if (fav.getBuyId() != null) {
                        dto.setType("buy");
                        dto.setTitle(fav.getBuyId().getTitle());
                        dto.setWriterNickname(
                                fav.getBuyId()
                                        .getUser()
                                        .getNickname()
                        );
                    } else if (fav.getSellId() != null) {
                        dto.setType("sell");
                        dto.setTitle(fav.getSellId().getTitle());
                        dto.setWriterNickname(
                                fav.getSellId()
                                    .getUser()
                                    .getNickname()
                        );
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void saveFavorite(FavoriteRequest resDto){
        Favorite favorite = new Favorite();
        favorite.setUserId(resDto.getUserId());
        if(resDto.getType().equals("buy")){
            TalentBuy buy = talentBuyRepository.findById(resDto.getBuyId())
                            .orElseThrow(()->new RuntimeException("값이 없음"));
            favorite.setBuyId(buy);
            favorite.setSellId(null);
        } else if (resDto.getType().equals("sell")) {
            TalentSell sell = talentSellRepository.findById(resDto.getId())
                    .orElseThrow(()->new RuntimeException("값이 없음"));
            favorite.setBuyId(null);
            favorite.setSellId(sell);
        }
        favoriteRepository.save(favorite);

    }

    public void deleteFavorite(Long id){
        favoriteRepository.deleteById(id);
    }

}
