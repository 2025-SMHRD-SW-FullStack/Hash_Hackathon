package com.talentlink.talentlink.chat;

import com.talentlink.talentlink.chat.dto.ChatMessageDto;
import com.talentlink.talentlink.chat.dto.ChatRoomListItemDto;
import com.talentlink.talentlink.chat.dto.CreateChatRoomRequest;
import com.talentlink.talentlink.chat.dto.CreateChatRoomResponse; // 👈 추가된 import
import com.talentlink.talentlink.chat.dto.SendMessageRequest;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "💬 채팅 API", description = "채팅방 및 메시지 관련 API")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    // ... (sendMessage, markAsRead, getUnreadCount 메소드는 기존과 동일) ...
    @Operation(summary = "메시지 전송", description = "해당 채팅방에 메시지를 보냅니다")
    @PostMapping("/rooms/{roomId}/send")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable Long roomId,
            @RequestBody SendMessageRequest req
    ) {
        System.out.println("send Message Controller");
        ChatMessage savedMsg = chatService.sendMessage(roomId, req.getSenderId(), req.getContent());
        ChatMessageDto dto = new ChatMessageDto(
                savedMsg.getId(),
                savedMsg.getChatRoom().getId(),
                savedMsg.getSender().getId(),
                savedMsg.getSender().getNickname(),
                savedMsg.getContent(),
                savedMsg.getSentAt(),
                savedMsg.isRead()
        );
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "읽음 처리", description = "해당 채팅방의 메시지를 읽음 처리합니다")
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long roomId,
            @RequestParam Long userId
    ) {
        chatService.markAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "안읽은 메시지 개수", description = "채팅방 내 안읽은 메시지 개수를 조회합니다")
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Long roomId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(chatService.getUnreadCount(roomId, userId));
    }


    // 👇 [수정된 부분] createRoom 메소드
    @Operation(summary = "채팅방 생성 또는 조회", description = "두 사용자를 위한 1:1 채팅방을 찾거나 새로 만듭니다.")
    @PostMapping("/rooms")
    public ResponseEntity<CreateChatRoomResponse> createRoom( // 👈 반환 타입을 ChatRoom -> CreateChatRoomResponse 로 변경
                                                              @RequestBody CreateChatRoomRequest req
    ) {
        User user1 = userService.findById(req.getMyUserId());
        User user2 = userService.findById(req.getOpponentUserId());

        ChatRoom room = chatService.findOrCreateRoom(user1, user2);

        // 👈 ChatRoom 객체에서 roomId를 꺼내 새로운 응답 객체를 만들어 반환
        return ResponseEntity.ok(new CreateChatRoomResponse(room.getId()));
    }

    // ... (getMyChatRooms, getRoomMessages 메소드는 기존과 동일) ...
    @Operation(summary = "내 채팅방 목록", description = "내가 참여한 1:1 채팅방 목록(안읽은 메시지, 최신순) 반환")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomListItemDto>> getMyChatRooms(@RequestParam Long userId) {
        System.out.println("=== [백엔드] 채팅방 목록 조회 API 호출됨 userId=" + userId);
        return ResponseEntity.ok(chatService.getChatRoomList(userId));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDto>> getRoomMessages(@PathVariable Long roomId){
        List<ChatMessageDto> result = chatService.getRoomMessages(roomId);
        result.forEach(dto -> System.out.println("msg id:"+dto.getId() + " isRead:"+dto.isRead()));
        return ResponseEntity.ok(chatService.getRoomMessages(roomId));
    }
}