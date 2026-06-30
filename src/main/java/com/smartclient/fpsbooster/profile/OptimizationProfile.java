package com.smartclient.fpsbooster.profile;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsPreset;

public enum OptimizationProfile {
    MAX_FPS(
        "Max FPS",
        "Maximum performance for competitive play",
        144,
        6, // render distance - lower for max fps
        GraphicsPreset.FAST,
        0, // particles (0=minimal, 1=decreased, 2=all)
        CloudStatus.OFF,
        50, // entity distance %
        false, // vsync OFF for max fps
        false, // smooth lighting OFF
        0 // mipmap OFF
    ),
    BALANCED(
        "Balanced",
        "Best of both worlds",
        60,
        12,
        GraphicsPreset.FANCY,
        1, // decreased
        CloudStatus.FAST,
        75,
        false,
        true,
        2
    ),
    QUALITY(
        "Visual Quality",
        "Maximum beauty, lower FPS",
        30,
        16,
        GraphicsPreset.FABULOUS,
        2, // all
        CloudStatus.FANCY,
        100,
        false,
        true,
        4
    ),
    BATTERY_SAVER(
        "Battery Saver",
        "For laptops, stable 30 FPS",
        30,
        6,
        GraphicsPreset.FAST,
        0, // minimal
        CloudStatus.OFF,
        50,
        true, // vsync on to cap fps
        false,
        0
    ),
    CUSTOM(
        "Custom",
        "Your personalized settings",
        60,
        12,
        GraphicsPreset.FANCY,
        2,
        CloudStatus.FAST,
        100,
        false,
        true,
        4
    );
    
    private final String displayName;
    private final String description;
    private final int targetFps;
    private final int renderDistance;
    private final GraphicsPreset graphicsMode;
    private final int particlesLevel; // 0=minimal, 1=decreased, 2=all
    private final CloudStatus cloudMode;
    private final int entityDistancePercent;
    private final boolean vsync;
    private final boolean smoothLighting;
    private final int mipmapLevels;
    
    OptimizationProfile(String displayName, String description, int targetFps,
                        int renderDistance, GraphicsPreset graphicsMode,
                        int particlesLevel, CloudStatus cloudMode,
                        int entityDistancePercent, boolean vsync,
                        boolean smoothLighting, int mipmapLevels) {
        this.displayName = displayName;
        this.description = description;
        this.targetFps = targetFps;
        this.renderDistance = renderDistance;
        this.graphicsMode = graphicsMode;
        this.particlesLevel = particlesLevel;
        this.cloudMode = cloudMode;
        this.entityDistancePercent = entityDistancePercent;
        this.vsync = vsync;
        this.smoothLighting = smoothLighting;
        this.mipmapLevels = mipmapLevels;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getTargetFps() { return targetFps; }
    public int getRenderDistance() { return renderDistance; }
    public GraphicsPreset getGraphicsMode() { return graphicsMode; }
    public int getParticlesLevel() { return particlesLevel; }
    public CloudStatus getCloudMode() { return cloudMode; }
    public int getEntityDistancePercent() { return entityDistancePercent; }
    public boolean isVsync() { return vsync; }
    public boolean isSmoothLighting() { return smoothLighting; }
    public int getMipmapLevels() { return mipmapLevels; }
}

