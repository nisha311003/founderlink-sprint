package com.founderlink.teamService.service;

import com.founderlink.teamService.dto.InviteRequest;
import com.founderlink.teamService.dto.TeamInvitationResponse;
import com.founderlink.teamService.dto.TeamMemberResponse;
import com.founderlink.teamService.dto.TeamResponse;
import com.founderlink.teamService.exception.TeamNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITeamService {

    TeamResponse sendInvite(
            Long founderId,
            String founderEmail,
            InviteRequest request);

    TeamResponse acceptInvite(Long inviteId, Long userId) throws TeamNotFoundException;

    TeamResponse rejectInvite(Long inviteId, Long userId) throws TeamNotFoundException;

    void deleteInvite(Long inviteId, Long founderId) throws TeamNotFoundException;

    Page<TeamResponse> getTeamByStartup(
            Long startupId, Pageable pageable);

    Page<TeamMemberResponse> getAcceptedTeamByStartup(
            Long startupId, Pageable pageable);

    Page<TeamInvitationResponse> getMyInvitations(
            Long userId, Pageable pageable);

    Page<TeamInvitationResponse> getPendingInvitations(
            Long userId, Pageable pageable);

    Page<TeamInvitationResponse> getInvitesSentByFounder(
            Long founderId, Pageable pageable);
}
