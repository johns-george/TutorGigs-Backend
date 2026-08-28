package com.tutorapp.webapp.Auth.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Common.Exceptions.BadRequestException;
import com.tutorapp.webapp.Common.Exceptions.ResourceNotFoundException;
import com.tutorapp.webapp.Common.Services.EmailService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@Service
public class AuthService {

    private final UserInfoDao userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${FRONTEND_URL:http://localhost:5173}")
    private String frontendUrl;

    public AuthService(UserInfoDao userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public String forgotPassword(String email) {
        Optional<UserInfo> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("No user found with this email");
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

    public String resetPassword(String token, String newPassword) {
        Optional<UserInfo> optionalUser = userRepository.findByResetToken(token);
        if (optionalUser.isEmpty()) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        UserInfo user = optionalUser.get();
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Password reset successful.";
    }

    public String forgotUsername(String email) {
        Optional<UserInfo> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("No user found with this email");
        }

        UserInfo user = optionalUser.get();
        emailService.sendEmail(email, "Your Username",
                "Your username is: " + user.getUsername());

        return "Your username has been sent to your email.";
    }
}
