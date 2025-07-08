package com.springcloud.personalfollower.domain.noun;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "user_noun_stats")
@Data
public class UserNounStats {

    @Id
    private String userId;

    // 명사: 카운트 (ex. "심리": 4, "광고": 2)
    private Map<String, Integer> nouns = new HashMap<>();

    private int totalVideos;
    private LocalDateTime lastUpdated;
}