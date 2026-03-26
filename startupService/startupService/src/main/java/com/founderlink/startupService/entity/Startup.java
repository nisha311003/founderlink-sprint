package com.founderlink.startupService.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "startups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String industry;

    @Column(nullable = false, length = 1000)
    private String problemStatement;

    @Column(nullable = false, length = 1000)
    private String solution;

    @Column(nullable = false)
    private Double fundingGoal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StartupStage stage;


    private Long founderId;

    private String founderEmail;

    private boolean approved = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
