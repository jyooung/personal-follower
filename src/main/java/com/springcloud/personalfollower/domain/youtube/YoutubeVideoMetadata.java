package com.springcloud.personalfollower.domain.youtube;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "youtube_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeVideoMetadata {
    @Id
    private String videoId;

    private String userId;
    private String title;
    private List<String> tags;
    private String categoryId;
    private String publishedAt;
    private String thumbnailUrl;
    private List<String> nouns;

    private String visitedAt;  // 시청 시간
    private String originalUrl; // 원래 방문한 URL
}

