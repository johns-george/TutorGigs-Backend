package com.tutorapp.webapp.Chat.Presence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@Component
@ConditionalOnBean(PresenceService.class)
public class WebSocketPingScheduler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketPingScheduler.class);

    private final SimpUserRegistry userRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;
    private final UserInfoDao userInfoDao;

    public WebSocketPingScheduler(
            SimpUserRegistry userRegistry,
            SimpMessagingTemplate messagingTemplate,
            PresenceService presenceService,
            UserInfoDao userInfoDao) {
        this.userRegistry = userRegistry;
        this.messagingTemplate = messagingTemplate;
        this.presenceService = presenceService;
        this.userInfoDao = userInfoDao;
    }

    @Scheduled(fixedRateString = "${chat.presence.ping-interval-ms:30000}")
    public void pingClientsAndRefreshTtl() {
        for (SimpUser user : userRegistry.getUsers()) {
            if (user.getName() == null) {
                continue;
            }
            messagingTemplate.convertAndSendToUser(user.getName(), "/queue/ping", "ping");
            userInfoDao.findByUsername(user.getName())
                    .map(UserInfo::getId)
                    .ifPresent(presenceService::refresh);
        }
        log.trace("Pinged {} websocket user(s) and refreshed Redis TTLs", userRegistry.getUserCount());
    }
}
