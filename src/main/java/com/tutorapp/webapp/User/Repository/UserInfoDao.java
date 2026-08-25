package com.tutorapp.webapp.User.Repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.User.Entity.UserInfo;



@Repository
public interface UserInfoDao extends JpaRepository<UserInfo, Long>{
   Optional<UserInfo> findById(UserInfo id);
   Optional<UserInfo> findByUsername(String username);
   boolean existsByUsername(String username);
   Optional<UserInfo> findByResetToken(String token);
   Optional<UserInfo> findByEmail(String email);


}
