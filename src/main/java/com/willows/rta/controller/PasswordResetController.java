package com.willows.rta.controller;

import com.willows.rta.model.User;
import com.willows.rta.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class PasswordResetController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Show change password page
    @GetMapping("/member/change-password")
    public String showChangePasswordPage(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "member/change-password";
    }

    // Process password change
    @PostMapping("/member/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        
        String username = authentication.getName();
        
        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/member/change-password";
        }

        // Validate password length
        if (newPassword.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters");
            return "redirect:/member/change-password";
        }

        // Get user
        Optional<User> userOpt = userService.getUserByUsername(username);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/member/change-password";
        }

        User user = userOpt.get();

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
            return "redirect:/member/change-password";
        }

        // Update password
        try {
            userService.updatePassword(user.getId(), newPassword);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            return "redirect:/member/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error changing password: " + e.getMessage());
            return "redirect:/member/change-password";
        }
    }
}
