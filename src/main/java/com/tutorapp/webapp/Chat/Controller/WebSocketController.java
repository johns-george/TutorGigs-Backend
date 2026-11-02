package com.tutorapp.webapp.Chat.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;
import com.tutorapp.webapp.Chat.Presence.PresenceService;
import com.tutorapp.webapp.Chat.Service.ChatService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@Controller
public class WebSocketController {

    @Autowired
    ChatService chatService;

    @Autowired
    UserInfoDao userInfoDao;

    @Autowired(required = false)
    @Nullable
    PresenceService presenceService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessageDTO messageDTO) {
        chatService.saveMessage(messageDTO.getChatId(), messageDTO.getSenderId(), messageDTO.getContent());
    }

    @MessageMapping("/chat.ping")
    public void ping(Principal principal) {
        if (principal == null || presenceService == null) {
            return;
        }
        userInfoDao.findByUsername(principal.getName())
                .map(UserInfo::getId)
                .ifPresent(presenceService::refresh);
    }
}
