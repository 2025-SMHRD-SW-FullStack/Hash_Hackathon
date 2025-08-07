package com.talentlink.talentlink.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 두 명의 사용자 ID로 1:1 채팅방을 찾는 쿼리
     * 두 명만 참여하고 있는 채팅방을 정확히 찾아냅니다.
     */
    @Query("SELECT cr FROM ChatRoom cr " +
            "WHERE EXISTS (SELECT 1 FROM ChatRoomUser cru WHERE cru.chatRoom.id = cr.id AND cru.user.id = :userId1) " +
            "AND EXISTS (SELECT 1 FROM ChatRoomUser cru WHERE cru.chatRoom.id = cr.id AND cru.user.id = :userId2) " +
            "AND (SELECT COUNT(cru) FROM ChatRoomUser cru WHERE cru.chatRoom.id = cr.id) = 2")
    Optional<ChatRoom> findExisting1on1Room(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}