package com.springcloud.personalfollower.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.personalfollower.common.util.NounExtractor;
import com.springcloud.personalfollower.common.util.YoutubeCategoryMapper;
import com.springcloud.personalfollower.domain.youtube.YoutubeVideoMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YoutubeApiService {

    private final OAuth2AuthorizedClientService clientService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱용


    public YoutubeVideoMetadata fetchVideoMetadata(String videoId, String userId, String visitedAt, String originalUrl) {
        try {
            String accessToken = getAccessToken();

            String url = "https://www.googleapis.com/youtube/v3/videos"
                    + "?part=snippet,contentDetails,statistics"
                    + "&id=" + videoId
                    + "&access_token=" + accessToken;

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            if (items != null && items.size() > 0) {
                JsonNode snippet = items.get(0).get("snippet");

                String title = snippet.get("title").asText();
                String description = snippet.has("description") ? snippet.get("description").asText() : "";

                List<String> tags = new ArrayList<>();
                if (snippet.has("tags")) {
                    for (JsonNode tag : snippet.get("tags")) {
                        tags.add(tag.asText());
                    }
                }

                String categoryId = snippet.has("categoryId") ? snippet.get("categoryId").asText() : null;
                String publishedAt = snippet.has("publishedAt") ? snippet.get("publishedAt").asText() : null;
                String thumbnailUrl = snippet.get("thumbnails").get("default").get("url").asText();
                String channelTitle = snippet.has("channelTitle") ? snippet.get("channelTitle").asText() : null;

                // 명사 추출
                String textForNouns = title + " " + String.join(" ", tags) + " " + description;
                List<String> nouns = NounExtractor.extractNouns(textForNouns);

                // categoryName 매핑 필요 시 여기서 해도 됨
                String categoryName = YoutubeCategoryMapper.getCategoryName(categoryId); // 예시

                // YoutubeVideoMetadata 객체 생성
                return new YoutubeVideoMetadata(
                        videoId,
                        userId,
                        title,
                        tags,
                        publishedAt,
                        thumbnailUrl,
                        description,
                        channelTitle,
                        categoryName,
                        nouns,
                        visitedAt,
                        originalUrl
                );

            } else {
                System.out.println("❗ videoId " + videoId + "에 대한 데이터 없음");
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ videoId: " + videoId + " - 메타데이터 요청 실패: " + e.getMessage());
            return null;
        }
    }


    private String getAccessToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", auth.getName());
        return client.getAccessToken().getTokenValue();
    }
}
