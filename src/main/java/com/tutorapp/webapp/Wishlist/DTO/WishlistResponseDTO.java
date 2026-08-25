package com.tutorapp.webapp.Wishlist.DTO;

import java.util.List;

public class WishlistResponseDTO {
    private Long id;
    private String subject;
    private Integer grade;
    private String syllabus;
    private String teacherUsername;
    private String teacherFullName; // 👈 new
    private Double amount;
    private Double rating;
    private List<String> acceptedStudentNames; // optional
    private String profileImageUrl;
    private String description;


    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }

    public String getSyllabus() { return syllabus; }
    public void setSyllabus(String syllabus) { this.syllabus = syllabus; }

    public String getTeacherUsername() { return teacherUsername; }
    public void setTeacherUsername(String teacherUsername) { this.teacherUsername = teacherUsername; }

    public String getTeacherFullName() { return teacherFullName; }
    public void setTeacherFullName(String teacherFullName) { this.teacherFullName = teacherFullName; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public List<String> getAcceptedStudentNames() { return acceptedStudentNames; }
    public void setAcceptedStudentNames(List<String> acceptedStudentNames) { this.acceptedStudentNames = acceptedStudentNames; }

     public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
     public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
