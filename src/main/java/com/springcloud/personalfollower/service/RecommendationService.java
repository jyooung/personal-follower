package com.springcloud.personalfollower.service;

import com.springcloud.personalfollower.domain.recommend.GlobalCategoryLinks;
import com.springcloud.personalfollower.domain.recommend.UserRecommendLog;
import com.springcloud.personalfollower.repository.GlobalCategoryLinksRepository;
import com.springcloud.personalfollower.repository.UserRecommendLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final GptService gptService;
    private final GlobalCategoryLinksRepository globalRepo;
    private final UserRecommendLogRepository userLogRepo;
    private final UserNounService userNounService;

    /**
     * 사용자 ID만으로 자동으로 관심 명사(카테고리)를 추출하여 GPT 추천을 받고,
     * 글로벌 통계에 누적 저장하며, 개인 추천 로그는 일괄 저장합니다.
     *
     * @param userId 사용자 식별자 (예: 이메일)
     * @param topN 관심 명사 중 상위 N개 사용
     * @return 추천된 키워드 전체 리스트
     */
    public List<String> recommendAndSave(String userId, int topN) {
        List<String> inputCategories = userNounService.getTopNouns(userId, topN);

        if (inputCategories.isEmpty()) return List.of("추천할 관심 카테고리가 없습니다.");

        Map<String, List<String>> categoryToRecommended = new HashMap<>();
        List<String> allRecommended = new ArrayList<>();

        for (String category : inputCategories) {
            // 🔹 GPT에 카테고리별로 요청
            List<String> recs = gptService.recommendKeywords(List.of(category));

            categoryToRecommended.put(category, recs);
            allRecommended.addAll(recs);

            // 🔹 글로벌 통계 저장
            GlobalCategoryLinks doc = globalRepo.findById(category).orElse(new GlobalCategoryLinks());
            doc.setBaseCategory(category);

            Map<String, Integer> related = doc.getRelated();
            for (String keyword : recs) {
                related.put(keyword, related.getOrDefault(keyword, 0) + 1);
            }

            doc.setLastUpdated(LocalDateTime.now());
            globalRepo.save(doc);
        }

        // 🔹 사용자 로그 저장
        UserRecommendLog log = new UserRecommendLog();
        log.setUserId(userId);
        log.setInputCategories(inputCategories);
        log.setRecommended(allRecommended);
        log.setModel("gpt-3.5-turbo");
        log.setTimestamp(LocalDateTime.now());
        userLogRepo.save(log);

        return allRecommended;
    }
}