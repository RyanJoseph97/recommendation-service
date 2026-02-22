package com.eventmaster.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendations")
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Double score;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "recommendation_type")
    private String recommendationType;

    // Constructors
    public Recommendation() {
        // Default constructor
    }

    public Recommendation(Long userId, Long eventId, Double score, String recommendationType) {
        this.userId = userId;
        this.eventId = eventId;
        this.score = score;
        this.recommendationType = recommendationType;
        this.createdAt = LocalDateTime.now();
    }

    public Recommendation(Long id, Long userId, Long eventId, Double score, String recommendationType, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.score = score;
        this.recommendationType = recommendationType;
        this.createdAt = createdAt;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRecommendationType() {
        return recommendationType;
    }

    public void setRecommendationType(String recommendationType) {
        this.recommendationType = recommendationType;
    }
}
