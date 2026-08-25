package com.tutorapp.webapp.Chat.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tutorapp.webapp.Chat.Entity.ChatParticipant;


@Repository
public interface ParticipantDao extends JpaRepository<ChatParticipant, Long> {
       List<ChatParticipant> findByChatId(Long chatId);
       List<ChatParticipant> findByUserId(Long userId);
       Optional<ChatParticipant> findByChatIdAndUserId(Long chatId, Long userId);
}
