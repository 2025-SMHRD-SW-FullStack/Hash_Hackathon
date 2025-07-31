package com.talentlink.talentlink.talentbuy;

import com.talentlink.talentlink.talentbuy.dto.TalentBuyRequest;
import com.talentlink.talentlink.talentbuy.dto.TalentBuyResponse;
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

    public TalentBuyResponse register(TalentBuyRequest dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        TalentBuy buy = TalentBuy.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .budget(dto.getBudget())
                .category(dto.getCategory())
                .deadline(dto.getDeadline())
                .user(user)
                .build();

        buyRepository.save(buy);
        return TalentBuyResponse.from(buy);
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