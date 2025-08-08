package com.talentlink.talentlink.talentsell;

import com.talentlink.talentlink.common.BaseTimeEntity;
import com.talentlink.talentlink.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter // ✅ 수정 기능을 위해 Setter 추가
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentSell extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(length = 1000)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}