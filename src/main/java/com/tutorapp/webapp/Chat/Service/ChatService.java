package com.tutorapp.webapp.Chat.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.tutorapp.webapp.Chat.DTO.ChatDTO;
import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;
import com.tutorapp.webapp.Chat.DTO.ParticipantDTO;
import com.tutorapp.webapp.Chat.Entity.ChatParticipant;
import com.tutorapp.webapp.Chat.Entity.Chats;
import com.tutorapp.webapp.Chat.Entity.Message;
import com.tutorapp.webapp.Chat.Enum.ChatType;
import com.tutorapp.webapp.Chat.Redis.RedisPublisher;
import com.tutorapp.webapp.Chat.Repository.ChatDao;
import com.tutorapp.webapp.Chat.Repository.MessageDao;
import com.tutorapp.webapp.Chat.Repository.ParticipantDao;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Autowired(required = false)
    @Nullable
    RedisPublisher redisPublisher;

    @Autowired
    MessageDao messageDao;

    @Autowired
     UserInfoDao userInfoDao;

     @Autowired
     ChatDao chatDao;

     @Autowired
     ParticipantDao participantDao;

    public List<ChatMessageDTO> getMessages(Long chatId) {
         return messageDao.findByChatIdOrderByTimestampAsc(chatId).stream()
                .map(msg -> {
                    ChatMessageDTO dto = new ChatMessageDTO();
                    dto.setId(msg.getId());
                    dto.setChatId(msg.getChat().getId());
                    dto.setSenderId(msg.getSender().getId());
                    dto.setSenderName(msg.getSender().getFullName()); // ✅ fixed
                    dto.setReceiverId(
                        participantDao.findByChatId(msg.getChat().getId()).stream()
                            .filter(p -> !p.getUser().getId().equals(msg.getSender().getId()))
                            .findFirst()
                            .map(p -> p.getUser().getId())
                            .orElse(null)
                    );
                    dto.setReceiverName(
                        participantDao.findByChatId(msg.getChat().getId()).stream()
                            .filter(p -> !p.getUser().getId().equals(msg.getSender().getId()))
                            .findFirst()
                            .map(p -> p.getUser().getFullName())
                            .orElse(null)
                    );
                    dto.setContent(msg.getContent());
                    dto.setSentAt(msg.getTimestamp().toString());
                    return dto;
                }).toList();
    }

    public ChatMessageDTO saveMessage(Long chatId, Long senderId, String content) {
              Chats chat = chatDao.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        UserInfo sender = userInfoDao.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Message msg = new Message();
        msg.setChat(chat);
        msg.setSender(sender);
        msg.setContent(content);
        msg.setTimestamp(LocalDateTime.now());
        messageDao.save(msg);

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(msg.getId());
        dto.setChatId(chat.getId());
        dto.setSenderId(sender.getId());
        dto.setSenderName(sender.getFullName());
        dto.setContent(msg.getContent());
        dto.setSentAt(msg.getTimestamp().toString());

         if (redisPublisher != null) {
            redisPublisher.publish(dto);
        }

        return dto;
    }

    public ChatDTO getChatDetails(Long chatId) {
        Chats chat = chatDao.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setTimestamp(chat.getCreatedAt().toString());
        dto.setName(chat.getName());
        dto.setType(chat.getType());
         // participants
        List<ParticipantDTO> participants = participantDao.findByChatId(chat.getId())
                .stream().map(p -> {
                    ParticipantDTO pdto = new ParticipantDTO();
                    pdto.setUserId(p.getUser().getId());
                    pdto.setFullName(p.getUser().getFullName());
                    pdto.setRole(p.getRole());
                    UserInfo user=userInfoDao.findById(p.getUser().getId()).orElse(null);
                    pdto.setProfileImageUrl(user.getProfileImageUrl());
                    return pdto;
                }).toList();
        dto.setParticipants(participants);

        // messages
        List<ChatMessageDTO> messages = getMessages(chat.getId());
        dto.setMessages(messages);

        if (!messages.isEmpty()) {
            dto.setLastMessage(messages.get(messages.size() - 1).getContent());
        }

        return dto;
    }

     // 🔹 Start private chat
    public ChatDTO getOrCreatePrivateChat(Long user1Id, Long user2Id) {
        
        Chats existing = chatDao.findPrivateChatBetweenUsers(user1Id, user2Id)
                .orElse(null);

        if (existing == null) {
            Chats chat = new Chats();
            chat.setType(ChatType.PRIVATE);
            chat.setCreatedAt(LocalDateTime.now());
            chat = chatDao.save(chat);
            log.debug("Creating new private chat between users {} and {}", user1Id, user2Id);

            UserInfo user1 = userInfoDao.findById(user1Id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            UserInfo user2 = userInfoDao.findById(user2Id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            participantDao.save(new ChatParticipant(chat, user1, user1.getRole()));
            participantDao.save(new ChatParticipant(chat, user2, user2.getRole()));

            return getChatDetails(chat.getId());
        } else {
            return getChatDetails(existing.getId());
        }
    }

    // 🔹 Create group chat
    public ChatDTO createGroupChat(String name, List<Long> userIds, Long adminId) {
        Chats chat = new Chats();
        chat.setType(ChatType.GROUP);
        chat.setName(name);
        chat.setCreatedAt(LocalDateTime.now());
        chat = chatDao.save(chat);
            UserInfo admin = userInfoDao.findById(adminId)
                    .orElseThrow(() -> new RuntimeException("Admin not found"));
            participantDao.save(new ChatParticipant(chat, admin, admin.getRole()));
        for (Long uid : userIds) {
            UserInfo user = userInfoDao.findById(uid)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            participantDao.save(new ChatParticipant(chat, user, user.getRole()));
        }

        return getChatDetails(chat.getId());
    }

    // 🔹 Get all chats for a user
    public List<ChatDTO> getChatsForUser(Long userId) {
        List<ChatParticipant> memberships = participantDao.findByUserId(userId);
        return memberships.stream()
                .map(m -> getChatDetails(m.getChat().getId()))
                .toList();
    }
    public void addParticipants(Long chatId, Long userIds) {
        Chats chat = chatDao.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        UserInfo user = userInfoDao.findById(userIds)
                .orElseThrow(() -> new RuntimeException("User not found"));
        participantDao.save(new ChatParticipant(chat, user, user.getRole()));
    }
    public void removeParticipant(Long chatId, Long userId) {
        ChatParticipant participant = participantDao.findByChatIdAndUserId(chatId, userId)
                .orElseThrow(() -> new RuntimeException("Participant not found in chat"));
        participantDao.delete(participant);
    }
}
