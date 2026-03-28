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
public class TeamInvitationResponse {

    private Long id;
    private String startupName;
    private String founderEmail;
    private TeamRole role;
    private InvitationStatus status;
    private LocalDateTime createdAt;
}
