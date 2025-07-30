package com.springcloud.personalfollower.service;

import com.springcloud.personalfollower.domain.noun.UserNounStats;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.context.annotation.Description;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Description("사용자의 noun 통계를 조회해서 상위 키워드를 뽑는 서비스")
@Service
@RequiredArgsConstructor
public class UserNounService {
    private final MongoTemplate mongoTemplate;

    /**
     * 사용자 ID로 user_noun_stats 컬렉션에서 상위 N개의 관심 명사를 추출
     */
    public List<String> getTopNouns(String userId, int topN) {
        // _id 기준으로 문서 조회
        UserNounStats stats = mongoTemplate.findById(userId, UserNounStats.class, "user_noun_stats");

        // 문서가 없거나, nouns 필드가 비었을 경우
        if (stats == null || stats.getNouns() == null || stats.getNouns().isEmpty()) {
            return List.of();
        }

        // 카운트 기준 내림차순 정렬 후 상위 topN 추출
        return stats.getNouns().entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
