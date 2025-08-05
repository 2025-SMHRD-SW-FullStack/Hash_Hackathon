package com.talentlink.talentlink.chat;

import com.talentlink.talentlink.chat.dto.ChatMessageDto;
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

}
