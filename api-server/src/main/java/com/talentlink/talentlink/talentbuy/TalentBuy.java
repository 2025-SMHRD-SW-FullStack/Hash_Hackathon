package com.talentlink.talentlink.talentbuy;

import com.talentlink.talentlink.common.BaseTimeEntity;
import com.talentlink.talentlink.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter // ✅ 수정 기능을 위해 Setter 추가
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalentBuy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private Integer budget;

    @Column(length = 1000)
    private String imageUrl;

    private LocalDateTime deadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}