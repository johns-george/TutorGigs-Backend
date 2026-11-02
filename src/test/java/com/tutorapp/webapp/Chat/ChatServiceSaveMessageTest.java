package com.tutorapp.webapp.Chat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;
import com.tutorapp.webapp.Chat.Entity.ChatParticipant;
import com.tutorapp.webapp.Chat.Entity.Chats;
import com.tutorapp.webapp.Chat.Entity.Message;
import com.tutorapp.webapp.Chat.Kafka.ChatKafkaPublisher;
import com.tutorapp.webapp.Chat.Presence.PresenceService;
import com.tutorapp.webapp.Chat.Push.PushNotificationService;
import com.tutorapp.webapp.Chat.Repository.ChatDao;
import com.tutorapp.webapp.Chat.Repository.MessageDao;
import com.tutorapp.webapp.Chat.Repository.ParticipantDao;
import com.tutorapp.webapp.Chat.Service.ChatService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Enum.Role;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@ExtendWith(MockitoExtension.class)
class ChatServiceSaveMessageTest {

    @Mock
    private ChatKafkaPublisher kafkaPublisher;
    @Mock
    private PresenceService presenceService;
    @Mock
    private PushNotificationService pushNotificationService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private MessageDao messageDao;
    @Mock
    private UserInfoDao userInfoDao;
    @Mock
    private ChatDao chatDao;
    @Mock
    private ParticipantDao participantDao;

    @InjectMocks
    private ChatService chatService;

    private Chats chat;
    private UserInfo sender;
    private UserInfo recipient;

    @BeforeEach
    void setUp() {
        chat = new Chats();
        chat.setId(1L);
        chat.setLastSequenceNumber(0L);

        sender = new UserInfo();
        sender.setId(10L);
        sender.setFullName("Sender");
        sender.setRole(Role.STUDENT);

        recipient = new UserInfo();
        recipient.setId(20L);
        recipient.setFullName("Recipient");
        recipient.setRole(Role.TUTOR);

        when(chatDao.findByIdForUpdate(1L)).thenReturn(Optional.of(chat));
        when(userInfoDao.findById(10L)).thenReturn(Optional.of(sender));
        when(chatDao.save(any(Chats.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageDao.save(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setId(99L);
            return message;
        });
        when(participantDao.findByChatId(1L)).thenReturn(List.of(
                new ChatParticipant(chat, sender, sender.getRole()),
                new ChatParticipant(chat, recipient, recipient.getRole())
        ));
    }

    @Test
    void saveMessagePersistsBeforePublish() {
        chatService.saveMessage(1L, 10L, "hello");

        InOrder order = inOrder(messageDao, kafkaPublisher);
        order.verify(messageDao).save(any(Message.class));
        order.verify(kafkaPublisher).publishCreated(any(ChatMessageDTO.class), anyList());
    }

    @Test
    void saveMessageSavesBeforePublishingAndSwallowsPublishFailure() {
        doThrow(new RuntimeException("broker down"))
                .when(kafkaPublisher).publishCreated(any(ChatMessageDTO.class), anyList());

        ChatMessageDTO saved = assertDoesNotThrow(
                () -> chatService.saveMessage(1L, 10L, "hello"));

        assertEquals("hello", saved.getContent());
        verify(messageDao).save(any(Message.class));
        InOrder order = inOrder(messageDao, kafkaPublisher);
        order.verify(messageDao).save(any(Message.class));
        order.verify(kafkaPublisher).publishCreated(any(ChatMessageDTO.class), anyList());
    }
}
