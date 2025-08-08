package com.talentlink.talentlink.locallife.dto;

import com.talentlink.talentlink.locallife.LocalPost;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalPostResponse {
    private Long id;
    private String title;
    private String content;
    private String writerNickname;
    private String address;
    private String imageUrl;
    private String createdAt;
    private int likeCount;
    private Long writerId; // 👈 writerId 필드 추가

    public static LocalPostResponse from(LocalPost post) {
        return LocalPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .writerNickname(post.getWriterNickname())
                .address(post.getAddress())
                .imageUrl(post.getImageUrl())
                .createdAt(post.getCreatedAt().toString())
                .likeCount(post.getLikeCount())
                .writerId(post.getUser() != null ? post.getUser().getId() : null) // 👈 writerId 추가
                .build();
    }
}