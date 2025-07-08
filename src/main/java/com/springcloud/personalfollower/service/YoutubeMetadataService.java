package com.springcloud.personalfollower.service;

import com.springcloud.personalfollower.domain.youtube.YoutubeVideoMetadata;
import com.springcloud.personalfollower.repository.YoutubeMetadataRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class YoutubeMetadataService {
    private final YoutubeMetadataRepository repository;

    public void saveOrUpdateMetadata(YoutubeVideoMetadata newData) {
        Optional<YoutubeVideoMetadata> existingOpt =
                repository.findByVideoIdAndUserId(newData.getVideoId(), newData.getUserId());

        if (existingOpt.isPresent()) {
            YoutubeVideoMetadata existing = existingOpt.get();

            // 시청 시간이 더 최신인 경우만 업데이트
            if (!existing.getVisitedAt().equals(newData.getVisitedAt())) {
                existing.setVisitedAt(newData.getVisitedAt());
                existing.setNouns(newData.getNouns()); // 최신 명사 추출 결과 반영
                existing.setOriginalUrl(newData.getOriginalUrl());
                repository.save(existing);
            }

        } else {
            // 새로운 영상이라면 전체 저장
            repository.save(newData);
        }
    }
}
