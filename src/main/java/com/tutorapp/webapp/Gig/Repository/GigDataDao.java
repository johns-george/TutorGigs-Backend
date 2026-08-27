package com.tutorapp.webapp.Gig.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Gig.Entity.GigData;

@Repository
public interface GigDataDao extends JpaRepository<GigData, Long> {

    List<GigData> findByTeacherUsername(String teacherUsername);
    GigData findById(long id);

    @Query("SELECT DISTINCT g FROM GigData g LEFT JOIN FETCH g.bids b LEFT JOIN FETCH b.student")
    Page<GigData> findAllWithBids(Pageable pageable);

    @Query("SELECT g FROM GigData g WHERE " +
           "(:subject IS NULL OR LOWER(g.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) AND " +
           "(:minGrade IS NULL OR g.grade >= :minGrade) AND " +
           "(:maxGrade IS NULL OR g.grade <= :maxGrade) AND " +
           "(:minAmount IS NULL OR g.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR g.amount <= :maxAmount)")
    Page<GigData> searchGigs(@Param("subject") String subject,
                              @Param("minGrade") Integer minGrade,
                              @Param("maxGrade") Integer maxGrade,
                              @Param("minAmount") Double minAmount,
                              @Param("maxAmount") Double maxAmount,
                              Pageable pageable);

}
