package com.smartclient.fpsbooster.optimization;

import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class DynamicTuner {
    private final ModConfig config;
    private final ProfileManager profileManager;
    
    private int currentAdjustment = 0;
    private long lastAdjustmentTime = 0;
    private static final long ADJUSTMENT_COOLDOWN = 60000; // 60 seconds - very conservative
    
    // Movement detection
    private boolean isMoving = false;
    private boolean isSprintingOrFlying = false;
    private double lastX, lastY, lastZ;
    
    // Combat detection
    private boolean inCombat = false;
    private long lastCombatTime = 0;
    private static final long COMBAT_TIMEOUT = 5000;
    
    // Heavy scene detection
    private boolean heavyScene = false;
    private int lowFpsCounter = 0;
    
    // Dimension tracking
    private String currentDimension = "overworld";
    
    // Track last render distance we set - don't change if same
    private int lastSetRenderDistance = -1;
    
    public DynamicTuner(ModConfig config, ProfileManager profileManager) {
        this.config = config;
        this.profileManager = profileManager;
    }
    
    private int getProfileRenderDistance() {
        return profileManager.getCurrentProfile().getRenderDistance();
    }
    
    public void evaluate(FPSMonitor monitor) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.level == null) return;
        
        Options options = client.options;
        if (options == null) return;
        
        int targetFps = config.getTargetFps();
        int currentFps = monitor.getCurrentFps();
        int avgFps = monitor.getAverageFps();
        
        // Update detection states (these are just for display, don't change settings)
        if (config.isMovementBasedTuning()) {
            updateMovementState(client);
        }
        if (config.isCombatOptimization()) {
            updateCombatState(client);
        }
        if (config.isDimensionPresets()) {
            updateDimensionState(client);
        }
        
        // Heavy scene detection
        if (config.isHeavySceneDetection()) {
            if (currentFps < 15 && avgFps < 20) {
                lowFpsCounter++;
            } else {
                lowFpsCounter = Math.max(0, lowFpsCounter - 1);
            }
            heavyScene = lowFpsCounter > 10; // Need 10+ seconds of very low FPS
        }
        
        // IMPORTANT: Don't change render distance if FPS is acceptable
        // This prevents the chunk reload issue
        if (currentFps >= 30 && avgFps >= targetFps * 0.5) {
            return; // FPS is acceptable, don't touch render distance
        }
        
        // Check cooldown
        long now = System.currentTimeMillis();
        if (now - lastAdjustmentTime < ADJUSTMENT_COOLDOWN) return;
        
        // Check if render distance is locked
        if (config.isSettingLocked("render_distance")) return;
        
        int currentRd = options.renderDistance().get();
        
        // Only reduce if FPS is CRITICALLY low (below 20 FPS average for extended time)
        boolean criticallyLowFps = avgFps < 20 && lowFpsCounter > 5;
        
        if (!criticallyLowFps) {
            return; // Don't change anything
        }
        
        // Only reduce, never increase (increasing causes chunk loads)
        if (currentRd > 6 && currentRd != lastSetRenderDistance) {
            int newValue = Math.max(6, currentRd - 2);
            options.renderDistance().set(newValue);
            lastSetRenderDistance = newValue;
            currentAdjustment = getProfileRenderDistance() - newValue;
            lastAdjustmentTime = now;
        }
    }
    
    private void updateMovementState(Minecraft client) {
        if (client.player == null) return;
        
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        
        double dx = x - lastX;
        double dy = y - lastY;
        double dz = z - lastZ;
        double distSq = dx * dx + dy * dy + dz * dz;
        
        isMoving = distSq > 0.01;
        isSprintingOrFlying = client.player.isSprinting() || client.player.isFallFlying() || 
                              (client.player.getAbilities().flying && distSq > 0.1);
        
        lastX = x;
        lastY = y;
        lastZ = z;
    }
    
    private void updateCombatState(Minecraft client) {
        if (client.player == null) return;
        
        long now = System.currentTimeMillis();
        
        if (client.player.hurtTime > 0 || client.player.swinging) {
            lastCombatTime = now;
            inCombat = true;
        }
        
        if (now - lastCombatTime > COMBAT_TIMEOUT) {
            inCombat = false;
        }
    }
    
    private void updateDimensionState(Minecraft client) {
        if (client.level == null) return;
        
        ResourceKey<Level> dimKey = client.level.dimension();
        if (dimKey == null) return;
        
        currentDimension = dimKey.identifier().getPath();
    }
    
    public void onProfileChange() {
        currentAdjustment = 0;
        lastAdjustmentTime = 0;
        lowFpsCounter = 0;
        lastSetRenderDistance = -1;
    }
    
    // Getters
    public int getCurrentAdjustment() { return currentAdjustment; }
    public boolean isMoving() { return isMoving; }
    public boolean isInCombat() { return inCombat; }
    public boolean isHeavyScene() { return heavyScene; }
    public int getNearbyEntityCount() { return 0; }
    public String getCurrentDimension() { return currentDimension; }
    
    public void reset() {
        currentAdjustment = 0;
        lowFpsCounter = 0;
        lastSetRenderDistance = -1;
    }
}
