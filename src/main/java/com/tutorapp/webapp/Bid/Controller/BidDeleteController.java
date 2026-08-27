package com.tutorapp.webapp.Bid.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Bid.Entity.BidInfo;
import com.tutorapp.webapp.Bid.Repository.BidInfoDao;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/auth")
public class BidDeleteController {
    @Autowired
    BidInfoDao bidInfoDao;

@PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
@DeleteMapping("/deletebids/{bidId}")
public ResponseEntity<?> deleteBid(@PathVariable Long bidId, Authentication authentication) {
    try {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String username = (String) authentication.getPrincipal();
        BidInfo bid = bidInfoDao.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        String bidOwner = bid.getStudent() != null ? bid.getStudent().getUsername() : null;
        String gigOwner = bid.getGigdata() != null ? bid.getGigdata().getTeacherUsername() : null;
        if (!username.equals(bidOwner) && !username.equals(gigOwner)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own bids or bids on your gigs");
        }
        bidInfoDao.deleteById(bidId);
        return ResponseEntity.ok("Bid deleted successfully");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting bid: " + e.getMessage());
    }
}
}
