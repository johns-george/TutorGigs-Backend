package com.tutorapp.webapp.Chat.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Chat.Entity.Chats;
import com.tutorapp.webapp.Chat.Entity.Message;
import com.tutorapp.webapp.Chat.Repository.ChatDao;
import com.tutorapp.webapp.Chat.Repository.MessageDao;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@Service
public class MessageService {

    @Autowired
    MessageDao messageDao;

    @Autowired
    ChatDao chatDao;

    @Autowired
    UserInfoDao userInfoDao;


    // public List<Message> getMessagesByChatId(long chatId) {
    //     return messageDao.findByChatIdOrderByTimestampAsc(chatId);
    // }

  public Message saveMessage(Long chatId, Long senderId, String content) {

     Chats chat = chatDao.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Chat not found"));

    UserInfo sender = userInfoDao.findById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
     
    // 💬 Save the message
    Message message = new Message();
    message.setChat(chat);
    message.setContent(content);
    message.setSender(sender);
     // ✅ Fetch receiver
//     UserInfo receiver = userInfoDao.findById(receiverId)
//             .orElseThrow(() -> new RuntimeException("Receiver not found"));
//     message.setReceiver(receiver);

    message.setChat(chatDao.findById(chatId)
            .orElseThrow(() -> new RuntimeException("Chat not found")));
    message.setTimestamp(LocalDateTime.now());

    return messageDao.save(message);
   }
}
