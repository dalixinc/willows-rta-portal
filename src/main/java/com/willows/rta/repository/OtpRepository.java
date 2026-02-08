package com.willows.rta.repository;

import com.willows.rta.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpCode, Long> {
    
    Optional<OtpCode> findTopByUsernameAndUsedFalseOrderByCreatedAtDesc(String username);
    
    List<OtpCode> findByUsernameAndUsedFalse(String username);
    
    void deleteByExpiryTimeBefore(LocalDateTime dateTime);
}
