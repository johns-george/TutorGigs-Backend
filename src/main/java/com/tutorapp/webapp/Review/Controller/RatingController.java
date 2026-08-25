package com.tutorapp.webapp.Review.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Review.DTO.RatingDTO;
import com.tutorapp.webapp.Review.DTO.RatingResponseDTO;
import com.tutorapp.webapp.Review.Entity.Rating;
import com.tutorapp.webapp.Review.Repository.RatingDao;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;


@RestController
public class RatingController {
    @Autowired
    private RatingDao ratingDao;

    @Autowired
    UserInfoDao userInfoDao;

    @PostMapping("/ratings")
    public ResponseEntity<String> createRating(@RequestBody RatingDTO entity,Authentication authentication) {
       try {
                if(authentication != null && authentication.isAuthenticated())
                    {
                    Rating rating = new Rating();
                    rating.setScore(entity.getScore());
                    rating.setComment(entity.getComment());
                    Optional<UserInfo> user = userInfoDao.findByUsername(entity.getRateeUsername());
                    user.ifPresent(u -> rating.setRateeId(u.getId()));
                    rating.setRaterId(entity.getRaterId());
                    rating.setGigId(entity.getGigId());
                    ratingDao.save(rating);
                    return ResponseEntity.ok("success");
                }
                else{
                    return ResponseEntity.status(403).body("forbidden");
                }
        }
        catch(Exception e){
                return ResponseEntity.badRequest().body("error");
            }
}

@GetMapping("/ratings/average/{teacherUsername:.+}")
public ResponseEntity<Double> getAverageRating(@PathVariable String teacherUsername) {
    try{
            
                Optional<UserInfo> teacher = userInfoDao.findByUsername(teacherUsername);
                if (teacher.isEmpty()) {
                return ResponseEntity.badRequest().build();
                    }

                    Long teacherId = teacher.get().getId();
                    System.out.println("Teacher ID: " + teacherId); // Debugging 
                        List<Rating> ratings = ratingDao.findByRateeId(teacherId);
                        if (ratings == null) ratings = new ArrayList<>();

                        Double avg = ratings.stream()
                                            .mapToInt(Rating::getScore)
                                            .average()
                                            .orElse(0.0);
                    return ResponseEntity.ok(avg);
    } catch(Exception e){
        return ResponseEntity.status(500).build();
    }
}
@GetMapping("/ratings/{teacherUsername:.+}")
public ResponseEntity<Map<String, Object>> getRatingsForTeacher(@PathVariable String teacherUsername,@RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size) {
    try{
                Optional<UserInfo> teacher = userInfoDao.findByUsername(teacherUsername);
                if (teacher.isEmpty()) {
                return ResponseEntity.badRequest().build();
}
              Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
                Page<Rating> ratingsPage = ratingDao.findByRateeId(teacher.get().getId(), pageable);
                List<RatingResponseDTO> ratingDTOs = new ArrayList<>();
                for (Rating rating : ratingsPage.getContent()) {
                    RatingResponseDTO dto = new RatingResponseDTO();
                    dto.setId(rating.getId());
                    dto.setRaterId(rating.getRaterId());
                    dto.setRateeId(rating.getRateeId());
                    userInfoDao.findById(rating.getRaterId()).ifPresent(user -> dto.setRaterFullname(user.getFullName()));
                    userInfoDao.findById(rating.getRateeId()).ifPresent(user -> dto.setRateeFullname(user.getFullName()));
                    dto.setGigId(rating.getGigId());
                    dto.setScore(rating.getScore());
                    dto.setComment(rating.getComment());
                    ratingDTOs.add(dto);
                }
                  Map<String, Object> response = new HashMap<>();
                response.put("reviews", ratingDTOs);
                response.put("currentPage", ratingsPage.getNumber());
                response.put("totalItems", ratingsPage.getTotalElements());
                response.put("totalPages", ratingsPage.getTotalPages());

                    return ResponseEntity.ok(response);
              
    } catch(Exception e){
        return ResponseEntity.status(500).build();
    }
}
}