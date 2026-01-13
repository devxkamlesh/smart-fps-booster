package com.smartclient.fpsbooster.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.profile.OptimizationProfile;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
        .getConfigDir().resolve("smartfpsbooster.json");
    
    // Settings - DEFAULTS OPTIMIZED FOR BEST PERFORMANCE
    private OptimizationProfile profile = OptimizationProfile.MAX_FPS;  // Default to Max FPS
    private int targetFps = 120;  // Higher target for better responsiveness
    private int customTargetFps = 120;
    private boolean autoOptimize = true;  // Auto-optimize ON by default
    private boolean showFpsOverlay = true;
    private OverlayPosition overlayPosition = OverlayPosition.TOP_LEFT;
    private OverlayStyle overlayStyle = OverlayStyle.MINIMAL;  // Minimal overlay = less overhead
    private int overlayX = 5;
    private int overlayY = 5;
    private boolean useCustomPosition = false;
    private boolean dynamicTuning = true;  // Dynamic tuning ON
    private boolean firstLaunch = true;
    
    // v1.1 - Locked settings
    private Set<String> lockedSettings = new HashSet<>();
    
    // v1.1 - Whitelist visuals (settings that won't be reduced)
    private Set<String> whitelistedVisuals = new HashSet<>();
    
    // v1.1 - Dynamic tuning options
    private boolean movementBasedTuning = true;
    private boolean combatOptimization = true;
    private boolean heavySceneDetection = true;
    private boolean dimensionPresets = true;
    
    // v2.0 - Server/World profiles
    private OptimizationProfile singleplayerProfile = null;
    private OptimizationProfile multiplayerProfile = null;
    private boolean useContextProfiles = false;
    
    // v2.0 - Benchmark
    private int lastBenchmarkScore = 0;
    private long lastBenchmarkTime = 0;
    
    // v2.0 - Notifications
    private boolean showNotifications = true;
    
    // v2.0 - Performance history (last 10 sessions avg FPS)
    private int[] fpsHistory = new int[10];
    private int historyIndex = 0;
    
    public static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                ModConfig config = GSON.fromJson(json, ModConfig.class);
                if (config.lockedSettings == null) config.lockedSettings = new HashSet<>();
                if (config.whitelistedVisuals == null) config.whitelistedVisuals = new HashSet<>();
                SmartFPSBoosterClient.LOGGER.info("Config loaded from {}", CONFIG_PATH);
                return config;
            } catch (IOException e) {
                SmartFPSBoosterClient.LOGGER.error("Failed to load config", e);
            }
        }
        return new ModConfig();
    }
    
    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            SmartFPSBoosterClient.LOGGER.error("Failed to save config", e);
        }
    }
    
    // Profile
    public OptimizationProfile getProfile() { return profile; }
    public void setProfile(OptimizationProfile profile) { 
        this.profile = profile; 
        save();
    }
    
    // Target FPS
    public int getTargetFps() { return targetFps; }
    public void setTargetFps(int targetFps) { 
        this.targetFps = targetFps; 
        save();
    }
    
    public int getCustomTargetFps() { return customTargetFps; }
    public void setCustomTargetFps(int fps) {
        this.customTargetFps = fps;
        save();
    }
    
    // Auto optimize
    public boolean isAutoOptimize() { return autoOptimize; }
    public void setAutoOptimize(boolean autoOptimize) { 
        this.autoOptimize = autoOptimize; 
        save();
    }
    
    // Overlay
    public boolean isShowFpsOverlay() { return showFpsOverlay; }
    public void setShowFpsOverlay(boolean show) { 
        this.showFpsOverlay = show; 
        save();
    }
    
    public OverlayPosition getOverlayPosition() { return overlayPosition; }
    public void setOverlayPosition(OverlayPosition pos) { 
        this.overlayPosition = pos; 
        save();
    }
    
    public OverlayStyle getOverlayStyle() { return overlayStyle; }
    public void setOverlayStyle(OverlayStyle style) { 
        this.overlayStyle = style; 
        save();
    }
    
    public int getOverlayX() { return overlayX; }
    public void setOverlayX(int x) { this.overlayX = x; }
    
    public int getOverlayY() { return overlayY; }
    public void setOverlayY(int y) { this.overlayY = y; }
    
    public boolean isUseCustomPosition() { return useCustomPosition; }
    public void setUseCustomPosition(boolean use) { 
        this.useCustomPosition = use; 
        save();
    }
    
    public void saveOverlayPosition() { save(); }
    
    // Dynamic tuning
    public boolean isDynamicTuning() { return dynamicTuning; }
    public void setDynamicTuning(boolean enabled) { 
        this.dynamicTuning = enabled; 
        save();
    }
    
    public boolean isMovementBasedTuning() { return movementBasedTuning; }
    public void setMovementBasedTuning(boolean enabled) {
        this.movementBasedTuning = enabled;
        save();
    }
    
    public boolean isCombatOptimization() { return combatOptimization; }
    public void setCombatOptimization(boolean enabled) {
        this.combatOptimization = enabled;
        save();
    }
    
    public boolean isHeavySceneDetection() { return heavySceneDetection; }
    public void setHeavySceneDetection(boolean enabled) {
        this.heavySceneDetection = enabled;
        save();
    }
    
    public boolean isDimensionPresets() { return dimensionPresets; }
    public void setDimensionPresets(boolean enabled) {
        this.dimensionPresets = enabled;
        save();
    }
    
    // Locked settings
    public Set<String> getLockedSettings() { return lockedSettings; }
    public void lockSetting(String setting) { 
        lockedSettings.add(setting); 
        save();
    }
    public void unlockSetting(String setting) { 
        lockedSettings.remove(setting); 
        save();
    }
    public boolean isSettingLocked(String setting) { 
        return lockedSettings.contains(setting); 
    }
    public void toggleSettingLock(String setting) {
        if (lockedSettings.contains(setting)) {
            lockedSettings.remove(setting);
        } else {
            lockedSettings.add(setting);
        }
        save();
    }
    
    // Whitelisted visuals
    public Set<String> getWhitelistedVisuals() { return whitelistedVisuals; }
    public void whitelistVisual(String visual) {
        whitelistedVisuals.add(visual);
        save();
    }
    public void unwhitelistVisual(String visual) {
        whitelistedVisuals.remove(visual);
        save();
    }
    public boolean isVisualWhitelisted(String visual) {
        return whitelistedVisuals.contains(visual);
    }
    public void toggleVisualWhitelist(String visual) {
        if (whitelistedVisuals.contains(visual)) {
            whitelistedVisuals.remove(visual);
        } else {
            whitelistedVisuals.add(visual);
        }
        save();
    }
    
    // First launch
    public boolean isFirstLaunch() { return firstLaunch; }
    public void setFirstLaunch(boolean firstLaunch) { 
        this.firstLaunch = firstLaunch; 
        save();
    }
    
    public enum OverlayPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
    
    public enum OverlayStyle {
        MINIMAL, COMPACT, DETAILED
    }
    
    // v2.0 - Context profiles (singleplayer/multiplayer)
    public OptimizationProfile getSingleplayerProfile() { return singleplayerProfile; }
    public void setSingleplayerProfile(OptimizationProfile p) { singleplayerProfile = p; save(); }
    
    public OptimizationProfile getMultiplayerProfile() { return multiplayerProfile; }
    public void setMultiplayerProfile(OptimizationProfile p) { multiplayerProfile = p; save(); }
    
    public boolean isUseContextProfiles() { return useContextProfiles; }
    public void setUseContextProfiles(boolean use) { useContextProfiles = use; save(); }
    
    // v2.0 - Benchmark
    public int getLastBenchmarkScore() { return lastBenchmarkScore; }
    public void setLastBenchmarkScore(int score) { lastBenchmarkScore = score; save(); }
    
    public long getLastBenchmarkTime() { return lastBenchmarkTime; }
    public void setLastBenchmarkTime(long time) { lastBenchmarkTime = time; save(); }
    
    // v2.0 - Notifications
    public boolean isShowNotifications() { return showNotifications; }
    public void setShowNotifications(boolean show) { showNotifications = show; save(); }
    
    // v2.0 - Performance history
    public int[] getFpsHistory() { return fpsHistory; }
    public void addFpsToHistory(int avgFps) {
        if (fpsHistory == null) fpsHistory = new int[10];
        fpsHistory[historyIndex % 10] = avgFps;
        historyIndex++;
        save();
    }
    public int getAverageHistoricalFps() {
        if (fpsHistory == null) return 0;
        int sum = 0, count = 0;
        for (int fps : fpsHistory) {
            if (fps > 0) { sum += fps; count++; }
        }
        return count > 0 ? sum / count : 0;
    }
    
    // v2.0 - Export config as string
    public String exportConfig() {
        return GSON.toJson(this);
    }
    
    // v2.0 - Import config from string
    public static ModConfig importConfig(String json) {
        try {
            ModConfig config = GSON.fromJson(json, ModConfig.class);
            if (config != null) {
                config.save();
                return config;
            }
        } catch (Exception e) {
            SmartFPSBoosterClient.LOGGER.error("Failed to import config", e);
        }
        return null;
    }
}
