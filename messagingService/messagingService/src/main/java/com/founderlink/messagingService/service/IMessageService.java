package com.founderlink.messagingService.service;

import com.founderlink.messagingService.dto.ConversationResponse;
import com.founderlink.messagingService.dto.MessageRequest;
import com.founderlink.messagingService.dto.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IMessageService {

    MessageResponse sendMessage(
            Long senderId,
            String senderEmail,
            MessageRequest request);

    Page<MessageResponse> getConversationMessages(
            Long conversationId,
            Pageable pageable);

    Page<ConversationResponse> getUserConversations(
            Long userId,
            Pageable pageable);

    ConversationResponse getOrCreateConversation(
            Long userId1,
            String email1,
            Long userId2,
            String email2);

    long getUnreadCount(Long conversationId);
}
