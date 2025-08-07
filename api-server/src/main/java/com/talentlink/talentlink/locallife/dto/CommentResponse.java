package com.talentlink.talentlink.locallife.dto;

import com.talentlink.talentlink.locallife.LocalComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String writerNickname;
    private String address;
    private LocalDateTime createdAt;

    public static CommentResponse from(LocalComment c) {
        return CommentResponse.builder()
                .id(c.getId())
                .content(c.getContent())
                .writerNickname(c.getWriterNickname())
                .address(c.getAddress())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
