package com.founderlink.notificationService.consumer;

import com.founderlink.notificationService.dto.MessageSentEvent;
import com.founderlink.notificationService.service.EmailService;
import com.founderlink.notificationService.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class MessageNotificationConsumer {

    private final EmailService emailService;
    private final NotificationService notificationService;

    @RabbitListener(queues = "notification.queue")
    public void handleMessageSent(MessageSentEvent event){
        log.info("--- NEW MESSAGE EVENT RECEIVED ---");
        log.info("From userId: {} to userId: {}", event.getSenderId(), event.getReceiverId());

        notificationService.saveNotification(
                event.getReceiverEmail(),
                event.getReceiverId(),
                "New_Message",
                "You have a new message from "+ event.getSenderEmail()+
                        ": "+ event.getContent().substring(0, Math.min(event.getContent().length(), 50))
        );

        emailService.sendEmail(
                event.getReceiverEmail(),
                "New Message on FounderLink",
                "Hello!\n\n"+
                        "You have received a new message from "+
                        event.getSenderEmail()+".\n\n"+
                        "Message: "+event.getContent()+"\n\n"+
                        "Login to FounderLink to reply.\n\n"+
                        "Team FounderLink"

        );
    }
}
