package com.willows.rta.controller;

import com.willows.rta.model.Notice;
import com.willows.rta.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @Autowired
    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    /**
     * Show notice board (public - all logged-in users)
     */
    @GetMapping
    public String showNoticeBoard(Model model) {
        model.addAttribute("notices", noticeService.getAllNotices());
        return "notices";
    }

    /**
     * Show create notice form (admin only)
     */
    @GetMapping("/create")
    public String showCreateForm() {
        return "notice-create";
    }

    /**
     * Create new notice (admin only)
     */
    @PostMapping("/create")
    public String createNotice(
            @RequestParam String title,
            @RequestParam String content,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        noticeService.createNotice(title, content, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Notice created successfully");
        return "redirect:/notices";
    }

    /**
     * Show edit notice form (admin only)
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Notice notice = noticeService.getNoticeById(id).orElse(null);
        if (notice == null) {
            redirectAttributes.addFlashAttribute("error", "Notice not found");
            return "redirect:/notices";
        }
        model.addAttribute("notice", notice);
        return "notice-edit";
    }

    /**
     * Update notice (admin only)
     */
    @PostMapping("/edit/{id}")
    public String updateNotice(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {
        
        try {
            noticeService.updateNotice(id, title, content);
            redirectAttributes.addFlashAttribute("successMessage", "Notice updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update notice");
        }
        return "redirect:/notices";
    }

    /**
     * Toggle pinned status (admin only)
     */
    @PostMapping("/toggle-pin/{id}")
    public String togglePinned(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            noticeService.togglePinned(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notice pinned status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update notice");
        }
        return "redirect:/notices";
    }

    /**
     * Delete notice (admin only)
     */
    @PostMapping("/delete/{id}")
    public String deleteNotice(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            noticeService.deleteNotice(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notice deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete notice");
        }
        return "redirect:/notices";
    }
}
