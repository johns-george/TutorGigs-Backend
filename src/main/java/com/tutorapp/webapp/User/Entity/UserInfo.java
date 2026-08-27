package com.tutorapp.webapp.User.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tutorapp.webapp.Bid.Entity.BidInfo;
import com.tutorapp.webapp.User.Enum.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import  jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

@Entity
public class UserInfo {
@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private Long mobile;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private Integer grade;
    private String subjects;
    private String experience;
    private String bio;
    private LocalDate dateOfBirth;
    private String profileImageUrl;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String resetToken; // for password reset
    private LocalDateTime resetTokenExpiry;


    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BidInfo> bids = new ArrayList<>();

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String confirmPassword;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String encryptedPassword;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passwordKeyId;

    public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}
    
public String getFullName() {
    return fullName;
}

public void setFullName(String fullName) {
    this.fullName = fullName;
}

public String getUsername() {
    return username;
}

public void setUsername(String username) {
    this.username = username;
}

public String getEmail() {
    return email;
}

public void setEmail(String email) {
    this.email = email;
}

public long getMobile() {
    return mobile;
}

public void setMobile(long mobile) {
    this.mobile = mobile;
}

public String getPassword() {
    return password;
}

public void setPassword(String password) {
    this.password = password;
}

  
// public String role;

public Role getRole() {
    return role;
}

public void setRole(Role role) {
    this.role = role;

}
public String getConfirmPassword() {
    return confirmPassword;
}

public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
}

public String getEncryptedPassword() {
    return encryptedPassword;
}

public void setEncryptedPassword(String encryptedPassword) {
    this.encryptedPassword = encryptedPassword;
}

public String getPasswordKeyId() {
    return passwordKeyId;
}

public void setPasswordKeyId(String passwordKeyId) {
    this.passwordKeyId = passwordKeyId;
}

   
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public List<BidInfo> getBids() {
        return bids;
    }

    public void setBids(List<BidInfo> bids) {
        this.bids = bids;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

}
