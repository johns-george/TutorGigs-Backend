package com.tutorapp.webapp.Admin.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Service.GigService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserInfoDao userInfoDao;

    @Autowired
    private GigService gigService;

    @GetMapping("/users")
    public ResponseEntity<Page<UserInfo>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserInfo> users = userInfoDao.findAll(pageable);
        // Clear sensitive fields
        users.getContent().forEach(user -> {
            user.setPassword(null);
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
        });
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userInfoDao.findById(id)
                .map(user -> {
                    user.setPassword(null);
                    user.setResetToken(null);
                    user.setResetTokenExpiry(null);
                    return ResponseEntity.ok((Object) user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userInfoDao.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userInfoDao.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully"));
    }

    @GetMapping("/gigs")
    public ResponseEntity<Page<Object>> getAllGigs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object> gigs = gigService.getGigData(pageable);
        return ResponseEntity.ok(gigs);
    }

    @DeleteMapping("/gigs/{id}")
    public ResponseEntity<?> deleteGig(@PathVariable Long id) {
        try {
            gigService.deleteGig(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Gig deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
