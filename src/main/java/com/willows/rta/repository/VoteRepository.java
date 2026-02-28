// ===== FILE 3: VoteRepository.java =====
package com.willows.rta.repository;

import com.willows.rta.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    
    // Find all votes for a poll
    List<Vote> findByPollId(Long pollId);
    
    // Find all votes for a specific option
    List<Vote> findByOptionId(Long optionId);
    
    // Find votes by a specific member
    List<Vote> findByMemberId(Long memberId);
    
    // Check if member has voted on a poll
    boolean existsByPollIdAndMemberId(Long pollId, Long memberId);
    
    // Get member's vote for a poll (for single choice)
    Optional<Vote> findByPollIdAndMemberId(Long pollId, Long memberId);
    
    // Get all member's votes for a poll (for multiple choice)
    List<Vote> findAllByPollIdAndMemberId(Long pollId, Long memberId);
    
    // Count votes for a poll
    long countByPollId(Long pollId);
    
    // Count votes for an option
    long countByOptionId(Long optionId);
    
    // Delete all votes for a poll
    void deleteByPollId(Long pollId);
    
    // Delete member's votes from a poll (for revoting)
    void deleteByPollIdAndMemberId(Long pollId, Long memberId);
}
