package com.tutorapp.webapp.Auth.DTO;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Encrypted password is required")
    private String encryptedPassword;

    @NotBlank(message = "Password key ID is required")
    private String passwordKeyId;

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public String getPasswordKeyId() { return passwordKeyId; }
    public void setPasswordKeyId(String passwordKeyId) { this.passwordKeyId = passwordKeyId; }
}
