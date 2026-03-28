package com.founderlink.messagingService.service;

import com.founderlink.messagingService.dto.ConversationResponse;
import com.founderlink.messagingService.dto.MessageRequest;
import com.founderlink.messagingService.dto.MessageResponse;
import com.founderlink.messagingService.entity.Conversation;
import com.founderlink.messagingService.entity.Message;
import com.founderlink.messagingService.exception.ConversationNotFoundException;
import com.founderlink.messagingService.repository.ConversationRepository;
import com.founderlink.messagingService.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MessageEventPublisher messageEventPublisher;

    @InjectMocks
    private MessageService messageService;

    @Test
    void sendMessage_shouldCreateConversationSaveMessageAndPublishEvent() {
        MessageRequest request = new MessageRequest();
        request.setReceiverId(2L);
        request.setReceiverEmail("receiver@example.com");
        request.setContent("Hello");

        Conversation savedConversation = Conversation.builder()
                .id(10L)
                .senderId(1L)
                .senderEmail("sender@example.com")
                .receiverId(2L)
                .receiverEmail("receiver@example.com")
                .build();
        Message savedMessage = Message.builder()
                .id(100L)
                .conversationId(10L)
                .senderId(1L)
                .senderEmail("sender@example.com")
                .content("Hello")
                .build();
        MessageResponse response = MessageResponse.builder()
                .conversationId(10L)
                .senderId(1L)
                .content("Hello")
                .build();

        when(conversationRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConversation);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(modelMapper.map(any(Message.class), any(Class.class))).thenReturn(response);

        MessageResponse result = messageService.sendMessage(1L, "sender@example.com", request);

        assertEquals(10L, result.getConversationId());
        assertEquals("Hello", result.getContent());
        verify(messageRepository).save(any(Message.class));
        verify(messageEventPublisher).publish(any());
    }

    @Test
    void getConversationMessages_shouldThrowWhenConversationIsMissing() {
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ConversationNotFoundException.class,
                () -> messageService.getConversationMessages(99L, Pageable.unpaged()));
    }

    @Test
    void getUnreadCount_shouldReturnRepositoryCount() {
        when(conversationRepository.findById(10L))
                .thenReturn(Optional.of(Conversation.builder().id(10L).build()));
        when(messageRepository.countByConversationIdAndIsReadFalse(10L)).thenReturn(3L);

        long result = messageService.getUnreadCount(10L);

        assertEquals(3L, result);
    }

    @Test
    void getOrCreateConversation_shouldReturnExistingConversation() {
        Conversation conversation = Conversation.builder()
                .id(10L)
                .senderId(1L)
                .receiverId(2L)
                .build();
        ConversationResponse response = ConversationResponse.builder().id(10L).build();

        when(conversationRepository.findBetweenUsers(1L, 2L)).thenReturn(Optional.of(conversation));
        when(modelMapper.map(conversation, ConversationResponse.class)).thenReturn(response);

        ConversationResponse result = messageService.getOrCreateConversation(
                1L, "one@example.com", 2L, "two@example.com");

        assertEquals(10L, result.getId());
    }
}
