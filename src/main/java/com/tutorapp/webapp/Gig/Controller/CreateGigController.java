package com.tutorapp.webapp.Gig.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Service.GigService;

@RestController
@RequestMapping("/api/auth")
public class CreateGigController {

    @Autowired
    GigService gigService;
    
    @PostMapping("/create-gig")
    public ResponseEntity<GigData> createGig(@RequestBody GigData gigdata,Authentication authentication) {
      try {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = (String) authentication.getPrincipal(); // 👈 username from JWT
            System.out.println("Authenticated user: " + username);
            gigdata.setTeacherUsername(username);
        } else {
            System.out.println("No authenticated user found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        GigData createdGig = gigService.createGig(gigdata);
        return ResponseEntity.ok(createdGig);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
    }
}
}