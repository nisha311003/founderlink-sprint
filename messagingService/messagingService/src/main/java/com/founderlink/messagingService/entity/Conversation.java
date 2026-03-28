package com.founderlink.messagingService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // two participants
    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String senderEmail;

    @Column(nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private String receiverEmail;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;


}
