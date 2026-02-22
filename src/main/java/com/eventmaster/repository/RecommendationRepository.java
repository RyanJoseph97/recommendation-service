package com.eventmaster.repository;

import com.eventmaster.model.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    // Find recommendations by user ID
    List<Recommendation> findByUserId(Long userId);

    // Find recommendations by event ID
    List<Recommendation> findByEventId(Long eventId);

    // Find recommendations by user ID and recommendation type
    List<Recommendation> findByUserIdAndRecommendationType(Long userId, String recommendationType);

    // Find a specific recommendation by user ID and event ID
    Optional<Recommendation> findByUserIdAndEventId(Long userId, Long eventId);

    // Find top N recommendations for a user ordered by score
    List<Recommendation> findByUserIdOrderByScoreDesc(Long userId);
}
