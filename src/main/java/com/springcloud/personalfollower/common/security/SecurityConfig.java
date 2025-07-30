package com.springcloud.personalfollower.common.security;

import com.springcloud.personalfollower.domain.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // OAuth2 이후 사용자 DB 처리를 담당할 서비스 주입
    private final CustomOAuth2UserService customOAuth2UserService;

    /**
     * Configures the Spring Security filter chain to require authentication for all requests and sets up OAuth2 login with a custom OIDC user service.
     *
     * All HTTP requests require authentication. OAuth2 login is enabled, and upon successful authentication, users are always redirected to the "/welcome" endpoint. User information from OIDC providers is processed using the injected custom OAuth2 user service.
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs during security configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 모든 요청은 인증 필요
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // 로그인 성공 후 이동할 경로
                        .defaultSuccessUrl("/welcome", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                // OIDC 기반 사용자 서비스 설정 (Google 등)
                                .oidcUserService(customOAuth2UserService)
                        )
                );

        return http.build(); // 설정된 SecurityFilterChain 반환
    }
}