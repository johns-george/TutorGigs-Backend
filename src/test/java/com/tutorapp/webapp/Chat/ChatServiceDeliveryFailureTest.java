package com.tutorapp.webapp.Chat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tutorapp.webapp.Chat.DTO.ChatDTO;
import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;
import com.tutorapp.webapp.Chat.Kafka.ChatKafkaPublisher;
import com.tutorapp.webapp.Chat.Repository.ChatDao;
import com.tutorapp.webapp.Chat.Repository.MessageDao;
import com.tutorapp.webapp.Chat.Service.ChatService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Enum.Role;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

/**
 * Proves the persistence claim against a real transaction: saveMessage() is
 * {@code @Transactional}, so a delivery exception must not propagate out of it,
 * otherwise the insert would be rolled back and the message lost.
 */
@SpringBootTest
@ActiveProfiles("test")
class ChatServiceDeliveryFailureTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserInfoDao userInfoDao;

    @Autowired
    private ChatDao chatDao;

    @Autowired
    private MessageDao messageDao;

    @MockitoBean
    private ChatKafkaPublisher kafkaPublisher;

    @Test
    void publishFailureDoesNotRollBackSavedMessage() {
        UserInfo sender = user("sender");
        UserInfo recipient = user("recipient");
        ChatDTO chat = chatService.getOrCreatePrivateChat(sender.getId(), recipient.getId());

        doThrow(new RuntimeException("broker down"))
                .when(kafkaPublisher).publishCreated(any(ChatMessageDTO.class), anyList());

        ChatMessageDTO saved = assertDoesNotThrow(
                () -> chatService.saveMessage(chat.getId(), sender.getId(), "hello"));

        assertEquals("hello", saved.getContent());
        assertEquals(1, messageDao.findByChatIdOrderBySequenceNumberAsc(chat.getId()).size());
        assertEquals(1L, chatDao.findById(chat.getId()).orElseThrow().getLastSequenceNumber());
    }

    private UserInfo user(String prefix) {
        UserInfo user = new UserInfo();
        user.setUsername(prefix + "_" + System.nanoTime());
        user.setEmail(prefix + "_" + System.nanoTime() + "@test.com");
        user.setPassword("password123");
        user.setFullName("Test " + prefix);
        user.setRole(Role.STUDENT);
        return userInfoDao.save(user);
    }
}
