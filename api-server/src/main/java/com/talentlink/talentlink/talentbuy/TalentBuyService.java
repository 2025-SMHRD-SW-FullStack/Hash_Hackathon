package com.talentlink.talentlink.talentbuy;

import com.talentlink.talentlink.talentbuy.dto.TalentBuyRequest;
import com.talentlink.talentlink.talentbuy.dto.TalentBuyResponse;
import com.talentlink.talentlink.talentsell.TalentSell;
import com.talentlink.talentlink.talentsell.dto.TalentSellRequest;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalentBuyService {

    private final TalentBuyRepository buyRepository;
    private final UserRepository userRepository;

    public TalentBuy register(TalentBuyRequest dto, String imageUrl, User user) {
        TalentBuy buy = TalentBuy.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .budget(dto.getBudget())
                .imageUrl(imageUrl)
                .deadline(dto.getDeadline())
                .user(user)
                .build();
        return buyRepository.save(buy);
    }

    public List<TalentBuyResponse> getList() {
        return buyRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TalentBuyResponse::from)
                .toList();
    }

    public TalentBuyResponse getDetail(Long id) {
        TalentBuy buy = buyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재능 구매 글 없음"));
        return TalentBuyResponse.from(buy);
    }
}