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

import com.tutorapp.webapp.Auth.Service.LoginService;
import com.tutorapp.webapp.Auth.Service.PasswordCryptoService;
import com.tutorapp.webapp.Common.Utils.JwtUtil;
import com.tutorapp.webapp.User.Entity.UserInfo;


@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    LoginService loginService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    PasswordCryptoService passwordCryptoService;

    @GetMapping("/password-key")
    public ResponseEntity<Map<String, String>> getPasswordKey() {
        return ResponseEntity.ok(passwordCryptoService.getPublicKeyPayload());
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserInfo userinfo) {
        Map<String, Object> response = new HashMap<>();
        String password;

        try {
            password = passwordCryptoService.decryptPassword(userinfo.getEncryptedPassword(), userinfo.getPasswordKeyId());
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        UserInfo user = loginService.validateUser(userinfo.getUsername(), password);

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
}
@PostMapping("/refresh")
public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("refreshToken");
    Map<String, Object> response = new HashMap<>();

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
