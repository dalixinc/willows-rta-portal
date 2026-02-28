package com.willows.rta.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Poll entity for voting system
 */
@Entity
@Table(name = "polls")
public class Poll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "poll_type", nullable = false, length = 50)
    private String pollType = "SINGLE_CHOICE"; // SINGLE_CHOICE, MULTIPLE_CHOICE

    @Column(nullable = false, length = 50)
    private String visibility = "OPEN"; // OPEN, ANONYMOUS

    @Column(name = "results_visibility", nullable = false, length = 50)
    private String resultsVisibility = "LIVE"; // LIVE, HIDDEN_UNTIL_CLOSE, ADMIN_ONLY

    @Column(nullable = false, length = 50)
    private String status = "DRAFT"; // DRAFT, ACTIVE, CLOSED

    @Column(name = "created_by_id", nullable = false)
    private Long createdById;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "open_at")
    private LocalDateTime openAt;

    @Column(name = "close_at")
    private LocalDateTime closeAt;

    @Column(name = "closed_manually")
    private Boolean closedManually = false;

    @Column(name = "allow_revote")
    private Boolean allowRevote = false;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<PollOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes = new ArrayList<>();

    // Constructors
    public Poll() {
        this.createdAt = LocalDateTime.now();
    }

    public Poll(String title, String description, String pollType, String visibility) {
        this();
        this.title = title;
        this.description = description;
        this.pollType = pollType;
        this.visibility = visibility;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPollType() {
        return pollType;
    }

    public void setPollType(String pollType) {
        this.pollType = pollType;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getResultsVisibility() {
        return resultsVisibility;
    }

    public void setResultsVisibility(String resultsVisibility) {
        this.resultsVisibility = resultsVisibility;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getOpenAt() {
        return openAt;
    }

    public void setOpenAt(LocalDateTime openAt) {
        this.openAt = openAt;
    }

    public LocalDateTime getCloseAt() {
        return closeAt;
    }

    public void setCloseAt(LocalDateTime closeAt) {
        this.closeAt = closeAt;
    }

    public Boolean getClosedManually() {
        return closedManually;
    }

    public void setClosedManually(Boolean closedManually) {
        this.closedManually = closedManually;
    }

    public Boolean getAllowRevote() {
        return allowRevote;
    }

    public void setAllowRevote(Boolean allowRevote) {
        this.allowRevote = allowRevote;
    }

    public List<PollOption> getOptions() {
        return options;
    }

    public void setOptions(List<PollOption> options) {
        this.options = options;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }

    // Helper methods
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(status);
    }

    public boolean isDraft() {
        return "DRAFT".equals(status);
    }

    public boolean isAnonymous() {
        return "ANONYMOUS".equals(visibility);
    }

    public boolean isOpen() {
        return "OPEN".equals(visibility);
    }

    public boolean isMultipleChoice() {
        return "MULTIPLE_CHOICE".equals(pollType);
    }

    public boolean isSingleChoice() {
        return "SINGLE_CHOICE".equals(pollType);
    }

    public boolean hasDeadline() {
        return closeAt != null;
    }

    public boolean isExpired() {
        return hasDeadline() && LocalDateTime.now().isAfter(closeAt);
    }

    public boolean canViewResults() {
        if ("ADMIN_ONLY".equals(resultsVisibility)) {
            return false; // Members can't see, only admin
        }
        if ("HIDDEN_UNTIL_CLOSE".equals(resultsVisibility)) {
            return isClosed();
        }
        return true; // LIVE results
    }

    public int getTotalVotes() {
        return votes.size();
    }

    public void addOption(PollOption option) {
        options.add(option);
        option.setPoll(this);
    }

    public void removeOption(PollOption option) {
        options.remove(option);
        option.setPoll(null);
    }
}
