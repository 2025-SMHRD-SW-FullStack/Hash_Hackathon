package com.talentlink.talentlink.email;

import com.talentlink.talentlink.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken extends BaseTimeEntity {

    @Id
    @Column(length = 36) // UUID 고정 길이
    private String token;  // UUID로 생성

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    public static EmailVerificationToken create(String email) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.token = UUID.randomUUID().toString();
        token.email = email;
        token.expiryDate = LocalDateTime.now().plusHours(1);
        return token;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isVerified() {
        return this.verified;
    }

    public void markAsVerified() {
        this.verified = true;
    }
}
