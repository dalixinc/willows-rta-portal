package com.willows.rta.repository;

import com.willows.rta.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    Optional<Member> findByEmail(String email);
    
    List<Member> findByMembershipStatus(String status);
    
    List<Member> findByFlatNumber(String flatNumber);
    
    boolean existsByEmail(String email);
}
