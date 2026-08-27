package com.tutorapp.webapp.Common.Utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tutorapp.webapp.User.Enum.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${JWT_SECRET:}")
    private String jwtSecretEnv;

    private Key SECRET_KEY;

    // Access token validity: 15 minutes
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15;

    // Refresh token validity: 7 days
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    @PostConstruct
    public void init() {
        if (jwtSecretEnv != null && !jwtSecretEnv.isBlank()) {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecretEnv);
            this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
            log.info("JWT secret key loaded from environment variable");
        } else {
            log.warn("JWT_SECRET not set — using generated key. Tokens will not survive restarts.");
            this.SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
    }

    // ✅ Generate Access Token
    public String generateAccessToken(String username, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username, ACCESS_TOKEN_EXPIRATION);
    }

    // ✅ Generate Refresh Token
    public String generateRefreshToken(String username, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, username, REFRESH_TOKEN_EXPIRATION);
    }

    // ✅ Generic token creation logic
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // ✅ Extract role from token
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ✅ Check if token is valid (not expired and well-formed)
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // ✅ Check token expiration
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ✅ Extract expiration
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ✅ Extract any claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // ✅ Extract all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
