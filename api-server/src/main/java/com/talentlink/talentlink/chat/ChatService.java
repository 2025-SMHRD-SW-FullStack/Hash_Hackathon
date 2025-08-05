package com.talentlink.talentlink.chat;

import com.talentlink.talentlink.chat.dto.ChatRoomListItemDto;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    public List<ChatRoomListItemDto> getChatRoomList(Long myUserId){
        // 참여중인 채팅방
        List<ChatRoomUser> myRoomLinks = chatRoomUserRepo.findByUserId(myUserId);

        List<ChatRoomListItemDto> roomDtos = new ArrayList<>();
        for(ChatRoomUser cru : myRoomLinks){
            ChatRoom room = cru.getChatRoom();

            // 마지막 메시지
            Optional<ChatMessage> optLastMsg = chatMessageRepo.findTopByChatRoomIdOrderBySentAtDesc(room.getId());
            ChatMessage lastMsg = optLastMsg.orElse(null);

            // 안읽은 메시지 수
            Long unreadCount = chatMessageRepo.countUnreadMessages(room.getId(),myUserId);

            // 상대방 정보
            List<ChatRoomUser> usersInRoom = chatRoomUserRepo.findByChatRoomId(room.getId());
            User opponent = usersInRoom.stream()
                    .map(ChatRoomUser::getUser)
                    .filter(u->!u.getId().equals(myUserId))
                    .findFirst()
                    .orElse(null);

//            roomDtos.add(new ChatRoomListItemDto(
//                    room.getId(),
//                    opponent != null ? opponent.getNickname() : "알 수 없음",
//                    opponent != null ? opponent.getProfileImageUrl() : null,
//                    lastMsg != null ? lastMsg.getContent() : "",
//                    lastMsg != null ? lastMsg.getSentAt() : null,
//                    unreadCount
//            ));

        }

//        roomDtos.sort(Comparator.comparing(ChatRoomListItemDto::getLastMessageAt,Comparator.nullsLast(Comparator.reverseOrder())));

        return roomDtos;
    }

}
