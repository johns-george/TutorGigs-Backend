package com.tutorapp.webapp.Gig.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Service.GigService;

@RestController
@RequestMapping("/api/auth")
public class CreateGigController {

    private static final Logger log = LoggerFactory.getLogger(CreateGigController.class);

    @Autowired
    GigService gigService;

    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/create-gig")
    public ResponseEntity<GigData> createGig(@RequestBody GigData gigdata, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String username = (String) authentication.getPrincipal();
            gigdata.setTeacherUsername(username);
            GigData createdGig = gigService.createGig(gigdata);
            return ResponseEntity.ok(createdGig);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}