package com.tutorapp.webapp.Bid.Entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.User.Entity.UserInfo;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

@Entity
public class BidInfo {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gig_id", nullable = false) // foreign key to gig table
    @JsonIgnore
    private GigData gigdata;
    @ManyToOne(fetch = FetchType.LAZY)  // Many bids can belong to one student (user)
    @JoinColumn(name = "student_id", nullable = false) // foreign key to user_info table
    @JsonIgnore
    private UserInfo student;
    private Double bidAmount;
    private LocalDateTime bidDate;
    private String status;


    @JsonProperty("gigId")
    public Long getGigId() {
        return gigdata != null ? gigdata.getId() : null;
    }

     @JsonProperty("gigId")
    public void setGigId(Long gigId) {
        // only set the ID, don’t fetch from DB here
        this.gigdata = new GigData();
        this.gigdata.setId(gigId);
    }
    
     @Transient
    @JsonProperty("studentFullName")
    public String getStudentFullName() {
        return student != null ? student.getFullName() : null;
    }

    @Transient
    @JsonProperty("studentProfileImageUrl")
    public String getStudentProfileImageUrl() {
        return student != null ? student.getProfileImageUrl() : null;
    }
    
    @Transient
    @JsonProperty("studentUsername")
    public String getStudentUsername() {
        return student != null ? student.getUsername() : null;
    }

     @Transient
    public String getGigSubject() {
        return gigdata != null ? gigdata.getSubject() : null;
    }
     @Transient
    public String getTeacherUsername() {
        return gigdata != null ? gigdata.getTeacherUsername() : null;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(Double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public LocalDateTime getBidDate() {
        return bidDate;
    }

    public void setBidDate(LocalDateTime bidDate) {
        this.bidDate = bidDate;
    }

    public GigData getGigdata() {
        return gigdata;
    }

    public void setGigdata(GigData gigdata) {
        this.gigdata = gigdata;
    }

    public UserInfo getStudent() {
        return student;
    }

    public void setStudent(UserInfo student) {
        this.student = student;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // public void setGigId(Long gigId) {
    //     this.gigId = gigId;
    // }
}
