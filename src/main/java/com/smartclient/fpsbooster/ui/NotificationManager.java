package com.smartclient.fpsbooster.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Simple single notification system - shows only ONE notification at a time
 */
public class NotificationManager {
    private static String currentMessage = null;
    private static int ticksRemaining = 0;
    private static final int DURATION = 60; // 3 seconds
    
    public static void show(String message) {
        currentMessage = message;
        ticksRemaining = DURATION;
    }
    
    public static void showInfo(String message) {
        show(message);
    }
    
    public static void showSuccess(String message) {
        show(message);
    }
    
    public static void showWarning(String message) {
        show(message);
    }
    
    public static void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
            if (ticksRemaining <= 0) {
                currentMessage = null;
            }
        }
    }
    
    public static void render(GuiGraphicsExtractor context) {
        if (currentMessage == null || ticksRemaining <= 0) return;
        
        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        
        int width = client.font.width(currentMessage) + 16;
        int x = screenWidth - width - 5;
        int y = 5;
        
        // Fade out in last 20 ticks
        float alpha = ticksRemaining < 20 ? ticksRemaining / 20.0f : 1.0f;
        int bgAlpha = (int) (180 * alpha);
        int textAlpha = (int) (255 * alpha);
        
        int bgTop = (bgAlpha << 24) | 0x202438;
        int bgBot = (bgAlpha << 24) | 0x121626;
        int borderColor = (textAlpha << 24) | 0x4C8DFF;
        int textColor = (textAlpha << 24) | 0xFFFFFF;
        
        context.fillGradient(x, y, x + width, y + 16, bgTop, bgBot);
        context.fill(x, y, x + 2, y + 16, borderColor); // accent bar
        context.outline(x, y, width, 16, borderColor);
        context.text(client.font, currentMessage, x + 8, y + 4, textColor, true);
    }
    
    public static void clear() {
        currentMessage = null;
        ticksRemaining = 0;
    }
}
