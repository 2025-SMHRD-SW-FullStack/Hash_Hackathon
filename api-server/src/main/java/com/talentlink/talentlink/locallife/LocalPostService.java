package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.locallife.dto.LocalPostRequest;
import com.talentlink.talentlink.locallife.dto.LocalPostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalPostService {

    private final LocalPostRepository postRepo;

    // ✅ 메서드 시그니처를 수정하여 imageUrl을 파라미터로 받습니다.
    public LocalPostResponse createPost(LocalPostRequest request, String imageUrl) {
        LocalPost post = LocalPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writerNickname(request.getWriterNickname())
                .address(request.getAddress())
                .imageUrl(imageUrl) // ✅ 파라미터로 받은 imageUrl을 사용합니다.
                .build();
        LocalPost saved = postRepo.save(post);
        return LocalPostResponse.from(saved);
    }

    // 전체 게시글 조회 (기존과 동일)
    public List<LocalPostResponse> getAllPosts() {
        return postRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(LocalPostResponse::from)
                .toList();
    }

    // 단일 게시글 조회 (기존과 동일)
    public LocalPostResponse getPost(Long id) {
        LocalPost post = postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        return LocalPostResponse.from(post);
    }
}