package com.tutorapp.webapp.Auth.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Auth.DTO.LoginRequest;
import com.tutorapp.webapp.Auth.Service.LoginService;
import com.tutorapp.webapp.Auth.Service.PasswordCryptoService;
import com.tutorapp.webapp.Auth.Service.TokenBlacklistService;
import com.tutorapp.webapp.Common.Utils.JwtUtil;
import com.tutorapp.webapp.User.Entity.UserInfo;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    LoginService loginService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    PasswordCryptoService passwordCryptoService;

    @Autowired
    TokenBlacklistService tokenBlacklistService;

    @GetMapping("/password-key")
    public ResponseEntity<Map<String, String>> getPasswordKey() {
        return ResponseEntity.ok(passwordCryptoService.getPublicKeyPayload());
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        String password;

        try {
            password = passwordCryptoService.decryptPassword(request.getEncryptedPassword(), request.getPasswordKeyId());
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        UserInfo user = loginService.validateUser(request.getUsername(), password);

    if (user != null) {
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(),user.getRole());

        response.put("success", true);
        response.put("message", "Login successful");
        response.put("user", user);
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return ResponseEntity.ok(response);
    } else {
        response.put("success", false);
        response.put("message", "Invalid username or password");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        // Blacklist access token
        String accessToken = request.get("accessToken");
        if (accessToken != null && !accessToken.isBlank()) {
            tokenBlacklistService.blacklistToken(accessToken);
        }

        // Blacklist refresh token so stolen refresh tokens can't be reused
        String refreshToken = request.get("refreshToken");
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenBlacklistService.blacklistToken(refreshToken);
        }

        response.put("success", true);
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("refreshToken");
    Map<String, Object> response = new HashMap<>();

    // Check if refresh token has been blacklisted (e.g. after logout)
    if (refreshToken != null && tokenBlacklistService.isBlacklisted(refreshToken)) {
        response.put("error", "Refresh token has been revoked");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    if (jwtUtil.validateToken(refreshToken)) {
        String username = jwtUtil.extractUsername(refreshToken);
        UserInfo user = loginService.getUserByUsername(username);
        if (user != null) {
            String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole());
            response.put("accessToken", newAccessToken);
            return ResponseEntity.ok(response);
        }
    }
    response.put("error", "Invalid or expired refresh token");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
}

}
