package com.tutorapp.webapp.Bid.Controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.tutorapp.webapp.Bid.DTO.BidRequest;
import com.tutorapp.webapp.Bid.Entity.BidInfo;
import com.tutorapp.webapp.Bid.Repository.BidInfoDao;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import com.tutorapp.webapp.Bid.Service.PlaceBidService;
import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Repository.GigDataDao;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;


@RestController
@RequestMapping("/api/auth")
public class PlaceBidController {

    private static final Logger log = LoggerFactory.getLogger(PlaceBidController.class);

    @Autowired
    PlaceBidService placeBidService;

    @Autowired
    UserInfoDao UserInfoDao;

    @Autowired
    GigDataDao GigDataDao;

    @Autowired
    BidInfoDao bidInfoDao;

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/placebid")
    public ResponseEntity<?> placeBid(@Valid @RequestBody BidRequest request, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            String username = (String) authentication.getPrincipal();
            UserInfo student = UserInfoDao.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            GigData gig = GigDataDao.findById(request.getGigId())
                    .orElseThrow(() -> new RuntimeException("Gig not found"));
            if (bidInfoDao.existsByGigdata_IdAndStudent_Id(request.getGigId(), student.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("You have already placed a bid on this gig");
            }
            BidInfo bid = new BidInfo();
            bid.setBidAmount(request.getBidAmount());
            bid.setBidDate(LocalDateTime.now());
            bid.setGigdata(gig);
            bid.setStudent(student);
            bid.setStatus("PENDING");

            BidInfo data = placeBidService.placeBid(bid);
            log.debug("Bid placed on gig {} by user {}", data.getGigdata().getId(), username);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error placing bid: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
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
