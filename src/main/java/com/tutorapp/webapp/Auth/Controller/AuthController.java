package com.tutorapp.webapp.Auth.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Auth.Service.AuthService;

@RestController
@RequestMapping("/api/forgot")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String message = authService.forgotPassword(email);
        return ResponseEntity.ok(Map.of("success", true, "message", message));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        String message = authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("success", true, "message", message));
    }
    @PostMapping("/forgot-username")
    public ResponseEntity<?> forgotUsername(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            String message = authService.forgotUsername(email);
            return ResponseEntity.ok(Map.of("success", true, "message", message));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
}

}
