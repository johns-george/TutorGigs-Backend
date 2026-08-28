package com.tutorapp.webapp.User.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.User.DTO.UserDetailsDTO;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@RestController
@RequestMapping("/api/auth")
public class StudentDetailsController {
    @Autowired
    UserInfoDao userInfoDao;

    @GetMapping("/student/{studentUsername}")
    public ResponseEntity<?> getStudentInfo(@PathVariable String studentUsername, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            UserInfo student = userInfoDao.findByUsername(studentUsername)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            UserDetailsDTO dto = new UserDetailsDTO();
            dto.setFullName(student.getFullName());
            dto.setUsername(student.getUsername());
            dto.setGrade(student.getGrade());
            dto.setBio(student.getBio());
            dto.setProfileImageUrl(student.getProfileImageUrl());
            // Note: email, mobile, and dateOfBirth are intentionally omitted
            // to protect student privacy on public profile pages
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
