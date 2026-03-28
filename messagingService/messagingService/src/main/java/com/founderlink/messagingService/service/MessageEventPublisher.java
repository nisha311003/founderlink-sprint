package com.founderlink.messagingService.service;

import com.founderlink.messagingService.dto.MessageSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(MessageSentEvent event){
        log.info("=== PUBLISHING EVENT TO RABBITMQ ===");
        log.info("Receiver: {}", event.getReceiverEmail());
        rabbitTemplate.convertAndSend(
                "messaging.exchange", "message.sent", event);
        log.info("=== EVENT PUBLISHED SUCCESSFULLY ===");
        rabbitTemplate.convertAndSend("messaging.exchange", "message.sent", event);
    }
}
