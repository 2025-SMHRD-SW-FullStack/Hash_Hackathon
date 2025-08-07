package com.talentlink.talentlink.config;

import com.talentlink.talentlink.auth.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 1. CONNECT 요청(최초 WebSocket 연결)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String headerAuth = accessor.getFirstNativeHeader("Authorization"); // (Bearer ...)

            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                String jwt = headerAuth.substring("Bearer ".length());
                if (jwtTokenProvider.validateToken(jwt)) {
                    Long userId = jwtTokenProvider.getUserId(jwt);

                    // Security 인증 객체 생성 (Role 없이 userId만 Principal에 담음)
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            userId, null, null // authorities 필요하면 추가
                    );
                    accessor.setUser(auth); // 연결 세션에 Principal로 심기
                }
            }
        }
        return message;
    }
}
