package com.founderlink.messagingService.service;

import com.founderlink.messagingService.dto.ConversationResponse;
import com.founderlink.messagingService.dto.MessageRequest;
import com.founderlink.messagingService.dto.MessageResponse;
import com.founderlink.messagingService.dto.MessageSentEvent;
import com.founderlink.messagingService.entity.Conversation;
import com.founderlink.messagingService.entity.Message;
import com.founderlink.messagingService.exception.ConversationNotFoundException;
import com.founderlink.messagingService.repository.ConversationRepository;
import com.founderlink.messagingService.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService{

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ModelMapper modelMapper;
    private final MessageEventPublisher messageEventPublisher;
    @Override
    public MessageResponse sendMessage(Long senderId, String senderEmail, MessageRequest request) {

        Conversation conversation = conversationRepository
                .findBetweenUsers(senderId, request.getReceiverId())
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .senderId(senderId)
                            .senderEmail(senderEmail)
                            .receiverId(request.getReceiverId())
                            .receiverEmail(request.getReceiverEmail())
                            .lastMessageAt(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(newConv);
                });

        // update last message time
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // save message
        Message message = Message.builder()
                .conversationId(conversation.getId())
                .senderId(senderId)
                .senderEmail(senderEmail)
                .content(request.getContent())
                .isRead(false)
                .build();

        Message saved = messageRepository.save(message);
        MessageSentEvent event = new MessageSentEvent(
                saved.getId(),
                saved.getConversationId(),
                senderId,
                senderEmail,
                request.getReceiverId(),
                request.getReceiverEmail(),
                saved.getContent(),
                saved.getCreatedAt()
        );
        messageEventPublisher.publish(event);
        return modelMapper.map(message, MessageResponse.class);
    }

    @Override
    public Page<MessageResponse> getConversationMessages(Long conversationId, Pageable pageable) {
        conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ConversationNotFoundException(
                                "Conversation not found: " + conversationId));

        return messageRepository
                .findByConversationId(conversationId, pageable)
                .map(msg ->
                        modelMapper.map(msg, MessageResponse.class));
    }

    @Override
    public Page<ConversationResponse> getUserConversations(Long userId, Pageable pageable) {
        return conversationRepository
                .findAllByUserId(userId, pageable)
                .map(conv ->
                        modelMapper.map(conv, ConversationResponse.class));
    }

    @Override
    public ConversationResponse getOrCreateConversation(Long userId1, String email1, Long userId2, String email2) {
        Conversation conversation = conversationRepository
                .findBetweenUsers(userId1, userId2)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .senderId(userId1)
                            .senderEmail(email1)
                            .receiverId(userId2)
                            .receiverEmail(email2)
                            .lastMessageAt(LocalDateTime.now())
                            .build();
                    return conversationRepository.save(newConv);
                });

        return modelMapper.map(conversation, ConversationResponse.class);
    }

    @Override
    public long getUnreadCount(Long conversationId) {
        conversationRepository.findById(conversationId)
                .orElseThrow(() ->
                        new ConversationNotFoundException(
                                "Conversation not found: " + conversationId));

        return messageRepository.countByConversationIdAndIsReadFalse(conversationId);
    }
}
