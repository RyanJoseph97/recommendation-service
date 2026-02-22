package com.eventmaster.controller;

import com.eventmaster.model.Recommendation;
import com.eventmaster.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }
    
    @GetMapping
    public ResponseEntity<List<Recommendation>> getAllRecommendations() {
        List<Recommendation> recommendations = recommendationService.getAllRecommendations();
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Recommendation>> getRecommendationsForUser(@PathVariable Long userId) {
        List<Recommendation> recommendations = recommendationService.getRecommendationsForUser(userId);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/user/{userId}/top")
    public ResponseEntity<List<Recommendation>> getTopRecommendationsForUser(@PathVariable Long userId) {
        List<Recommendation> recommendations = recommendationService.getTopRecommendationsForUser(userId);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Recommendation>> getRecommendationsForUserByType(
            @PathVariable Long userId, 
            @PathVariable String type) {
        List<Recommendation> recommendations = recommendationService.getRecommendationsForUserByType(userId, type);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Recommendation>> getRecommendationsForEvent(@PathVariable Long eventId) {
        List<Recommendation> recommendations = recommendationService.getRecommendationsForEvent(eventId);
        return ResponseEntity.ok(recommendations);
    }
    
    @GetMapping("/user/{userId}/event/{eventId}")
    public ResponseEntity<Recommendation> getRecommendationByUserAndEvent(
            @PathVariable Long userId, 
            @PathVariable Long eventId) {
        Optional<Recommendation> recommendation = recommendationService.getRecommendationByUserAndEvent(userId, eventId);
        return recommendation.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Recommendation> createRecommendation(@RequestBody Recommendation recommendation) {
        Recommendation savedRecommendation = recommendationService.saveRecommendation(recommendation);
        return ResponseEntity.ok(savedRecommendation);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Recommendation> updateRecommendation(
            @PathVariable Long id, 
            @RequestBody Recommendation recommendation) {
        recommendation.setId(id);
        Recommendation updatedRecommendation = recommendationService.saveRecommendation(recommendation);
        return ResponseEntity.ok(updatedRecommendation);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecommendation(@PathVariable Long id) {
        recommendationService.deleteRecommendation(id);
        return ResponseEntity.noContent().build();
    }
}
