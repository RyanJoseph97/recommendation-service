package com.eventmaster.model;

import java.time.LocalDateTime;

public class RecommendedEvent {

    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;
    private String creatorUsername;
    private String visibility;
    private String imageUrl;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer goingCount;
    private Integer interestedCount;
    private String category;
    private Double latitude;
    private Double longitude;
    private double score;

    public RecommendedEvent(CandidateEvent e, double score) {
        this.id = e.getId();
        this.title = e.getTitle();
        this.description = e.getDescription();
        this.location = e.getLocation();
        this.startTime = e.getStartTime();
        this.endTime = e.getEndTime();
        this.capacity = e.getCapacity();
        this.creatorUsername = e.getCreatorUsername();
        this.visibility = e.getVisibility();
        this.imageUrl = e.getImageUrl();
        this.createdAt = e.getCreatedAt();
        this.likeCount = e.getLikeCount();
        this.goingCount = e.getGoingCount();
        this.interestedCount = e.getInterestedCount();
        this.category = e.getCategory();
        this.latitude = e.getLatitude();
        this.longitude = e.getLongitude();
        this.score = score;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Integer getCapacity() { return capacity; }
    public String getCreatorUsername() { return creatorUsername; }
    public String getVisibility() { return visibility; }
    public String getImageUrl() { return imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getLikeCount() { return likeCount; }
    public Integer getGoingCount() { return goingCount; }
    public Integer getInterestedCount() { return interestedCount; }
    public String getCategory() { return category; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public double getScore() { return score; }
}
