package com.talentlink.talentlink.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, ChatRoomUserId> {
//    Optional<ChatRoomUser> findByChatRoomIdAndUserId(Long roomId, Long userId);
    // 1. 내가 참여한 모든 방 찾기 (채팅방 목록 조회)
    List<ChatRoomUser> findByUserId(Long userId);

    // 2. 특정 채팅방의 모든 참여자 (상대방 정보 확인)
    List<ChatRoomUser> findByChatRoomId(Long chatRoomId);

    // 3. 복합키(방+유저)로 찾기 (읽음 처리 등에 사용)
    Optional<ChatRoomUser> findByChatRoomIdAndUserId(Long chatRoomId, Long userId);
}