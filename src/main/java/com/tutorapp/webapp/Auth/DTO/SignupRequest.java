package com.tutorapp.webapp.Auth.DTO;

import com.tutorapp.webapp.User.Enum.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Encrypted password is required")
    private String encryptedPassword;

    @NotBlank(message = "Password key ID is required")
    private String passwordKeyId;

    @NotNull(message = "Role is required")
    private Role role;

    // Getters and setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public String getPasswordKeyId() { return passwordKeyId; }
    public void setPasswordKeyId(String passwordKeyId) { this.passwordKeyId = passwordKeyId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
