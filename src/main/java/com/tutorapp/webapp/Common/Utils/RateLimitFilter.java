package com.tutorapp.webapp.Common.Utils;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rate limiter for auth and password-reset endpoints.
 *
 * Each protected path has its own limit and window:
 *   POST /api/auth/login         → 10 requests / 15 min per IP
 *   POST /api/auth/signup        →  5 requests / 15 min per IP
 *   POST /api/forgot/**          →  5 requests / 15 min per IP
 *
 * Storage is Redis (fixed window) when Redis is configured, otherwise in-memory.
 * Exceeding the limit returns 429 Too Many Requests.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final Map<String, PathConfig> PATH_CONFIGS = Map.of(
        "/api/auth/login",  new PathConfig(10, 15 * 60 * 1000L),
        "/api/auth/signup", new PathConfig(5,  15 * 60 * 1000L)
    );

    private static final String FORGOT_PREFIX = "/api/forgot/";
    private static final PathConfig FORGOT_CONFIG = new PathConfig(5, 15 * 60 * 1000L);

    private final RateLimitStore rateLimitStore;
    private final boolean trustForwardedHeaders;

    public RateLimitFilter(
            RateLimitStore rateLimitStore,
            @Value("${app.security.trust-forwarded-headers:false}") boolean trustForwardedHeaders) {
        this.rateLimitStore = rateLimitStore;
        this.trustForwardedHeaders = trustForwardedHeaders;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        PathConfig config = resolveConfig(path);

        if (config == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        String key = ip + ":" + path;

        int count = rateLimitStore.incrementAndCount(key, config.windowMs);

        if (count > config.maxRequests) {
            long retryAfter = rateLimitStore.retryAfterMs(key, config.windowMs);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(Math.max(1, retryAfter / 1000)));
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Too many requests. Please try again later.\"}"
            );
            log.warn("Rate limit exceeded for {} on {} (count={}, limit={})",
                     ip, path, count, config.maxRequests);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private PathConfig resolveConfig(String path) {
        PathConfig exact = PATH_CONFIGS.get(path);
        if (exact != null) return exact;
        if (path.startsWith(FORGOT_PREFIX)) return FORGOT_CONFIG;
        return null;
    }

    String getClientIp(HttpServletRequest request) {
        if (trustForwardedHeaders) {
            String xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private static class PathConfig {
        final int maxRequests;
        final long windowMs;

        PathConfig(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
        }
    }
}
