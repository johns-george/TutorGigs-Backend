package com.tutorapp.webapp.Chat.Redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void publish(Object message) {
        redisTemplate.convertAndSend("chat", message);
    }
}