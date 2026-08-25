package com.tutorapp.webapp.Wishlist.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Wishlist.Entity.Wishlist;

@Repository
public interface WishlistDao extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByStudentUsername(String username);

    void deleteByGigdataIdAndStudentId(Long gigId, Long userId);

    void deleteByGigdata_Id(Long gigId);

}
