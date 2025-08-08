package com.talentlink.talentlink.user;

import com.talentlink.talentlink.common.FileService;
import com.talentlink.talentlink.user.dto.MyPageResponse;
import com.talentlink.talentlink.user.dto.UserResponse;
import com.talentlink.talentlink.user.dto.UserUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "회원 API", description = "회원 정보 조회, 마이페이지, 닉네임 수정, 프로필 이미지 변경 API")
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    /**
     * 마이페이지 정보 조회
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/mypage")
    @Operation(
            summary = "마이페이지 조회",
            description = "현재 로그인한 사용자의 마이페이지 정보를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "마이페이지 정보 조회 성공")
    public ResponseEntity<MyPageResponse> getMyPage(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        log.info("📥 [MyPage] 요청 받음 - 인증된 사용자: {}", userDetails != null ? userDetails.getUsername() : "null");
        if (userDetails == null) {
            log.warn("[MyPage] 인증된 사용자 정보가 없습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("📥 [MyPage] 요청 받음 - 인증된 사용자: {}", userDetails.getUsername());

        String email = userDetails.getUsername();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        return ResponseEntity.ok(new MyPageResponse(user));
    }

    /**
     * 닉네임 수정
     */
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    @Operation(
            summary = "닉네임 수정",
            description = "현재 로그인한 사용자의 닉네임을 수정합니다."
    )
    @ApiResponse(responseCode = "200", description = "닉네임 수정 성공")
    public ResponseEntity<UserResponse> updateUserInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequest request) {

        String email = userDetails.getUsername();
        User updatedUser = userService.updateUserInfo(email, request);
        return ResponseEntity.ok(new UserResponse(updatedUser));
    }

    /**
     * 프로필 이미지 변경
     */
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/profile-image")
    public ResponseEntity<String> uploadProfileImage(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {   // 추가

        String email = userDetails.getUsername();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        String relativeUrl = fileService.upload(file);

        // 절대 URL 생성
        String scheme = request.getScheme();              // http or https
        String serverName = request.getServerName();      // 도메인 예: yourdomain.com
        int serverPort = request.getServerPort();         // 80, 443 등 포트번호

        String baseUrl = scheme + "://" + serverName +
                ((serverPort == 80 || serverPort == 443) ? "" : ":" + serverPort);

        String absoluteUrl = baseUrl + relativeUrl;

        userService.updateProfileImage(user.getId(), absoluteUrl);  // 절대 URL 저장

        return ResponseEntity.ok(absoluteUrl);  // 클라이언트에 절대 URL 리턴
    }

    // 👇 회원 탈퇴 API를 새로 추가합니다.
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자의 계정을 삭제합니다.")
    public ResponseEntity<Map<String, String>> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.deleteUser(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 성공적으로 처리되었습니다."));
    }
}
