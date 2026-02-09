package com.willows.rta.controller;

import com.willows.rta.model.Member;
import com.willows.rta.model.User;
import com.willows.rta.service.MemberService;
import com.willows.rta.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final UserService userService;

    @Autowired
    public AdminController(MemberService memberService, UserService userService) {
        this.memberService = memberService;
        this.userService = userService;
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
    public String viewAllMembers(Model model) {
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
                        model.addAttribute("errorMessage", "Passwords do not match");
                        model.addAttribute("member", member);
                        return "admin/add-member";
                    }
                    
                    if (accountPassword.length() < 8) {
                        model.addAttribute("errorMessage", "Password must be at least 8 characters");
                        model.addAttribute("member", member);
                        return "admin/add-member";
                    }
                }
                
                try {
                    // Create user account
                    User newUser = userService.createUser(member.getEmail(), accountPassword, "ROLE_MEMBER");
                    newUser.setMember(savedMember);
                    
                    // Update member record
                    savedMember.setHasUserAccount(true);
                    savedMember.setAccountCreationMethod("ADMIN_CREATED");
                    memberService.updateMemberAccountStatus(savedMember.getId(), true, "ADMIN_CREATED");
                    
                    redirectAttributes.addFlashAttribute("successMessage", "Member added successfully with login account!");
                    redirectAttributes.addFlashAttribute("generatedPassword", accountPassword);
                    redirectAttributes.addFlashAttribute("memberEmail", member.getEmail());
                    return "redirect:/admin/members/" + savedMember.getId();
                    
                } catch (RuntimeException e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Member added but login creation failed: " + e.getMessage());
                    return "redirect:/admin/members/" + savedMember.getId();
                }
            } else {
                // No account created
                redirectAttributes.addFlashAttribute("successMessage", "Member added successfully! You can create a login account later.");
                return "redirect:/admin/members/" + savedMember.getId();
            }
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding member: " + e.getMessage());
            model.addAttribute("member", member);
            return "admin/add-member";
        }
    }

    // View members without accounts
    @GetMapping("/members/no-accounts")
    public String viewMembersWithoutAccounts(Model model) {
        model.addAttribute("members", memberService.getMembersWithoutAccounts());
        return "admin/members-no-accounts";
    }

    // View member details
    @GetMapping("/members/{id}")
    public String viewMemberDetails(@PathVariable Long id, Model model) {
        Member member = memberService.getMemberById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        model.addAttribute("member", member);
        
        // If member has user account, get the user details
        if (member.isHasUserAccount()) {
            Optional<User> userOpt = userService.getUserByUsername(member.getEmail());
            userOpt.ifPresent(user -> model.addAttribute("user", user));
        }
        
        return "admin/member-details";
    }

    // Edit member form
    @GetMapping("/members/edit/{id}")
    public String editMemberForm(@PathVariable Long id, Model model) {
        Member member = memberService.getMemberById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        model.addAttribute("member", member);
        return "admin/edit-member";
    }

    // Update member
    @PostMapping("/members/update/{id}")
    public String updateMember(@PathVariable Long id, 
                              @ModelAttribute Member member,
                              RedirectAttributes redirectAttributes) {
        try {
            memberService.updateMember(id, member);
            redirectAttributes.addFlashAttribute("successMessage", "Member updated successfully");
            return "redirect:/admin/members";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/members/edit/" + id;
        }
    }

    // Update membership status
    @PostMapping("/members/status/{id}")
    public String updateMembershipStatus(@PathVariable Long id,
                                        @RequestParam String status,
                                        RedirectAttributes redirectAttributes) {
        try {
            memberService.updateMembershipStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Membership status updated");
            return "redirect:/admin/members";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/members";
        }
    }

    // Create user account for member
    @PostMapping("/members/create-account/{id}")
    public String createUserAccount(@PathVariable Long id,
                                   @RequestParam(required = false) String customPassword,
                                   RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.getMemberById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            
            // Check if account already exists
            if (member.isHasUserAccount()) {
                redirectAttributes.addFlashAttribute("errorMessage", "This member already has a user account");
                return "redirect:/admin/members/" + id;
            }
            
            // Generate password if not provided
            String password = (customPassword != null && !customPassword.trim().isEmpty()) 
                ? customPassword 
                : generateTemporaryPassword();
            
            // Create user account
            User newUser = userService.createUser(member.getEmail(), password, "ROLE_MEMBER");
            newUser.setMember(member);
            
            // Update member record
            memberService.updateMemberAccountStatus(id, true, "ADMIN_CREATED");
            
            // Store password in flash to show to admin
            redirectAttributes.addFlashAttribute("successMessage", 
                "Login account created successfully!");
            redirectAttributes.addFlashAttribute("generatedPassword", password);
            redirectAttributes.addFlashAttribute("memberEmail", member.getEmail());
            
            return "redirect:/admin/members/" + id;
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating account: " + e.getMessage());
            return "redirect:/admin/members/" + id;
        }
    }

    // Delete member
    @PostMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.getMemberById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            
            // If member has a user account, delete it first
            if (member.isHasUserAccount()) {
                Optional<User> userOpt = userService.getUserByUsername(member.getEmail());
                if (userOpt.isPresent()) {
                    userService.deleteUser(userOpt.get().getId());
                }
            }
            
            // Now delete the member
            memberService.deleteMember(id);
            redirectAttributes.addFlashAttribute("successMessage", "Member deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting member: " + e.getMessage());
        }
        return "redirect:/admin/members";
    }

    // Reset member password
    @PostMapping("/members/reset-password/{id}")
    public String resetMemberPassword(@PathVariable Long id,
                                      @RequestParam(required = false) String customPassword,
                                      RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.getMemberById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            
            if (!member.isHasUserAccount()) {
                redirectAttributes.addFlashAttribute("errorMessage", "This member has no user account");
                return "redirect:/admin/members/" + id;
            }
            
            // Get user account
            Optional<User> userOpt = userService.getUserByUsername(member.getEmail());
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User account not found");
                return "redirect:/admin/members/" + id;
            }
            
            User user = userOpt.get();
            
            // Generate password if not provided
            String newPassword = (customPassword != null && !customPassword.trim().isEmpty()) 
                ? customPassword 
                : generateTemporaryPassword();
            
            // Reset password and require change on next login
            userService.updatePassword(user.getId(), newPassword);
            user.setPasswordChangeRequired(true);
            userService.saveUser(user);
            
            // Store password in flash to show to admin
            redirectAttributes.addFlashAttribute("successMessage", 
                "Password reset successfully!");
            redirectAttributes.addFlashAttribute("generatedPassword", newPassword);
            redirectAttributes.addFlashAttribute("memberEmail", member.getEmail());
            
            return "redirect:/admin/members/" + id;
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error resetting password: " + e.getMessage());
            return "redirect:/admin/members/" + id;
        }
    }

    // Lock/Unlock user account
    @PostMapping("/members/toggle-lock/{id}")
    public String toggleAccountLock(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.getMemberById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            
            if (!member.isHasUserAccount()) {
                redirectAttributes.addFlashAttribute("errorMessage", "This member has no user account");
                return "redirect:/admin/members/" + id;
            }
            
            // Get user account
            Optional<User> userOpt = userService.getUserByUsername(member.getEmail());
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User account not found");
                return "redirect:/admin/members/" + id;
            }
            
            User user = userOpt.get();
            
            // Toggle enabled status
            user.setEnabled(!user.isEnabled());
            userService.saveUser(user);
            
            String status = user.isEnabled() ? "unlocked" : "locked";
            redirectAttributes.addFlashAttribute("successMessage", 
                "Account " + status + " successfully!");
            
            return "redirect:/admin/members/" + id;
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error toggling account lock: " + e.getMessage());
            return "redirect:/admin/members/" + id;
        }
    }

    // Unlock failed login attempts
    @PostMapping("/members/unlock-failed-attempts/{id}")
    public String unlockFailedAttempts(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.getMemberById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            
            if (!member.isHasUserAccount()) {
                redirectAttributes.addFlashAttribute("errorMessage", "This member has no user account");
                return "redirect:/admin/members/" + id;
            }
            
            // Unlock the account
            userService.unlockAccount(member.getEmail());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Failed login attempts cleared and account unlocked!");
            
            return "redirect:/admin/members/" + id;
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error unlocking account: " + e.getMessage());
            return "redirect:/admin/members/" + id;
        }
    }

    // Toggle user role (Admin <-> Member)
    @PostMapping("/members/toggle-role/{id}")
    public String toggleUserRole(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.getMemberById(id)
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            
            if (!member.isHasUserAccount()) {
                redirectAttributes.addFlashAttribute("errorMessage", "This member has no user account");
                return "redirect:/admin/members/" + id;
            }
            
            // Get user account
            Optional<User> userOpt = userService.getUserByUsername(member.getEmail());
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User account not found");
                return "redirect:/admin/members/" + id;
            }
            
            User user = userOpt.get();
            
            // Prevent changing role of system admin
            if (user.isSystemAdmin()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot change role of system administrator");
                return "redirect:/admin/members/" + id;
            }
            
            // Toggle role
            if ("ROLE_ADMIN".equals(user.getRole())) {
                user.setRole("ROLE_MEMBER");
                redirectAttributes.addFlashAttribute("successMessage", "User role changed to Member");
            } else {
                user.setRole("ROLE_ADMIN");
                redirectAttributes.addFlashAttribute("successMessage", "User role changed to Administrator");
            }
            
            userService.saveUser(user);
            
            return "redirect:/admin/members/" + id;
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error changing role: " + e.getMessage());
            return "redirect:/admin/members/" + id;
        }
    }

    // Helper method to generate temporary password
    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(12);
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
}
