package com.tutorapp.webapp.Auth.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.tutorapp.webapp.Common.Services.EmailService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;


@Service
public class AuthService {

    @Autowired
    private UserInfoDao userRepository;

    @Autowired
    private EmailService emailService;

    // Handle forgot password
    public String forgotPassword(String email) {
        Optional<UserInfo> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("No user found with this email");
        }

        UserInfo user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        // In real project, generate link to your frontend reset page
        String resetLink = "http://localhost:3000/reset-password?token=" + token;

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
       BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
       user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
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
