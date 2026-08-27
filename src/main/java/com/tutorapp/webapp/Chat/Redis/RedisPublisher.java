package com.tutorapp.webapp.Chat.Redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Common.Config.RedisConfig;

@Service
@ConditionalOnBean(RedisConfig.class)
public class RedisPublisher {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void publish(Object message) {
        redisTemplate.convertAndSend("chat", message);
    }
}