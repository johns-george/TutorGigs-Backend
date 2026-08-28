package com.tutorapp.webapp.Auth.Controller;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tutorapp.webapp.Auth.DTO.SignupRequest;
import com.tutorapp.webapp.Auth.Service.PasswordCryptoService;
import com.tutorapp.webapp.Auth.Service.SignupService;
import com.tutorapp.webapp.Common.Exceptions.ResourceNotFoundException;
import com.tutorapp.webapp.Common.Utils.JwtUtil;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class SignupController {

    private static final Logger log = LoggerFactory.getLogger(SignupController.class);

    @Autowired
    SignupService signupService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    UserInfoDao userInfoDao;

    @Autowired
    PasswordCryptoService passwordCryptoService;

    @PostMapping("/signup")
    public ResponseEntity<?> addUserInfo(@Valid @RequestBody SignupRequest request) {
        String password;
        try {
            password = passwordCryptoService.decryptPassword(request.getEncryptedPassword(), request.getPasswordKeyId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        UserInfo user = new UserInfo();
        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPassword(passwordEncoder.encode(password));

        // Save user first — if DB constraint fails (e.g. duplicate username),
        // we don't want to return invalid tokens
        UserInfo savedUser = signupService.addUserInfo(user);

        String accessToken = jwtUtil.generateAccessToken(savedUser.getUsername(), savedUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUsername(), savedUser.getRole());

        return ResponseEntity.ok(Map.of(
                "user", savedUser,
                "accessToken", accessToken,
                "refreshToken", refreshToken));
    }

    @PutMapping("/student-dashboard/{id}")
    public ResponseEntity<?> updateStudentDetails(
            @PathVariable Long id,
            @RequestBody UserInfo user,
            Authentication authentication) {

        // 1. Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        // 2. Fetch existing user and check ownership BEFORE any writes
        UserInfo existingUser = userInfoDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!authentication.getName().equals(existingUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You can only update your own profile"));
        }

        // 3. Now safe to update
        UserInfo updatedUser = signupService.updateStudentDetails(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/teacher-dashboard/{id}")
    public ResponseEntity<?> updateTeacherDetails(
            @PathVariable Long id,
            @RequestBody UserInfo user,
            Authentication authentication) {

        // 1. Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        // 2. Fetch existing user and check ownership BEFORE any writes
        UserInfo existingUser = userInfoDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!authentication.getName().equals(existingUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You can only update your own profile"));
        }

        // 3. Now safe to update
        UserInfo updatedUser = signupService.updateTeacherDetails(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/check-username/{username:.+}")
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
        boolean exists = userInfoDao.existsByUsername(username);
        return ResponseEntity.ok(!exists); // true = available, false = taken
    }

    @PutMapping(value = "/student-dashboard/{id}/upload-photo", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadStudentProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        // 1. Check authentication
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        // 2. Fetch existing user and check ownership BEFORE any writes
        UserInfo existingUser = userInfoDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!authentication.getName().equals(existingUser.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You can only update your own profile"));
        }

        // 3. Now safe to upload
        try {
            UserInfo updatedUser = signupService.uploadProfilePicture(id, file);
            return ResponseEntity.ok(updatedUser);
        } catch (IOException e) {
            log.error("Failed to upload profile picture for user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to upload profile picture"));
        }
    }
}
