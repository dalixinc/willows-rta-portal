package com.willows.rta.controller;

import com.willows.rta.model.Member;
import com.willows.rta.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PublicController {

    private final MemberService memberService;

    @Autowired
    public PublicController(MemberService memberService) {
        this.memberService = memberService;
    }

    // Home page
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Registration page
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("member", new Member());
        return "register";
    }

    // Handle registration submission
    @PostMapping("/register")
    public String registerMember(@Valid @ModelAttribute Member member, 
                                 BindingResult bindingResult,
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
            memberService.registerMember(member);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Please contact the committee to receive your login credentials.");
            return "redirect:/";
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
