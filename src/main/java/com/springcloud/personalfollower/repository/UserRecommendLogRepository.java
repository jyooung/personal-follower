package com.springcloud.personalfollower.repository;

import com.springcloud.personalfollower.domain.recommend.UserRecommendLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRecommendLogRepository extends MongoRepository<UserRecommendLog, String> {
}
