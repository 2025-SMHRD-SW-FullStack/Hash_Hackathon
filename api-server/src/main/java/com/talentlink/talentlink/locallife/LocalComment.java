package com.talentlink.talentlink.locallife;


import com.talentlink.talentlink.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalComment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private LocalPost post;

    private String writerNickname;
    private String address;
    private String content;
    // createdAt(상속), 필요시 프로필 이미지 등 추가
}
