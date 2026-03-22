package com.willows.rta.controller;

import com.willows.rta.model.Member;
import com.willows.rta.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    
    private static final int PAGE_SIZE = 20; // 20 members per page

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
    public String viewDirectory(
            @RequestParam(defaultValue = "0") int page,
            Model model, 
            Authentication authentication) {
        
        model.addAttribute("username", authentication.getName());
        
        // Check if user is admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        
        // Create pageable object - sort by flatNumber for easy navigation
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("flatNumber").ascending());
        
        // Get paginated active members
        Page<Member> memberPage = memberService.getMembersByStatusPaginated("ACTIVE", pageable);
        
        model.addAttribute("memberPage", memberPage);
        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", memberPage.getTotalPages());
        model.addAttribute("totalMembers", memberPage.getTotalElements());
        
        return "member/directory";
    }
}
