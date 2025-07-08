package com.springcloud.personalfollower.repository;

import com.springcloud.personalfollower.domain.noun.UserNounStats;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserNounStatsRepository extends MongoRepository<UserNounStats, String> {
    // userId가 곧 _id 이므로 기본 메서드만 있어도 충분
}
