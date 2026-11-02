package com.tutorapp.webapp.Common.Config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.tutorapp.webapp.Auth.Service.InMemoryTokenBlacklistService;
import com.tutorapp.webapp.Auth.Service.RedisTokenBlacklistService;
import com.tutorapp.webapp.Auth.Service.TokenBlacklistService;
import com.tutorapp.webapp.Common.Utils.InMemoryRateLimitStore;
import com.tutorapp.webapp.Common.Utils.JwtUtil;
import com.tutorapp.webapp.Common.Utils.RateLimitStore;
import com.tutorapp.webapp.Common.Utils.RedisRateLimitStore;

/**
 * Wires the Redis-backed stores when Redis is configured (same condition as
 * {@link RedisConfig}) and the in-memory implementations otherwise.
 *
 * <p>The conditions live here rather than on the implementation classes because
 * {@code @ConditionalOnBean} / {@code @ConditionalOnMissingBean} on
 * component-scanned beans are evaluated before {@link RedisConfig} has
 * registered its {@code StringRedisTemplate}, which previously made the
 * in-memory implementation win even in production.
 */
@Configuration
public class ServiceStoreConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public TokenBlacklistService redisTokenBlacklistService(StringRedisTemplate redis, JwtUtil jwtUtil) {
        return new RedisTokenBlacklistService(redis, jwtUtil);
    }

    @Bean
    @ConditionalOnMissingBean(TokenBlacklistService.class)
    public TokenBlacklistService inMemoryTokenBlacklistService() {
        return new InMemoryTokenBlacklistService();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.data.redis.host")
    public RateLimitStore redisRateLimitStore(StringRedisTemplate redis) {
        return new RedisRateLimitStore(redis);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitStore.class)
    public RateLimitStore inMemoryRateLimitStore() {
        return new InMemoryRateLimitStore();
    }
}
