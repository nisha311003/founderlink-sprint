package com.founderlink.teamService.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // startup details
    @Column(nullable = false)
    private Long startupId;

    @Column(nullable = false)
    private String startupName;

    // founder who sent invite
    @Column(nullable = false)
    private Long founderId;

    @Column(nullable = false)
    private String founderEmail;

    // invited co-founder
    @Column(nullable = false)
    private Long invitedUserId;

    @Column(nullable = false)
    private String invitedUserEmail;

    // role offered
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamRole role;

    // invitation status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime joinedAt;
}
