package com.smartclient.fpsbooster.optimization;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.profile.OptimizationProfile;
import com.smartclient.fpsbooster.profile.ProfileManager;
import com.smartclient.fpsbooster.ui.NotificationManager;
import net.minecraft.client.MinecraftClient;

public class OptimizationManager {
    private final ModConfig config;
    private final ProfileManager profileManager;
    private final FPSMonitor fpsMonitor;
    private final DynamicTuner dynamicTuner;
    private final BenchmarkManager benchmarkManager;
    
    private int tickCounter = 0;
    private static final int MONITOR_INTERVAL = 100; // Every 5 seconds (was 20)
    
    // v2.0 - Context tracking
    private boolean wasMultiplayer = false;
    private int sessionTicks = 0;
    
    public OptimizationManager(ModConfig config, ProfileManager profileManager) {
        this.config = config;
        this.profileManager = profileManager;
        this.fpsMonitor = new FPSMonitor();
        this.dynamicTuner = new DynamicTuner(config, profileManager);
        this.benchmarkManager = new BenchmarkManager(config);
    }
    
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Update FPS monitor every tick
        fpsMonitor.update(client.getCurrentFps());
        
        // Update benchmark if running
        benchmarkManager.tick();
        
        // Update notifications
        NotificationManager.tick();
        
        // Track session
        sessionTicks++;
        
        tickCounter++;
        if (tickCounter >= MONITOR_INTERVAL) {
            tickCounter = 0;
            
            // v2.0 - Check context change (singleplayer/multiplayer)
            if (config.isUseContextProfiles()) {
                checkContextChange(client);
            }
            
            // Perform periodic optimization checks
            if (config.isAutoOptimize() && config.isDynamicTuning() && !benchmarkManager.isRunning()) {
                dynamicTuner.evaluate(fpsMonitor);
            }
        }
    }
    
    private void checkContextChange(MinecraftClient client) {
        boolean isMultiplayer = client.getCurrentServerEntry() != null;
        
        if (isMultiplayer != wasMultiplayer) {
            wasMultiplayer = isMultiplayer;
            
            OptimizationProfile contextProfile = isMultiplayer ? 
                config.getMultiplayerProfile() : config.getSingleplayerProfile();
            
            if (contextProfile != null && contextProfile != profileManager.getCurrentProfile()) {
                profileManager.applyProfile(contextProfile);
                if (config.isShowNotifications()) {
                    String context = isMultiplayer ? "Multiplayer" : "Singleplayer";
                    NotificationManager.showInfo("Switched to " + context + " profile: " + contextProfile.getDisplayName());
                }
            }
        }
    }
    
    public void onSessionEnd() {
        // Save average FPS to history when leaving world
        if (sessionTicks > 200) { // At least 10 seconds
            int avgFps = fpsMonitor.getAverageFps();
            if (avgFps > 0) {
                config.addFpsToHistory(avgFps);
            }
        }
        sessionTicks = 0;
    }
    
    public void toggleAutoOptimize() {
        config.setAutoOptimize(!config.isAutoOptimize());
        if (config.isShowNotifications()) {
            NotificationManager.showInfo("Auto-Optimize: " + (config.isAutoOptimize() ? "ON" : "OFF"));
        }
        SmartFPSBoosterClient.LOGGER.info("Auto-optimization: {}", 
            config.isAutoOptimize() ? "ON" : "OFF");
    }
    
    public FPSMonitor getFpsMonitor() { return fpsMonitor; }
    public DynamicTuner getDynamicTuner() { return dynamicTuner; }
    public BenchmarkManager getBenchmarkManager() { return benchmarkManager; }
}
