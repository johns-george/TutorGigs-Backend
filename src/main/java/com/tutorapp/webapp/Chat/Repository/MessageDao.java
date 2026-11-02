package com.tutorapp.webapp.Chat.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Chat.Entity.Message;


@Repository
public interface MessageDao extends JpaRepository<Message, Long> {
    List<Message> findByChatIdOrderByTimestampAsc(long chatId);

    List<Message> findByChatIdOrderBySequenceNumberAsc(long chatId);

    List<Message> findByChatIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(long chatId, long sequenceNumber);

    @Query("SELECT COALESCE(MAX(m.sequenceNumber), 0) FROM Message m WHERE m.chat.id = :chatId")
    Long findMaxSequenceNumberByChatId(@Param("chatId") Long chatId);
}

