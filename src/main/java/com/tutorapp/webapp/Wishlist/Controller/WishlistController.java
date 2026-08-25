package com.tutorapp.webapp.Wishlist.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
    public String addwishlist(@RequestBody WishlistRequestDTO request, Authentication authentication) {
          if(authentication != null && authentication.isAuthenticated()) {
            System.out.println("request: " + request);
              wishlistService.addToWishlist(request.getGigId(), request.getUserId());
          }
           return "Added to wishlist";
        }

        @GetMapping("/getwishlist")
        public List<WishlistResponseDTO> getWishlist(Authentication authentication) {
              if(authentication != null && authentication.isAuthenticated()) {
                  String username = authentication.getName();
                  return wishlistService.getWishlistByUsername(username);
              }
              return null;
        }

        @DeleteMapping("/removewishlist")
        public String removewishlist(@RequestBody Map<String, Long> request, Authentication authentication) {
              if(authentication != null && authentication.isAuthenticated()) {
                  wishlistService.removeFromWishlist(request.get("gigId"), request.get("userId"));
              }
              return "Removed from wishlist";

            }
        }
