package com.talentlink.talentlink.user;

import com.talentlink.talentlink.auth.AuthProvider;
import com.talentlink.talentlink.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "providerId"})
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String nickname;

    // 데이터베이스와 맞추기 위해 새로 추가된 필드입니다.
    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = true, length = 255)
    private String email; // 로그인용 유저ID

    @Setter
    @Column(nullable = true)
    private String password; // 소셜은 null, 로컬은 존재

    @Setter
    @Column(nullable = true, length = 500)
    private String profileImageUrl; // 프로필 이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false, length = 100)
    private String providerId;

    @Setter
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;
}