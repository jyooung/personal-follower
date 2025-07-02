package com.springcloud.personalfollower.domain.oauth2;

import com.springcloud.personalfollower.domain.user.User;
import com.springcloud.personalfollower.dto.SessionUser;
import com.springcloud.personalfollower.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        // Google에서 받은 사용자 정보 객체
        OidcUser oidcUser = new OidcUserService().loadUser(userRequest);

        // 사용자 정보 Map 꺼내기
        Map<String, Object> attributes = oidcUser.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String provider = userRequest.getClientRegistration().getRegistrationId(); // 예: "google"

        System.out.println("로그인 시도: " + email + ", " + name + ", provider: " + provider);

        // 사용자 정보가 이미 존재하는지 확인 → 없으면 새로 저장
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setProvider(provider);
                    newUser.setRole("USER"); // 기본 권한

                    try {
                        User savedUser = userRepository.save(newUser);
                        System.out.println("사용자 저장 완료: " + savedUser.getId());
                        return savedUser;
                    } catch (Exception e) {
                        System.out.println("사용자 저장 실패: " + e.getMessage());
                        return null;
                    }
                });
        // 세션에 사용자 정보 저장
        httpSession.setAttribute("user", new SessionUser(user));
        // 반환: Spring Security에서 인증된 사용자로 처리
        return oidcUser;
    }
}