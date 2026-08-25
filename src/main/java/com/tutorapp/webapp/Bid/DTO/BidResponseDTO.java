package com.tutorapp.webapp.Bid.DTO;

import java.time.LocalDateTime;

public class BidResponseDTO {
    private Long bidId;
    private Long gigId;
    private String teacherUsername;
    private String teacherFullName; // NEW
    private Double bidAmount;
    private String status;
    private String subject;
    private Integer grade;
    private String syllabus;
    private LocalDateTime bidPlacedAt;
    private String teacherProfileImageUrl;


    public Long getBidId() {
        return bidId;
    }

    public void setBidId(Long bidId) {
        this.bidId = bidId;
    }

    public Long getGigId() {
        return gigId;
    }

    public void setGigId(Long gigId) {
        this.gigId = gigId;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }

    public String getTeacherFullName() {
        return teacherFullName;
    }

    public void setTeacherFullName(String teacherFullName) {
        this.teacherFullName = teacherFullName;
    }

    public Double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(Double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getBidPlacedAt() {
        return bidPlacedAt;
    }

    public void setBidPlacedAt(LocalDateTime bidPlacedAt) {
        this.bidPlacedAt = bidPlacedAt;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }
     public String getTeacherProfileImageurl() {
        return teacherProfileImageUrl;
    }

    public void setTeacherProfileImageurl(String teacherProfileImageUrl) {
        this.teacherProfileImageUrl = teacherProfileImageUrl;
    }
}
