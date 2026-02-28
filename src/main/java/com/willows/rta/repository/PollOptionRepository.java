// ===== FILE 2: PollOptionRepository.java =====
package com.willows.rta.repository;

import com.willows.rta.model.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
    
    // Find options for a specific poll
    List<PollOption> findByPollIdOrderByDisplayOrder(Long pollId);
    
    // Delete all options for a poll
    void deleteByPollId(Long pollId);
}