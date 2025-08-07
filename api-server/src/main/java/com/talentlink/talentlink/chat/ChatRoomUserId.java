package com.talentlink.talentlink.chat;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomUserId implements Serializable {
    private Long chatRoomId;
    private Long userId;

    // ★ equals & hashCode 꼭 구현! (이 부분 추가)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatRoomUserId)) return false;
        ChatRoomUserId that = (ChatRoomUserId) o;
        return Objects.equals(chatRoomId, that.chatRoomId)
                && Objects.equals(userId, that.userId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(chatRoomId, userId);
    }
}

