package com.founderlink.teamService.repository;

import com.founderlink.teamService.entity.InvitationStatus;
import com.founderlink.teamService.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    // get all team members of a startup
    Page<Team> findByStartupId(Long startupId, Pageable pageable);

    // get all invitations sent by a founder
    Page<Team> findByFounderId(Long founderId, Pageable pageable);

    // get all invitations received by a user
    Page<Team> findByInvitedUserId(Long invitedUserId, Pageable pageable);

    // get pending invitations for a user
    Page<Team> findByInvitedUserIdAndStatus(Long invitedUserId,
            InvitationStatus status,
            Pageable pageable);

    // check if already invited
    Optional<Team> findByStartupIdAndInvitedUserId(Long startupId, Long invitedUserId);

    // get accepted members of a startup
    Page<Team> findByStartupIdAndStatus(Long startupId,
            InvitationStatus status,
            Pageable pageable);
}
