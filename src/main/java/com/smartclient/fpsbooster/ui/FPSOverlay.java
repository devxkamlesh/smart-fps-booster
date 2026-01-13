package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.optimization.FPSMonitor;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

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
        
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
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
            
            // Render lag spike flash (red border around screen)
            if (lagSpikeAlpha > 0) {
                renderLagSpikeAlert(context);
                lagSpikeAlpha -= 5; // Fade out
            }
            
            // Render notifications
            if (config.isShowNotifications()) {
                NotificationManager.render(context);
            }
            
            if (!config.isShowFpsOverlay()) return;
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getDebugHud().shouldShowDebugHud()) return;
            
            render(context, client, config, monitor);
        });
    }
    
    private static void renderLagSpikeAlert(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        int w = client.getWindow().getScaledWidth();
        int h = client.getWindow().getScaledHeight();
        
        int color = (lagSpikeAlpha << 24) | 0xFF0000; // Red with alpha
        
        // Draw red border around screen
        int thickness = 3;
        context.fill(0, 0, w, thickness, color); // Top
        context.fill(0, h - thickness, w, h, color); // Bottom
        context.fill(0, 0, thickness, h, color); // Left
        context.fill(w - thickness, 0, w, h, color); // Right
    }
    
    private static void render(DrawContext context, MinecraftClient client, ModConfig config, FPSMonitor monitor) {
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
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
    
    private static void renderMinimal(DrawContext context, TextRenderer textRenderer, int x, int y, int fps, int color) {
        context.fill(x - 2, y - 2, x + 52, y + 12, 0xCC000000);
        context.drawTextWithShadow(textRenderer, fps + " FPS", x, y, color);
    }
    
    private static void renderCompact(DrawContext context, TextRenderer textRenderer, int x, int y, int fps, int fpsColor, FPSMonitor monitor) {
        context.fill(x - 2, y - 2, x + 78, y + 12, 0xCC000000);
        context.drawTextWithShadow(textRenderer, fps + " FPS", x, y, fpsColor);
        context.drawTextWithShadow(textRenderer, (int) monitor.getMemoryPercent() + "%", x + 52, y, 0xFFbbe1fa);
    }
    
    private static void renderDetailed(DrawContext context, TextRenderer textRenderer, int x, int y, int fps, int fpsColor, FPSMonitor monitor, ModConfig config) {
        int width = 92;
        int height = 48;
        
        context.fill(x - 4, y - 4, x + width, y + height, 0xDD1a1a2e);
        context.drawBorder(x - 4, y - 4, width + 4, height + 4, 0xFF3282b8);
        
        context.drawTextWithShadow(textRenderer, String.format("FPS: %d", fps), x, y, fpsColor);
        context.drawTextWithShadow(textRenderer, String.format("Avg: %d", monitor.getAverageFps()), x, y + 12, 0xFFaaaaaa);
        
        int barY = y + 26;
        int barWidth = width - 8;
        context.fill(x, barY, x + barWidth, barY + 8, 0xFF333333);
        int fillWidth = (int) (barWidth * monitor.getMemoryPercent() / 100);
        int memColor = monitor.getMemoryPercent() > 80 ? 0xFFfc5185 : 0xFF4ecca3;
        context.fill(x, barY, x + fillWidth, barY + 8, memColor);
        context.drawCenteredTextWithShadow(textRenderer, (int) monitor.getMemoryPercent() + "%", x + barWidth / 2, barY, 0xFFFFFFFF);
        
        String autoText = config.isAutoOptimize() ? "●" : "○";
        int autoColor = config.isAutoOptimize() ? 0xFF4ecca3 : 0xFF666666;
        context.drawTextWithShadow(textRenderer, autoText, x + width - 12, y, autoColor);
    }
    
    public static int getOverlayWidth() { return overlayWidth; }
    public static int getOverlayHeight() { return overlayHeight; }
}
