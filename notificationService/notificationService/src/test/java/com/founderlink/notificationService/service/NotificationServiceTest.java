package com.founderlink.notificationService.service;

import com.founderlink.notificationService.entity.Notification;
import com.founderlink.notificationService.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void saveNotification_shouldPersistUnreadNotification() {
        notificationService.saveNotification("user@example.com", 1L, "MESSAGE", "New message");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertEquals("user@example.com", saved.getUserEmail());
        assertEquals(1L, saved.getUserId());
        assertEquals("MESSAGE", saved.getType());
        assertEquals("New message", saved.getMessage());
        assertFalse(saved.isRead());
    }

    @Test
    void getUnreadNotification_shouldReturnRepositoryResult() {
        List<Notification> notifications = List.of(Notification.builder().userEmail("user@example.com").build());
        when(notificationRepository.findByUserEmailAndIsReadFalse("user@example.com")).thenReturn(notifications);

        List<Notification> result = notificationService.getUnreadNotification("user@example.com");

        assertEquals(1, result.size());
    }
}
