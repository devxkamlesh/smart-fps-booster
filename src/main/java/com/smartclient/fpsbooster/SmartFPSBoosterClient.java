package com.smartclient.fpsbooster;

import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.hardware.HardwareDetector;
import com.smartclient.fpsbooster.optimization.OptimizationManager;
import com.smartclient.fpsbooster.profile.ProfileManager;
import com.smartclient.fpsbooster.ui.KeybindManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartFPSBoosterClient implements ClientModInitializer {
    public static final String MOD_ID = "smartfpsbooster";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static SmartFPSBoosterClient instance;
    private ModConfig config;
    private HardwareDetector hardwareDetector;
    private ProfileManager profileManager;
    private OptimizationManager optimizationManager;
    private boolean gpuDetectionDone = false;
    
    @Override
    public void onInitializeClient() {
        instance = this;
        LOGGER.info("Smart FPS Booster initializing...");
        
        // Load config
        config = ModConfig.load();
        
        // Initialize hardware detection
        hardwareDetector = new HardwareDetector();
        hardwareDetector.detect();
        
        // Initialize profile manager
        profileManager = new ProfileManager(config, hardwareDetector);
        
        // Initialize optimization manager
        optimizationManager = new OptimizationManager(config, profileManager);
        
        // Register keybinds
        KeybindManager.register();
        
        // Register FPS overlay
        com.smartclient.fpsbooster.ui.FPSOverlay.register();
        
        // Register tick event for monitoring
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Deferred GPU detection - runs once when OpenGL context is ready
            if (!gpuDetectionDone && client.getWindow() != null) {
                try {
                    hardwareDetector.detectGPUDeferred();
                    gpuDetectionDone = true;
                    LOGGER.info("GPU detection complete: {}", hardwareDetector.getGpuRenderer());
                } catch (Exception e) {
                    LOGGER.warn("GPU detection failed, will retry: {}", e.getMessage());
                }
            }
            
            if (client.world != null) {
                optimizationManager.tick();
            }
        });
        
        LOGGER.info("Smart FPS Booster initialized! Hardware: {}", hardwareDetector.getSummary());
    }
    
    public static SmartFPSBoosterClient getInstance() {
        return instance;
    }
    
    public ModConfig getConfig() {
        return config;
    }
    
    public HardwareDetector getHardwareDetector() {
        return hardwareDetector;
    }
    
    public ProfileManager getProfileManager() {
        return profileManager;
    }
    
    public OptimizationManager getOptimizationManager() {
        return optimizationManager;
    }
}
