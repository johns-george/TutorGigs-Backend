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
    public ResponseEntity<?> getTeacherInfo(@PathVariable String studentUsername, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = (String) authentication.getPrincipal();
                System.out.println("Authenticated user: " + username);
                UserInfo student = userInfoDao.findByUsername(studentUsername)
                        .orElseThrow(() -> new RuntimeException("Student not found"));
                         System.out.println("Student info: " + student.toString());

                UserDetailsDTO dto = new UserDetailsDTO();
                dto.setBio(student.getBio());
                dto.setDateOfBirth(student.getDateOfBirth());
                dto.setEmail(student.getEmail());
                dto.setFullName(student.getFullName());
                dto.setGrade(student.getGrade());
                dto.setMobile(student.getMobile());
                dto.setUsername(student.getUsername());
                dto.setProfileImageUrl(student.getProfileImageUrl());
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
}
