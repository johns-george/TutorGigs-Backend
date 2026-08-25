package com.tutorapp.webapp.Bid.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; 
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Bid.Repository.BidInfoDao;

@RestController
@RequestMapping("/api/auth")
public class BidDeleteController {
    @Autowired
    BidInfoDao bidInfoDao;
@DeleteMapping("/deletebids/{bidId}")
public ResponseEntity<?> deleteBid(@PathVariable Long bidId,Authentication authentication) {
    try{
        if(authentication!=null&&authentication.isAuthenticated()){
            bidInfoDao.deleteById(bidId);
            return ResponseEntity.ok("Bid deleted successfully");
        }
        else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
    }catch(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting bid: " + e.getMessage());
    }
}
}
