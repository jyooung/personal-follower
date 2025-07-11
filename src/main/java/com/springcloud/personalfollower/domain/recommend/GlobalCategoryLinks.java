package com.springcloud.personalfollower.domain.recommend;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "global_category_links")
@Data
public class GlobalCategoryLinks {

    @Id
    private String baseCategory;  // 예: "심리"

    private Map<String, Integer> related = new HashMap<>();
    // 예시:
    // {
    //   "마케팅 심리학": 13,
    //   "심리 실험": 7,
    //   "감정 조절": 3
    // }

    private LocalDateTime lastUpdated;         // 마지막 갱신 시각
}

