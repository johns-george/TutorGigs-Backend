package com.tutorapp.webapp.Wishlist.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Wishlist.DTO.WishlistRequestDTO;
import com.tutorapp.webapp.Wishlist.DTO.WishlistResponseDTO;
import com.tutorapp.webapp.Wishlist.Service.WishlistService;


@RestController
@RequestMapping("/api/auth")
public class WishlistController {

    @Autowired
    WishlistService wishlistService;


    @PostMapping("/addwishlist")
    public ResponseEntity<?> addwishlist(@RequestBody WishlistRequestDTO request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        wishlistService.addToWishlist(request.getGigId(), request.getUserId());
        return ResponseEntity.ok("Added to wishlist");
    }

    @GetMapping("/getwishlist")
    public ResponseEntity<?> getWishlist(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = authentication.getName();
        return ResponseEntity.ok(wishlistService.getWishlistByUsername(username));
    }

    @DeleteMapping("/removewishlist")
    public ResponseEntity<?> removewishlist(@RequestBody Map<String, Long> request, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        wishlistService.removeFromWishlist(request.get("gigId"), request.get("userId"));
        return ResponseEntity.ok("Removed from wishlist");
    }
}
