package com.tutorapp.webapp.Auth.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@Service
public class LoginService {

    @Autowired
    UserInfoDao userInfoDao;


    @Autowired
    PasswordEncoder passwordEncoder;

    public UserInfo validateUser(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        UserInfo user = userInfoDao.findByUsername(username).orElse(null);
         if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user; // password matched
        }
        return null; // invalid credentials
    }

    public UserInfo getUserByUsername(String username) {
        return userInfoDao.findByUsername(username).orElse(null);
    }
}
