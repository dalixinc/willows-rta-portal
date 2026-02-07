package com.willows.rta.controller;

import com.willows.rta.model.Member;
import com.willows.rta.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;

    @Autowired
    public AdminController(MemberService memberService) {
        this.memberService = memberService;
    }

    // Admin dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("totalMembers", memberService.getTotalMemberCount());
        model.addAttribute("activeMembers", memberService.getActiveMemberCount());
        return "admin/dashboard";
    }

    // View all members
    @GetMapping("/members")
    public String viewAllMembers(Model model) {
        model.addAttribute("members", memberService.getAllMembers());
        return "admin/members";
    }

    // View member details
    @GetMapping("/members/{id}")
    public String viewMemberDetails(@PathVariable Long id, Model model) {
        Member member = memberService.getMemberById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        model.addAttribute("member", member);
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

    // Delete member
    @PostMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            memberService.deleteMember(id);
            redirectAttributes.addFlashAttribute("successMessage", "Member deleted successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting member");
        }
        return "redirect:/admin/members";
    }
}
