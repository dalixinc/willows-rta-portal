package com.willows.rta.repository;

import com.willows.rta.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    
    /**
     * Find all active blocks ordered by display order
     */
    List<Block> findByActiveTrueOrderByDisplayOrder();
    
    /**
     * Find all blocks ordered by display order
     */
    List<Block> findAllByOrderByDisplayOrder();
    
    /**
     * Find block by name
     */
    Optional<Block> findByName(String name);
    
    /**
     * Check if block name exists
     */
    boolean existsByName(String name);
    
    /**
     * Count active blocks
     */
    long countByActiveTrue();
}
