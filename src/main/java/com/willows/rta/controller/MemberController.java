package com.willows.rta.controller;

import com.willows.rta.model.Member;
import com.willows.rta.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

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

    @GetMapping("/directory")
    public String viewDirectory(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        
        // Get only active members for directory (limited PII)
        List<Member> activeMembers = memberService.getMembersByStatus("ACTIVE");
        model.addAttribute("members", activeMembers);
        
        return "member/directory";
    }
}
