package com.tutorapp.webapp.Gig.Controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Bid.Entity.BidInfo;
import com.tutorapp.webapp.Bid.Repository.BidInfoDao;
import org.springframework.security.access.prepost.PreAuthorize;
import com.tutorapp.webapp.Bid.Service.PlaceBidService;
import com.tutorapp.webapp.Gig.DTO.GigDataDTO;
import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Repository.GigDataDao;
import com.tutorapp.webapp.Gig.Service.GigService;



@RestController
@RequestMapping("/api/auth")
public class myGigController {

    private static final Logger log = LoggerFactory.getLogger(myGigController.class);

    @Autowired
    GigService gigService;

    @Autowired
    PlaceBidService placeBidService;

    @Autowired
    GigDataDao gigDataDao;

    @Autowired
    BidInfoDao bidInfoDao;


@PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/my-gigs")
    public ResponseEntity<List<GigDataDTO>> gigData(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String username = (String) authentication.getPrincipal();
            List<GigDataDTO> gigs = gigService.getGigDataByTeacherUsername(username);
            return ResponseEntity.ok(gigs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/requests/{teacherUsername}")
    public ResponseEntity<?> getRequestsByTeacherId(
            @PathVariable String teacherUsername,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(placeBidService.getRequestsByTeacherId(teacherUsername, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("bids/{bidId}/status")
    public ResponseEntity<String> updateBidStatus(@PathVariable Long bidId, @RequestParam String status, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            String username = (String) authentication.getPrincipal();
            BidInfo bid = bidInfoDao.findById(bidId)
                    .orElseThrow(() -> new RuntimeException("Bid not found"));
            if (bid.getGigdata() == null || !bid.getGigdata().getTeacherUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update bids on your own gigs");
            }
            placeBidService.updateBidStatus(bidId, status);
            return ResponseEntity.ok("Bid status updated to " + status);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating bid status: " + e.getMessage());
        }
    }
        @PreAuthorize("hasRole('TUTOR')")
    @DeleteMapping("/delete-gigs/{gigId}")
    public ResponseEntity<String> deleteGig(@PathVariable Long gigId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            String username = (String) authentication.getPrincipal();
            GigData gig = gigDataDao.findById(gigId)
                    .orElseThrow(() -> new RuntimeException("Gig not found"));
            if (!gig.getTeacherUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own gigs");
            }
            gigService.deleteGig(gigId);
            return ResponseEntity.ok("Gig deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting gig: " + e.getMessage());
        }
    }
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/gigs/{gigId}")
    public ResponseEntity<Optional<GigData>> getGigById(@PathVariable Long gigId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Optional<GigData> gig = gigDataDao.findById(gigId);
            return ResponseEntity.ok(gig);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("edit-gigs/{gigId}")
    public ResponseEntity<String> editGig(@PathVariable Long gigId, @RequestBody GigData gigdata, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            String username = (String) authentication.getPrincipal();
            GigData updatedgig = gigDataDao.findById(gigId)
                    .orElseThrow(() -> new RuntimeException("Gig not found"));
            if (!updatedgig.getTeacherUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only edit your own gigs");
            }
            updatedgig.setSubject(gigdata.getSubject());
            updatedgig.setAmount(gigdata.getAmount());
            updatedgig.setDescription(gigdata.getDescription());
            updatedgig.setGrade(gigdata.getGrade());
            updatedgig.setSyllabus(gigdata.getSyllabus());
            gigDataDao.save(updatedgig);
            return ResponseEntity.ok("Gig updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating gig: " + e.getMessage());
        }
    }
}
