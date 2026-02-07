package com.willows.rta.service;

import com.willows.rta.model.Member;
import com.willows.rta.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // Register a new member
    public Member registerMember(Member member) {
        // Check if email already exists
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new RuntimeException("A member with this email already exists");
        }
        return memberRepository.save(member);
    }

    // Get all members
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // Get active members only
    public List<Member> getActiveMembers() {
        return memberRepository.findByMembershipStatus("ACTIVE");
    }

    // Get member by ID
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    // Get member by email
    public Optional<Member> getMemberByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    // Update member details
    public Member updateMember(Long id, Member memberDetails) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
        
        member.setFullName(memberDetails.getFullName());
        member.setFlatNumber(memberDetails.getFlatNumber());
        member.setAddress(memberDetails.getAddress());
        member.setEmail(memberDetails.getEmail());
        member.setPhoneNumber(memberDetails.getPhoneNumber());
        member.setLeaseholder(memberDetails.isLeaseholder());
        member.setPreferredCommunication(memberDetails.getPreferredCommunication());
        
        return memberRepository.save(member);
    }

    // Update membership status
    public Member updateMembershipStatus(Long id, String status) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
        
        member.setMembershipStatus(status);
        return memberRepository.save(member);
    }

    // Delete member
    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

    // Get total member count
    public long getTotalMemberCount() {
        return memberRepository.count();
    }

    // Get active member count
    public long getActiveMemberCount() {
        return memberRepository.findByMembershipStatus("ACTIVE").size();
    }
}
