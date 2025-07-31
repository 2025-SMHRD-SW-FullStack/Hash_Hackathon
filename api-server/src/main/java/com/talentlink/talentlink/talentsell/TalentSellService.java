package com.talentlink.talentlink.talentsell;

import com.talentlink.talentlink.talentsell.dto.TalentSellRequest;
import com.talentlink.talentlink.talentsell.dto.TalentSellResponse;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalentSellService {

    private final TalentSellRepository sellRepository;
    private final UserRepository userRepository;

    public TalentSellResponse register(TalentSellRequest dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        TalentSell sell = TalentSell.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .user(user)
                .build();

        sellRepository.save(sell);
        return TalentSellResponse.from(sell);
    }

    public List<TalentSellResponse> getList() {
        return sellRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TalentSellResponse::from)
                .toList();
    }

    public TalentSellResponse getDetail(Long id) {
        TalentSell sell = sellRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재능 판매 글 없음"));
        return TalentSellResponse.from(sell);
    }

}