package com.tutorapp.webapp.Common.Config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tutorapp.webapp.Common.Utils.JwtAuthFilter;
import com.tutorapp.webapp.Common.Utils.RateLimitFilter;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity

public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, RateLimitFilter rateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // enable CORS here
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/password-key").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/check-username/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/create-gig").authenticated()
                .requestMatchers("/ws/**").permitAll()   // <-- allow WebSocket handshake
                .requestMatchers(HttpMethod.GET, "/api/chat/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/chat/**").authenticated()
                .requestMatchers("/ratings/average/**").permitAll() // allow any authenticated user
                .requestMatchers("/uploads/**").permitAll() // allow public access to images
                .requestMatchers("/api/forgot/**").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()
                .requestMatchers("/api/health").permitAll()
                .anyRequest().authenticated()
                );
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
     @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "https://*.onrender.com",
            "https://*.lovable.app"
        )); // Allowed origin patterns
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
}
