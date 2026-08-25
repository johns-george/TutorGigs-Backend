package com.tutorapp.webapp.Chat.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Chat.Entity.Message;


@Repository
public interface MessageDao extends JpaRepository<Message, Long> {
    List<Message> findByChatIdOrderByTimestampAsc(long chatId);
}
