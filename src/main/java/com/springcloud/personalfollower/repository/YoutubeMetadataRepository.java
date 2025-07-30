package com.springcloud.personalfollower.repository;

import com.springcloud.personalfollower.domain.youtube.YoutubeVideoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface YoutubeMetadataRepository extends MongoRepository<YoutubeVideoMetadata, String> {

    Optional<YoutubeVideoMetadata> findByVideoIdAndUserId(String videoId, String userId);

    // userId로 가장 최근 방문 기록 1개 조회 (내림차순)
    Optional<YoutubeVideoMetadata> findTopByUserIdOrderByVisitedAtDesc(String userId);
}

