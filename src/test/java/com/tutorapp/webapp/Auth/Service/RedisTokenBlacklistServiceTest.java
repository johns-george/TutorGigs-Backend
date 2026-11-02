package com.tutorapp.webapp.Auth.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.tutorapp.webapp.Common.Utils.JwtUtil;

@ExtendWith(MockitoExtension.class)
class RedisTokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> values;

    @Mock
    private JwtUtil jwtUtil;

    private RedisTokenBlacklistService service;

    @BeforeEach
    void setUp() {
        service = new RedisTokenBlacklistService(redis, jwtUtil);
    }

    @Test
    void blacklistedTokenIsRejected() {
        String token = "access-token";
        when(redis.hasKey(RedisTokenBlacklistService.keyFor(token))).thenReturn(true);

        assertTrue(service.isBlacklisted(token));
    }

    @Test
    void nonBlacklistedTokenPasses() {
        String token = "access-token";
        when(redis.hasKey(RedisTokenBlacklistService.keyFor(token))).thenReturn(false);

        assertFalse(service.isBlacklisted(token));
    }

    @Test
    void blacklistSetsHashedKeyWithTtlFromJwt() {
        String token = "access-token";
        long ttlMillis = 45_000L;
        when(redis.opsForValue()).thenReturn(values);
        when(jwtUtil.extractExpiration(token)).thenReturn(new Date(System.currentTimeMillis() + ttlMillis));

        service.blacklistToken(token);

        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(values).set(eq(RedisTokenBlacklistService.keyFor(token)), eq("1"), ttlCaptor.capture());
        long storedTtl = ttlCaptor.getValue().toMillis();
        assertTrue(storedTtl > 0);
        assertTrue(storedTtl <= ttlMillis);
        assertEquals(64, RedisTokenBlacklistService.keyFor(token)
                .substring(RedisTokenBlacklistService.KEY_PREFIX.length())
                .length());
    }

    @Test
    void expiredTokenIsNotStored() {
        when(jwtUtil.extractExpiration("expired")).thenReturn(new Date(System.currentTimeMillis() - 1_000));

        service.blacklistToken("expired");

        verifyNoInteractions(redis);
    }
}
