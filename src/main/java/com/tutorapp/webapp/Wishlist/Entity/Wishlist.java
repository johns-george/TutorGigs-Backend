package com.tutorapp.webapp.Wishlist.Entity;

import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.User.Entity.UserInfo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

  @ManyToOne
  @JoinColumn(name = "student_id", nullable = false)
    private UserInfo student;

    @ManyToOne
    @JoinColumn(name = "gig_id", nullable = false)
    private GigData gigdata;
    // Constructors, getters, and setters

    public Long getId() {
        return id;  
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserInfo getStudent() {
        return student;
    }

    public void setStudent(UserInfo userinfo) {
        this.student = userinfo;
    }

    public GigData getGigdata() {
        return gigdata;
    }

    public void setGigdata(GigData gigData2) {
        this.gigdata = gigData2;
    }
}
