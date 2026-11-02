package com.tutorapp.webapp.Chat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.tutorapp.webapp.Chat.DTO.ChatDTO;
import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;
import com.tutorapp.webapp.Chat.Entity.Chats;
import com.tutorapp.webapp.Chat.Repository.ChatDao;
import com.tutorapp.webapp.Chat.Service.ChatService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Enum.Role;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatDao chatDao;

    @Autowired
    private UserInfoDao userInfoDao;

    private UserInfo user1;
    private UserInfo user2;
    private UserInfo user3;

    @BeforeEach
    void setUp() {
        user1 = new UserInfo();
        user1.setUsername("user1_" + System.nanoTime());
        user1.setEmail("user1_" + System.nanoTime() + "@test.com");
        user1.setPassword("password123");
        user1.setFullName("User One");
        user1.setRole(Role.STUDENT);
        user1 = userInfoDao.save(user1);

        user2 = new UserInfo();
        user2.setUsername("user2_" + System.nanoTime());
        user2.setEmail("user2_" + System.nanoTime() + "@test.com");
        user2.setPassword("password123");
        user2.setFullName("User Two");
        user2.setRole(Role.TUTOR);
        user2 = userInfoDao.save(user2);

        user3 = new UserInfo();
        user3.setUsername("user3_" + System.nanoTime());
        user3.setEmail("user3_" + System.nanoTime() + "@test.com");
        user3.setPassword("password123");
        user3.setFullName("User Three");
        user3.setRole(Role.STUDENT);
        user3 = userInfoDao.save(user3);
    }

    @Test
    void testPerChatMessageSequencing() {
        ChatDTO chat1 = chatService.getOrCreatePrivateChat(user1.getId(), user2.getId());
        ChatDTO chat2 = chatService.getOrCreatePrivateChat(user1.getId(), user3.getId());

        // Send messages in chat 1
        ChatMessageDTO m1 = chatService.saveMessage(chat1.getId(), user1.getId(), "Hello user2");
        ChatMessageDTO m2 = chatService.saveMessage(chat1.getId(), user2.getId(), "Hi user1");
        ChatMessageDTO m3 = chatService.saveMessage(chat1.getId(), user1.getId(), "How are you?");

        assertEquals(1L, m1.getSequenceNumber());
        assertEquals(2L, m2.getSequenceNumber());
        assertEquals(3L, m3.getSequenceNumber());

        // Send messages in chat 2 - sequence must start from 1 independently for chat 2
        ChatMessageDTO c2m1 = chatService.saveMessage(chat2.getId(), user1.getId(), "Hello user3");
        ChatMessageDTO c2m2 = chatService.saveMessage(chat2.getId(), user3.getId(), "Hi user1 from chat2");

        assertEquals(1L, c2m1.getSequenceNumber());
        assertEquals(2L, c2m2.getSequenceNumber());

        // Check last sequence number on chat entity
        Chats updatedChat1 = chatDao.findById(chat1.getId()).orElseThrow();
        assertEquals(3L, updatedChat1.getLastSequenceNumber());

        Chats updatedChat2 = chatDao.findById(chat2.getId()).orElseThrow();
        assertEquals(2L, updatedChat2.getLastSequenceNumber());
    }

    @Test
    void testGetMessagesAfterSequenceForGapCatchup() {
        ChatDTO chat = chatService.getOrCreatePrivateChat(user1.getId(), user2.getId());

        chatService.saveMessage(chat.getId(), user1.getId(), "Msg 1");
        chatService.saveMessage(chat.getId(), user2.getId(), "Msg 2");
        chatService.saveMessage(chat.getId(), user1.getId(), "Msg 3");
        chatService.saveMessage(chat.getId(), user2.getId(), "Msg 4");
        chatService.saveMessage(chat.getId(), user1.getId(), "Msg 5");

        // Client has messages up to sequence 2, needs 3, 4, 5
        List<ChatMessageDTO> missed = chatService.getMessagesAfterSequence(chat.getId(), 2L);
        assertEquals(3, missed.size());
        assertEquals(3L, missed.get(0).getSequenceNumber());
        assertEquals(4L, missed.get(1).getSequenceNumber());
        assertEquals(5L, missed.get(2).getSequenceNumber());
    }
}
