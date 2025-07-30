package com.springcloud.personalfollower.service;

import com.springcloud.personalfollower.domain.recommend.GlobalCategoryLinks;
import com.springcloud.personalfollower.domain.recommend.UserRecommendLog;
import com.springcloud.personalfollower.repository.GlobalCategoryLinksRepository;
import com.springcloud.personalfollower.repository.UserRecommendLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final GptService gptService;
    private final GlobalCategoryLinksRepository globalRepo;
    private final UserRecommendLogRepository userLogRepo;
    private final UserNounService userNounService;
    private final QdrantVectorStore vectorStore;

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

        List<String> allRecommended = new ArrayList<>();

        for (String category : inputCategories) {
            // 1. 벡터스토어에서 유사 카테고리 검색
            List<String> similar = new ArrayList<>();
            List<Document> results = vectorStore.similaritySearch(category);//4,5개가 나올 것
            for (Document doc : results) {
                similar.add(doc.getText());
            }

            // 2. 유사 키워드 + 입력 카테고리를 합쳐 GPT에 요청
            List<String> keywordsForGpt = new ArrayList<>(similar);
            keywordsForGpt.add(category);

            // 3. GPT에 추천 요청
            List<String> recs = gptService.recommendKeywords(keywordsForGpt);

            // 4. 결과를 벡터스토어에 저장 (optional)
            List<Document> newDocs = recs.stream()
                    .map(keyword -> new Document(
                            keyword,
                            Map.of("baseCategory", category, "source", "gpt")
                    ))
                    .collect(Collectors.toList());

            vectorStore.add(newDocs);

            allRecommended.addAll(recs);

            // 글로벌 통계 저장
            GlobalCategoryLinks doc = globalRepo.findById(category).orElse(new GlobalCategoryLinks());
            doc.setBaseCategory(category);

            Map<String, Integer> related = doc.getRelated();
            for (String keyword : recs) {
                related.put(keyword, related.getOrDefault(keyword, 0) + 1);
            }

            doc.setLastUpdated(LocalDateTime.now());
            globalRepo.save(doc);
        }

        // 사용자 로그 저장
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