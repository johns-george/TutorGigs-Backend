package com.tutorapp.webapp.Common.Utils;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis fixed-window rate limit store. Wired by {@code ServiceStoreConfig}
 * when {@code spring.data.redis.host} is set.
 */
public class RedisRateLimitStore implements RateLimitStore {

    static final String KEY_PREFIX = "ratelimit:";

    private final StringRedisTemplate redis;

    public RedisRateLimitStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public int incrementAndCount(String key, long windowMs) {
        String redisKey = KEY_PREFIX + key;
        Long count = redis.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redis.expire(redisKey, windowMs, TimeUnit.MILLISECONDS);
        }
        return count == null ? 0 : count.intValue();
    }

    @Override
    public long retryAfterMs(String key, long windowMs) {
        Long ttl = redis.getExpire(KEY_PREFIX + key, TimeUnit.MILLISECONDS);
        if (ttl == null || ttl < 0) {
            return windowMs;
        }
        return ttl;
    }
}
