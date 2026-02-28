package com.willows.rta.controller;

import com.willows.rta.model.Member;
import com.willows.rta.model.Poll;
import com.willows.rta.model.User;
import com.willows.rta.service.MemberService;
import com.willows.rta.service.PollService;
import com.willows.rta.service.UserService;
import com.willows.rta.service.VotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller for member voting
 */
@Controller
@RequestMapping("/polls")
public class MemberPollController {

    private final PollService pollService;
    private final VotingService votingService;
    private final UserService userService;
    private final MemberService memberService;

    @Autowired
    public MemberPollController(PollService pollService, VotingService votingService, 
                                UserService userService, MemberService memberService) {
        this.pollService = pollService;
        this.votingService = votingService;
        this.userService = userService;
        this.memberService = memberService;
    }

    /**
     * Show active polls to members
     */
    @GetMapping
    public String showActivePolls(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get member
        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Member member = user.getMember();
        if (member == null) {
            throw new RuntimeException("Member not found");
        }
        
        model.addAttribute("memberId", member.getId());
        
        List<Poll> activePolls = pollService.getActivePolls();
        model.addAttribute("polls", activePolls);
        
        // Check which polls the member has voted on
        for (Poll poll : activePolls) {
            boolean hasVoted = votingService.hasVoted(poll.getId(), member.getId());
            poll.setAllowRevote(hasVoted); // Reusing this field to pass voted status to template
        }
        
        return "polls/active-polls";
    }

    /**
     * Show voting form for a poll
     */
    @GetMapping("/{id}/vote")
    public String showVotingForm(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get poll
        Optional<Poll> pollOpt = pollService.getPollById(id);
        if (pollOpt.isEmpty()) {
            return "redirect:/polls";
        }
        
        Poll poll = pollOpt.get();
        
        if (!"ACTIVE".equals(poll.getStatus())) {
            return "redirect:/polls";
        }
        
        // Get member
        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Member member = user.getMember();
        
        // Check if already voted
        boolean hasVoted = votingService.hasVoted(id, member.getId());
        
        if (hasVoted && !poll.getAllowRevote()) {
            return "redirect:/polls/" + id + "/results";
        }
        
        model.addAttribute("poll", poll);
        model.addAttribute("hasVoted", hasVoted);
        
        return "polls/vote";
    }

    /**
     * Submit vote
     */
    @PostMapping("/{id}/vote")
    public String submitVote(@PathVariable Long id,
                            @RequestParam(required = false) List<Long> optionIds,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            // Get member
            User user = userService.getUserByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Member member = user.getMember();
            
            if (optionIds == null || optionIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one option");
                return "redirect:/polls/" + id + "/vote";
            }
            
            // Cast vote
            votingService.castVote(id, optionIds, member.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Your vote has been recorded. Thank you!");
            return "redirect:/polls/" + id + "/results";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error casting vote: " + e.getMessage());
            return "redirect:/polls/" + id + "/vote";
        }
    }

    /**
     * View poll results
     */
    @GetMapping("/{id}/results")
    public String viewResults(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        Optional<Poll> pollOpt = pollService.getPollById(id);
        if (pollOpt.isEmpty()) {
            return "redirect:/polls";
        }
        
        Poll poll = pollOpt.get();
        model.addAttribute("poll", poll);
        
        // Check if member can view results
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !poll.canViewResults()) {
            model.addAttribute("cannotViewResults", true);
            return "polls/results";
        }
        
        // Get results
        VotingService.PollResults results = votingService.getPollResults(id);
        model.addAttribute("results", results);
        
        // Get member
        User user = userService.getUserByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Member member = user.getMember();
        
        // Check if member voted
        boolean hasVoted = votingService.hasVoted(id, member.getId());
        model.addAttribute("hasVoted", hasVoted);
        
        return "polls/results";
    }
}
