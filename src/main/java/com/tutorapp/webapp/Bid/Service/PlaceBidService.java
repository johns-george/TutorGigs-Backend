package com.tutorapp.webapp.Bid.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Bid.Entity.BidInfo;
import com.tutorapp.webapp.Bid.Repository.BidInfoDao;

import jakarta.transaction.Transactional;

@Service
public class PlaceBidService {
    @Autowired
     BidInfoDao bidInfoDao;

     public BidInfo placeBid(BidInfo bidinfo){
        return bidInfoDao.save(bidinfo);
     }

     @Transactional
    public void withdrawBid(Long gigId, Long studentId) {
        bidInfoDao.deleteByGigdata_IdAndStudent_Id(gigId, studentId);
    }

    public List<BidInfo> getRequestsByTeacherId(String teacherUsername) {
        return bidInfoDao.getRequestsByTeacherUsername(teacherUsername);
    }

     public void updateBidStatus(Long bidId, String status) {
        int updated = bidInfoDao.updateBidStatus(bidId, status);
        if (updated == 0) {
            throw new RuntimeException("Bid not found with id " + bidId);
        }
    }
}
