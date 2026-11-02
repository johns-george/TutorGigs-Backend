package com.tutorapp.webapp.Common.Config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import com.tutorapp.webapp.Auth.Service.InMemoryTokenBlacklistService;
import com.tutorapp.webapp.Auth.Service.TokenBlacklistService;
import com.tutorapp.webapp.Common.Utils.InMemoryRateLimitStore;
import com.tutorapp.webapp.Common.Utils.RateLimitStore;

/**
 * Without Redis configured, the in-memory fallbacks must be the single beans
 * selected for both stores.
 */
@SpringBootTest
@ActiveProfiles("test")
class InMemoryStoreWiringTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void tokenBlacklistFallsBackToInMemoryWithoutRedis() {
        assertEquals(1, context.getBeansOfType(TokenBlacklistService.class).size());
        assertInstanceOf(InMemoryTokenBlacklistService.class, context.getBean(TokenBlacklistService.class));
    }

    @Test
    void rateLimitStoreFallsBackToInMemoryWithoutRedis() {
        assertEquals(1, context.getBeansOfType(RateLimitStore.class).size());
        assertInstanceOf(InMemoryRateLimitStore.class, context.getBean(RateLimitStore.class));
    }
}
