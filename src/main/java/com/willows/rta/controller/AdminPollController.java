package com.willows.rta.controller;

import com.willows.rta.model.Poll;
import com.willows.rta.model.User;
import com.willows.rta.service.PollService;
import com.willows.rta.service.VotingService;
import com.willows.rta.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for admin poll management
 */
@Controller
@RequestMapping("/admin/polls")
public class AdminPollController {

    private final PollService pollService;
    private final VotingService votingService;
    private final UserService userService;
     @Value("${polls.beta.enabled:false}")
    private boolean pollsBeta;

    @Autowired
    public AdminPollController(PollService pollService, VotingService votingService, UserService userService) {
        this.pollService = pollService;
        this.votingService = votingService;
        this.userService = userService;
    }

    /**
     * Show poll dashboard
     */
    @GetMapping
    public String pollDashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        List<Poll> activePolls = pollService.getActivePolls();
        List<Poll> draftPolls = pollService.getDraftPolls();
        List<Poll> closedPolls = pollService.getClosedPolls();
        
        model.addAttribute("activePolls", activePolls);
        model.addAttribute("draftPolls", draftPolls);
        model.addAttribute("closedPolls", closedPolls);
        
        return "admin/polls-dashboard";
    }

    /**
     * Show create poll form
     */
    @GetMapping("/create")
    public String showCreatePollForm(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("poll", new Poll());
        return "admin/poll-create";
    }

    /**
     * Create new poll
     */
    @PostMapping("/create")
    public String createPoll(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String pollType,
            @RequestParam String visibility,
            @RequestParam String resultsVisibility,
            @RequestParam(required = false) String closeAt,
            @RequestParam(required = false) boolean allowRevote,
            @RequestParam List<String> options,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            User user = userService.getUserByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Create poll
            Poll poll = new Poll();
            poll.setTitle(title);
            poll.setDescription(description);
            poll.setPollType(pollType);
            poll.setVisibility(visibility);
            poll.setResultsVisibility(resultsVisibility);
            poll.setAllowRevote(allowRevote);
            
            // Parse deadline if provided
            if (closeAt != null && !closeAt.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                poll.setCloseAt(LocalDateTime.parse(closeAt, formatter));
            }
            
            // Filter out empty options
            List<String> validOptions = new ArrayList<>();
            for (String option : options) {
                if (option != null && !option.trim().isEmpty()) {
                    validOptions.add(option.trim());
                }
            }
            
            if (validOptions.size() < 2) {
                redirectAttributes.addFlashAttribute("errorMessage", "Poll must have at least 2 options");
                return "redirect:/admin/polls/create";
            }
            
            Poll savedPoll = pollService.createPoll(poll, validOptions, user.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Poll created successfully! You can publish it when ready.");
            return "redirect:/admin/polls/" + savedPoll.getId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating poll: " + e.getMessage());
            return "redirect:/admin/polls/create";
        }
    }

    /**
     * View poll details and results
     */
    @GetMapping("/{id}")
    public String viewPoll(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        Optional<Poll> pollOpt = pollService.getPollById(id);
        if (pollOpt.isEmpty()) {
            return "redirect:/admin/polls";
        }
        
        Poll poll = pollOpt.get();
        model.addAttribute("poll", poll);
        
        // Get results
        VotingService.PollResults results = votingService.getPollResults(id);
        model.addAttribute("results", results);
        
        // Get stats
        PollService.PollStats stats = pollService.getPollStats(id);
        model.addAttribute("stats", stats);

        model.addAttribute("pollsBeta", pollsBeta);
        
        return "admin/poll-details";
    }

    /**
     * Publish poll (make active)
     */
    @PostMapping("/{id}/publish")
    public String publishPoll(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pollService.publishPoll(id);
            redirectAttributes.addFlashAttribute("successMessage", "Poll published successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error publishing poll: " + e.getMessage());
        }
        return "redirect:/admin/polls/" + id;
    }

    /**
     * Close poll
     */
    @PostMapping("/{id}/close")
    public String closePoll(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pollService.closePoll(id);
            redirectAttributes.addFlashAttribute("successMessage", "Poll closed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error closing poll: " + e.getMessage());
        }
        return "redirect:/admin/polls/" + id;
    }

    /**
     * Reopen poll
     */
    @PostMapping("/{id}/reopen")
    public String reopenPoll(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pollService.reopenPoll(id);
            redirectAttributes.addFlashAttribute("successMessage", "Poll reopened successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error reopening poll: " + e.getMessage());
        }
        return "redirect:/admin/polls/" + id;
    }

    /**
     * Delete poll
     */
    @PostMapping("/{id}/delete")
    public String deletePoll(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pollService.deletePoll(id);
            redirectAttributes.addFlashAttribute("successMessage", "Poll deleted successfully!");
            return "redirect:/admin/polls";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting poll: " + e.getMessage());
            return "redirect:/admin/polls/" + id;
        }
    }
}
