package com.eventmaster.service;

import com.eventmaster.model.Recommendation;
import com.eventmaster.repository.RecommendationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecommendationService {
    
    @Autowired
    private RecommendationRepository recommendationRepository;
    
    /**
     * Save a recommendation
     */
    public Recommendation saveRecommendation(Recommendation recommendation) {
        return recommendationRepository.save(recommendation);
    }
    
    /**
     * Get recommendations for a specific user
     */
    public List<Recommendation> getRecommendationsForUser(Long userId) {
        return recommendationRepository.findByUserId(userId);
    }
    
    /**
     * Get recommendations for a specific user by type
     */
    public List<Recommendation> getRecommendationsForUserByType(Long userId, String recommendationType) {
        return recommendationRepository.findByUserIdAndRecommendationType(userId, recommendationType);
    }
    
    /**
     * Get top recommendations for a user ordered by score
     */
    public List<Recommendation> getTopRecommendationsForUser(Long userId) {
        return recommendationRepository.findByUserIdOrderByScoreDesc(userId);
    }
    
    /**
     * Get recommendations for a specific event
     */
    public List<Recommendation> getRecommendationsForEvent(Long eventId) {
        return recommendationRepository.findByEventId(eventId);
    }
    
    /**
     * Get a specific recommendation by user and event
     */
    public Optional<Recommendation> getRecommendationByUserAndEvent(Long userId, Long eventId) {
        return recommendationRepository.findByUserIdAndEventId(userId, eventId);
    }
    
    /**
     * Delete a recommendation
     */
    public void deleteRecommendation(Long id) {
        recommendationRepository.deleteById(id);
    }
    
    /**
     * Get all recommendations
     */
    public List<Recommendation> getAllRecommendations() {
        return recommendationRepository.findAll();
    }
}
