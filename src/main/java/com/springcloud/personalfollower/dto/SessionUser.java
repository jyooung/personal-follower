package com.springcloud.personalfollower.dto;

import com.springcloud.personalfollower.domain.user.User;
import lombok.Getter;

import java.io.Serializable;

// OAuth2User 객체는 직렬화가 안되기 때문에, 세션에 저장할 수 있는 별도의 직렬화 가능한 DTO 만들었음
@Getter
public class SessionUser implements Serializable {
    private final String name;
    private final String email;

    public SessionUser(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
    }
}
