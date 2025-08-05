package com.talentlink.talentlink.talentbuy;

import com.talentlink.talentlink.common.BaseTimeEntity;
import com.talentlink.talentlink.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
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

    private Integer budget;   // 판매에서는 price

    @Column(length = 1000)
    private String imageUrl;

    private LocalDateTime deadline;  // 요청 마감일

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}

