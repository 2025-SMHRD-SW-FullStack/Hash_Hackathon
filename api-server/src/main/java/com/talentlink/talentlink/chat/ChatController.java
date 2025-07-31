package com.talentlink.talentlink.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "💬 채팅 API", description = "채팅방 및 메시지 관련 API")
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "메시지 전송", description = "해당 채팅방에 메시지를 보냅니다")
    @PostMapping("/rooms/{roomId}/send")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable Long roomId,
            @RequestParam Long senderId,
            @RequestParam String content
    ) {
        return ResponseEntity.ok(chatService.sendMessage(roomId, senderId, content));
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
}
