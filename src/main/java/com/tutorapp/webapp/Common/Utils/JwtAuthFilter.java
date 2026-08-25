package com.tutorapp.webapp.Common.Utils;

import java.io.IOException;
import java.util.Collections;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    //private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        //this.userDetailsService = userDetailsService;
    }
    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
    String authHeader = request.getHeader("Authorization");
    

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    String token = authHeader.substring(7);
    try {
        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        System.out.println("Auth header: " + authHeader);
        System.out.println("Extracted username: " + username);
        System.out.println("Extracted role: " + role);


        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    } catch (ExpiredJwtException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Token expired");
        return;
    } catch (JwtException | IllegalArgumentException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Invalid token");
        return;
    }

    filterChain.doFilter(request, response);
}
}

//     @Override
    
//     protected void doFilterInternal(HttpServletRequest request,
//                                     HttpServletResponse response,
//                                     FilterChain filterChain) throws ServletException, IOException {
//         String authHeader = request.getHeader("Authorization");
//         System.out.println("Auth header: " + request.getHeader("Authorization"));
//         if (authHeader != null && authHeader.startsWith("Bearer ")) {
//             String token = authHeader.substring(7);
//             try {
//                 String username = jwtUtil.extractUsername(token);

//                 if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                     UserDetails userDetails = userDetailsService.loadUserByUsername(username);

//                     if (jwtUtil.validateToken(token, userDetails.getUsername())) {
//                         UsernamePasswordAuthenticationToken authToken =
//                                 new UsernamePasswordAuthenticationToken(
//                                         userDetails, null, userDetails.getAuthorities());

//                         SecurityContextHolder.getContext().setAuthentication(authToken);
//                     }
//                 }
//             } catch (Exception e) {
//                 System.out.println("JWT validation failed: " + e.getMessage());
//             }
//         }

//         filterChain.doFilter(request, response);
//     }
//}
