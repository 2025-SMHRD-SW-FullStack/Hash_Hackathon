package com.talentlink.talentlink.auth.oauth2;

import com.talentlink.talentlink.auth.AuthProvider;
import com.talentlink.talentlink.user.Role;
import com.talentlink.talentlink.user.User;
import com.talentlink.talentlink.user.UserRepository;
import com.talentlink.talentlink.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Lazy
    @Autowired
    UserService userService;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        System.out.println("✅ [" + registrationId + "] OAuth2 attributes: " + attributes);

        String email = null;
        String name = null;
        String nickname = null;
        String providerId = null;
        String profileImage = null;

        try {
            if ("google".equals(registrationId)) {
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                nickname = name;
                providerId = (String) attributes.get("sub");
                profileImage = (String) attributes.get("picture");

            } else if ("kakao".equals(registrationId)) {
                providerId = String.valueOf(attributes.get("id"));
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

                email = (String) kakaoAccount.get("email");
                name = profile != null ? (String) profile.get("nickname") : null;
                nickname = name;
                profileImage = profile != null ? (String) profile.get("profile_image_url") : null;

                if (email == null || name == null) {
                    throw new OAuth2AuthenticationException("카카오에서 필수 정보를 받지 못했습니다.");
                }

            } else if ("naver".equals(registrationId)) {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");

                if (response == null) {
                    throw new OAuth2AuthenticationException("네이버 응답에서 response 필드 누락");
                }

                providerId = String.valueOf(response.get("id"));
                email = (String) response.get("email");
                name = (String) response.get("name");
                nickname = name;
                profileImage = (String) response.get("profile_image");

                if (email == null) {
                    email = "naver_" + providerId + "@social.talentmarket";
                }
                if (name == null) {
                    name = "naver_user_" + providerId;
                    nickname = name;
                }

            } else {
                throw new IllegalArgumentException("지원하지 않는 OAuth Provider: " + registrationId);
            }

            final String finalEmail = email;
            final String finalName = name;
            final String finalNickname = nickname;
            final String finalProviderId = providerId;
            final String finalRegistrationId = registrationId.toUpperCase();
            final String finalProfileImage = profileImage;

            User user = userRepository.findByProviderAndProviderId(
                    AuthProvider.valueOf(finalRegistrationId), finalProviderId
            ).orElseGet(() ->
                    userRepository.save(
                            User.builder()
                                    .email(finalEmail)
                                    .nickname(finalNickname)
                                    .password(UUID.randomUUID().toString())
                                    .provider(AuthProvider.valueOf(finalRegistrationId))
                                    .providerId(finalProviderId)
                                    .profileImageUrl(finalProfileImage)
                                    .role(Role.USER)
                                    .enabled(true)
                                    .build()
                    )
            );

            Map<String, Object> customAttributes = Map.of(
                    "email", email,
                    "nickname", nickname,
                    "profileImageUrl", profileImage,
                    "provider", registrationId
            );

            return new CustomOAuth2User(user, customAttributes);

        } catch (Exception e) {
            System.out.println("❌ OAuth2UserService 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
