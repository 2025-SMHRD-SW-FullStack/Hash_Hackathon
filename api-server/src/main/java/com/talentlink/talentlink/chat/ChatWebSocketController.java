package com.talentlink.talentlink.chat;

import com.talentlink.talentlink.chat.dto.ChatMessageDto;
import com.talentlink.talentlink.chat.dto.ChatReadEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/message")
    public void sendMessage(@Payload ChatMessageDto message) {
        ChatMessage saved = chatService.sendMessage(message.getRoomId(), message.getSenderId(), message.getContent());

        ChatMessageDto dto = new ChatMessageDto(
                saved.getId(),
                saved.getChatRoom().getId(),
                saved.getSender().getId(),
                saved.getSender().getNickname(),
                saved.getContent(),
                saved.getSentAt(),
                saved.isRead()
        );

        messagingTemplate.convertAndSend("/sub/chat/" + message.getRoomId(), dto);
    }

    @MessageMapping("/chat/read")
    public void markRead(@Payload ChatReadEventDto event) {
        chatService.markAsRead(event.getRoomId(), event.getUserId());
        // 읽음 이벤트 브로드캐스트!
        messagingTemplate.convertAndSend("/sub/chat/" + event.getRoomId() + "/read", event);
    }

}