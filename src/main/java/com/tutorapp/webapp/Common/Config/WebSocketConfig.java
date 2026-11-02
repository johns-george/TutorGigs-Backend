package com.tutorapp.webapp.Common.Config;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.tutorapp.webapp.Chat.Entity.Chats;
import com.tutorapp.webapp.Chat.Presence.PresenceService;
import com.tutorapp.webapp.Chat.Repository.ChatDao;
import com.tutorapp.webapp.Common.Utils.JwtUtil;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

import io.jsonwebtoken.JwtException;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);
    private static final Pattern CHAT_TOPIC_PATTERN = Pattern.compile("^/topic/chat/(\\d+)$");

    private final JwtUtil jwtUtil;
    private final ChatDao chatDao;
    private final UserInfoDao userInfoDao;

    @Autowired(required = false)
    @Nullable
    private PresenceService presenceService;

    public WebSocketConfig(JwtUtil jwtUtil, ChatDao chatDao, UserInfoDao userInfoDao) {
        this.jwtUtil = jwtUtil;
        this.chatDao = chatDao;
        this.userInfoDao = userInfoDao;
    }

    @Bean
    public ThreadPoolTaskScheduler webSocketHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] {30_000, 30_000})
                .setTaskScheduler(webSocketHeartbeatScheduler());
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
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

                if (accessor == null) {
                    return message;
                }

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    UserInfo user = authenticate(accessor);
                    accessor.setUser(new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            Collections.singletonList(
                                    new SimpleGrantedAuthority("ROLE_" + jwtRole(accessor, user)))));
                    if (presenceService != null) {
                        presenceService.register(user.getId(), user.getUsername(), accessor.getSessionId());
                    }
                    return message;
                }

                if (StompCommand.DISCONNECT.equals(accessor.getCommand()) && presenceService != null) {
                    presenceService.unregisterSession(accessor.getSessionId());
                    return message;
                }

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    authorizeSubscribe(accessor);
                }

                return message;
            }
        });
    }

    private UserInfo authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MessagingException("WebSocket CONNECT requires a Bearer token");
        }
        String token = authHeader.substring(7);
        try {
            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.validateToken(token)) {
                throw new MessagingException("WebSocket CONNECT rejected: invalid token");
            }
            return userInfoDao.findByUsername(username)
                    .orElseThrow(() -> new MessagingException("WebSocket CONNECT rejected: user not found"));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("WebSocket CONNECT rejected: invalid token");
            throw new MessagingException("WebSocket CONNECT rejected: invalid token");
        }
    }

    private String jwtRole(StompHeaderAccessor accessor, UserInfo user) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String role = jwtUtil.extractRole(authHeader.substring(7));
                if (role != null) {
                    return role;
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                // fall through to persisted role
            }
        }
        return user.getRole() != null ? user.getRole().name() : "USER";
    }

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        Matcher matcher = CHAT_TOPIC_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            return;
        }
        long chatId = Long.parseLong(matcher.group(1));
        if (accessor.getUser() == null) {
            throw new MessagingException("SUBSCRIBE rejected: not authenticated");
        }
        String username = accessor.getUser().getName();
        UserInfo userInfo = userInfoDao.findByUsername(username)
                .orElseThrow(() -> new MessagingException("SUBSCRIBE rejected: user not found"));
        Chats chat = chatDao.findById(chatId)
                .orElseThrow(() -> new MessagingException("SUBSCRIBE rejected: chat not found"));
        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(userInfo.getId()));
        if (!isParticipant) {
            throw new MessagingException("SUBSCRIBE rejected: not a participant of chat " + chatId);
        }
    }
}
