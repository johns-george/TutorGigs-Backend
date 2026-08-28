package com.tutorapp.webapp.Gig.Controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.security.access.prepost.PreAuthorize;
import com.tutorapp.webapp.Gig.DTO.GigDataDTO;
import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Repository.GigDataDao;
import com.tutorapp.webapp.Gig.Service.GigService;
import com.tutorapp.webapp.User.DTO.UserDetailsDTO;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;




@RestController
@RequestMapping("/api/auth")
public class ExplorePageContoller {

    private static final Logger log = LoggerFactory.getLogger(ExplorePageContoller.class);

    @Autowired
    GigService gigService;

    @Autowired
    BidInfoDao bidInfoDao;

    @Autowired
    UserInfoDao userInfoDao;

    @Autowired  
    GigDataDao gigDataDao;

  @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
  @GetMapping("/gigs")
public ResponseEntity<Page<Object>> gigData(
        Authentication authentication,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    try {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Object> gigs = gigService.getGigData(pageable);
        return ResponseEntity.ok(gigs);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
    @GetMapping("/gigs/search")
    public ResponseEntity<Page<Object>> searchGigs(
            Authentication authentication,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Integer minGrade,
            @RequestParam(required = false) Integer maxGrade,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            Pageable pageable = PageRequest.of(page, size);
            Page<GigData> gigs = gigDataDao.searchGigs(search, subject, minGrade, maxGrade, minAmount, maxAmount, pageable);
            Page<Object> dtoPage = gigs.map(gig -> {
                GigDataDTO dto = new GigDataDTO();
                dto.setId(gig.getId());
                dto.setSubject(gig.getSubject());
                dto.setGrade(gig.getGrade());
                dto.setSyllabus(gig.getSyllabus());
                dto.setTeacherUsername(gig.getTeacherUsername());
                dto.setAmount(gig.getAmount());
                dto.setRating(gig.getRating());
                dto.setDescription(gig.getDescription());
                List<String> studentNames = gig.getBids().stream()
                        .filter(bid -> "ACCEPTED".equals(bid.getStatus()))
                        .map(bid -> bid.getStudent() != null ? bid.getStudent().getFullName() : "Unknown")
                        .toList();
                dto.setStudentsAccepted(studentNames.size());
                dto.setAcceptedStudentNames(studentNames);
                UserInfo teacherInfo = userInfoDao.findByUsername(gig.getTeacherUsername()).orElse(null);
                dto.setTeacherFullName(teacherInfo != null ? teacherInfo.getFullName() : "Unknown");
                dto.setProfileImageUrl(teacherInfo != null ? teacherInfo.getProfileImageUrl() : "NA");
                return dto;
            });
            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/studentplacedbid")
    public ResponseEntity<?> studentplacedbid(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String username = (String) authentication.getPrincipal();
            UserInfo userInfo = userInfoDao.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
            Pageable pageable = PageRequest.of(page, size);
            Page<BidInfo> placedbids = bidInfoDao.findByStudentId(userInfo.getId(), pageable);
            return ResponseEntity.ok(placedbids);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/bids/student/{studentId}")
    public ResponseEntity<List<BidResponseDTO>> getBidsByStudentId(@PathVariable Long studentId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            List<BidInfo> bids = bidInfoDao.findByStudentId(studentId);

            List<BidResponseDTO> response = bids.stream().map(bid -> {
                BidResponseDTO dto = new BidResponseDTO();
                dto.setBidId(bid.getId());
                dto.setGigId(bid.getGigId());
                dto.setTeacherUsername(bid.getTeacherUsername());

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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @PreAuthorize("hasRole('STUDENT') or hasRole('TUTOR')")
    @GetMapping("/teacher/{teacherusername}")
    public ResponseEntity<?> getTeacherInfo(@PathVariable String teacherusername, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            UserInfo teacher = userInfoDao.findByUsername(teacherusername)
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            UserDetailsDTO dto = new UserDetailsDTO();
            dto.setFullName(teacher.getFullName());
            dto.setUsername(teacher.getUsername());
            dto.setSubjects(teacher.getSubjects());
            dto.setExperience(teacher.getExperience());
            dto.setBio(teacher.getBio());
            dto.setProfileImageUrl(teacher.getProfileImageUrl());
            // Note: email, mobile, and dateOfBirth are intentionally omitted
            // to protect teacher privacy on public profile pages
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/bids/{id}/complete")
    public ResponseEntity<String> completeBid(@PathVariable Long id, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            BidInfo bid = bidInfoDao.findById(id)
                    .orElseThrow(() -> new RuntimeException("Bid not found"));
            bid.setStatus("COMPLETED");
            bidInfoDao.save(bid);
            return ResponseEntity.ok("Bid marked as completed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
