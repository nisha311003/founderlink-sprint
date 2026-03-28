package com.founderlink.messagingService.repository;

import com.founderlink.messagingService.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // find conversation between two users
    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.senderId = :userId1 AND c.receiverId = :userId2) OR " +
            "(c.senderId = :userId2 AND c.receiverId = :userId1)")
    Optional<Conversation> findBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    // get all conversations of a user
    @Query("SELECT c FROM Conversation c WHERE " +
            "c.senderId = :userId OR c.receiverId = :userId " +
            "ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findAllByUserId(
            @Param("userId") Long userId,
            Pageable pageable);
}
