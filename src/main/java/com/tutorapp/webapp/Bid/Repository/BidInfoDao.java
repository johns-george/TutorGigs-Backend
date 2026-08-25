package com.tutorapp.webapp.Bid.Repository;

import java.util.List;
//import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Bid.Entity.BidInfo;

import jakarta.transaction.Transactional;

@Repository
public interface  BidInfoDao  extends JpaRepository<BidInfo, Long>{

    List<BidInfo> findByStudentId(Long studentId);

    List<BidInfo> findByGigdata_IdAndStatus(Long gigId, String status);

    @Transactional
    @Override
    void deleteById(Long bidId);

    void deleteByGigdata_IdAndStudent_Id(Long gigId, long id);

    void deleteByGigdata_Id(Long gigId);

    @EntityGraph(attributePaths = {"gigdata", "student"})
    @Query("SELECT b FROM BidInfo b where b.gigdata.teacherUsername = :teacherUsername")
    List<BidInfo> getRequestsByTeacherUsername(String teacherUsername);

    @Modifying
    @Transactional
    @Query("UPDATE BidInfo b SET b.status = :status WHERE b.id = :bidId")
    int updateBidStatus(@Param("bidId") Long bidId, @Param("status") String status);

}
