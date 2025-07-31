package com.talentlink.talentlink.chat;

import com.talentlink.talentlink.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomUser {

    @EmbeddedId
    private ChatRoomUserId id = new ChatRoomUserId();

    @MapsId("chatRoomId")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private LocalDateTime lastReadAt; // 마지막으로 읽은 시간
}
