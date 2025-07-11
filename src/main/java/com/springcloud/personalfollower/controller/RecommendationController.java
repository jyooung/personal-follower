package com.springcloud.personalfollower.controller;

import com.springcloud.personalfollower.dto.SessionUser;
import com.springcloud.personalfollower.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommend")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<String>> recommend(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = user.getEmail(); // 또는 getId(), getSub(), getName() 등
        List<String> recommendations = recommendationService.recommendByUserId(userId, 5);
        return ResponseEntity.ok(recommendations);
    }
}
