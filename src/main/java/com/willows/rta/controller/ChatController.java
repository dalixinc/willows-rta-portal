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
import java.util.Optional;

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
     * Also returns list of deleted/edited message IDs
     */
    @GetMapping("/messages/new")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getNewMessages(@RequestParam(required = false) Long lastId) {
        List<ChatMessage> newMessages = chatService.getNewMessages(lastId);
        
        // Return both new messages and change notifications
        Map<String, Object> response = new HashMap<>();
        response.put("newMessages", newMessages);
        response.put("deletedIds", chatService.getAndClearDeletedIds());
        response.put("editedMessages", chatService.getAndClearEditedMessages());
        
        return ResponseEntity.ok(response);
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

    /**
     * Delete a message (admin can delete any, user can delete their own)
     */
    @PostMapping("/messages/{id}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMessage(
            @PathVariable Long id,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Get the message
            Optional<ChatMessage> messageOpt = chatService.getMessageById(id);
            if (messageOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Message not found");
                return ResponseEntity.notFound().build();
            }
            
            ChatMessage message = messageOpt.get();
            
            // Check permissions
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwner = message.getSenderName().equals(authentication.getName());
            
            if (!isAdmin && !isOwner) {
                response.put("success", false);
                response.put("message", "You can only delete your own messages");
                return ResponseEntity.status(403).body(response);
            }
            
            // Delete the message
            chatService.deleteMessage(id);
            
            // Notify other clients
            chatService.notifyMessageDeleted(id);
            
            response.put("success", true);
            response.put("messageId", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting message: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Edit a message (user can edit their own messages)
     */
    @PostMapping("/messages/{id}/edit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editMessage(
            @PathVariable Long id,
            @RequestParam String content,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate content
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Message cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            if (content.length() > 1000) {
                response.put("success", false);
                response.put("message", "Message too long (max 1000 characters)");
                return ResponseEntity.badRequest().body(response);
            }

            // Get the message
            Optional<ChatMessage> messageOpt = chatService.getMessageById(id);
            if (messageOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Message not found");
                return ResponseEntity.notFound().build();
            }
            
            ChatMessage message = messageOpt.get();
            
            // Check permissions (only owner can edit)
            boolean isOwner = message.getSenderName().equals(authentication.getName());
            
            if (!isOwner) {
                response.put("success", false);
                response.put("message", "You can only edit your own messages");
                return ResponseEntity.status(403).body(response);
            }
            
            // Update the message
            ChatMessage updatedMessage = chatService.updateMessage(id, content.trim());
            
            // Notify other clients
            chatService.notifyMessageEdited(updatedMessage);
            
            response.put("success", true);
            response.put("messageId", id);
            response.put("newContent", content.trim());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error editing message: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
