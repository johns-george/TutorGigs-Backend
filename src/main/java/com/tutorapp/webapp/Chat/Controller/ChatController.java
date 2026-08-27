package com.tutorapp.webapp.Chat.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tutorapp.webapp.Chat.DTO.ChatMessageDTO;
import com.tutorapp.webapp.Chat.DTO.GroupChatRequestDTO;
import com.tutorapp.webapp.Chat.Service.ChatService;
import com.tutorapp.webapp.User.Entity.UserInfo;
import com.tutorapp.webapp.User.Repository.UserInfoDao;

@RestController
@RequestMapping("/api/chat")
class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    ChatService chatService;

    @Autowired
    UserInfoDao userInfoDao;

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long chatId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(chatService.getMessages(chatId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<?> getchatDetails(@PathVariable Long chatId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(chatService.getChatDetails(chatId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/getchatlist/{userId}")
    public ResponseEntity<?> getchatlistbyid(@PathVariable Long userId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(chatService.getChatsForUser(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> startChat(@PathVariable Long chatId,
                                       @RequestParam Long senderId,
                                       @RequestParam String content,
                                       Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            ChatMessageDTO dto = chatService.saveMessage(chatId, senderId, content);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/private")
    public ResponseEntity<?> startPrivateChat(@RequestParam Long user1Id,
                                              @RequestParam String user2Username,
                                              Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserInfo user2 = userInfoDao.findByUsername(user2Username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Long user2Id = user2.getId();
        log.debug("Starting private chat between users {} and {}", user1Id, user2Id);
        return ResponseEntity.ok(chatService.getOrCreatePrivateChat(user1Id, user2Id));
    }

    @PostMapping("/group")
    public ResponseEntity<?> createGroupChat(@RequestBody GroupChatRequestDTO request,
                                             Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(chatService.createGroupChat(request.getName(),
                request.getUserIds(), request.getAdminId()));
    }

    @PostMapping("{chatsId}/add")
    public ResponseEntity<?> addParticipants(@PathVariable Long chatsId, @RequestBody Long userId, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            chatService.addParticipants(chatsId, userId);
            return ResponseEntity.ok("Participants added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding participants");
        }
    }

    @DeleteMapping("{chatsId}/remove/{userId}")
    public ResponseEntity<?> removeParticipant(@PathVariable Long chatsId, @PathVariable Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            chatService.removeParticipant(chatsId, userId);
            return ResponseEntity.ok("Participant removed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error removing participant");
        }
    }
}