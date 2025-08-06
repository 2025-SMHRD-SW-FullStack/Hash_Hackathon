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

    public TalentSell register(TalentSellRequest dto, String imageUrl, User user) {
        TalentSell sell = TalentSell.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(imageUrl)
                .user(user)
                .build();
        return sellRepository.save(sell);
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