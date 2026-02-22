package com.willows.rta.model;

/**
 * Data Transfer Object for block analytics
 */
public class BlockStats {
    
    private Long blockId;
    private String blockName;
    private String shortName;
    private int totalFlats;
    private int currentMembers;
    private double percentage;
    private String statusColor; // green, yellow, orange, red
    private String statusIcon;  // ✅, ⚠️, 🚨
    
    public BlockStats(Long blockId, String blockName, String shortName, int totalFlats, int currentMembers) {
        this.blockId = blockId;
        this.blockName = blockName;
        this.shortName = shortName;
        this.totalFlats = totalFlats;
        this.currentMembers = currentMembers;
        this.percentage = totalFlats > 0 ? (currentMembers * 100.0 / totalFlats) : 0;
        this.statusColor = calculateStatusColor();
        this.statusIcon = calculateStatusIcon();
    }
    
    private String calculateStatusColor() {
        if (percentage >= 70) return "green";
        if (percentage >= 50) return "yellow";
        if (percentage >= 30) return "orange";
        return "red";
    }
    
    private String calculateStatusIcon() {
        if (percentage >= 70) return "✅";
        if (percentage >= 50) return "⚠️";
        return "🚨";
    }
    
    // Getters and Setters
    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getTotalFlats() {
        return totalFlats;
    }

    public void setTotalFlats(int totalFlats) {
        this.totalFlats = totalFlats;
    }

    public int getCurrentMembers() {
        return currentMembers;
    }

    public void setCurrentMembers(int currentMembers) {
        this.currentMembers = currentMembers;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }

    public String getStatusIcon() {
        return statusIcon;
    }

    public void setStatusIcon(String statusIcon) {
        this.statusIcon = statusIcon;
    }

    public String getFormattedPercentage() {
        return String.format("%.0f%%", percentage);
    }
}
