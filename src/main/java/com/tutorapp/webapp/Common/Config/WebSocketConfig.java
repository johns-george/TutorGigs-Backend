package com.tutorapp.webapp.Common.Config;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.tutorapp.webapp.Chat.Entity.Chats;
import com.tutorapp.webapp.Chat.Entity.ChatParticipant;
import com.tutorapp.webapp.Chat.Repository.ChatDao;
import com.tutorapp.webapp.Common.Utils.JwtUtil;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    private static final Pattern CHAT_TOPIC_PATTERN = Pattern.compile("^/topic/chat/(\\d+)$");

    private final JwtUtil jwtUtil;
    private final ChatDao chatDao;
    private final UserInfoDao userInfoDao;

    public WebSocketConfig(JwtUtil jwtUtil, ChatDao chatDao, UserInfoDao userInfoDao) {
        this.jwtUtil = jwtUtil;
        this.chatDao = chatDao;
        this.userInfoDao = userInfoDao;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:*", "https://*.onrender.com", "https://*.lovable.app")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor == null) return message;

                // ── CONNECT: authenticate via JWT ──────────────────────
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            String username = jwtUtil.extractUsername(token);
                            String role = jwtUtil.extractRole(token);
                            if (username != null && jwtUtil.validateToken(token)) {
                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(
                                                username,
                                                null,
                                                Collections.singletonList(
                                                        new SimpleGrantedAuthority("ROLE_" + role)));
                                accessor.setUser(auth);
                            }
                        } catch (JwtException | IllegalArgumentException e) {
                            log.warn("WebSocket CONNECT rejected: invalid token");
                        }
                    }
                    return message;
                }

                // ── SUBSCRIBE: verify user is a participant of the chat ─
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    if (destination == null) return message;

                    Matcher matcher = CHAT_TOPIC_PATTERN.matcher(destination);
                    if (!matcher.matches()) return message; // not a chat topic, allow

                    long chatId = Long.parseLong(matcher.group(1));

                    // Get the authenticated username from the STOMP session
                    Object userPrincipal = accessor.getUser();
                    if (userPrincipal == null) {
                        log.warn("SUBSCRIBE rejected: no authenticated user for chat {}", chatId);
                        accessor.setLeaveMutable(true);
                        return message; // Spring will deny if no auth
                    }

                    String username = ((UsernamePasswordAuthenticationToken) userPrincipal).getName();

                    // Look up user and verify they are a participant of this chat
                    UserInfo userInfo = userInfoDao.findByUsername(username).orElse(null);
                    if (userInfo == null) {
                        log.warn("SUBSCRIBE rejected: user {} not found", username);
                        accessor.setLeaveMutable(true);
                        return message;
                    }

                    Chats chat = chatDao.findById(chatId).orElse(null);
                    if (chat == null) {
                        log.warn("SUBSCRIBE rejected: chat {} not found", chatId);
                        accessor.setLeaveMutable(true);
                        return message;
                    }

                    boolean isParticipant = chat.getParticipants().stream()
                            .anyMatch(p -> p.getUser().getId().equals(userInfo.getId()));

                    if (!isParticipant) {
                        log.warn("SUBSCRIBE rejected: user {} is not a participant of chat {}", username, chatId);
                        accessor.setLeaveMutable(true);
                        return message;
                    }
                }

                return message;
            }
        });
    }
}
