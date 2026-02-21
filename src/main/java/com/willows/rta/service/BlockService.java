package com.willows.rta.service;

import com.willows.rta.model.Block;
import com.willows.rta.model.BlockStats;
import com.willows.rta.repository.BlockRepository;
import com.willows.rta.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public BlockService(BlockRepository blockRepository, MemberRepository memberRepository) {
        this.blockRepository = blockRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Get all active blocks ordered by display order
     */
    public List<Block> getAllActiveBlocks() {
        return blockRepository.findByActiveTrueOrderByDisplayOrder();
    }

    /**
     * Get all blocks (including inactive) ordered by display order
     */
    public List<Block> getAllBlocks() {
        return blockRepository.findAllByOrderByDisplayOrder();
    }

    /**
     * Get block by ID
     */
    public Optional<Block> getBlockById(Long id) {
        return blockRepository.findById(id);
    }

    /**
     * Create new block
     */
    @Transactional
    public Block createBlock(Block block) {
        return blockRepository.save(block);
    }

    /**
     * Update existing block
     */
    @Transactional
    public Block updateBlock(Long id, Block updatedBlock) {
        Optional<Block> existingBlock = blockRepository.findById(id);
        if (existingBlock.isPresent()) {
            Block block = existingBlock.get();
            block.setName(updatedBlock.getName());
            block.setShortName(updatedBlock.getShortName());
            block.setTotalFlats(updatedBlock.getTotalFlats());
            block.setDisplayOrder(updatedBlock.getDisplayOrder());
            block.setActive(updatedBlock.getActive());
            return blockRepository.save(block);
        }
        return null;
    }

    /**
     * Delete block
     */
    @Transactional
    public void deleteBlock(Long id) {
        blockRepository.deleteById(id);
    }

    /**
     * Check if block name exists
     */
    public boolean blockNameExists(String name) {
        return blockRepository.existsByName(name);
    }

    /**
     * Calculate membership stats for all active blocks
     */
    public List<BlockStats> calculateBlockStats() {
        List<Block> blocks = getAllActiveBlocks();
        List<BlockStats> statsList = new ArrayList<>();

        for (Block block : blocks) {
            int memberCount = countMembersByBlock(block);
            BlockStats stats = new BlockStats(
                block.getId(),
                block.getName(),
                block.getShortName(),
                block.getTotalFlats(),
                memberCount
            );
            statsList.add(stats);
        }

        return statsList;
    }

    /**
     * Count active members in a block by checking if flat_number OR address contains block short name
     * Uses DISTINCT to avoid double-counting if both fields match
     */
    private int countMembersByBlock(Block block) {
        // Use SHORT name for flexibility (e.g., "Field" instead of "Field House")
        // Matches in EITHER flat_number OR address fields
        return memberRepository.countActiveByBlockName("ACTIVE", block.getShortName());
    }

    /**
     * Calculate overall statistics
     */
    public OverallStats calculateOverallStats() {
        List<BlockStats> blockStats = calculateBlockStats();
        
        int totalMembers = blockStats.stream()
                .mapToInt(BlockStats::getCurrentMembers)
                .sum();
        
        int totalFlats = blockStats.stream()
                .mapToInt(BlockStats::getTotalFlats)
                .sum();
        
        double overallPercentage = totalFlats > 0 ? (totalMembers * 100.0 / totalFlats) : 0;
        
        return new OverallStats(totalMembers, totalFlats, overallPercentage, blockStats.size());
    }

    /**
     * Inner class for overall statistics
     */
    public static class OverallStats {
        private int totalMembers;
        private int totalFlats;
        private double percentage;
        private int activeBlocks;

        public OverallStats(int totalMembers, int totalFlats, double percentage, int activeBlocks) {
            this.totalMembers = totalMembers;
            this.totalFlats = totalFlats;
            this.percentage = percentage;
            this.activeBlocks = activeBlocks;
        }

        public int getTotalMembers() {
            return totalMembers;
        }

        public int getTotalFlats() {
            return totalFlats;
        }

        public double getPercentage() {
            return percentage;
        }

        public int getActiveBlocks() {
            return activeBlocks;
        }

        public String getFormattedPercentage() {
            return String.format("%.0f%%", percentage);
        }
    }
}
