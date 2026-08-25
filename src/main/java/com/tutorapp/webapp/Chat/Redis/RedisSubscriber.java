package com.tutorapp.webapp.Chat.Redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;

@Service
public class RedisSubscriber {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void onMessage(ChatMessageDTO message) {
        messagingTemplate.convertAndSend(
            "/topic/chat/" + message.getChatId(),
            message
        );
    }
}