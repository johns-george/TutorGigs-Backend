package com.tutorapp.webapp.Gig.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Gig.Entity.GigData;

@Repository
public interface  GigDataDao extends JpaRepository<GigData, Long> {

    List<GigData> findByTeacherUsername(String teacherUsername);
    GigData findById(long id);

}
