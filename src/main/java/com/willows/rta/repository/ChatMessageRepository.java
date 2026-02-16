package com.willows.rta.repository;

import com.willows.rta.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // Get last 100 messages ordered by time (newest first for display)
    @Query("SELECT c FROM ChatMessage c ORDER BY c.sentAt DESC")
    List<ChatMessage> findLast100Messages();
    
    // Get messages after a specific ID (for polling updates)
    @Query("SELECT c FROM ChatMessage c WHERE c.id > ?1 ORDER BY c.sentAt ASC")
    List<ChatMessage> findMessagesAfterId(Long lastId);
}
