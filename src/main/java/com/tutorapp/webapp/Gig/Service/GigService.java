package com.tutorapp.webapp.Gig.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Bid.Entity.BidInfo;
import com.tutorapp.webapp.Bid.Repository.BidInfoDao;
import com.tutorapp.webapp.Gig.DTO.GigDataDTO;
import com.tutorapp.webapp.Gig.Entity.GigData;
import com.tutorapp.webapp.Gig.Repository.GigDataDao;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;
import com.tutorapp.webapp.Wishlist.Repository.WishlistDao;

import jakarta.transaction.Transactional;

@Service
public class GigService {

    private static final Logger log = LoggerFactory.getLogger(GigService.class);
     
    @Autowired
    GigDataDao gigDataDao;

    @Autowired
    BidInfoDao bidInfoDao;

    @Autowired
    UserInfoDao userInfoDao;

    @Autowired
    WishlistDao wishlistDao;

    public Page<Object> getGigData(Pageable pageable) {
        Page<GigData> gigs = gigDataDao.findAllWithBids(pageable);

        return gigs.map(gig -> {
            GigDataDTO dto = new GigDataDTO();
            dto.setId(gig.getId());
            dto.setSubject(gig.getSubject());
            dto.setGrade(gig.getGrade());
            dto.setSyllabus(gig.getSyllabus());
            dto.setTeacherUsername(gig.getTeacherUsername());
            dto.setAmount(gig.getAmount());
            dto.setRating(gig.getRating());
            dto.setDescription(gig.getDescription());

            // Filter accepted bids from eagerly loaded collection (no extra query)
            List<String> studentNames = gig.getBids().stream()
                    .filter(bid -> "ACCEPTED".equals(bid.getStatus()))
                    .map(bid -> bid.getStudent() != null ? bid.getStudent().getFullName() : "Unknown")
                    .toList();

            dto.setStudentsAccepted(studentNames.size());
            dto.setAcceptedStudentNames(studentNames);

            // Single query for teacher info instead of two
            UserInfo teacherInfo = userInfoDao.findByUsername(gig.getTeacherUsername()).orElse(null);
            dto.setTeacherFullName(teacherInfo != null ? teacherInfo.getFullName() : "Unknown");
            dto.setProfileImageUrl(teacherInfo != null ? teacherInfo.getProfileImageUrl() : "NA");

            return dto;
        });
    }

   public List<GigDataDTO> getGigDataByTeacherUsername(String username) {
    List<GigData> gigs = gigDataDao.findByTeacherUsername(username);

    return gigs.stream().map(gig -> {
        GigDataDTO dto = new GigDataDTO();
        dto.setId(gig.getId());
        dto.setSubject(gig.getSubject());
        dto.setTeacherUsername(gig.getTeacherUsername());
        dto.setAmount(gig.getAmount());
        dto.setRating(gig.getRating());
        dto.setGrade(gig.getGrade());
        dto.setSyllabus(gig.getSyllabus());
       dto.setDescription(gig.getDescription());
        // Fetch accepted bids
        List<BidInfo> acceptedBids = bidInfoDao.findByGigdata_IdAndStatus(gig.getId(), "ACCEPTED");

        // Fetch student full names
        List<String> studentNames = acceptedBids.stream()
                .map(bid -> bid.getStudent().getFullName()) // we already have UserInfo object
                .toList();

        dto.setStudentsAccepted(studentNames.size());
        dto.setAcceptedStudentNames(studentNames);

        return dto;
    }).toList();
}

    
    public GigData createGig(GigData gigdata) {
        return gigDataDao.save(gigdata);
    }

    @Transactional
    public void deleteGig(Long gigId) {
        if (!gigDataDao.existsById(gigId)) {
            throw new RuntimeException("Gig not found with id " + gigId);
        }

        wishlistDao.deleteByGigdata_Id(gigId);
        bidInfoDao.deleteByGigdata_Id(gigId);
        gigDataDao.deleteById(gigId);
    }
}
