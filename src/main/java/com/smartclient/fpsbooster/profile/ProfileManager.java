package com.smartclient.fpsbooster.profile;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.hardware.HardwareDetector;
import com.smartclient.fpsbooster.hardware.HardwareTier;
import com.smartclient.fpsbooster.optimization.DynamicTuner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class ProfileManager {
    private final ModConfig config;
    private final HardwareDetector hardware;
    private OptimizationProfile currentProfile;
    private OptimizationProfile recommendedProfile;
    
    public ProfileManager(ModConfig config, HardwareDetector hardware) {
        this.config = config;
        this.hardware = hardware;
        this.recommendedProfile = calculateRecommendedProfile();
        this.currentProfile = config.getProfile() != null ? 
            config.getProfile() : recommendedProfile;
    }
    
    private OptimizationProfile calculateRecommendedProfile() {
        HardwareTier tier = hardware.getTier();
        boolean isLaptop = hardware.isLaptop();
        
        if (isLaptop) {
            return OptimizationProfile.BATTERY_SAVER;
        }
        
        return switch (tier) {
            case HIGH_END -> OptimizationProfile.BALANCED;
            case MID_RANGE -> OptimizationProfile.BALANCED;
            case LOW_END -> OptimizationProfile.MAX_FPS;
        };
    }
    
    public void applyProfile(OptimizationProfile profile) {
        this.currentProfile = profile;
        config.setProfile(profile);
        
        Minecraft client = Minecraft.getInstance();
        Options options = client.options;
        
        // Apply settings
        options.renderDistance().set(profile.getRenderDistance());
        options.graphicsPreset().set(profile.getGraphicsMode());
        // Note: Particles setting handled via OptionInstance directly
        options.cloudStatus().set(profile.getCloudMode());
        options.entityDistanceScaling().set(profile.getEntityDistancePercent() / 100.0);
        options.enableVsync().set(profile.isVsync());
        options.ambientOcclusion().set(profile.isSmoothLighting());
        options.mipmapLevels().set(profile.getMipmapLevels());
        
        // Save options
        options.save();
        
        // Notify DynamicTuner that profile changed - reset adjustments
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        if (mod != null && mod.getOptimizationManager() != null) {
            DynamicTuner tuner = mod.getOptimizationManager().getDynamicTuner();
            if (tuner != null) {
                tuner.onProfileChange();
            }
        }
        
        SmartFPSBoosterClient.LOGGER.info("Applied profile: {}", profile.getDisplayName());
    }
    
    public void cycleProfile() {
        OptimizationProfile[] profiles = OptimizationProfile.values();
        int currentIndex = currentProfile.ordinal();
        int nextIndex = (currentIndex + 1) % (profiles.length - 1); // Skip CUSTOM
        applyProfile(profiles[nextIndex]);
    }
    
    public OptimizationProfile getCurrentProfile() { return currentProfile; }
    public OptimizationProfile getRecommendedProfile() { return recommendedProfile; }
}
