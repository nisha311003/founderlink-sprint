package com.founderlink.messagingService.controller;

import com.founderlink.messagingService.dto.ConversationResponse;
import com.founderlink.messagingService.dto.MessageRequest;
import com.founderlink.messagingService.dto.MessageResponse;
import com.founderlink.messagingService.service.IMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/messages", "/api/messages/"})
@RequiredArgsConstructor
public class MessageController {

    private final IMessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestHeader("X-User-Id") Long senderId,
            @RequestHeader("X-User-Email") String senderEmail,
            @Valid @RequestBody MessageRequest request) {

        return ResponseEntity.ok(messageService.sendMessage(senderId, senderEmail, request));
    }

    @GetMapping("/conversation/{id}")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        return ResponseEntity.ok(messageService.getConversationMessages(id, pageable));
    }

    @GetMapping("/conversations")
    public ResponseEntity<Page<ConversationResponse>> getConversations(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                messageService.getUserConversations(userId, pageable));
    }

    @PostMapping("/conversation")
    public ResponseEntity<ConversationResponse> getOrCreateConversation(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Email") String email,
            @RequestParam Long receiverId,
            @RequestParam String receiverEmail) {
        return ResponseEntity.ok(
                messageService.getOrCreateConversation(
                        userId, email, receiverId, receiverEmail));
    }

    @GetMapping("/conversation/{id}/unread")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Long id) {
        return ResponseEntity.ok(messageService.getUnreadCount(id));
    }
}
