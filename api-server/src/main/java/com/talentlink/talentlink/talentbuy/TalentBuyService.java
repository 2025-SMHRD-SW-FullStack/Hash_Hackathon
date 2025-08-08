package com.talentlink.talentlink.talentbuy;

import com.talentlink.talentlink.talentbuy.dto.TalentBuyRequest;
import com.talentlink.talentlink.talentbuy.dto.TalentBuyResponse;
import com.talentlink.talentlink.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TalentBuyService {

    private final TalentBuyRepository buyRepository;

    @Transactional
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

    @Transactional(readOnly = true)
    public List<TalentBuyResponse> getList() {
        return buyRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(TalentBuyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TalentBuyResponse getDetail(Long id) {
        TalentBuy buy = buyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재능 구매 글 없음"));
        return TalentBuyResponse.from(buy);
    }

    // ✅ 수정 서비스 로직 추가
    @Transactional
    public TalentBuy update(Long id, TalentBuyRequest dto, String imageUrl, User user) {
        TalentBuy buy = buyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재능 구매 글 없음"));

        if (!buy.getUser().getId().equals(user.getId())) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        buy.setTitle(dto.getTitle());
        buy.setDescription(dto.getDescription());
        buy.setBudget(dto.getBudget());
        buy.setDeadline(dto.getDeadline());
        if (imageUrl != null) { // ✅ 이미지가 새로 첨부된 경우에만 URL 업데이트
            buy.setImageUrl(imageUrl);
        }
        return buyRepository.save(buy);
    }

    // ✅ 삭제 서비스 로직 추가
    @Transactional
    public void delete(Long id, User user) {
        TalentBuy buy = buyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("재능 구매 글 없음"));

        if (!buy.getUser().getId().equals(user.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        buyRepository.delete(buy);
    }
}