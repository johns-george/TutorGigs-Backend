package com.tutorapp.webapp.Common.Utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> values;

    @Test
    void inMemoryAllowsThenRejectsAfterLimit() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new InMemoryRateLimitStore(), false);
        MockHttpServletRequest request = loginRequest("1.1.1.1");

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            assertEquals(200, response.getStatus());
            Thread.sleep(1);
        }

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        filter.doFilter(request, blocked, new MockFilterChain());
        assertEquals(429, blocked.getStatus());
        assertTrue(blocked.getContentAsString().contains("Too many requests"));
    }

    @Test
    void ignoresXForwardedForUnlessTrustedProxyEnabled() {
        RateLimitFilter untrusted = new RateLimitFilter(new InMemoryRateLimitStore(), false);
        RateLimitFilter trusted = new RateLimitFilter(new InMemoryRateLimitStore(), true);

        MockHttpServletRequest request = loginRequest("10.0.0.1");
        request.addHeader("X-Forwarded-For", "8.8.8.8, 1.1.1.1");

        assertEquals("10.0.0.1", untrusted.getClientIp(request));
        assertEquals("8.8.8.8", trusted.getClientIp(request));
    }

    @Test
    void redisIncrementsAndSetsExpireOnFirstHit() throws Exception {
        when(redis.opsForValue()).thenReturn(values);
        when(values.increment("ratelimit:9.9.9.9:/api/auth/login")).thenReturn(1L);

        RateLimitFilter filter = new RateLimitFilter(new RedisRateLimitStore(redis), false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(loginRequest("9.9.9.9"), response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        verify(values).increment("ratelimit:9.9.9.9:/api/auth/login");
        verify(redis).expire("ratelimit:9.9.9.9:/api/auth/login", 15 * 60 * 1000L, TimeUnit.MILLISECONDS);
    }

    @Test
    void redisDoesNotResetTtlOnSubsequentHitsAndBlocksOverLimit() throws Exception {
        when(redis.opsForValue()).thenReturn(values);
        when(values.increment("ratelimit:9.9.9.9:/api/auth/login")).thenReturn(11L);
        when(redis.getExpire("ratelimit:9.9.9.9:/api/auth/login", TimeUnit.MILLISECONDS)).thenReturn(30_000L);

        RateLimitFilter filter = new RateLimitFilter(new RedisRateLimitStore(redis), false);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(loginRequest("9.9.9.9"), response, new MockFilterChain());

        assertEquals(429, response.getStatus());
        verify(redis, never()).expire("ratelimit:9.9.9.9:/api/auth/login", 15 * 60 * 1000L, TimeUnit.MILLISECONDS);
        verify(redis, times(1)).getExpire("ratelimit:9.9.9.9:/api/auth/login", TimeUnit.MILLISECONDS);
        assertEquals("30", response.getHeader("Retry-After"));
    }

    private static MockHttpServletRequest loginRequest(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setServletPath("/api/auth/login");
        request.setRemoteAddr(remoteAddr);
        return request;
    }
}
