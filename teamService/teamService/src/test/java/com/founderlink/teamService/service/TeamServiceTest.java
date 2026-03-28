package com.founderlink.teamService.service;

import com.founderlink.teamService.dto.InviteRequest;
import com.founderlink.teamService.dto.TeamResponse;
import com.founderlink.teamService.dto.UserDto;
import com.founderlink.teamService.entity.InvitationStatus;
import com.founderlink.teamService.entity.Team;
import com.founderlink.teamService.entity.TeamRole;
import com.founderlink.teamService.exception.TeamNotFoundException;
import com.founderlink.teamService.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TeamService teamService;

    @Test
    void sendInvite_shouldValidateUserAndPersistPendingInvite() {
        InviteRequest request = new InviteRequest();
        request.setStartupId(10L);
        request.setStartupName("FounderLink");
        request.setInvitedUserId(2L);
        request.setInvitedUserEmail("cofounder@example.com");
        request.setRole("cto");

        Team team = Team.builder()
                .startupId(10L)
                .founderId(1L)
                .invitedUserId(2L)
                .role(TeamRole.CTO)
                .status(InvitationStatus.PENDING)
                .build();
        TeamResponse response = TeamResponse.builder()
                .startupId(10L)
                .invitedUserId(2L)
                .role(TeamRole.CTO)
                .status(InvitationStatus.PENDING)
                .build();

        when(teamRepository.findByStartupIdAndInvitedUserId(10L, 2L)).thenReturn(Optional.empty());
        when(restTemplate.getForEntity("http://userService/api/users/2", UserDto.class))
                .thenReturn(new ResponseEntity<>(new UserDto(2L, "cofounder@example.com"), HttpStatus.OK));
        when(modelMapper.map(any(Team.class), eq(TeamResponse.class))).thenReturn(response);

        TeamResponse result = teamService.sendInvite(1L, "founder@example.com", request);

        assertEquals(InvitationStatus.PENDING, result.getStatus());
        assertEquals(TeamRole.CTO, result.getRole());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void acceptInvite_shouldUpdateStatusAndJoinedAt() throws TeamNotFoundException {
        Team team = Team.builder()
                .id(1L)
                .invitedUserId(2L)
                .status(InvitationStatus.PENDING)
                .build();
        TeamResponse response = TeamResponse.builder()
                .id(1L)
                .status(InvitationStatus.ACCEPTED)
                .build();

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(modelMapper.map(team, TeamResponse.class)).thenReturn(response);

        TeamResponse result = teamService.acceptInvite(1L, 2L);

        assertEquals(InvitationStatus.ACCEPTED, team.getStatus());
        assertNotNull(team.getJoinedAt());
        assertEquals(InvitationStatus.ACCEPTED, result.getStatus());
        verify(teamRepository).save(team);
    }

    @Test
    void rejectInvite_shouldThrowWhenUserDoesNotOwnInvite() {
        Team team = Team.builder()
                .id(1L)
                .invitedUserId(2L)
                .status(InvitationStatus.PENDING)
                .build();

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> teamService.rejectInvite(1L, 99L));

        assertEquals("You can only reject your own invitation", exception.getMessage());
    }

    @Test
    void deleteInvite_shouldThrowWhenInviteIsMissing() {
        when(teamRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(TeamNotFoundException.class, () -> teamService.deleteInvite(100L, 1L));
    }
}
