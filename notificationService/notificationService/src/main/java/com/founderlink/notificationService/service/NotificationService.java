package com.founderlink.notificationService.service;

import com.founderlink.notificationService.entity.Notification;
import com.founderlink.notificationService.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void saveNotification(String userEmail, Long userId, String type, String message){
        Notification notification = Notification.builder()
                .userId(userId)
                .userEmail(userEmail)
                .type(type)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.info("Notification saved for: {}", userEmail);
    }

    public List<Notification> getUserNotification(String email){
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(email);
    }

    public List<Notification> getUnreadNotification(String email){
        return notificationRepository.findByUserEmailAndIsReadFalse(email);
    }
}
