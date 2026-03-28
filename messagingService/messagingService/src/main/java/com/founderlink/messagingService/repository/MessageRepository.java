package com.founderlink.messagingService.repository;

import com.founderlink.messagingService.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {

    // get all messages in a conversation
    Page<Message> findByConversationId(
            Long conversationId, Pageable pageable);

    // count unread messages in a conversation
    long countByConversationIdAndIsReadFalse(Long conversationId);
}
