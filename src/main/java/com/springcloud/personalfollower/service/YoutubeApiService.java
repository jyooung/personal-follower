package com.springcloud.personalfollower.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class YoutubeApiService {

    private final OAuth2AuthorizedClientService clientService;
    private final RestTemplate restTemplate = new RestTemplate();

    public void logVideoMetadata(String videoId) {
        try {
            String accessToken = getAccessToken();

            String url = "https://www.googleapis.com/youtube/v3/videos"
                    + "?part=snippet,contentDetails,statistics"
                    + "&id=" + videoId
                    + "&access_token=" + accessToken;

            String response = restTemplate.getForObject(url, String.class);

            System.out.println("📺 videoId: " + videoId);
            System.out.println("▶ [YouTube API 응답]");
            System.out.println(response);

        } catch (Exception e) {
            System.err.println("❌ videoId: " + videoId + " - 메타데이터 요청 실패: " + e.getMessage());
        }
    }

    private String getAccessToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", auth.getName());
        return client.getAccessToken().getTokenValue();
    }
}

