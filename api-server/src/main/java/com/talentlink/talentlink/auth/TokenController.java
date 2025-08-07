package com.talentlink.talentlink.auth;

import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserService;
import com.talentlink.talentlink.user.dto.LoginResponse;
import com.talentlink.talentlink.user.dto.RefreshRequest;
import com.talentlink.talentlink.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "토큰 API", description = "AccessToken 재발급")
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @PostMapping("/refresh")
    @Operation(summary = "AccessToken 재발급", description = "만료된 AccessToken을 RefreshToken을 이용해 재발급합니다.")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshRequest request) {
        String refreshToken = request.getToken();

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("저장된 리프레시 토큰이 없습니다."));

        if (!savedToken.getToken().equals(refreshToken) || savedToken.isExpired()) {
            return ResponseEntity.status(401).build();
        }

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        // DB 갱신
        RefreshToken newToken = new RefreshToken(userId, newRefreshToken, LocalDateTime.now().plusDays(14));
        refreshTokenRepository.save(newToken);

        // (선택) 쿠키 설정 없애거나 유지할지 결정 — 안드로이드에선 쿠키로 안써도 됨

        User user = userService.findById(userId);

        return ResponseEntity.ok(new LoginResponse(newAccessToken, newRefreshToken, new UserResponse(user)));

    }

}