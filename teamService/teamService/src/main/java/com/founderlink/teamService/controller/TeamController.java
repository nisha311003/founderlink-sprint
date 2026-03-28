package com.founderlink.teamService.controller;

import com.founderlink.teamService.dto.*;
import com.founderlink.teamService.exception.TeamNotFoundException;
import com.founderlink.teamService.service.ITeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping({"/api/teams", "/api/teams/"})
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "APIs for managing team invitations and memberships")
public class TeamController {

    private final ITeamService teamService;

    @PostMapping("/invite")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    @Operation(summary = "Send team invitation", description = "Allows a founder to send an invitation to a co-founder to join their startup team")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitation sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<TeamResponse> sendInvite(
            @Parameter(description = "ID of the founder sending the invitation") @RequestHeader("X-User-Id") Long founderId,
            @Parameter(description = "Email of the founder sending the invitation") @RequestHeader("X-User-Email") String founderEmail,
            @Valid @RequestBody InviteRequest request) {

        return ResponseEntity.ok(teamService.sendInvite(founderId, founderEmail, request));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('ROLE_COFOUNDER')")
    @Operation(summary = "Accept team invitation", description = "Allows a co-founder to accept a team invitation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitation accepted successfully"),
        @ApiResponse(responseCode = "404", description = "Invitation not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<TeamResponse> acceptInvite(
            @Parameter(description = "ID of the invitation") @PathVariable Long id,
            @Parameter(description = "ID of the user accepting the invitation") @RequestHeader("X-User-Id") Long userId) throws TeamNotFoundException {

        return ResponseEntity.ok(teamService.acceptInvite(id, userId));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_COFOUNDER')")
    @Operation(summary = "Reject team invitation", description = "Allows a co-founder to reject a team invitation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitation rejected successfully"),
        @ApiResponse(responseCode = "404", description = "Invitation not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<TeamResponse> rejectInvite(
            @Parameter(description = "ID of the invitation") @PathVariable Long id,
            @Parameter(description = "ID of the user rejecting the invitation") @RequestHeader("X-User-Id") Long userId) throws TeamNotFoundException {

        return ResponseEntity.ok(teamService.rejectInvite(id, userId));
    }

    @GetMapping("/startup/{startupId}")
    @Operation(summary = "Get team by startup", description = "Retrieves all team members and invitations for a specific startup")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Startup not found")
    })
    public ResponseEntity<Page<TeamResponse>> getTeamByStartup(
            @Parameter(description = "ID of the startup") @PathVariable Long startupId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(teamService.getTeamByStartup(startupId, pageable));
    }

    @GetMapping("/startup/{startupId}/members")
    @Operation(summary = "Get accepted team members", description = "Retrieves only accepted team members for a specific startup")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Team members retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Startup not found")
    })
    public ResponseEntity<StandardApiResponse<Page<TeamMemberResponse>>> getAcceptedTeam(
            @Parameter(description = "ID of the startup") @PathVariable Long startupId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").descending());
        Page<TeamMemberResponse> data = teamService.getAcceptedTeamByStartup(startupId, pageable);

        StandardApiResponse<Page<TeamMemberResponse>> response = StandardApiResponse.<Page<TeamMemberResponse>>builder()
                .success(true)
                .message("Team members retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-invitations")
    @PreAuthorize("hasAuthority('ROLE_COFOUNDER')")
    @Operation(summary = "Get my invitations", description = "Retrieves all invitations sent to the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitations retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<StandardApiResponse<Page<TeamInvitationResponse>>> getMyInvitations(
            @Parameter(description = "ID of the user") @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TeamInvitationResponse> data = teamService.getMyInvitations(userId, pageable);

        StandardApiResponse<Page<TeamInvitationResponse>> response = StandardApiResponse.<Page<TeamInvitationResponse>>builder()
                .success(true)
                .message("Invitations retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending-invitations")
    @PreAuthorize("hasAuthority('ROLE_COFOUNDER')")
    @Operation(summary = "Get pending invitations", description = "Retrieves pending invitations for the current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pending invitations retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<StandardApiResponse<Page<TeamInvitationResponse>>> getPendingInvitations(
            @Parameter(description = "ID of the user") @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TeamInvitationResponse> data = teamService.getPendingInvitations(userId, pageable);

        StandardApiResponse<Page<TeamInvitationResponse>> response = StandardApiResponse.<Page<TeamInvitationResponse>>builder()
                .success(true)
                .message("Pending invitations retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sent-invites")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    @Operation(summary = "Get sent invites", description = "Retrieves invitations sent by the current founder")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sent invites retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<StandardApiResponse<Page<TeamInvitationResponse>>> getSentInvites(
            @Parameter(description = "ID of the founder") @RequestHeader("X-User-Id") Long founderId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TeamInvitationResponse> data = teamService.getInvitesSentByFounder(founderId, pageable);

        StandardApiResponse<Page<TeamInvitationResponse>> response = StandardApiResponse.<Page<TeamInvitationResponse>>builder()
                .success(true)
                .message("Sent invites retrieved successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_FOUNDER')")
    @Operation(summary = "Delete invitation", description = "Allows a founder to delete one of their sent invitations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Invitation deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Invitation not found"),
        @ApiResponse(responseCode = "403", description = "Access denied or not the owner")
    })
    public ResponseEntity<Void> deleteInvite(
            @Parameter(description = "ID of the invitation") @PathVariable Long id,
            @Parameter(description = "ID of the founder") @RequestHeader("X-User-Id") Long founderId) throws TeamNotFoundException {

        teamService.deleteInvite(id, founderId);
        return ResponseEntity.noContent().build();
    }


}
