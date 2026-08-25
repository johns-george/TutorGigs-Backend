package com.tutorapp.webapp.Review.Entity;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long raterId;   // Student/User who gave the rating
    private Long rateeId;   // Tutor/User being rated
    private Long gigId;     // Which gig this rating is for
    private int score;      // e.g., 1 to 5 stars
    private String comment;
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters & setters

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
}
