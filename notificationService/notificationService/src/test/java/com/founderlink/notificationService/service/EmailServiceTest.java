package com.founderlink.notificationService.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendEmail_shouldBuildAndSendMailMessage() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@founderlink.com");

        emailService.sendEmail("user@example.com", "Subject", "Body");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("noreply@founderlink.com", message.getFrom());
        assertEquals("Subject", message.getSubject());
        assertEquals("Body", message.getText());
        assertEquals("user@example.com", message.getTo()[0]);
    }

    @Test
    void sendEmail_shouldSwallowMailerExceptions() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@founderlink.com");
        doThrow(new RuntimeException("SMTP down")).when(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        emailService.sendEmail("user@example.com", "Subject", "Body");
    }
}
