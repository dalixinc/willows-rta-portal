package com.willows.rta.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
public class MemberController {

    @GetMapping("/dashboard")
    public String memberDashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "member/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        // TODO: Load actual member profile details
        return "member/profile";
    }
}
