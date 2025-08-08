package com.talentlink.talentlink.locallife;

import com.talentlink.talentlink.common.BaseTimeEntity;
import com.talentlink.talentlink.user.User; // 👈 추가
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocalPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private String writerNickname;
    private String address;
    private String imageUrl;

    // 👇 작성자 정보를 저장하기 위한 User 연관 관계 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LocalPostLike> likes = new ArrayList<>();

    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }
}