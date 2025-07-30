package com.springcloud.personalfollower.service;

import com.springcloud.personalfollower.domain.noun.UserNounStats;
import com.springcloud.personalfollower.repository.UserNounStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserNounStatsService {

    private final UserNounStatsRepository repository;

    @Transactional
    public void updateNounStats(String userId, List<String> extractedNouns) {
        UserNounStats stats = repository.findById(userId).orElseGet(() -> {
            UserNounStats newStats = new UserNounStats();
            newStats.setUserId(userId);
            return newStats;
        });

        Map<String, Integer> nounMap = stats.getNouns();

        for (String noun : extractedNouns) {
            nounMap.put(noun, nounMap.getOrDefault(noun, 0) + 1);
        }

        stats.setTotalVideos(stats.getTotalVideos() + 1);
        stats.setLastUpdated(LocalDateTime.now());

        repository.save(stats);
    }
}
