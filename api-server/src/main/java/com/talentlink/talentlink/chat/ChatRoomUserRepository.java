package com.talentlink.talentlink.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, ChatRoomUserId> {
    Optional<ChatRoomUser> findByChatRoomIdAndUserId(Long roomId, Long userId);
}