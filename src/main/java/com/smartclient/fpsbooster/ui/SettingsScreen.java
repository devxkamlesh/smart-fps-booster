package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.optimization.BenchmarkManager;
import com.smartclient.fpsbooster.profile.OptimizationProfile;
import com.smartclient.fpsbooster.profile.ProfileManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private static final int PANEL_WIDTH = 340;
    private static final int PANEL_HEIGHT = 360;
    
    private int scrollOffset = 0;
    private int maxScroll = 250;
    
    public SettingsScreen(Screen parent) {
        super(Text.literal("Settings"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        ModConfig config = mod.getConfig();
        ProfileManager profileManager = mod.getProfileManager();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = centerY - PANEL_HEIGHT / 2;
        
        int buttonWidth = PANEL_WIDTH - 40;
        int halfWidth = (buttonWidth - 10) / 2;
        int buttonX = panelLeft + 20;
        int col1 = buttonX;
        int col2 = buttonX + halfWidth + 10;
        
        int contentTop = panelTop + 30;
        int contentBottom = panelTop + PANEL_HEIGHT - 45;
        
        // ═══════════════════════════════════════════
        // PROFILE SECTION
        // ═══════════════════════════════════════════
        int y = panelTop + 45 - scrollOffset;
        
        OptimizationProfile[] profiles = {
            OptimizationProfile.MAX_FPS,
            OptimizationProfile.BALANCED,
            OptimizationProfile.QUALITY,
            OptimizationProfile.BATTERY_SAVER
        };
        
        for (int i = 0; i < profiles.length; i++) {
            final OptimizationProfile profile = profiles[i];
            int btnX = (i % 2 == 0) ? col1 : col2;
            int btnY = y + (i / 2) * 24;
            
            if (btnY > contentTop && btnY < contentBottom) {
                boolean isSelected = profileManager.getCurrentProfile() == profile;
                String prefix = isSelected ? "● " : "○ ";
                
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal(prefix + profile.getDisplayName()),
                    button -> {
                        profileManager.applyProfile(profile);
                        this.clearAndInit();
                    }
                ).dimensions(btnX, btnY, halfWidth, 20).build());
            }
        }
        
        // ═══════════════════════════════════════════
        // OPTIMIZATION SECTION
        // ═══════════════════════════════════════════
        y = panelTop + 105 - scrollOffset;
        
        if (y > contentTop && y < contentBottom) {
            boolean autoOn = config.isAutoOptimize();
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto-Optimize: " + (autoOn ? "ON" : "OFF")),
                button -> {
                    config.setAutoOptimize(!config.isAutoOptimize());
                    this.clearAndInit();
                }
            ).dimensions(buttonX, y, buttonWidth, 20).build());
        }
        
        y += 24;
        
        if (y > contentTop && y < contentBottom) {
            this.addDrawableChild(new SliderWidget(buttonX, y, buttonWidth, 20,
                Text.literal("Target FPS: " + config.getTargetFps()), config.getTargetFps() / 240.0) {
                @Override
                protected void updateMessage() {
                    int fps = (int) (this.value * 240);
                    if (fps < 30) fps = 30;
                    this.setMessage(Text.literal("Target FPS: " + fps));
                }
                @Override
                protected void applyValue() {
                    int fps = (int) (this.value * 240);
                    if (fps < 30) fps = 30;
                    config.setTargetFps(fps);
                }
            });
        }
        
        // ═══════════════════════════════════════════
        // TUNING SECTION
        // ═══════════════════════════════════════════
        y = panelTop + 165 - scrollOffset;
        
        if (y > contentTop && y < contentBottom) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Movement: " + (config.isMovementBasedTuning() ? "ON" : "OFF")),
                button -> {
                    config.setMovementBasedTuning(!config.isMovementBasedTuning());
                    button.setMessage(Text.literal("Movement: " + (config.isMovementBasedTuning() ? "ON" : "OFF")));
                }
            ).dimensions(col1, y, halfWidth, 20).build());
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Combat: " + (config.isCombatOptimization() ? "ON" : "OFF")),
                button -> {
                    config.setCombatOptimization(!config.isCombatOptimization());
                    button.setMessage(Text.literal("Combat: " + (config.isCombatOptimization() ? "ON" : "OFF")));
                }
            ).dimensions(col2, y, halfWidth, 20).build());
        }
        
        y += 24;
        
        if (y > contentTop && y < contentBottom) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Heavy Scene: " + (config.isHeavySceneDetection() ? "ON" : "OFF")),
                button -> {
                    config.setHeavySceneDetection(!config.isHeavySceneDetection());
                    button.setMessage(Text.literal("Heavy Scene: " + (config.isHeavySceneDetection() ? "ON" : "OFF")));
                }
            ).dimensions(col1, y, halfWidth, 20).build());
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Dimension: " + (config.isDimensionPresets() ? "ON" : "OFF")),
                button -> {
                    config.setDimensionPresets(!config.isDimensionPresets());
                    button.setMessage(Text.literal("Dimension: " + (config.isDimensionPresets() ? "ON" : "OFF")));
                }
            ).dimensions(col2, y, halfWidth, 20).build());
        }
        
        // ═══════════════════════════════════════════
        // LOCK SECTION
        // ═══════════════════════════════════════════
        y = panelTop + 225 - scrollOffset;
        
        if (y > contentTop && y < contentBottom) {
            boolean rdLocked = config.isSettingLocked("render_distance");
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal((rdLocked ? "🔒" : "🔓") + " Render Dist"),
                button -> {
                    config.toggleSettingLock("render_distance");
                    this.clearAndInit();
                }
            ).dimensions(col1, y, halfWidth, 20).build());
            
            boolean gfxLocked = config.isSettingLocked("graphics");
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal((gfxLocked ? "🔒" : "🔓") + " Graphics"),
                button -> {
                    config.toggleSettingLock("graphics");
                    this.clearAndInit();
                }
            ).dimensions(col2, y, halfWidth, 20).build());
        }
        
        // ═══════════════════════════════════════════
        // OVERLAY SECTION
        // ═══════════════════════════════════════════
        y = panelTop + 260 - scrollOffset;
        
        if (y > contentTop && y < contentBottom) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Overlay: " + (config.isShowFpsOverlay() ? "ON" : "OFF")),
                button -> {
                    config.setShowFpsOverlay(!config.isShowFpsOverlay());
                    this.clearAndInit();
                }
            ).dimensions(col1, y, halfWidth, 20).build());
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Style: " + config.getOverlayStyle().name()),
                button -> {
                    ModConfig.OverlayStyle[] styles = ModConfig.OverlayStyle.values();
                    int next = (config.getOverlayStyle().ordinal() + 1) % styles.length;
                    config.setOverlayStyle(styles[next]);
                    button.setMessage(Text.literal("Style: " + config.getOverlayStyle().name()));
                }
            ).dimensions(col2, y, halfWidth, 20).build());
        }
        
        y += 24;
        
        // Overlay Position
        if (y > contentTop && y < contentBottom) {
            String posName = switch (config.getOverlayPosition()) {
                case TOP_LEFT -> "Top-Left";
                case TOP_RIGHT -> "Top-Right";
                case BOTTOM_LEFT -> "Bottom-Left";
                case BOTTOM_RIGHT -> "Bottom-Right";
            };
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Position: " + posName),
                button -> {
                    ModConfig.OverlayPosition[] positions = ModConfig.OverlayPosition.values();
                    int next = (config.getOverlayPosition().ordinal() + 1) % positions.length;
                    config.setOverlayPosition(positions[next]);
                    config.setUseCustomPosition(false);
                    this.clearAndInit();
                }
            ).dimensions(col1, y, halfWidth, 20).build());
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Drag: " + (config.isUseCustomPosition() ? "Custom" : "Preset")),
                button -> {
                    config.setUseCustomPosition(!config.isUseCustomPosition());
                    if (config.isUseCustomPosition()) {
                        NotificationManager.showInfo("Drag overlay to move it");
                    }
                    this.clearAndInit();
                }
            ).dimensions(col2, y, halfWidth, 20).build());
        }
        
        // ═══════════════════════════════════════════
        // TOOLS SECTION
        // ═══════════════════════════════════════════
        y = panelTop + 320 - scrollOffset;
        
        if (y > contentTop && y < contentBottom) {
            BenchmarkManager benchmark = mod.getOptimizationManager().getBenchmarkManager();
            if (benchmark.isRunning()) {
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Benchmarking... " + (int)(benchmark.getProgress() * 100) + "%"),
                    button -> {}
                ).dimensions(buttonX, y, buttonWidth, 20).build());
            } else {
                String scoreText = config.getLastBenchmarkScore() > 0 ? 
                    " (Score: " + config.getLastBenchmarkScore() + ")" : "";
                this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Run Benchmark" + scoreText),
                    button -> {
                        benchmark.startBenchmark(() -> this.clearAndInit());
                        NotificationManager.showInfo("Benchmark started - play for 10 sec");
                        this.clearAndInit();
                    }
                ).dimensions(buttonX, y, buttonWidth, 20).build());
            }
        }
        
        // ═══════════════════════════════════════════
        // EXTRAS SECTION
        // ═══════════════════════════════════════════
        y = panelTop + 355 - scrollOffset;
        
        if (y > contentTop && y < contentBottom) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Notify: " + (config.isShowNotifications() ? "ON" : "OFF")),
                button -> {
                    config.setShowNotifications(!config.isShowNotifications());
                    button.setMessage(Text.literal("Notify: " + (config.isShowNotifications() ? "ON" : "OFF")));
                }
            ).dimensions(col1, y, halfWidth, 20).build());
            
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto Switch: " + (config.isUseContextProfiles() ? "ON" : "OFF")),
                button -> {
                    config.setUseContextProfiles(!config.isUseContextProfiles());
                    button.setMessage(Text.literal("Auto Switch: " + (config.isUseContextProfiles() ? "ON" : "OFF")));
                }
            ).dimensions(col2, y, halfWidth, 20).build());
        }
        
        // ═══════════════════════════════════════════
        // RESET SECTION
        // ═══════════════════════════════════════════
        y = panelTop + 390 - scrollOffset;
        
        if (y > contentTop && y < contentBottom) {
            this.addDrawableChild(ButtonWidget.builder(
                Text.literal("⟲ Reset to Defaults"),
                button -> {
                    // Reset all settings to defaults
                    config.setProfile(OptimizationProfile.MAX_FPS);
                    config.setTargetFps(120);
                    config.setAutoOptimize(true);
                    config.setShowFpsOverlay(true);
                    config.setOverlayStyle(ModConfig.OverlayStyle.MINIMAL);
                    config.setMovementBasedTuning(true);
                    config.setCombatOptimization(true);
                    config.setHeavySceneDetection(true);
                    config.setDimensionPresets(true);
                    config.setShowNotifications(true);
                    config.setUseContextProfiles(false);
                    profileManager.applyProfile(OptimizationProfile.MAX_FPS);
                    NotificationManager.showInfo("Settings reset to defaults");
                    this.clearAndInit();
                }
            ).dimensions(buttonX, y, buttonWidth, 20).build());
        }
        
        // Done button (always visible)
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Done"),
            button -> this.close()
        ).dimensions(centerX - 50, panelTop + PANEL_HEIGHT - 28, 100, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x90000000);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = centerY - PANEL_HEIGHT / 2;
        
        context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xF0181830);
        context.drawBorder(panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT, 0xFF4080c0);
        
        context.drawCenteredTextWithShadow(this.textRenderer, "Smart FPS Booster v1.0.0", centerX, panelTop + 10, 0xFFbbe1fa);
        
        int leftCol = panelLeft + 20;
        int labelY;
        
        // Section labels (only show if visible)
        labelY = panelTop + 34 - scrollOffset;
        if (labelY > panelTop + 25) context.drawTextWithShadow(this.textRenderer, "Profile", leftCol, labelY, 0xFF4080c0);
        
        labelY = panelTop + 94 - scrollOffset;
        if (labelY > panelTop + 25 && labelY < panelTop + PANEL_HEIGHT - 50) 
            context.drawTextWithShadow(this.textRenderer, "Optimization", leftCol, labelY, 0xFF4080c0);
        
        labelY = panelTop + 154 - scrollOffset;
        if (labelY > panelTop + 25 && labelY < panelTop + PANEL_HEIGHT - 50) 
            context.drawTextWithShadow(this.textRenderer, "Tuning", leftCol, labelY, 0xFF4080c0);
        
        // Lock and Overlay sections - no labels, just icons on buttons
        
        labelY = panelTop + 249 - scrollOffset;
        if (labelY > panelTop + 25 && labelY < panelTop + PANEL_HEIGHT - 50) 
            context.drawTextWithShadow(this.textRenderer, "Overlay", leftCol, labelY, 0xFF4080c0);
        
        labelY = panelTop + 309 - scrollOffset;
        if (labelY > panelTop + 25 && labelY < panelTop + PANEL_HEIGHT - 50) 
            context.drawTextWithShadow(this.textRenderer, "Tools", leftCol, labelY, 0xFF4080c0);
        
        labelY = panelTop + 344 - scrollOffset;
        if (labelY > panelTop + 25 && labelY < panelTop + PANEL_HEIGHT - 50) 
            context.drawTextWithShadow(this.textRenderer, "Extras", leftCol, labelY, 0xFF4080c0);
        
        labelY = panelTop + 379 - scrollOffset;
        if (labelY > panelTop + 25 && labelY < panelTop + PANEL_HEIGHT - 50) 
            context.drawTextWithShadow(this.textRenderer, "Reset", leftCol, labelY, 0xFFff6b6b);
        
        // Scrollbar
        if (maxScroll > 0) {
            int scrollbarX = panelLeft + PANEL_WIDTH - 8;
            int scrollbarY = panelTop + 30;
            int scrollbarHeight = PANEL_HEIGHT - 70;
            context.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0xFF303050);
            int thumbHeight = Math.max(20, scrollbarHeight * scrollbarHeight / (scrollbarHeight + maxScroll));
            int thumbY = scrollbarY + (scrollOffset * (scrollbarHeight - thumbHeight) / Math.max(1, maxScroll));
            context.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xFF4080c0);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(verticalAmount * 20)));
        this.clearAndInit();
        return true;
    }
    
    @Override
    public void close() {
        SmartFPSBoosterClient.getInstance().getConfig().save();
        this.client.setScreen(parent);
    }
    
    @Override
    public boolean shouldPause() { return false; }
}
