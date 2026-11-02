package com.tutorapp.webapp.Chat.Push;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;

/**
 * Offline fallback when Redis has no presence key for the recipient.
 * Stores a short-lived inbox entry (swap this for FCM/APNs later).
 */
@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    @Autowired(required = false)
    @Nullable
    private RedisTemplate<String, Object> redisTemplate;

    public void notifyOfflineUser(Long userId, ChatMessageDTO message) {
        log.info("Push fallback: user {} offline for chat {} from sender {}",
                userId, message != null ? message.getChatId() : null,
                message != null ? message.getSenderId() : null);

        if (redisTemplate == null || message == null || userId == null) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("chatId", message.getChatId());
        payload.put("senderId", message.getSenderId());
        payload.put("senderName", message.getSenderName());
        payload.put("content", message.getContent());
        payload.put("sentAt", message.getSentAt());
        redisTemplate.opsForList().leftPush("chat:push:inbox:" + userId, payload);
        redisTemplate.expire("chat:push:inbox:" + userId, Duration.ofDays(7));
    }
}
