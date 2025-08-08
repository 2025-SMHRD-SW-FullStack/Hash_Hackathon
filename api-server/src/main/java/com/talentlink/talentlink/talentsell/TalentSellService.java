package com.talentlink.talentlink.talentsell;

import com.talentlink.talentlink.talentsell.dto.TalentSellRequest;
import com.talentlink.talentlink.talentsell.dto.TalentSellResponse;
import com.talentlink.talentlink.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalentSellService {

    private final TalentSellRepository sellRepository;

    @Transactional
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

    @Transactional(readOnly = true)
    public List<TalentSellResponse> getList() {
        return sellRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TalentSellResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TalentSellResponse getDetail(Long id) {
        TalentSell sell = sellRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재능 판매 글 없음"));
        return TalentSellResponse.from(sell);
    }

    // ✅ 수정 서비스 로직 추가
    @Transactional
    public TalentSell update(Long id, TalentSellRequest dto, String imageUrl, User user) {
        TalentSell sell = sellRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재능 판매 글 없음"));

        if (!sell.getUser().getId().equals(user.getId())) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        sell.setTitle(dto.getTitle());
        sell.setDescription(dto.getDescription());
        sell.setPrice(dto.getPrice());
        if (imageUrl != null) { // ✅ 이미지가 새로 첨부된 경우에만 URL 업데이트
            sell.setImageUrl(imageUrl);
        }
        return sellRepository.save(sell);
    }

    // ✅ 삭제 서비스 로직 추가
    @Transactional
    public void delete(Long id, User user) {
        TalentSell sell = sellRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재능 판매 글 없음"));

        if (!sell.getUser().getId().equals(user.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        sellRepository.delete(sell);
    }
}