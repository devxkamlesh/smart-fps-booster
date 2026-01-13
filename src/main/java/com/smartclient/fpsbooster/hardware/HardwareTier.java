package com.smartclient.fpsbooster.hardware;

public enum HardwareTier {
    LOW_END("Low-End", "Basic optimization for older hardware"),
    MID_RANGE("Mid-Range", "Balanced performance and visuals"),
    HIGH_END("High-End", "Maximum quality with good performance");
    
    private final String displayName;
    private final String description;
    
    HardwareTier(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}
