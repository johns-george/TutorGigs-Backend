package com.tutorapp.webapp.Common.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import com.tutorapp.webapp.Auth.Service.RedisTokenBlacklistService;
import com.tutorapp.webapp.Auth.Service.TokenBlacklistService;
import com.tutorapp.webapp.Common.Utils.RateLimitStore;
import com.tutorapp.webapp.Common.Utils.RedisRateLimitStore;

/**
 * With {@code spring.data.redis.host} set (as the real application.properties
 * always does), the Redis-backed implementations must be selected — and only
 * one bean of each type must exist.
 */
@SpringBootTest(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
@ActiveProfiles("test")
class RedisStoreWiringTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void tokenBlacklistUsesRedisWhenConfigured() {
        assertEquals(1, context.getBeansOfType(TokenBlacklistService.class).size());
        assertInstanceOf(RedisTokenBlacklistService.class, context.getBean(TokenBlacklistService.class));
    }

    @Test
    void rateLimitStoreUsesRedisWhenConfigured() {
        assertEquals(1, context.getBeansOfType(RateLimitStore.class).size());
        assertInstanceOf(RedisRateLimitStore.class, context.getBean(RateLimitStore.class));
    }
}
