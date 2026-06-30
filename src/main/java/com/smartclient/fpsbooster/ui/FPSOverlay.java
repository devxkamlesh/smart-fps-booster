package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.optimization.FPSMonitor;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class FPSOverlay {
    private static boolean registered = false;
    private static int overlayWidth = 90;
    private static int overlayHeight = 45;
    
    // Lag spike detection
    private static int lastFps = 0;
    private static int lagSpikeAlpha = 0;
    private static final int LAG_SPIKE_THRESHOLD = 30; // FPS drop of 30+ = lag spike
    
    public static void register() {
        if (registered) return;
        registered = true;
        
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath(SmartFPSBoosterClient.MOD_ID, "overlay"),
            FPSOverlay::renderHud
        );
    }
    
    private static void renderHud(GuiGraphicsExtractor context, DeltaTracker tickDelta) {
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        if (mod == null) return;
        
        ModConfig config = mod.getConfig();
        FPSMonitor monitor = mod.getOptimizationManager().getFpsMonitor();
        
        // Lag spike detection
        int currentFps = monitor.getCurrentFps();
        if (lastFps > 0 && lastFps - currentFps >= LAG_SPIKE_THRESHOLD) {
            lagSpikeAlpha = 150; // Flash red
        }
        lastFps = currentFps;
        
        // Render lag spike flash (red border around screen) - only if enabled
        if (config.isShowLagSpikeAlert() && lagSpikeAlpha > 0) {
            renderLagSpikeAlert(context);
            lagSpikeAlpha -= 5; // Fade out
        } else if (lagSpikeAlpha > 0) {
            lagSpikeAlpha = 0; // alert disabled - clear any pending flash
        }
        
        // Render notifications
        if (config.isShowNotifications()) {
            NotificationManager.render(context);
        }
        
        if (!config.isShowFpsOverlay()) return;
        
        Minecraft client = Minecraft.getInstance();
        if (client.getDebugOverlay().showDebugScreen()) return;
        
        render(context, client, config, monitor);
    }
    
    private static void renderLagSpikeAlert(GuiGraphicsExtractor context) {
        Minecraft client = Minecraft.getInstance();
        int w = client.getWindow().getGuiScaledWidth();
        int h = client.getWindow().getGuiScaledHeight();
        
        int color = (lagSpikeAlpha << 24) | 0xFF0000; // Red with alpha
        
        // Draw red border around screen
        int thickness = 3;
        context.fill(0, 0, w, thickness, color); // Top
        context.fill(0, h - thickness, w, h, color); // Bottom
        context.fill(0, 0, thickness, h, color); // Left
        context.fill(w - thickness, 0, w, h, color); // Right
    }
    
    private static void render(GuiGraphicsExtractor context, Minecraft client, ModConfig config, FPSMonitor monitor) {
        Font textRenderer = client.font;
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        
        int x, y;
        
        if (config.isUseCustomPosition()) {
            x = config.getOverlayX();
            y = config.getOverlayY();
            // Clamp to screen bounds
            x = Math.max(0, Math.min(x, screenWidth - overlayWidth));
            y = Math.max(0, Math.min(y, screenHeight - overlayHeight));
        } else {
            switch (config.getOverlayPosition()) {
                case TOP_RIGHT -> { x = screenWidth - 95; y = 5; }
                case BOTTOM_LEFT -> { x = 5; y = screenHeight - 50; }
                case BOTTOM_RIGHT -> { x = screenWidth - 95; y = screenHeight - 50; }
                default -> { x = 5; y = 5; }
            }
        }
        
        int fps = monitor.getCurrentFps();
        int targetFps = config.getTargetFps();
        int fpsColor = fps >= targetFps ? 0xFF4ecca3 : (fps >= targetFps * 0.7 ? 0xFFffd93d : 0xFFfc5185);
        
        switch (config.getOverlayStyle()) {
            case MINIMAL -> { overlayWidth = 55; overlayHeight = 16; renderMinimal(context, textRenderer, x, y, fps, fpsColor); }
            case COMPACT -> { overlayWidth = 80; overlayHeight = 16; renderCompact(context, textRenderer, x, y, fps, fpsColor, monitor); }
            case DETAILED -> { overlayWidth = 95; overlayHeight = 50; renderDetailed(context, textRenderer, x, y, fps, fpsColor, monitor, config); }
        }
    }
    
    private static void renderMinimal(GuiGraphicsExtractor context, Font textRenderer, int x, int y, int fps, int color) {
        context.fillGradient(x - 4, y - 3, x + 52, y + 12, 0xE6202438, 0xE6121626);
        context.fill(x - 4, y - 3, x - 2, y + 12, color); // accent bar
        context.outline(x - 4, y - 3, 56, 15, 0x40FFFFFF);
        context.text(textRenderer, fps + " FPS", x + 2, y, color, true);
    }
    
    private static void renderCompact(GuiGraphicsExtractor context, Font textRenderer, int x, int y, int fps, int fpsColor, FPSMonitor monitor) {
        context.fillGradient(x - 4, y - 3, x + 80, y + 12, 0xE6202438, 0xE6121626);
        context.fill(x - 4, y - 3, x - 2, y + 12, fpsColor); // accent bar
        context.outline(x - 4, y - 3, 84, 15, 0x40FFFFFF);
        context.text(textRenderer, fps + " FPS", x + 2, y, fpsColor, true);
        context.text(textRenderer, (int) monitor.getMemoryPercent() + "%", x + 54, y, 0xFFbbe1fa, true);
    }
    
    private static void renderDetailed(GuiGraphicsExtractor context, Font textRenderer, int x, int y, int fps, int fpsColor, FPSMonitor monitor, ModConfig config) {
        int width = 94;
        int height = 48;
        
        context.fillGradient(x - 5, y - 5, x + width, y + height, 0xEE1B1E33, 0xEE0F1120);
        context.fill(x - 5, y - 5, x - 2, y + height, fpsColor); // accent bar
        context.outline(x - 5, y - 5, width + 5, height + 5, 0xFF313663);
        
        context.text(textRenderer, String.format("FPS  %d", fps), x, y, fpsColor, true);
        context.text(textRenderer, String.format("Avg  %d", monitor.getAverageFps()), x, y + 12, 0xFFaaaaaa, true);
        
        int barY = y + 26;
        int barWidth = width - 8;
        float memPct = monitor.getMemoryPercent();
        int memColor = memPct > 80 ? 0xFFfc5185 : (memPct > 60 ? 0xFFffd93d : 0xFF4ecca3);
        context.fill(x, barY, x + barWidth, barY + 8, 0xFF2A2E4A);
        int fillWidth = (int) (barWidth * memPct / 100);
        if (fillWidth > 0) context.fill(x, barY, x + fillWidth, barY + 8, memColor);
        context.text(textRenderer, (int) memPct + "% RAM", x + 2, barY + 9, 0xFF888fa6, true);
        
        String autoText = config.isAutoOptimize() ? "\u25CF" : "\u25CB";
        int autoColor = config.isAutoOptimize() ? 0xFF4ecca3 : 0xFF666666;
        context.text(textRenderer, autoText, x + width - 12, y, autoColor, true);
    }
    
    public static int getOverlayWidth() { return overlayWidth; }
    public static int getOverlayHeight() { return overlayHeight; }
}
