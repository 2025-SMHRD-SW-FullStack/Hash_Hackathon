package com.talentlink.talentlink.chat;

import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepo;
    private final ChatMessageRepository chatMessageRepo;
    private final ChatRoomUserRepository chatRoomUserRepo;
    private final UserService userService;

    public ChatRoom createRoom(User user1, User user2) {
        ChatRoom room = ChatRoom.builder().build();
        chatRoomRepo.save(room);
        chatRoomUserRepo.save(ChatRoomUser.builder().chatRoom(room).user(user1).build());
        chatRoomUserRepo.save(ChatRoomUser.builder().chatRoom(room).user(user2).build());
        return room;
    }

    public ChatMessage sendMessage(Long roomId, Long senderId, String content) {
        ChatRoom room = chatRoomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
        User sender = userService.findById(senderId); // ✅ 여기서 실제 유저 조회

        ChatMessage msg = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        return chatMessageRepo.save(msg);
    }

    public void markAsRead(Long roomId, Long userId) {
        ChatRoomUser roomUser = chatRoomUserRepo.findByChatRoomIdAndUserId(roomId, userId).orElseThrow();
        roomUser.setLastReadAt(LocalDateTime.now());
        chatRoomUserRepo.save(roomUser);

        // isRead true로도 반영
        List<ChatMessage> unread = chatMessageRepo.findByChatRoomIdOrderBySentAtAsc(roomId).stream()
                .filter(msg -> !msg.getSender().getId().equals(userId) && !msg.isRead())
                .collect(Collectors.toList());

        unread.forEach(msg -> msg.setRead(true));
        chatMessageRepo.saveAll(unread);
    }

    public long getUnreadCount(Long roomId, Long userId) {
        return chatMessageRepo.countUnreadMessages(roomId, userId);
    }
}
