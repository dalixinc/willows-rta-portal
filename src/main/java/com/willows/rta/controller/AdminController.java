package com.willows.rta.controller;

import com.willows.rta.model.Block;
import com.willows.rta.model.Member;
import com.willows.rta.model.User;
import com.willows.rta.service.BlockService;
import com.willows.rta.service.MemberService;
import com.willows.rta.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final UserService userService;
    private final BlockService blockService;
    @Value("${analytics.beta.enabled:false}")
    private boolean analyticsEnabled;

    @Autowired
    public AdminController(MemberService memberService, UserService userService, BlockService blockService) {
        this.memberService = memberService;
        this.userService = userService;
        this.blockService = blockService;
    }

    // Admin dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("isAdmin", true);
        model.addAttribute("totalMembers", memberService.getTotalMemberCount());
        model.addAttribute("activeMembers", memberService.getActiveMemberCount());
        model.addAttribute("membersWithoutAccounts", memberService.getMembersWithoutAccounts().size());
        return "admin/dashboard";
    }

    // View all members
    @GetMapping("/members")
    public String viewAllMembers(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        List<Member> members = memberService.getAllMembers();
        
        // Populate user status for each member
        for (Member member : members) {
            if (member.isHasUserAccount()) {
                Optional<User> userOpt = userService.getUserByUsername(member.getEmail());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    member.setUserEnabled(user.isEnabled());
                    member.setUserAccountLocked(user.isAccountLocked());
                    member.setUserFailedAttempts(user.getFailedLoginAttempts());
                    member.setUserRole(user.getRole());
                }
            }
        }
        
        model.addAttribute("members", members);
        return "admin/members";
    }

    // Show add member form
    @GetMapping("/members/add")
    public String showAddMemberForm(Model model) {
        model.addAttribute("member", new Member());
        return "admin/add-member";
    }

    // Handle add member submission
    @PostMapping("/members/add")
    public String addMember(@ModelAttribute Member member,
                           @RequestParam(required = false) String createAccount,
                           @RequestParam(required = false) String passwordOption,
                           @RequestParam(required = false) String password,
                           @RequestParam(required = false) String confirmPassword,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        try {
            // Validate email uniqueness
            if (memberService.emailExists(member.getEmail())) {
                model.addAttribute("errorMessage", "Email already exists. Please use a different email.");
                return "admin/add-member";
            }

            // Save the member
            Member savedMember = memberService.registerMember(member);
            
            // Check if we need to create an account
            if ("yes".equals(createAccount)) {
                String accountPassword = password;
                
                // Generate password if auto option selected
                if ("auto".equals(passwordOption) || accountPassword == null || accountPassword.trim().isEmpty()) {
                    accountPassword = generateTemporaryPassword();
                } else {
                    // Validate manual password
                    if (!accountPassword.equals(confirmPassword)) {
                        model.addAttribute("errorMessage", "Passwords do not match.");
                        model.addAttribute("member", member);
                        return "admin/add-member";
                    }
                }
                
                // Create user account using YOUR UserService method
                userService.createUser(savedMember.getEmail(), accountPassword, "ROLE_MEMBER");
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Member added and account created. Email: " + savedMember.getEmail() + 
                    ", Password: " + accountPassword);
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Member added successfully (no account created).");
            }
            
            return "redirect:/admin/members";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding member: " + e.getMessage());
            model.addAttribute("member", member);
            return "admin/add-member";
        }
    }

    // View member details
    @GetMapping("/members/{id}")
    public String viewMemberDetails(@PathVariable Long id, Model model) {
        Optional<Member> memberOpt = memberService.getMemberById(id);
        if (memberOpt.isPresent()) {
            model.addAttribute("member", memberOpt.get());
            
            // If member has user account, get account details
            if (memberOpt.get().isHasUserAccount()) {
                Optional<User> userOpt = userService.getUserByUsername(memberOpt.get().getEmail());
                userOpt.ifPresent(user -> model.addAttribute("user", user));
            }
            
            return "admin/member-details";
        }
        return "redirect:/admin/members";
    }

    // Show edit member form
    @GetMapping("/members/edit/{id}")
    public String showEditMemberForm(@PathVariable Long id, Model model) {
        Optional<Member> memberOpt = memberService.getMemberById(id);
        if (memberOpt.isPresent()) {
            model.addAttribute("member", memberOpt.get());
            return "admin/edit-member";
        }
        return "redirect:/admin/members";
    }

    // Handle edit member submission
    @PostMapping("/members/edit/{id}")
    public String editMember(@PathVariable Long id, 
                            @ModelAttribute Member member,
                            RedirectAttributes redirectAttributes) {
        try {
            memberService.updateMember(id, member);
            redirectAttributes.addFlashAttribute("successMessage", "Member updated successfully.");
            return "redirect:/admin/members/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating member: " + e.getMessage());
            return "redirect:/admin/members/edit/" + id;
        }
    }

    // Delete member
    @PostMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            memberService.deleteMember(id);
            redirectAttributes.addFlashAttribute("successMessage", "Member deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting member: " + e.getMessage());
        }
        return "redirect:/admin/members";
    }

    // Create user account for member
    @PostMapping("/members/{id}/create-account")
    public String createUserAccount(@PathVariable Long id, 
                                    @RequestParam(required = false) String passwordOption,
                                    @RequestParam(required = false) String password,
                                    RedirectAttributes redirectAttributes) {
        try {
            Optional<Member> memberOpt = memberService.getMemberById(id);
            if (memberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
                return "redirect:/admin/members";
            }

            Member member = memberOpt.get();
            
            // Check if account already exists
            if (userService.getUserByUsername(member.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User account already exists for this member.");
                return "redirect:/admin/members/" + id;
            }

            // Determine password
            String accountPassword = password;
            if ("auto".equals(passwordOption) || accountPassword == null || accountPassword.trim().isEmpty()) {
                accountPassword = generateTemporaryPassword();
            }

            // Create account
            userService.createUser(member.getEmail(), accountPassword, "ROLE_MEMBER");
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "User account created. Email: " + member.getEmail() + ", Password: " + accountPassword);
            return "redirect:/admin/members/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating account: " + e.getMessage());
            return "redirect:/admin/members/" + id;
        }
    }

    // Unlock account
    @PostMapping("/members/{id}/unlock")
    public String unlockAccount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Member> memberOpt = memberService.getMemberById(id);
            if (memberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
                return "redirect:/admin/members";
            }

            Member member = memberOpt.get();
            
            // Use YOUR UserService method (takes username)
            userService.unlockAccount(member.getEmail());
            
            redirectAttributes.addFlashAttribute("successMessage", "Account unlocked successfully.");
            return "redirect:/admin/members/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error unlocking account: " + e.getMessage());
            return "redirect:/admin/members/" + id;
        }
    }

    // Reset password
    @PostMapping("/members/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                               @RequestParam(required = false) String passwordOption,
                               @RequestParam(required = false) String password,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Member> memberOpt = memberService.getMemberById(id);
            if (memberOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Member not found.");
                return "redirect:/admin/members";
            }

            Member member = memberOpt.get();
            Optional<User> userOpt = userService.getUserByUsername(member.getEmail());
            
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "No user account found for this member.");
                return "redirect:/admin/members/" + id;
            }

            // Determine password
            String newPassword = password;
            if ("auto".equals(passwordOption) || newPassword == null || newPassword.trim().isEmpty()) {
                newPassword = generateTemporaryPassword();
            }

            User user = userOpt.get();
            // Use YOUR UserService method (takes Long userId)
            userService.updatePassword(user.getId(), newPassword);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Password reset successfully. New password: " + newPassword);
            return "redirect:/admin/members/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error resetting password: " + e.getMessage());
            return "redirect:/admin/members/" + id;
        }
    }

    /**
     * Show block configuration page
     */
    @GetMapping("/blocks")
    public String showBlocksPage(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("blocks", blockService.getAllBlocks());
        return "admin/blocks";
    }

    /**
     * Show add block form
     */
    @GetMapping("/blocks/add")
    public String showAddBlockForm(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("block", new Block());
        return "admin/block-form";
    }

    /**
     * Create new block
     */
    @PostMapping("/blocks/add")
    public String addBlock(@ModelAttribute Block block, RedirectAttributes redirectAttributes) {
        try {
            if (blockService.blockNameExists(block.getName())) {
                redirectAttributes.addFlashAttribute("error", "Block name already exists");
                return "redirect:/admin/blocks/add";
            }
            blockService.createBlock(block);
            redirectAttributes.addFlashAttribute("success", "Block added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding block: " + e.getMessage());
        }
        return "redirect:/admin/blocks";
    }

    /**
     * Show edit block form
     */
    @GetMapping("/blocks/edit/{id}")
    public String showEditBlockForm(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        Optional<Block> block = blockService.getBlockById(id);
        if (block.isPresent()) {
            model.addAttribute("block", block.get());
            return "admin/block-form";
        }
        return "redirect:/admin/blocks";
    }

    /**
     * Update block
     */
    @PostMapping("/blocks/edit/{id}")
    public String updateBlock(@PathVariable Long id, @ModelAttribute Block block, RedirectAttributes redirectAttributes) {
        try {
            Block updated = blockService.updateBlock(id, block);
            if (updated != null) {
                redirectAttributes.addFlashAttribute("success", "Block updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Block not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating block: " + e.getMessage());
        }
        return "redirect:/admin/blocks";
    }

    /**
     * Delete block
     */
    @PostMapping("/blocks/delete/{id}")
    public String deleteBlock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            blockService.deleteBlock(id);
            redirectAttributes.addFlashAttribute("success", "Block deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting block: " + e.getMessage());
        }
        return "redirect:/admin/blocks";
    }

    /**
     * Show analytics page
     */
    @GetMapping("/analytics")
    public String showAnalyticsPage(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("blockStats", blockService.calculateBlockStats());
        model.addAttribute("overallStats", blockService.calculateOverallStats());
        model.addAttribute("analyticsBeta", analyticsEnabled);  // ADD THIS LINE
        return "admin/analytics";
}

    // Helper method to generate temporary password
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
