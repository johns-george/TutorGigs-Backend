package com.tutorapp.webapp.Auth.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Common.Services.EmailService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;


@Service
public class AuthService {

    @Autowired
    private UserInfoDao userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    // Handle forgot password
    public String forgotPassword(String email) {
        Optional<UserInfo> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("No user found with this email");
        }

        UserInfo user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        String resetLink = frontendUrl + "/reset-password?token=" + token;

        emailService.sendEmail(email, "Password Reset Request",
                "Click the link below to reset your password:\n" + resetLink);

        return "Password reset link sent to your email.";
    }

    // Handle password reset (after clicking the link)
    public String resetPassword(String token, String newPassword) {
        Optional<UserInfo> optionalUser = userRepository.findByResetToken(token);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        UserInfo user = optionalUser.get();
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Password reset successful.";
    }

    // Handle forgot username
    public String forgotUsername(String email) {
        Optional<UserInfo> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("No user found with this email");
        }

        UserInfo user = optionalUser.get();
        emailService.sendEmail(email, "Your Username",
                "Your username is: " + user.getUsername());

        return "Your username has been sent to your email.";
    }
}
