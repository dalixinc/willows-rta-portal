package com.willows.rta.service;

import com.willows.rta.model.Member;
import com.willows.rta.model.Poll;
import com.willows.rta.model.PollOption;
import com.willows.rta.model.Vote;
import com.willows.rta.repository.PollRepository;
import com.willows.rta.repository.PollOptionRepository;
import com.willows.rta.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling voting operations
 */
@Service
public class VotingService {

    private final VoteRepository voteRepository;
    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final MemberService memberService;

    @Autowired
    public VotingService(VoteRepository voteRepository,
                        PollRepository pollRepository,
                        PollOptionRepository pollOptionRepository,
                        MemberService memberService) {
        this.voteRepository = voteRepository;
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.memberService = memberService;
    }

    /**
     * Cast vote(s) on a poll
     * For single choice: optionIds should have 1 element
     * For multiple choice: optionIds can have multiple elements
     */
    @Transactional
    public void castVote(Long pollId, List<Long> optionIds, Long memberId) {
        // Validate poll exists and is active
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (!"ACTIVE".equals(poll.getStatus())) {
            throw new RuntimeException("Poll is not active");
        }
        
        // Check if poll is expired
        if (poll.isExpired()) {
            throw new RuntimeException("Poll has expired");
        }
        
        // Validate member
        Member member = memberService.getMemberById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        // Check if already voted
        boolean hasVoted = voteRepository.existsByPollIdAndMemberId(pollId, memberId);
        
        if (hasVoted && !poll.getAllowRevote()) {
            throw new RuntimeException("You have already voted on this poll");
        }
        
        // If revoting is allowed, delete previous votes
        if (hasVoted && poll.getAllowRevote()) {
            voteRepository.deleteByPollIdAndMemberId(pollId, memberId);
        }
        
        // Validate option count based on poll type
        if ("SINGLE_CHOICE".equals(poll.getPollType()) && optionIds.size() != 1) {
            throw new RuntimeException("Single choice polls require exactly one option");
        }
        
        if ("MULTIPLE_CHOICE".equals(poll.getPollType()) && optionIds.isEmpty()) {
            throw new RuntimeException("You must select at least one option");
        }
        
        // Validate all options belong to this poll and cast votes
        boolean isAnonymous = "ANONYMOUS".equals(poll.getVisibility());
        
        for (Long optionId : optionIds) {
            PollOption option = pollOptionRepository.findById(optionId)
                    .orElseThrow(() -> new RuntimeException("Option not found"));
            
            if (!option.getPoll().getId().equals(pollId)) {
                throw new RuntimeException("Option does not belong to this poll");
            }
            
            // Create vote
            Vote vote = new Vote(poll, option, member, isAnonymous);
            voteRepository.save(vote);
        }
    }

    /**
     * Check if member has voted on poll
     */
    public boolean hasVoted(Long pollId, Long memberId) {
        return voteRepository.existsByPollIdAndMemberId(pollId, memberId);
    }

    /**
     * Get member's vote(s) for a poll
     */
    public List<Vote> getMemberVotes(Long pollId, Long memberId) {
        return voteRepository.findAllByPollIdAndMemberId(pollId, memberId);
    }

    /**
     * Get all votes for a poll
     */
    public List<Vote> getPollVotes(Long pollId) {
        return voteRepository.findByPollId(pollId);
    }

    /**
     * Get vote count for an option
     */
    public long getOptionVoteCount(Long optionId) {
        return voteRepository.countByOptionId(optionId);
    }

    /**
     * Get poll results with vote counts per option
     */
    public PollResults getPollResults(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        List<PollOption> options = pollOptionRepository.findByPollIdOrderByDisplayOrder(pollId);
        List<Vote> allVotes = voteRepository.findByPollId(pollId);
        
        List<OptionResult> optionResults = new ArrayList<>();
        
        for (PollOption option : options) {
            long voteCount = voteRepository.countByOptionId(option.getId());
            double percentage = allVotes.size() > 0 ? (voteCount * 100.0 / allVotes.size()) : 0;
            
            // Get voters (only for open polls)
            List<String> voters = new ArrayList<>();
            if ("OPEN".equals(poll.getVisibility())) {
                List<Vote> optionVotes = voteRepository.findByOptionId(option.getId());
                for (Vote vote : optionVotes) {
                    voters.add(vote.getMember().getFullName());
                }
            }
            
            optionResults.add(new OptionResult(
                option.getId(),
                option.getOptionText(),
                voteCount,
                percentage,
                voters
            ));
        }
        
        return new PollResults(
            pollId,
            poll.getTitle(),
            poll.getVisibility(),
            allVotes.size(),
            optionResults
        );
    }

    /**
     * Get list of members who voted (for open polls only)
     */
    public List<String> getVoterNames(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if ("ANONYMOUS".equals(poll.getVisibility())) {
            throw new RuntimeException("Cannot get voter names for anonymous polls");
        }
        
        List<Vote> votes = voteRepository.findByPollId(pollId);
        List<String> voterNames = new ArrayList<>();
        
        for (Vote vote : votes) {
            String name = vote.getMember().getFullName();
            if (!voterNames.contains(name)) {
                voterNames.add(name);
            }
        }
        
        return voterNames;
    }

    /**
     * Inner class for option results
     */
    public static class OptionResult {
        private Long optionId;
        private String optionText;
        private long voteCount;
        private double percentage;
        private List<String> voters;

        public OptionResult(Long optionId, String optionText, long voteCount, double percentage, List<String> voters) {
            this.optionId = optionId;
            this.optionText = optionText;
            this.voteCount = voteCount;
            this.percentage = percentage;
            this.voters = voters;
        }

        public Long getOptionId() { return optionId; }
        public String getOptionText() { return optionText; }
        public long getVoteCount() { return voteCount; }
        public double getPercentage() { return percentage; }
        public List<String> getVoters() { return voters; }
        public String getFormattedPercentage() {
            return String.format("%.0f%%", percentage);
        }
    }

    /**
     * Inner class for poll results
     */
    public static class PollResults {
        private Long pollId;
        private String title;
        private String visibility;
        private long totalVotes;
        private List<OptionResult> options;

        public PollResults(Long pollId, String title, String visibility, long totalVotes, List<OptionResult> options) {
            this.pollId = pollId;
            this.title = title;
            this.visibility = visibility;
            this.totalVotes = totalVotes;
            this.options = options;
        }

        public Long getPollId() { return pollId; }
        public String getTitle() { return title; }
        public String getVisibility() { return visibility; }
        public long getTotalVotes() { return totalVotes; }
        public List<OptionResult> getOptions() { return options; }
        public boolean isAnonymous() { return "ANONYMOUS".equals(visibility); }
    }
}
