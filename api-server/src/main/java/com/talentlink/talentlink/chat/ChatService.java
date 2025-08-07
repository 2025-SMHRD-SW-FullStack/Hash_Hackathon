package com.talentlink.talentlink.chat;

import com.talentlink.talentlink.chat.dto.ChatMessageDto;
import com.talentlink.talentlink.chat.dto.ChatRoomListItemDto;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ChatRoom findOrCreateRoom(User user1, User user2) {
        return chatRoomRepo.findExisting1on1Room(user1.getId(), user2.getId())
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder().build();
                    chatRoomRepo.save(newRoom);

                    ChatRoomUser cru1 = ChatRoomUser.builder()
                            .id(new ChatRoomUserId(newRoom.getId(), user1.getId()))
                            .chatRoom(newRoom)
                            .user(user1)
                            .lastReadAt(LocalDateTime.now())
                            .build();
                    chatRoomUserRepo.save(cru1);

                    ChatRoomUser cru2 = ChatRoomUser.builder()
                            .id(new ChatRoomUserId(newRoom.getId(), user2.getId()))
                            .chatRoom(newRoom)
                            .user(user2)
                            .lastReadAt(LocalDateTime.now())
                            .build();
                    chatRoomUserRepo.save(cru2);

                    return newRoom;
                });
    }

    public ChatMessage sendMessage(Long roomId, Long senderId, String content) {
        ChatRoom room = chatRoomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
        User sender = userService.findById(senderId);

        ChatMessage msg = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(content)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .build();
        return chatMessageRepo.save(msg);
    }

    @Transactional
    public void markAsRead(Long roomId, Long userId) {
        if (roomId == null || roomId <= 0) {
            System.out.println("경고: 유효하지 않은 roomId(" + roomId + ")로 읽음 처리를 시도하여 작업을 중단합니다.");
            return;
        }

        ChatRoomUser roomUser = chatRoomUserRepo.findByChatRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalStateException("채팅방 사용자 정보를 찾을 수 없습니다. roomId=" + roomId + ", userId=" + userId));

        roomUser.setLastReadAt(LocalDateTime.now());
        chatRoomUserRepo.save(roomUser);

        List<ChatMessage> unreadMessages = chatMessageRepo.findByChatRoomIdOrderBySentAtAsc(roomId).stream()
                .filter(msg -> !msg.getSender().getId().equals(userId) && !msg.isRead())
                .collect(Collectors.toList());

        unreadMessages.forEach(msg -> msg.setRead(true));
        chatMessageRepo.saveAll(unreadMessages);
    }

    public long getUnreadCount(Long roomId, Long userId) {
        return chatMessageRepo.countUnreadMessages(roomId, userId);
    }

    public List<ChatRoomListItemDto> getChatRoomList(Long myUserId){
        List<ChatRoomUser> myRoomLinks = chatRoomUserRepo.findByUserId(myUserId);
        List<ChatRoomListItemDto> roomDtos = new ArrayList<>();
        for(ChatRoomUser cru : myRoomLinks){
            ChatRoom room = cru.getChatRoom();
            Optional<ChatMessage> optLastMsg = chatMessageRepo.findTopByChatRoomIdOrderBySentAtDesc(room.getId());
            Long unreadCount = chatMessageRepo.countUnreadMessages(room.getId(), myUserId);
            List<ChatRoomUser> usersInRoom = chatRoomUserRepo.findByChatRoomId(room.getId());
            User opponent = usersInRoom.stream()
                    .map(ChatRoomUser::getUser)
                    .filter(u -> !u.getId().equals(myUserId))
                    .findFirst()
                    .orElse(null);

            roomDtos.add(new ChatRoomListItemDto(
                    room.getId(),
                    opponent != null ? opponent.getNickname() : "알 수 없음",
                    opponent != null ? opponent.getProfileImageUrl() : null,
                    optLastMsg.map(ChatMessage::getContent).orElse(""),
                    optLastMsg.map(ChatMessage::getSentAt).orElse(null),
                    unreadCount
            ));
        }
        roomDtos.sort(Comparator.comparing(ChatRoomListItemDto::getLastMessageAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return roomDtos;
    }

    public List<ChatMessageDto> getRoomMessages(Long roomId){
        return chatMessageRepo.findByChatRoomIdOrderBySentAtAsc(roomId).stream()
                .map(this::toDto).collect(Collectors.toList());
    }

    private ChatMessageDto toDto(ChatMessage msg){
        return new ChatMessageDto(
                msg.getId(),
                msg.getChatRoom().getId(),
                msg.getSender().getId(),
                msg.getSender().getNickname(),
                msg.getContent(),
                msg.getSentAt(),
                msg.isRead()
        );
    }
}