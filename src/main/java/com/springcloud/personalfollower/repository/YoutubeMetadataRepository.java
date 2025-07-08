package com.springcloud.personalfollower.repository;

import com.springcloud.personalfollower.dto.response.YoutubeMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface YoutubeMetadataRepository extends MongoRepository<YoutubeMetadata, String> {
}

