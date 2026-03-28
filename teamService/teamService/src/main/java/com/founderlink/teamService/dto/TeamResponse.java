package com.founderlink.teamService.dto;

import com.founderlink.teamService.entity.InvitationStatus;
import com.founderlink.teamService.entity.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeamResponse {

    private Long id;
    private Long startupId;
    private String startupName;
    private Long founderId;
    private String founderEmail;
    private Long invitedUserId;
    private String invitedUserEmail;
    private TeamRole role;
    private InvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime joinedAt;
}
