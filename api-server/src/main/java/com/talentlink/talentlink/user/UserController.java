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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Operation(
            summary = "프로필 이미지 수정",
            description = "마이페이지에서 현재 로그인한 사용자의 프로필 이미지를 수정합니다. " +
                    "이미지는 Multipart/form-data 형식으로 업로드하며, 성공 시 업로드된 이미지의 URL을 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "프로필 이미지 수정 성공")
    public ResponseEntity<String> uploadProfileImage(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("file") MultipartFile file) {

        String email = userDetails.getUsername();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        String imageUrl = fileService.upload(file);
        userService.updateProfileImage(user.getId(), imageUrl);

        return ResponseEntity.ok(imageUrl);
    }
}
