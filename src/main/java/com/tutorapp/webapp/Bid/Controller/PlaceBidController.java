package com.tutorapp.webapp.Bid.Controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Bid.Entity.BidInfo;
import com.tutorapp.webapp.Bid.Service.PlaceBidService;
import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Repository.GigDataDao;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;


@RestController
@RequestMapping("/api/auth")
public class PlaceBidController {

    @Autowired
    PlaceBidService placeBidService;

    @Autowired
    UserInfoDao UserInfoDao;

    @Autowired
    GigDataDao GigDataDao;
   

    @PostMapping("/placebid")
    public ResponseEntity<?> placeBid(@RequestBody BidInfo bidinfo,Authentication authentication){
        try {

            System.out.println("Received bidinfo: " + bidinfo);
        System.out.println("gigId from frontend: " + bidinfo.getGigId());
        System.out.println("bidAmount from frontend: " + bidinfo.getBidAmount());
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = (String) authentication.getPrincipal();
        UserInfo student = UserInfoDao.findByUsername(username)
           .orElseThrow(() -> new RuntimeException("User not found"));
             GigData gig = GigDataDao.findById(bidinfo.getGigId())
            .orElseThrow(() -> new RuntimeException("Gig not found"));
             BidInfo bid = new BidInfo();
                bid.setBidAmount(bidinfo.getBidAmount());
                bid.setBidDate(LocalDateTime.now());
                bid.setGigdata(gig);
                bid.setStudent(student);
                bid.setStatus("PENDING");

          BidInfo data = placeBidService.placeBid(bid);
          System.out.println("Placed bid: " + data.getGigdata().getId());
        return ResponseEntity.ok(data);
    }
     catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing bid: " + e.getMessage());
    }
}  

@DeleteMapping("/withdrawbid/{gigId}")
public ResponseEntity<String> withdrawBid(@PathVariable Long gigId, Authentication authentication) {
    try {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = (String) authentication.getPrincipal();
        UserInfo student = UserInfoDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
         placeBidService.withdrawBid(gigId, student.getId());
        return ResponseEntity.ok("Bid withdrawn successfully");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error withdrawing bid: " + e.getMessage());
    }
}
}
