package com.talentlink.talentlink.chat;

import com.talentlink.talentlink.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ChatMessage.java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;

    private String content;

    private LocalDateTime sentAt;

    private boolean isRead;
}
