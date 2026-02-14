package com.willows.rta.repository;

import com.willows.rta.model.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    // Get all notices ordered by pinned first, then by date (newest first)
    @Query("SELECT n FROM Notice n ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findAllOrderedByPinnedAndDate();
}
