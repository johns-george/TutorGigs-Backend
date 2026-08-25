package com.tutorapp.webapp.Gig.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Bid.DTO.BidResponseDTO;
import com.tutorapp.webapp.Bid.Entity.BidInfo;
import com.tutorapp.webapp.Bid.Repository.BidInfoDao;
import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Repository.GigDataDao;
import com.tutorapp.webapp.Gig.Service.GigService;
import com.tutorapp.webapp.User.DTO.UserDetailsDTO;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;





@RestController
@RequestMapping("/api/auth")
public class ExplorePageContoller {
    @Autowired
    GigService gigService;

    @Autowired
    BidInfoDao bidInfoDao;

    @Autowired
    UserInfoDao userInfoDao;

    @Autowired  
    GigDataDao gigDataDao;

  @GetMapping("/gigs")
public ResponseEntity<Page<Object>> gigData(
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    try {
        if (authentication != null && authentication.isAuthenticated()) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Object> gigs = gigService.getGigData(pageable);

            System.out.println("Returning " + gigs.getTotalElements() + " gigs");
            return ResponseEntity.ok(gigs);
        } else {
            System.out.println("No authenticated user found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

    @GetMapping("/studentplacedbid")
    public ResponseEntity<List<BidInfo>> studentplacedbid(Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
            String username = (String) authentication.getPrincipal();
              System.out.println("Authenticated user: " + username);
              UserInfo userInfo = userInfoDao.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            List<BidInfo> placedbids = bidInfoDao.findByStudentId(userInfo.getId());
            System.out.println("placedbids: " + placedbids);
            return ResponseEntity.ok(placedbids);
            }
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }

    @GetMapping("/bids/student/{studentId}")
    public ResponseEntity<List<BidResponseDTO>> getBidsByStudentId(@PathVariable Long studentId, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = (String) authentication.getPrincipal();
                System.out.println("Authenticated user: " + username);
                List<BidInfo> bids = bidInfoDao.findByStudentId(studentId);

                List<BidResponseDTO> response = bids.stream().map(bid -> {
                    BidResponseDTO dto = new BidResponseDTO();
                    dto.setBidId(bid.getId());
                    dto.setGigId(bid.getGigId());
                    dto.setTeacherUsername(bid.getTeacherUsername());

                // Fetch teacher full name from UserInfoDao
                Optional<UserInfo> teacherOpt = userInfoDao.findByUsername(bid.getTeacherUsername());
                dto.setTeacherFullName(teacherOpt.map(UserInfo::getFullName).orElse("Unknown"));
                dto.setTeacherProfileImageurl(teacherOpt.map(UserInfo::getProfileImageUrl).orElse("Unknown")); 
                dto.setBidAmount(bid.getBidAmount());
                dto.setBidPlacedAt(bid.getBidDate());
                dto.setStatus(bid.getStatus());
                dto.setSubject(bid.getGigSubject());
                dto.setGrade(gigDataDao.findById(bid.getGigId()).map(GigData::getGrade).orElse(0));
                dto.setSyllabus(gigDataDao.findById(bid.getGigId()).map(GigData::getSyllabus).orElse("Unknown"));
                return dto;
            }).toList();

            return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    @GetMapping("/teacher/{teacherusername}")
    public ResponseEntity<?> getTeacherInfo(@PathVariable String teacherusername, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = (String) authentication.getPrincipal();
                System.out.println("Authenticated user: " + username);
                UserInfo teacher = userInfoDao.findByUsername(teacherusername)
                        .orElseThrow(() -> new RuntimeException("Teacher not found"));
                         System.out.println("Teacher info: " + teacher.toString());
                 UserDetailsDTO dto = new UserDetailsDTO();
                dto.setBio(teacher.getBio());
                dto.setExperience(teacher.getExperience());
                dto.setDateOfBirth(teacher.getDateOfBirth());
                dto.setEmail(teacher.getEmail());
                dto.setFullName(teacher.getFullName());
                dto.setSubjects(teacher.getSubjects());
                dto.setMobile(teacher.getMobile());
                dto.setUsername(teacher.getUsername());
                dto.setProfileImageUrl(teacher.getProfileImageUrl());
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
        }
    }
    @PutMapping("/bids/{id}/complete")
    public ResponseEntity<String> completeBid(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String username = (String) authentication.getPrincipal();
                System.out.println("Authenticated user: " + username);
                BidInfo bid = bidInfoDao.findById(id)
                        .orElseThrow(() -> new RuntimeException("Bid not found"));
                bid.setStatus("COMPLETED");
                bidInfoDao.save(bid);
                return ResponseEntity.ok("Bid marked as completed");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        
        }
    }
}
