package com.willows.rta.service;

import com.willows.rta.model.Poll;
import com.willows.rta.model.PollOption;
import com.willows.rta.model.Vote;
import com.willows.rta.repository.PollRepository;
import com.willows.rta.repository.MemberRepository;
import com.willows.rta.repository.PollOptionRepository;
import com.willows.rta.repository.VoteRepository;

import org.apache.poi.ss.formula.functions.Replace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing polls
 */
@Service
public class PollService {

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final VoteRepository voteRepository;
    private final MemberRepository memberRepository;

@Autowired
public PollService(PollRepository pollRepository, 
                  PollOptionRepository pollOptionRepository,
                  VoteRepository voteRepository,
                  MemberRepository memberRepository) {
    this.pollRepository = pollRepository;
    this.pollOptionRepository = pollOptionRepository;
    this.voteRepository = voteRepository;
    this.memberRepository = memberRepository;
}

    /**
     * Get all polls ordered by creation date
     */
    public List<Poll> getAllPolls() {
        return pollRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get active polls
     */
    public List<Poll> getActivePolls() {
        return pollRepository.findByStatusOrderByOpenAtDesc("ACTIVE");
    }

    /**
     * Get closed polls
     */
    public List<Poll> getClosedPolls() {
        return pollRepository.findByStatusOrderByCreatedAtDesc("CLOSED");
    }

    /**
     * Get draft polls
     */
    public List<Poll> getDraftPolls() {
        return pollRepository.findByStatusOrderByCreatedAtDesc("DRAFT");
    }

    /**
     * Get poll by ID
     */
    public Optional<Poll> getPollById(Long id) {
        return pollRepository.findById(id);
    }

    /**
     * Create new poll
     */
    @Transactional
    public Poll createPoll(Poll poll, List<String> optionTexts, Long createdById) {
        // Set metadata
        poll.setCreatedById(createdById);
        poll.setCreatedAt(LocalDateTime.now());
        poll.setStatus("DRAFT");
        
        // Save poll first
        Poll savedPoll = pollRepository.save(poll);
        
        // Add options
        if (optionTexts != null && !optionTexts.isEmpty()) {
            for (int i = 0; i < optionTexts.size(); i++) {
                PollOption option = new PollOption(optionTexts.get(i), i + 1);
                option.setPoll(savedPoll);
                pollOptionRepository.save(option);
            }
        }
        
        return savedPoll;
    }

    /**
     * Update poll (only if draft)
     */
    @Transactional
    public Poll updatePoll(Long pollId, Poll updatedPoll, List<String> optionTexts) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (!"DRAFT".equals(poll.getStatus())) {
            throw new RuntimeException("Cannot edit poll that is not in draft status");
        }
        
        // Update poll details
        poll.setTitle(updatedPoll.getTitle());
        poll.setDescription(updatedPoll.getDescription());
        poll.setPollType(updatedPoll.getPollType());
        poll.setVisibility(updatedPoll.getVisibility());
        poll.setResultsVisibility(updatedPoll.getResultsVisibility());
        poll.setCloseAt(updatedPoll.getCloseAt());
        poll.setAllowRevote(updatedPoll.getAllowRevote());
        
        // Update options if provided
        if (optionTexts != null) {
            // Delete existing options
            pollOptionRepository.deleteByPollId(pollId);
            
            // Add new options
            for (int i = 0; i < optionTexts.size(); i++) {
                PollOption option = new PollOption(optionTexts.get(i), i + 1);
                option.setPoll(poll);
                pollOptionRepository.save(option);
            }
        }
        
        return pollRepository.save(poll);
    }

    /**
     * Publish poll (make it active)
     */
    @Transactional
    public Poll publishPoll(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (!"DRAFT".equals(poll.getStatus())) {
            throw new RuntimeException("Poll is already published");
        }
        
        // Verify poll has options
        List<PollOption> options = pollOptionRepository.findByPollIdOrderByDisplayOrder(pollId);
        if (options.isEmpty()) {
            throw new RuntimeException("Cannot publish poll without options");
        }
        
        poll.setStatus("ACTIVE");
        poll.setOpenAt(LocalDateTime.now());
        
        return pollRepository.save(poll);
    }

    /**
     * Close poll manually
     */
    @Transactional
    public Poll closePoll(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if ("CLOSED".equals(poll.getStatus())) {
            throw new RuntimeException("Poll is already closed");
        }
        
        poll.setStatus("CLOSED");
        poll.setClosedManually(true);
        
        return pollRepository.save(poll);
    }

    /**
     * Reopen poll
     */
    @Transactional
    public Poll reopenPoll(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (!"CLOSED".equals(poll.getStatus())) {
            throw new RuntimeException("Only closed polls can be reopened");
        }
        
        poll.setStatus("ACTIVE");
        poll.setClosedManually(false);
        poll.setCloseAt(null); // Remove deadline
        
        return pollRepository.save(poll);
    }

    /**
     * Delete poll (only if draft or closed with no votes)
     */
    @Transactional
    public void deletePoll(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        // Can delete draft polls anytime
        if ("DRAFT".equals(poll.getStatus())) {
            pollRepository.deleteById(pollId);
            return;
        }
        
        // Can delete closed polls only if no votes
        if ("CLOSED".equals(poll.getStatus())) {
            long voteCount = voteRepository.countByPollId(pollId);
            if (voteCount > 0) {
                throw new RuntimeException("Cannot delete poll with votes. Close it instead.");
            }
            pollRepository.deleteById(pollId);
            return;
        }
        
        throw new RuntimeException("Cannot delete active poll. Close it first.");
    }

    /**
     * Auto-close expired polls
     * Called by scheduled task
     */
    @Transactional
    public void autoCloseExpiredPolls() {
        List<Poll> expiredPolls = pollRepository.findExpiredActivePolls(LocalDateTime.now());
        
        for (Poll poll : expiredPolls) {
            poll.setStatus("CLOSED");
            poll.setClosedManually(false);
            pollRepository.save(poll);
        }
    }

    /**
     * Get poll statistics
     */
    public PollStats getPollStats(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        long totalVotes = voteRepository.countByPollId(pollId);
        long totalMembers = getTotalActiveMembers(); // You'll need to inject MemberRepository
        
        return new PollStats(
            pollId,
            poll.getTitle(),
            totalVotes,
            totalMembers,
            poll.getStatus()
        );
    }

  
    private long getTotalActiveMembers() {
        return memberRepository.countByMembershipStatus("ACTIVE");
    }

    /**
     * Inner class for poll statistics
     */
    public static class PollStats {
        private Long pollId;
        private String title;
        private long totalVotes;
        private long totalMembers;
        private double participationRate;
        private String status;

        public PollStats(Long pollId, String title, long totalVotes, long totalMembers, String status) {
            this.pollId = pollId;
            this.title = title;
            this.totalVotes = totalVotes;
            this.totalMembers = totalMembers;
            this.participationRate = totalMembers > 0 ? (totalVotes * 100.0 / totalMembers) : 0;
            this.status = status;
        }

        // Getters
        public Long getPollId() { return pollId; }
        public String getTitle() { return title; }
        public long getTotalVotes() { return totalVotes; }
        public long getTotalMembers() { return totalMembers; }
        public double getParticipationRate() { return participationRate; }
        public String getStatus() { return status; }
        public String getFormattedParticipationRate() {
            return String.format("%.0f%%", participationRate);
        }
    }
}
