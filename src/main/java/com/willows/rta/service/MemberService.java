package com.willows.rta.service;

import com.willows.rta.model.Member;
import com.willows.rta.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // Check if email exists
    public boolean emailExists(String email) {
        return memberRepository.existsByEmail(email);
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
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
        
        // If member has a user account, we need to delete it first due to foreign key constraint
        // Note: This will be handled in the service layer by finding and deleting the user
        
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

    // Update member account creation status
    public Member updateMemberAccountStatus(Long id, boolean hasAccount, String creationMethod) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
        
        member.setHasUserAccount(hasAccount);
        member.setAccountCreationMethod(creationMethod);
        return memberRepository.save(member);
    }

    // Get members without user accounts
    public List<Member> getMembersWithoutAccounts() {
        return memberRepository.findAll().stream()
                .filter(m -> !m.isHasUserAccount())
                .toList();
    }

    // Get members by status
    public List<Member> getMembersByStatus(String status) {
        return memberRepository.findByMembershipStatus(status);
    }

    // Get members by status with pagination (NEW METHOD)
    public Page<Member> getMembersByStatusPaginated(String status, Pageable pageable) {
        return memberRepository.findByMembershipStatus(status, pageable);
    }

   // Get filtered members for export
    public List<Member> getFilteredMembers(String block, String status, Boolean hasAccount) {
        List<Member> allMembers = memberRepository.findAll();
        
        return allMembers.stream()
            .filter(m -> {
                // Filter by status if provided
                if (status != null && !status.isEmpty()) {
                    if (!status.equals(m.getMembershipStatus())) {
                        return false;
                    }
                }
                
                // Filter by account status if provided
                if (hasAccount != null) {
                    if (hasAccount != m.isHasUserAccount()) {
                        return false;
                    }
                }
                
                // Filter by block if provided
                if (block != null && !block.isEmpty()) {
                    String flatNum = m.getFlatNumber() != null ? m.getFlatNumber().toLowerCase() : "";
                    String address = m.getAddress() != null ? m.getAddress().toLowerCase() : "";
                    String blockLower = block.toLowerCase();
                    
                    if (!flatNum.contains(blockLower) && !address.contains(blockLower)) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Retrieves all members with pagination.
     * 
     * @param pageable the pagination parameters
     * @return a page of members
     */
    public Page<Member> findAll(Pageable pageable) {
        return memberRepository.findAll(pageable);
    }
}
