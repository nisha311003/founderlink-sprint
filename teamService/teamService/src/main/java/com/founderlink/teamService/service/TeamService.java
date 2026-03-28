package com.founderlink.teamService.service;

import com.founderlink.teamService.dto.*;
import com.founderlink.teamService.entity.InvitationStatus;
import com.founderlink.teamService.entity.Team;
import com.founderlink.teamService.entity.TeamRole;
import com.founderlink.teamService.exception.TeamNotFoundException;
import com.founderlink.teamService.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TeamService implements ITeamService{

    private final TeamRepository teamRepository;
    private final ModelMapper modelMapper;
    private final RestTemplate restTemplate;

    @Override
    public TeamResponse sendInvite(Long founderId, String founderEmail, InviteRequest request) {
        teamRepository.findByStartupIdAndInvitedUserId(
                        request.getStartupId(),
                        request.getInvitedUserId())
                .ifPresent(t -> {
                    throw new RuntimeException(
                            "User already invited to this startup");
                });

        // Validate user ID and email match
        try {
            String userServiceUrl = "http://userService/api/users/" + request.getInvitedUserId();
            ResponseEntity<UserDto> response = restTemplate.getForEntity(userServiceUrl, UserDto.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                UserDto user = response.getBody();
                if (user == null || !user.getEmail().equals(request.getInvitedUserEmail())) {
                    throw new RuntimeException("User ID and email do not match");
                }
            } else {
                throw new RuntimeException("User not found");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to validate user: " + e.getMessage());
        }

        TeamRole role;
        try {
            role = TeamRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid role. Use: CTO, CPO, MARKETING_HEAD, " +
                            "ENGINEERING_LEAD, DESIGN_LEAD, " +
                            "BUSINESS_DEVELOPMENT, FINANCE_HEAD");
        }

        Team team = Team.builder()
                .startupId(request.getStartupId())
                .startupName(request.getStartupName())
                .founderId(founderId)
                .founderEmail(founderEmail)
                .invitedUserId(request.getInvitedUserId())
                .invitedUserEmail(request.getInvitedUserEmail())
                .role(role)
                .status(InvitationStatus.PENDING)
                .build();

        teamRepository.save(team);
        return modelMapper.map(team, TeamResponse.class);
    }

    @Override
    public TeamResponse acceptInvite(Long inviteId, Long userId) throws TeamNotFoundException {
        Team team = teamRepository.findById(inviteId)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Invitation not found with id: " + inviteId));

        if (!team.getInvitedUserId().equals(userId)) {
            throw new RuntimeException(
                    "You can only accept your own invitation");
        }

        if (team.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException(
                    "Only PENDING invitations can be accepted");
        }

        team.setStatus(InvitationStatus.ACCEPTED);
        team.setJoinedAt(LocalDateTime.now());
        teamRepository.save(team);
        return modelMapper.map(team, TeamResponse.class);
    }

    @Override
    public TeamResponse rejectInvite(Long inviteId, Long userId) throws TeamNotFoundException {
        Team team = teamRepository.findById(inviteId)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Invitation not found with id: " + inviteId));

        if (!team.getInvitedUserId().equals(userId)) {
            throw new RuntimeException(
                    "You can only reject your own invitation");
        }

        if (team.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException(
                    "Only PENDING invitations can be rejected");
        }

        team.setStatus(InvitationStatus.REJECTED);
        teamRepository.save(team);
        return modelMapper.map(team, TeamResponse.class);
    }

    @Override
    public void deleteInvite(Long inviteId, Long founderId) throws TeamNotFoundException {
        Team team = teamRepository.findById(inviteId)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Invitation not found with id: " + inviteId));

        if (!team.getFounderId().equals(founderId)) {
            throw new RuntimeException(
                    "You can only delete your own invitations");
        }

        teamRepository.delete(team);
    }

    @Override
    public Page<TeamResponse> getTeamByStartup(Long startupId, Pageable pageable) {
        return teamRepository.findByStartupId(startupId, pageable)
                .map(team -> modelMapper.map(team, TeamResponse.class));
    }

    @Override
    public Page<TeamMemberResponse> getAcceptedTeamByStartup(Long startupId, Pageable pageable) {
        return teamRepository.findByStartupIdAndStatus(
                        startupId, InvitationStatus.ACCEPTED, pageable)
                .map(team -> modelMapper.map(team, TeamMemberResponse.class));
    }

    @Override
    public Page<TeamInvitationResponse> getMyInvitations(Long userId, Pageable pageable) {
        return teamRepository.findByInvitedUserId(
                        userId,  pageable)
                .map(team -> modelMapper.map(team, TeamInvitationResponse.class));
    }

    @Override
    public Page<TeamInvitationResponse> getPendingInvitations(Long userId, Pageable pageable) {
        return teamRepository.findByInvitedUserIdAndStatus(
                        userId, InvitationStatus.PENDING, pageable)
                .map(team -> modelMapper.map(team, TeamInvitationResponse.class));
    }

    @Override
    public Page<TeamInvitationResponse> getInvitesSentByFounder(Long founderId, Pageable pageable) {
        return teamRepository.findByFounderId(
                        founderId, pageable)
                .map(team -> modelMapper.map(team, TeamInvitationResponse.class));
    }
}
