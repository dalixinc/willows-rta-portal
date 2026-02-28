package com.willows.rta.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Vote entity - represents a member's vote on a poll option
 */
@Entity
@Table(name = "votes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"poll_id", "member_id", "option_id"})
})
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private PollOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "voted_at")
    private LocalDateTime votedAt;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    // Constructors
    public Vote() {
        this.votedAt = LocalDateTime.now();
    }

    public Vote(Poll poll, PollOption option, Member member, Boolean isAnonymous) {
        this();
        this.poll = poll;
        this.option = option;
        this.member = member;
        this.isAnonymous = isAnonymous;
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

    public PollOption getOption() {
        return option;
    }

    public void setOption(PollOption option) {
        this.option = option;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDateTime getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(LocalDateTime votedAt) {
        this.votedAt = votedAt;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    // Helper methods
    public boolean isAnonymous() {
        return Boolean.TRUE.equals(isAnonymous);
    }
}
