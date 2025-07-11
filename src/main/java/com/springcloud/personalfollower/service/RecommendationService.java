package com.springcloud.personalfollower.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserNounService userNounService;
    private final GptService gptService;

    /**
     * 사용자의 관심 명사 기반 GPT 추천
     */
    public List<String> recommendByUserId(String userId, int topN) {
        List<String> topNouns = userNounService.getTopNouns(userId, topN);
        if (topNouns.isEmpty()) return List.of("추천 데이터를 찾을 수 없습니다.");
        return gptService.recommendKeywords(topNouns);
    }
}