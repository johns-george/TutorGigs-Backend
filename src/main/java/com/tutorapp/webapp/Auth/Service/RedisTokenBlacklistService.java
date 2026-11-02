package com.tutorapp.webapp.Auth.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.tutorapp.webapp.Common.Utils.JwtUtil;

/**
 * Redis-backed blacklist. Wired by {@code ServiceStoreConfig} when
 * {@code spring.data.redis.host} is set.
 */
public class RedisTokenBlacklistService implements TokenBlacklistService {

    static final String KEY_PREFIX = "blacklist:";

    private final StringRedisTemplate redis;
    private final JwtUtil jwtUtil;

    public RedisTokenBlacklistService(StringRedisTemplate redis, JwtUtil jwtUtil) {
        this.redis = redis;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void blacklistToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        long ttlMillis = remainingTtlMillis(token);
        if (ttlMillis <= 0) {
            return;
        }
        redis.opsForValue().set(keyFor(token), "1", Duration.ofMillis(ttlMillis));
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redis.hasKey(keyFor(token)));
    }

    static String keyFor(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private long remainingTtlMillis(String token) {
        try {
            return jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}
