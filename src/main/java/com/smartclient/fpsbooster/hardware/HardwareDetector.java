package com.smartclient.fpsbooster.hardware;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import net.minecraft.client.MinecraftClient;

public class HardwareDetector {
    private int cpuCores;
    private long totalMemoryMB;
    private long allocatedMemoryMB;
    private String gpuVendor = "Unknown";
    private String gpuRenderer = "Unknown";
    private int screenWidth;
    private int screenHeight;
    private String osName;
    private HardwareTier tier = HardwareTier.MID_RANGE; // Default to mid-range
    private boolean gpuDetected = false;
    
    public void detect() {
        detectCPU();
        detectMemory();
        detectOS();
        // GPU detection deferred until OpenGL context is ready
        // Screen detection deferred until window is ready
        calculateTier();
        
        SmartFPSBoosterClient.LOGGER.info("Hardware Detection (initial):");
        SmartFPSBoosterClient.LOGGER.info("  CPU Cores: {}", cpuCores);
        SmartFPSBoosterClient.LOGGER.info("  Memory: {}MB allocated", allocatedMemoryMB);
        SmartFPSBoosterClient.LOGGER.info("  Tier: {}", tier);
    }
    
    // Call this later when OpenGL context is available
    public void detectGPUDeferred() {
        if (gpuDetected) return;
        
        try {
            gpuVendor = org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_VENDOR);
            gpuRenderer = org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER);
            gpuDetected = true;
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getWindow() != null) {
                screenWidth = client.getWindow().getWidth();
                screenHeight = client.getWindow().getHeight();
            }
            
            // Recalculate tier with GPU info
            calculateTier();
            
            SmartFPSBoosterClient.LOGGER.info("GPU Detected: {} - {}", gpuVendor, gpuRenderer);
        } catch (Exception e) {
            SmartFPSBoosterClient.LOGGER.warn("Could not detect GPU: {}", e.getMessage());
        }
    }
    
    private void detectCPU() {
        cpuCores = Runtime.getRuntime().availableProcessors();
    }
    
    private void detectMemory() {
        Runtime runtime = Runtime.getRuntime();
        totalMemoryMB = runtime.maxMemory() / (1024 * 1024);
        allocatedMemoryMB = totalMemoryMB;
    }
    
    private void detectOS() {
        osName = System.getProperty("os.name");
    }
    
    private void calculateTier() {
        int score = 0;
        
        // CPU scoring
        if (cpuCores >= 8) score += 3;
        else if (cpuCores >= 4) score += 2;
        else score += 1;
        
        // Memory scoring
        if (allocatedMemoryMB >= 8192) score += 3;
        else if (allocatedMemoryMB >= 4096) score += 2;
        else score += 1;
        
        // GPU scoring (basic heuristics)
        if (gpuDetected && gpuRenderer != null) {
            String gpuLower = gpuRenderer.toLowerCase();
            if (gpuLower.contains("rtx") || gpuLower.contains("rx 6") || gpuLower.contains("rx 7")) {
                score += 3;
            } else if (gpuLower.contains("gtx 16") || gpuLower.contains("gtx 10") || 
                       gpuLower.contains("rx 5") || gpuLower.contains("radeon") ||
                       gpuLower.contains("1650") || gpuLower.contains("1660")) {
                score += 2;
            } else {
                score += 1;
            }
        } else {
            score += 2; // Default mid-range if unknown
        }
        
        // Resolution penalty
        if (screenWidth * screenHeight > 2073600) { // > 1080p
            score -= 1;
        }
        
        // Determine tier
        if (score >= 8) tier = HardwareTier.HIGH_END;
        else if (score >= 5) tier = HardwareTier.MID_RANGE;
        else tier = HardwareTier.LOW_END;
    }
    
    public String getSummary() {
        return String.format("%s (%d cores, %dMB RAM, %s)", 
            tier, cpuCores, allocatedMemoryMB, gpuRenderer);
    }
    
    // Getters
    public int getCpuCores() { return cpuCores; }
    public long getTotalMemoryMB() { return totalMemoryMB; }
    public long getAllocatedMemoryMB() { return allocatedMemoryMB; }
    public String getGpuVendor() { return gpuVendor; }
    public String getGpuRenderer() { return gpuRenderer; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public String getOsName() { return osName; }
    public HardwareTier getTier() { return tier; }
    
    public boolean isLaptop() {
        if (!gpuDetected) return false;
        String gpuLower = gpuRenderer.toLowerCase();
        return gpuLower.contains("intel") || 
               gpuLower.contains("mobile") || 
               gpuLower.contains("laptop") ||
               gpuLower.contains("max-q") ||
               osName.toLowerCase().contains("mac");
    }
}
