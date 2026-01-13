package com.smartclient.fpsbooster.compat;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Sodium Compatibility Layer
 * 
 * When Sodium is installed:
 * - We detect it and adjust our behavior
 * - We don't touch render settings that Sodium manages better
 * - We focus on profiling, monitoring, and non-rendering optimizations
 */
public class SodiumCompat {
    private static boolean sodiumLoaded = false;
    private static boolean irisLoaded = false;
    private static boolean lithiumLoaded = false;
    private static boolean checked = false;
    
    public static void init() {
        if (checked) return;
        checked = true;
        
        FabricLoader loader = FabricLoader.getInstance();
        
        sodiumLoaded = loader.isModLoaded("sodium");
        irisLoaded = loader.isModLoaded("iris");
        lithiumLoaded = loader.isModLoaded("lithium");
        
        if (sodiumLoaded) {
            SmartFPSBoosterClient.LOGGER.info("Sodium detected - coordinating optimizations");
        }
        if (irisLoaded) {
            SmartFPSBoosterClient.LOGGER.info("Iris detected - shader-aware mode enabled");
        }
        if (lithiumLoaded) {
            SmartFPSBoosterClient.LOGGER.info("Lithium detected - game logic already optimized");
        }
    }
    
    /**
     * Check if Sodium is handling rendering
     */
    public static boolean isSodiumLoaded() {
        if (!checked) init();
        return sodiumLoaded;
    }
    
    /**
     * Check if Iris shaders are available
     */
    public static boolean isIrisLoaded() {
        if (!checked) init();
        return irisLoaded;
    }
    
    /**
     * Check if Lithium is optimizing game logic
     */
    public static boolean isLithiumLoaded() {
        if (!checked) init();
        return lithiumLoaded;
    }
    
    /**
     * Should we manage render distance?
     * If Sodium is loaded, we can still adjust it but be more conservative
     */
    public static boolean shouldManageRenderDistance() {
        return true; // We can still adjust, Sodium respects vanilla settings
    }
    
    /**
     * Should we manage graphics mode?
     * Sodium has its own quality settings
     */
    public static boolean shouldManageGraphicsMode() {
        return !sodiumLoaded; // Let Sodium handle if present
    }
    
    /**
     * Get optimization status string for dashboard
     */
    public static String getOptimizationStatus() {
        StringBuilder sb = new StringBuilder();
        if (sodiumLoaded) sb.append("Sodium ");
        if (irisLoaded) sb.append("Iris ");
        if (lithiumLoaded) sb.append("Lithium ");
        return sb.length() > 0 ? sb.toString().trim() : "Vanilla";
    }
    
    /**
     * Get recommended profile based on installed mods
     */
    public static String getRecommendation() {
        if (sodiumLoaded && lithiumLoaded) {
            return "Great setup! Use Balanced profile for best experience.";
        } else if (sodiumLoaded) {
            return "Sodium installed. Consider adding Lithium for more FPS.";
        } else {
            return "Install Sodium + Lithium for 2-3x more FPS!";
        }
    }
}
