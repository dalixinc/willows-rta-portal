// ============================================
// VOTING SYSTEM REPOSITORIES
// Create these 3 files in: src/main/java/com/willows/rta/repository/
// ============================================

// ===== FILE 1: PollRepository.java =====
package com.willows.rta.repository;

import com.willows.rta.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    
    // Find polls by status
    List<Poll> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find active polls
    List<Poll> findByStatusOrderByOpenAtDesc(String status);
    
    // Find all polls ordered by creation date
    List<Poll> findAllByOrderByCreatedAtDesc();
    
    // Find polls created by specific user
    List<Poll> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);
    
    // Find polls that should auto-close (deadline passed)
    @Query("SELECT p FROM Poll p WHERE p.status = 'ACTIVE' AND p.closeAt <= :now")
    List<Poll> findExpiredActivePolls(LocalDateTime now);
    
    // Count active polls
    long countByStatus(String status);
}