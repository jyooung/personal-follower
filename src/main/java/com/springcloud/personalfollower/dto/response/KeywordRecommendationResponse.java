package com.springcloud.personalfollower.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class KeywordRecommendationResponse {
    private List<String> keywords;
    private String message;
}
