package com.tutorapp.webapp.Auth.Controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tutorapp.webapp.Auth.Service.PasswordCryptoService;
import com.tutorapp.webapp.Auth.Service.SignupService;
import com.tutorapp.webapp.Common.Utils.JwtUtil;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class SignupController {

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
    public ResponseEntity<?> addUserInfo(@RequestBody UserInfo user) {
        String password;
        try {
            password = passwordCryptoService.decryptPassword(user.getEncryptedPassword(), user.getPasswordKeyId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole());
        user.setPassword(passwordEncoder.encode(password));
        UserInfo savedUser = signupService.addUserInfo(user);
        return ResponseEntity.ok(Map.of(
                "user", savedUser,
                "accessToken", accessToken,
                "refreshToken", refreshToken));
    }

    @PutMapping("/student-dashboard/{id}")
    public ResponseEntity<UserInfo> updateStudentDetails(@PathVariable Long id,@RequestBody UserInfo user,Authentication authentication) {
        try {
            UserInfo updatedUser = signupService.updateStudentDetails(id, user);
        if(authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
        
            if (!username.equals(updatedUser.getUsername())) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
        } else {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        return ResponseEntity.ok(updatedUser); 

    } catch (Exception e) {
        return ResponseEntity.status(500).build(); // Internal Server Error
    }
}

    @PutMapping("/teacher-dashboard/{id}")
    public ResponseEntity<UserInfo> updateTeacherDetails(@PathVariable Long id,@RequestBody UserInfo user,Authentication authentication) {
        try {
            UserInfo updatedUser = signupService.updateTeacherDetails(id, user);
            System.out.println("Updated User: " + updatedUser);
        if(authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
        
            if (!username.equals(updatedUser.getUsername())) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
        } else {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        return ResponseEntity.ok(updatedUser); 
        
    } catch (Exception e) {
        return ResponseEntity.status(500).build(); // Internal Server Error
    }
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
    try {
        UserInfo updatedUser = signupService.uploadProfilePicture(id, file);

        // Auth check
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            if (!username.equals(updatedUser.getUsername())) {
                return ResponseEntity.status(403).build();
            }
        } else {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(updatedUser);
    } catch (IOException e) {
        return ResponseEntity.status(500)
                .build(); 
    }
}

}
