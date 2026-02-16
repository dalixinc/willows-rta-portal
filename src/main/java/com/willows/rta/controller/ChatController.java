package com.willows.rta.controller;

import com.willows.rta.model.ChatMessage;
import com.willows.rta.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Show chat page
     */
    @GetMapping
    public String showChatPage(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        
        // Check if user is admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        
        return "chat";
    }

    /**
     * Get recent messages (initial load)
     */
    @GetMapping("/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getMessages() {
        List<ChatMessage> messages = chatService.getRecentMessages();
        return ResponseEntity.ok(messages);
    }

    /**
     * Get new messages after a specific ID (for polling)
     */
    @GetMapping("/messages/new")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getNewMessages(@RequestParam(required = false) Long lastId) {
        List<ChatMessage> newMessages = chatService.getNewMessages(lastId);
        return ResponseEntity.ok(newMessages);
    }

    /**
     * Post a new message
     */
    @PostMapping("/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> postMessage(
            @RequestParam String content,
            Authentication authentication) {
        
        if (content == null || content.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Message cannot be empty");
            return ResponseEntity.badRequest().body(error);
        }

        if (content.length() > 1000) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Message too long (max 1000 characters)");
            return ResponseEntity.badRequest().body(error);
        }

        // Determine user role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        String role = isAdmin ? "ADMIN" : "MEMBER";

        // Post message
        ChatMessage message = chatService.postMessage(
            authentication.getName(),
            content.trim(),
            role
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}
