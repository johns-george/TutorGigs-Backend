package com.tutorapp.webapp.Auth.Service;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tutorapp.webapp.Common.Services.FileStorageService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@Service
public class SignupService {

	@Autowired
	UserInfoDao userInfoDao;

    @Autowired
    FileStorageService fileStorageService;

    public UserInfo addUserInfo(UserInfo user) {
        return userInfoDao.save(user);
    }

    public UserInfo updateStudentDetails(Long id, UserInfo user) {
        UserInfo existingUser = userInfoDao.findById(id) .orElseThrow(() -> new RuntimeException("User not found"));
            existingUser.setFullName(user.getFullName());
            existingUser.setEmail(user.getEmail());
            existingUser.setMobile(user.getMobile());
            existingUser.setGrade(user.getGrade());
            existingUser.setDateOfBirth(user.getDateOfBirth());
            existingUser.setBio(user.getBio());
            existingUser.setProfileImageUrl(user.getProfileImageUrl());
         return userInfoDao.save(existingUser);
    }

     public UserInfo updateTeacherDetails(Long id, UserInfo user) {
        UserInfo existingUser = userInfoDao.findById(id) .orElseThrow(() -> new RuntimeException("User not found"));
            existingUser.setFullName(user.getFullName());
            existingUser.setEmail(user.getEmail());
            existingUser.setMobile(user.getMobile());
            existingUser.setSubjects(user.getSubjects());
            existingUser.setDateOfBirth(user.getDateOfBirth());
            existingUser.setExperience(user.getExperience());
            existingUser.setBio(user.getBio());
            existingUser.setProfileImageUrl(user.getProfileImageUrl());
         return userInfoDao.save(existingUser);
    }
     // ✅ New method for image upload
    public UserInfo uploadProfilePicture(Long id, MultipartFile file) throws IOException {
        UserInfo user = userInfoDao.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getProfileImageUrl() != null) {
            fileStorageService.deleteOldProfilePicture(user.getProfileImageUrl());
        }

        String newUrl = fileStorageService.saveProfilePicture(file, id);
        user.setProfileImageUrl(newUrl);
        return userInfoDao.save(user);
    }

}
