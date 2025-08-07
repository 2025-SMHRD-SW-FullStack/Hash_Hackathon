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

    // 게시글 등록 (Request DTO → Entity → Response DTO)
    public LocalPostResponse createPost(LocalPostRequest request) {
        LocalPost post = LocalPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writerNickname(request.getWriterNickname())
                .address(request.getAddress())
                .imageUrl(request.getImageUrl())
                .build();
        LocalPost saved = postRepo.save(post);
        return LocalPostResponse.from(saved);
    }

    // 전체 게시글 조회 (Entity List → Response DTO List)
    public List<LocalPostResponse> getAllPosts() {
        return postRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(LocalPostResponse::from)
                .toList();
    }

    // 단일 게시글 조회 (Entity → Response DTO)
    public LocalPostResponse getPost(Long id) {
        LocalPost post = postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        return LocalPostResponse.from(post);
    }
}
