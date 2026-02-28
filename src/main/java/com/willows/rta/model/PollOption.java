package com.willows.rta.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Poll option entity - represents a choice in a poll
 */
@Entity
@Table(name = "poll_options")
public class PollOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @Column(name = "option_text", nullable = false, length = 500)
    private String optionText;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes = new ArrayList<>();

    // Constructors
    public PollOption() {
        this.createdAt = LocalDateTime.now();
    }

    public PollOption(String optionText, Integer displayOrder) {
        this();
        this.optionText = optionText;
        this.displayOrder = displayOrder;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }

    // Helper methods
    public int getVoteCount() {
        return votes.size();
    }

    public double getPercentage(int totalVotes) {
        if (totalVotes == 0) return 0.0;
        return (getVoteCount() * 100.0) / totalVotes;
    }

    public String getFormattedPercentage(int totalVotes) {
        return String.format("%.0f%%", getPercentage(totalVotes));
    }
}
