package com.tutorapp.webapp.Chat.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;
import com.tutorapp.webapp.Chat.Service.MessageService;



@Controller
public class WebSocketController {
@Autowired
MessageService messageService;



@Autowired
SimpMessagingTemplate messagingTemplate;
@MessageMapping("/chat.sendMessage")
public void sendMessage(ChatMessageDTO messageDTO) {
    // Save message to DB
     var savedMessage = messageService.saveMessage(
        messageDTO.getChatId(),
        messageDTO.getSenderId(),
       // messageDTO.getReceiverId(),
        messageDTO.getContent()
    );    
            ChatMessageDTO response = new ChatMessageDTO();
            response.setId(savedMessage.getId());
            response.setChatId(savedMessage.getChat().getId());
            response.setSenderId(savedMessage.getSender().getId());
            response.setSenderName(savedMessage.getSender().getFullName());
            //response.setReceiverId(savedMessage.getReceiver().getId());
            response.setContent(savedMessage.getContent());
            response.setSentAt(savedMessage.getTimestamp().toString());
    // Broadcast to subscribers
    messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getChatId(), response);
}
}