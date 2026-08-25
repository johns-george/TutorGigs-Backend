package com.tutorapp.webapp.Wishlist.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Bid.Entity.BidInfo;
import com.tutorapp.webapp.Bid.Repository.BidInfoDao;
import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Repository.GigDataDao;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;
import com.tutorapp.webapp.Wishlist.DTO.WishlistResponseDTO;
import com.tutorapp.webapp.Wishlist.Entity.Wishlist;
import com.tutorapp.webapp.Wishlist.Repository.WishlistDao;

import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {

        @Autowired
        WishlistDao wishlistDao;

        @Autowired
        UserInfoDao userinfodao;

        @Autowired
        GigDataDao gigdatadao;

        @Autowired
        BidInfoDao bidInfoDao;

        public Wishlist addToWishlist(Long gigdata, Long userid) {
              Optional<UserInfo> userinfo = userinfodao.findById(userid);
              Optional<GigData> gigData = gigdatadao.findById(gigdata);

              if (userinfo.isPresent() && gigData.isPresent()) {
                  Wishlist wishlist = new Wishlist();
                  wishlist.setStudent(userinfo.get());
                  wishlist.setGigdata(gigData.get());
                  return wishlistDao.save(wishlist);
              }
              return null;
        }

        public List<WishlistResponseDTO> getWishlistByUsername(String username) {
        List<Wishlist> wishlist = wishlistDao.findByStudentUsername(username);
                 return wishlist.stream().map(item -> {
                        WishlistResponseDTO dto = new WishlistResponseDTO();
                        dto.setId(item.getGigdata().getId());
                        dto.setSubject(item.getGigdata().getSubject());
                        dto.setGrade(item.getGigdata().getGrade());
                        dto.setSyllabus(item.getGigdata().getSyllabus());
                        dto.setTeacherUsername(item.getGigdata().getTeacherUsername());
                        dto.setAmount(item.getGigdata().getAmount());
                        dto.setRating(item.getGigdata().getRating());
                        dto.setDescription(item.getGigdata().getDescription());
                 List<BidInfo> acceptedBids = bidInfoDao.findByGigdata_IdAndStatus(item.getGigdata().getId(), "ACCEPTED");

                        List<String> studentNames = acceptedBids.stream()
                                .map(bid -> bid.getStudent().getFullName()) // we already have UserInfo object
                                .toList();
                        dto.setAcceptedStudentNames(studentNames);
       
                 userinfodao.findByUsername(item.getGigdata().getTeacherUsername())
                   .ifPresent(u -> dto.setTeacherFullName(u.getFullName()));

                   userinfodao.findByUsername(item.getGigdata().getTeacherUsername())
                   .ifPresent(u -> dto.setProfileImageUrl(u.getProfileImageUrl()));
                                        return dto;
                                }).toList();
        }
        
        @Transactional
        public void removeFromWishlist(Long gigId, Long userId) {
                wishlistDao.deleteByGigdataIdAndStudentId(gigId, userId);
        }
}
