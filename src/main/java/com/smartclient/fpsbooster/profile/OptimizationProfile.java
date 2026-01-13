package com.smartclient.fpsbooster.profile;

import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;

public enum OptimizationProfile {
    MAX_FPS(
        "Max FPS",
        "Maximum performance for competitive play",
        144,
        6, // render distance - lower for max fps
        GraphicsMode.FAST,
        0, // particles (0=minimal, 1=decreased, 2=all)
        CloudRenderMode.OFF,
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
        GraphicsMode.FANCY,
        1, // decreased
        CloudRenderMode.FAST,
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
        GraphicsMode.FABULOUS,
        2, // all
        CloudRenderMode.FANCY,
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
        GraphicsMode.FAST,
        0, // minimal
        CloudRenderMode.OFF,
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
        GraphicsMode.FANCY,
        2,
        CloudRenderMode.FAST,
        100,
        false,
        true,
        4
    );
    
    private final String displayName;
    private final String description;
    private final int targetFps;
    private final int renderDistance;
    private final GraphicsMode graphicsMode;
    private final int particlesLevel; // 0=minimal, 1=decreased, 2=all
    private final CloudRenderMode cloudMode;
    private final int entityDistancePercent;
    private final boolean vsync;
    private final boolean smoothLighting;
    private final int mipmapLevels;
    
    OptimizationProfile(String displayName, String description, int targetFps,
                        int renderDistance, GraphicsMode graphicsMode,
                        int particlesLevel, CloudRenderMode cloudMode,
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
    public GraphicsMode getGraphicsMode() { return graphicsMode; }
    public int getParticlesLevel() { return particlesLevel; }
    public CloudRenderMode getCloudMode() { return cloudMode; }
    public int getEntityDistancePercent() { return entityDistancePercent; }
    public boolean isVsync() { return vsync; }
    public boolean isSmoothLighting() { return smoothLighting; }
    public int getMipmapLevels() { return mipmapLevels; }
}
