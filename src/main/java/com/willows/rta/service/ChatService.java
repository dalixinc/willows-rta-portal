package com.willows.rta.service;

import com.willows.rta.model.ChatMessage;
import com.willows.rta.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Get last 100 messages (for initial load)
     */
    public List<ChatMessage> getRecentMessages() {
        List<ChatMessage> messages = chatMessageRepository.findLast100Messages();
        
        // Limit to 100 messages
        if (messages.size() > 100) {
            messages = messages.subList(0, 100);
        }
        
        // Reverse to show oldest first (chronological order for chat)
        Collections.reverse(messages);
        return messages;
    }

    /**
     * Get new messages after a specific ID (for polling)
     */
    public List<ChatMessage> getNewMessages(Long lastMessageId) {
        if (lastMessageId == null || lastMessageId == 0) {
            return Collections.emptyList();
        }
        return chatMessageRepository.findMessagesAfterId(lastMessageId);
    }

    /**
     * Post a new message
     */
    @Transactional
    public ChatMessage postMessage(String senderName, String content, String senderRole) {
        ChatMessage message = new ChatMessage(senderName, content, senderRole);
        return chatMessageRepository.save(message);
    }

    /**
     * Delete old messages (optional - for cleanup)
     */
    @Transactional
    public void deleteOldMessages(int keepLastN) {
        List<ChatMessage> allMessages = chatMessageRepository.findLast100Messages();
        if (allMessages.size() > keepLastN) {
            List<ChatMessage> toDelete = allMessages.subList(keepLastN, allMessages.size());
            chatMessageRepository.deleteAll(toDelete);
        }
    }

    /**
     * Get a specific message by ID
     */
    public Optional<ChatMessage> getMessageById(Long id) {
        return chatMessageRepository.findById(id);
    }

    /**
     * Delete a specific message
     */
    @Transactional
    public void deleteMessage(Long id) {
        chatMessageRepository.deleteById(id);
    }

    /**
     * Update a message content
     */
    @Transactional
    public void updateMessage(Long id, String newContent) {
        Optional<ChatMessage> messageOpt = chatMessageRepository.findById(id);
        if (messageOpt.isPresent()) {
            ChatMessage message = messageOpt.get();
            message.setContent(newContent);
            chatMessageRepository.save(message);
        }
    }
}
