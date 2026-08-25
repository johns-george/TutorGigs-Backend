package com.tutorapp.webapp.Gig.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.tutorapp.webapp.Bid.Service.PlaceBidService;
import com.tutorapp.webapp.Gig.DTO.GigDataDTO;
import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Repository.GigDataDao;
import com.tutorapp.webapp.Gig.Service.GigService;



@RestController
@RequestMapping("/api/auth")
public class myGigController {
      @Autowired
    GigService gigService;

    @Autowired
    PlaceBidService placeBidService;

    @Autowired
    GigDataDao gigDataDao;


@GetMapping("/my-gigs")
    public ResponseEntity<List<GigDataDTO>> gigData(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
            String username = (String) authentication.getPrincipal(); // 👈 username from JWT
            System.out.println("Authenticated user: " + username);
            List<GigDataDTO> gigs = gigService.getGigDataByTeacherUsername(username);
            System.out.println("Gigs found: " + gigs.size());
            return ResponseEntity.ok(gigs);
        } else {
            System.out.println("No authenticated user found");
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
            
        } 
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
        
    }
    @GetMapping("/requests/{teacherUsername}")
    public ResponseEntity<List<BidInfo>> getRequestsByTeacherId(@PathVariable String teacherUsername, Authentication authentication) {
        try {
             System.out.println("Teacher username received: " + teacherUsername);
            if (authentication != null && authentication.isAuthenticated()) {
                String username = (String) authentication.getPrincipal();
                System.out.println("Authenticated user: " + username);
                List<BidInfo> requests = placeBidService.getRequestsByTeacherId(teacherUsername);
                System.out.println(requests.toString());
                return ResponseEntity.ok(requests);
            } else {
                System.out.println("No authenticated user found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
             //e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    @PutMapping("bids/{bidId}/status")
    public ResponseEntity<String> updateBidStatus(@PathVariable Long bidId, @RequestParam String status,Authentication authentication) {
       try {
       if (authentication != null && authentication.isAuthenticated()) 
       {
          placeBidService.updateBidStatus(bidId, status);
       return ResponseEntity.ok("Bid status updated to " + status);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
       }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating bid status: " + e.getMessage());
        }

    }
        @DeleteMapping("/delete-gigs/{gigId}")
    public ResponseEntity<String> deleteGig(@PathVariable Long gigId, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = (String) authentication.getPrincipal();
                System.out.println("Authenticated user: " + username);
                gigService.deleteGig(gigId);
                return ResponseEntity.ok("Gig deleted successfully");
            } else {
                System.out.println("No authenticated user found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting gig: " + e.getMessage());
        }
    }
    @GetMapping("/gigs/{gigId}")
    public ResponseEntity<Optional<GigData>> getGigById(@PathVariable Long gigId, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = (String) authentication.getPrincipal();
                System.out.println("Authenticated user: " + username);
                Optional<GigData> gig = gigDataDao.findById(gigId);
                return ResponseEntity.ok(gig);
            } else {
                System.out.println("No authenticated user found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PutMapping("edit-gigs/{gigId}")
    public String putMethodName(@PathVariable Long gigId, @RequestBody GigData gigdata) {
      GigData updatedgig = gigDataDao.findById(gigId).orElseThrow();
      updatedgig.setSubject(gigdata.getSubject());
      updatedgig.setAmount(gigdata.getAmount());
      updatedgig.setDescription(gigdata.getDescription());
      updatedgig.setGrade(gigdata.getGrade());
      updatedgig.setSyllabus(gigdata.getSyllabus());
      gigDataDao.save(updatedgig);
      return "Gig updated successfully";
    }
}
