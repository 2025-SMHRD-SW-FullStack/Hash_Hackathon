package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.locallife.dto.LocalPostRequest;
import com.talentlink.talentlink.locallife.dto.LocalPostResponse;
import com.talentlink.talentlink.user.User; // 👈 User 임포트 추가
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 👈 @Transactional 임포트 추가

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalPostService {

    private final LocalPostRepository postRepo;

    // ✅ User 객체를 파라미터로 받도록 수정
    @Transactional
    public LocalPostResponse createPost(LocalPostRequest request, String imageUrl, User user) {
        LocalPost post = LocalPost.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writerNickname(user.getNickname()) // 👈 인증된 사용자의 닉네임 사용
                .address(request.getAddress())
                .imageUrl(imageUrl)
                .user(user) // 👈 작성자 정보 설정
                .build();
        LocalPost saved = postRepo.save(post);
        return LocalPostResponse.from(saved);
    }

    // ✅ 게시글 수정 로직 추가
    @Transactional
    public LocalPostResponse updatePost(Long id, LocalPostRequest request, String newImageUrl, User user) {
        LocalPost post = postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 본인 확인
        if (!post.getUser().getId().equals(user.getId())) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAddress(request.getAddress());
        if (newImageUrl != null) { // 새 이미지가 있을 경우에만 업데이트
            post.setImageUrl(newImageUrl);
        }

        return LocalPostResponse.from(postRepo.save(post));
    }

    // ✅ 게시글 삭제 로직 추가
    @Transactional
    public void deletePost(Long id, User user) {
        LocalPost post = postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        // 본인 확인
        if (!post.getUser().getId().equals(user.getId())) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        postRepo.delete(post);
    }


    @Transactional(readOnly = true)
    public List<LocalPostResponse> getAllPosts() {
        return postRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(LocalPostResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LocalPostResponse getPost(Long id) {
        LocalPost post = postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        return LocalPostResponse.from(post);
    }
}