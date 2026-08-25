package com.tutorapp.webapp.Chat.DTO;

import com.tutorapp.webapp.User.Enum.Role;

public class ParticipantDTO {
    private Long userId;
    private String fullName;
    private String profileImageUrl;
    private Role role; // ADMIN / MEMBER

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
     public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
