package com.willows.rta.controller;

import com.willows.rta.model.Member;
import com.willows.rta.model.User;
import com.willows.rta.service.MemberService;
import com.willows.rta.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PublicController {

    private final MemberService memberService;
    private final UserService userService;

    @Value("${app.self-registration.enabled:true}")
    private boolean selfRegistrationEnabled;

    @Autowired
    public PublicController(MemberService memberService, UserService userService) {
        this.memberService = memberService;
        this.userService = userService;
    }

    // Home page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Registration page
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("selfRegistrationEnabled", selfRegistrationEnabled);
        return "register";
    }

    // Handle registration submission
    @PostMapping("/register")
    public String registerMember(@Valid @ModelAttribute Member member, 
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) String createAccount,
                                 @RequestParam(required = false) String password,
                                 @RequestParam(required = false) String confirmPassword,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!member.isConsentGiven()) {
            model.addAttribute("consentError", "You must agree to the constitution to register");
            return "register";
        }

        try {
            // Register the member
            Member savedMember = memberService.registerMember(member);
            
            // Check if self-registration is enabled and user wants to create account now
            if ("yes".equals(createAccount)) {
                // Check if self-registration is allowed
                if (!selfRegistrationEnabled) {
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Registration successful! An administrator will create your login credentials and contact you.");
                    return "redirect:/";
                }
                
                // Validate passwords
                if (password == null || password.trim().isEmpty()) {
                    model.addAttribute("errorMessage", "Password is required when creating an account");
                    model.addAttribute("selfRegistrationEnabled", selfRegistrationEnabled);
                    return "register";
                }
                
                if (!password.equals(confirmPassword)) {
                    model.addAttribute("errorMessage", "Passwords do not match");
                    model.addAttribute("selfRegistrationEnabled", selfRegistrationEnabled);
                    return "register";
                }
                
                if (password.length() < 8) {
                    model.addAttribute("errorMessage", "Password must be at least 8 characters");
                    model.addAttribute("selfRegistrationEnabled", selfRegistrationEnabled);
                    return "register";
                }
                
                // Create user account
                try {
                    User newUser = userService.createUser(member.getEmail(), password, "ROLE_MEMBER");
                    newUser.setMember(savedMember);
                    
                    // Update member record
                    savedMember.setHasUserAccount(true);
                    savedMember.setAccountCreationMethod("SELF_REGISTRATION");
                    memberService.updateMemberAccountStatus(savedMember.getId(), true, "SELF_REGISTRATION");
                    
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Registration successful! You can now login with your email and password.");
                    return "redirect:/login";
                    
                } catch (RuntimeException e) {
                    model.addAttribute("errorMessage", "Account created but login setup failed: " + e.getMessage());
                    return "register";
                }
            } else {
                // No account created - admin will do it later
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Registration successful! The committee will review your application and contact you with login details.");
                return "redirect:/";
            }
            
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    // Constitution page
    @GetMapping("/constitution")
    public String constitution() {
        return "constitution";
    }
}
