package com.tutorapp.webapp.Gig.Entity;

import java.util.ArrayList;
import java.util.List;

import com.tutorapp.webapp.Bid.Entity.BidInfo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class GigData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String teacherUsername;
    private String description;
    private String subject;
    private String syllabus;
    private Integer grade;
    private Double amount;
    private Double rating;

    @OneToMany(mappedBy = "gigdata", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BidInfo> bids = new ArrayList<>();
    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSyllabus() {
        return syllabus;
    }

    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }

    public List<BidInfo> getBids() {
        return bids;
    }

    public void setBids(List<BidInfo> bids) {
        this.bids = bids;
    }

    public void setAcceptedStudentNames(List<String> studentNames) {

    }

    public Double getRating() {
        return rating;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
}
