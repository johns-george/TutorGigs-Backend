package com.tutorapp.webapp.Review.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Review.Entity.Rating;

@Repository
public interface  RatingDao extends JpaRepository<Rating, Long> {

    Page<Rating> findByRateeId(Long teacherId, Pageable pageable);
    List<Rating> findByRateeId(Long teacherId);

}
