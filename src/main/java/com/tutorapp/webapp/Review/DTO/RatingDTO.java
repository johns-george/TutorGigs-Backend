package com.tutorapp.webapp.Review.DTO;

import java.time.LocalDateTime;

public class RatingDTO {
    private Long id;
    private Long raterId;
    private String rateeUsername;
    private Long gigId;
    private int score;
    private String comment;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRaterId() {
        return raterId;
    }

    public void setRaterId(Long raterId) {
        this.raterId = raterId;
    }

    public String getRateeUsername() {
        return rateeUsername;
    }

    public void setRateeUsername(String rateeUsername) {
        this.rateeUsername = rateeUsername;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getGigId() {
        return gigId;
    }

    public void setGigId(Long gigId) {
        this.gigId = gigId;
    }


}
