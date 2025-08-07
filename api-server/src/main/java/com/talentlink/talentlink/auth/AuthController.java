package com.talentlink.talentlink.auth;

import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserService;
import com.talentlink.talentlink.user.dto.LoginRequest;
import com.talentlink.talentlink.user.dto.LoginResponse;
import com.talentlink.talentlink.user.dto.SignupRequest;
import com.talentlink.talentlink.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 발급")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 이메일 인증이 완료된 사용자만 가입 가능
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(
            @RequestPart("request") SignupRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        User user = userService.registerUser(request, profileImage);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 일반 로그인 → Access Token + Refresh Token 발급
     * AccessToken: 응답 본문
     * RefreshToken: HTTP-only 쿠키 저장
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호 기반 로그인 후 JWT 토큰을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공 - JWT 토큰 반환")
    public ResponseEntity<LoginResponse> login(
            @RequestBody @Parameter(description = "이메일/비밀번호 정보") LoginRequest request,
            HttpServletRequest httpRequest, HttpServletResponse response
    ) {
        // 사용자 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 유저 조회
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("유저 정보를 찾을 수 없습니다."));

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // RefreshToken 저장 (DB)
        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken, LocalDateTime.now().plusDays(14))); // 토큰 만료기간 14일 유효

        // 응답 반환 (accessToken + refreshToken + 유저 정보)
        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, new UserResponse(user)));
    }


    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "RefreshToken 삭제 및 쿠키 제거")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 refreshToken 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();

                    // 2. 이메일 추출 후 DB에서 삭제
                    if (jwtTokenProvider.validateToken(refreshToken)) {
                        Long userId = jwtTokenProvider.getUserId(refreshToken);
                        refreshTokenRepository.deleteById(userId);
                    }

                    // 3. 쿠키 삭제
                    Cookie deleteCookie = new Cookie("refreshToken", null);
                    deleteCookie.setHttpOnly(true);
                    deleteCookie.setMaxAge(0);
                    deleteCookie.setPath("/");

                    if (!request.getServerName().equals("localhost")) {
                        deleteCookie.setSecure(true); // ✅ HTTPS에서만 전송
                    }

                    response.addCookie(deleteCookie);
                }
            }
        }

        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }

}
