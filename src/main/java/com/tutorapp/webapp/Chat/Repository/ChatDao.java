package com.tutorapp.webapp.Chat.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Chat.Entity.Chats;


@Repository
public interface ChatDao extends JpaRepository<Chats, Long> {

    // Optional<Chats> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    // List<Chats> findBySenderIdOrReceiverId(Long senderId,Long receiverId);

     @Query("SELECT c FROM Chats c " +
           "JOIN c.participants p1 " +
           "JOIN c.participants p2 " +
           "WHERE c.type = 'PRIVATE' " +
           "AND p1.user.id = :user1 AND p2.user.id = :user2")
    Optional<Chats> findPrivateChatBetweenUsers(@Param("user1") Long user1Id, @Param("user2") Long user2Id);
}