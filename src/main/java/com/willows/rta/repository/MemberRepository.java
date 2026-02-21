package com.willows.rta.repository;

import com.willows.rta.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    Optional<Member> findByEmail(String email);
    
    List<Member> findByMembershipStatus(String status);
    
    List<Member> findByFlatNumber(String flatNumber);
    
    boolean existsByEmail(String email);

    /**
     * Count active members by address containing block name (case-insensitive)
     */
    int countByMembershipStatusAndAddressContainingIgnoreCase(String membershipStatus, String addressFragment);

    /**
     * Count active members by checking if EITHER flat_number OR address contains the block name
     * Uses DISTINCT to avoid double-counting if both fields match
     */
    @Query("SELECT COUNT(DISTINCT m) FROM Member m WHERE m.membershipStatus = :status " +
        "AND (LOWER(m.flatNumber) LIKE LOWER(CONCAT('%', :blockName, '%')) " +
        "OR LOWER(m.address) LIKE LOWER(CONCAT('%', :blockName, '%')))")
    int countActiveByBlockName(
        @Param("status") String membershipStatus,
        @Param("blockName") String blockName
    );
}
