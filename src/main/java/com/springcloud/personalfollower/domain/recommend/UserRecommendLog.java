package com.springcloud.personalfollower.domain.recommend;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document("user_recommend_logs")
@Data
public class UserRecommendLog {
    @Id
    private String id; // 자동 생성됨

    private String userId;                 // 사용자 식별자 (예: 이메일)
    private List<String> inputCategories;  // GPT 요청에 사용된 관심 카테고리 (ex: ["심리", "광고"])
    private List<String> recommended;      // GPT로부터 받은 추천 키워드 (ex: ["감정조절", "마케팅 심리학", ...])
    private String model;                  // 사용한 모델명 (ex: "gpt-3.5-turbo")
    private LocalDateTime timestamp;       // 요청 및 저장 시각
}