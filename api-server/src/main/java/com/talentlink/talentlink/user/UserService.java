package com.talentlink.talentlink.user;

import com.talentlink.talentlink.auth.AuthProvider;
import com.talentlink.talentlink.common.FileService;
import com.talentlink.talentlink.exception.EmailAlreadyExistsException;
import com.talentlink.talentlink.exception.PasswordMismatchException;
import com.talentlink.talentlink.exception.SocialAccountExistsException;
import com.talentlink.talentlink.user.dto.SignupRequest;
import com.talentlink.talentlink.user.dto.UserUpdateRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       FileService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
    }

    /**
     * 일반 회원가입 처리
     */
    public User registerUser(SignupRequest request, MultipartFile profileImage) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("이미 가입된 이메일입니다.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        String imageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            imageUrl = fileService.upload(profileImage); // 실제 저장하고 URL 반환
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .profileImageUrl(imageUrl)
                .provider(AuthProvider.LOCAL)
                .providerId("local_" + request.getEmail())
                .role(Role.USER)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }



    /**
     * 이메일로 사용자 조회
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public User getUserFromPrincipal(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    /**
     * Id로 사용자 조회
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저 없음"));
    }

    /**
     * provider + providerId 기반 사용자 조회 (소셜 전용)
     */
    public Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    /**
     * 사용자 정보 업데이트 (ex. 닉네임 변경 등)
     */
    public User updateUser(User user, String newNickname) {
        user.setNickname(newNickname);
        return userRepository.save(user);
    }

    /**
     * 마이페이지 응답용: isSocialUser 여부 포함
     */
    public boolean isSocialUser(User user) {
        return user.getProvider() != null && user.getProvider() != AuthProvider.LOCAL;
    }

    /**
     * 일단 닉네임만 수정 가능 // 나중에 회사정보수정이랑 비번수정도 넣을거임
     */
    public User updateNickname(String email, String newNickname) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setNickname(newNickname);
        return userRepository.save(user);
    }

    public boolean existsByProviderAndProviderId(AuthProvider provider, String providerId) {
        return userRepository.existsByProviderAndProviderId(provider, providerId);
    }

    /**
     * 회원정보 수정 (닉네임, 이름, 전화번호, 생년월일)
     */
    public User updateUserInfo(String email, UserUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        user.setNickname(request.getNickname());

        return userRepository.save(user);
    }

    public void updateProfileImage(Long userId, String imageUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
    }

}