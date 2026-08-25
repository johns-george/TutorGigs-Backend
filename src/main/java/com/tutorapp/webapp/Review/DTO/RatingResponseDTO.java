package com.tutorapp.webapp.Review.DTO;

import java.time.LocalDateTime;

public class RatingResponseDTO {
    private Long id;

    private Long raterId;   // Student/User who gave the rating
    private Long rateeId;   // Tutor/User being rated
    private Long gigId;     // Which gig this rating is for
    private String rateeFullname;
    private String raterFullname;
    private int score;      // e.g., 1 to 5 stars
    private String comment;

    private LocalDateTime createdAt;

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

    public Long getRateeId() {
        return rateeId;
    }

    public void setRateeId(Long rateeId) {
        this.rateeId = rateeId;
    }

    public Long getGigId() {
        return gigId;
    }

    public void setGigId(Long gigId) {
        this.gigId = gigId;
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

    public String getRateeFullname() {
        return rateeFullname;
    }

    public void setRateeFullname(String rateeFullname) {
        this.rateeFullname = rateeFullname;
    }

    public String getRaterFullname() {
        return raterFullname;
    }

    public void setRaterFullname(String raterFullname) {
        this.raterFullname = raterFullname;
    }
}
