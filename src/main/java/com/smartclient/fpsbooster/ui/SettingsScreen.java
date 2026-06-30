package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.optimization.BenchmarkManager;
import com.smartclient.fpsbooster.profile.OptimizationProfile;
import com.smartclient.fpsbooster.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class SettingsScreen extends Screen {
    private final Screen parent;
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 360;
    
    private int scrollOffset = 0;
    private int maxScroll = 0;
    
    // Geometry, recomputed each init()
    private int panelLeft, panelTop, contentX, contentW, contentTop, contentBottom;
    private final List<int[]> sectionMarks = new ArrayList<>(); // {virtualY}
    private final List<String> sectionLabels = new ArrayList<>();
    
    public SettingsScreen(Screen parent) {
        super(Component.literal("Settings"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        sectionMarks.clear();
        sectionLabels.clear();
        
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        ModConfig config = mod.getConfig();
        ProfileManager pm = mod.getProfileManager();
        BenchmarkManager benchmark = mod.getOptimizationManager().getBenchmarkManager();
        
        int centerX = this.width / 2;
        panelLeft = centerX - PANEL_WIDTH / 2;
        panelTop = this.height / 2 - PANEL_HEIGHT / 2;
        int pad = 16;
        contentX = panelLeft + pad;
        contentW = PANEL_WIDTH - 2 * pad;
        contentTop = panelTop + 30;
        contentBottom = panelTop + PANEL_HEIGHT - 38;
        int halfGap = 8;
        int halfW = (contentW - halfGap) / 2;
        int col2 = contentX + halfW + halfGap;
        
        Cursor c = new Cursor();
        
        // ── Profiles ─────────────────────────────────────────────────
        section(c, "Profiles");
        OptimizationProfile[] profiles = {
            OptimizationProfile.MAX_FPS, OptimizationProfile.BALANCED,
            OptimizationProfile.QUALITY, OptimizationProfile.BATTERY_SAVER
        };
        for (int i = 0; i < profiles.length; i += 2) {
            int rowY = c.row();
            for (int j = 0; j < 2 && i + j < profiles.length; j++) {
                final OptimizationProfile p = profiles[i + j];
                boolean sel = pm.getCurrentProfile() == p;
                int bx = (j == 0) ? contentX : col2;
                addIfVisible(bx, rowY, halfW, Button.builder(
                    Component.literal((sel ? "\u25C9 " : "\u25CB ") + p.getDisplayName()),
                    b -> { pm.applyProfile(p); this.rebuildWidgets(); }
                ));
            }
        }
        
        // ── Optimization ─────────────────────────────────────────────
        section(c, "Optimization");
        int ay = c.row();
        addIfVisible(contentX, ay, contentW, Button.builder(
            Component.literal("Auto-Optimize: " + onOff(config.isAutoOptimize())),
            b -> { config.setAutoOptimize(!config.isAutoOptimize()); this.rebuildWidgets(); }));
        ay = c.row();
        if (visible(ay)) {
            this.addRenderableWidget(new AbstractSliderButton(contentX, ay, contentW, 20,
                Component.literal("Target FPS: " + config.getTargetFps()), config.getTargetFps() / 240.0) {
                @Override protected void updateMessage() {
                    int f = Math.max(30, (int) (this.value * 240));
                    this.setMessage(Component.literal("Target FPS: " + f));
                }
                @Override protected void applyValue() {
                    config.setTargetFps(Math.max(30, (int) (this.value * 240)));
                }
            });
        }
        
        // ── Smart Tuning ─────────────────────────────────────────────
        section(c, "Smart Tuning");
        int r = c.row();
        addToggle(config, contentX, r, halfW, "Movement", config.isMovementBasedTuning(), config::setMovementBasedTuning);
        addToggle(config, col2, r, halfW, "Combat", config.isCombatOptimization(), config::setCombatOptimization);
        r = c.row();
        addToggle(config, contentX, r, halfW, "Heavy Scene", config.isHeavySceneDetection(), config::setHeavySceneDetection);
        addToggle(config, col2, r, halfW, "Dimension", config.isDimensionPresets(), config::setDimensionPresets);
        
        // ── Locks ────────────────────────────────────────────────────
        section(c, "Locks");
        r = c.row();
        boolean rdLocked = config.isSettingLocked("render_distance");
        addIfVisible(contentX, r, halfW, Button.builder(
            Component.literal((rdLocked ? "\uD83D\uDD12" : "\uD83D\uDD13") + " Render Dist"),
            b -> { config.toggleSettingLock("render_distance"); this.rebuildWidgets(); }));
        boolean gfxLocked = config.isSettingLocked("graphics");
        addIfVisible(col2, r, halfW, Button.builder(
            Component.literal((gfxLocked ? "\uD83D\uDD12" : "\uD83D\uDD13") + " Graphics"),
            b -> { config.toggleSettingLock("graphics"); this.rebuildWidgets(); }));
        
        // ── Overlay ──────────────────────────────────────────────────
        section(c, "Overlay");
        r = c.row();
        addIfVisible(contentX, r, halfW, Button.builder(
            Component.literal("Overlay: " + onOff(config.isShowFpsOverlay())),
            b -> { config.setShowFpsOverlay(!config.isShowFpsOverlay()); this.rebuildWidgets(); }));
        addIfVisible(col2, r, halfW, Button.builder(
            Component.literal("Style: " + cap(config.getOverlayStyle().name())),
            b -> {
                ModConfig.OverlayStyle[] s = ModConfig.OverlayStyle.values();
                config.setOverlayStyle(s[(config.getOverlayStyle().ordinal() + 1) % s.length]);
                this.rebuildWidgets();
            }));
        r = c.row();
        addIfVisible(contentX, r, halfW, Button.builder(
            Component.literal("Position: " + posName(config)),
            b -> {
                ModConfig.OverlayPosition[] p = ModConfig.OverlayPosition.values();
                config.setOverlayPosition(p[(config.getOverlayPosition().ordinal() + 1) % p.length]);
                config.setUseCustomPosition(false);
                this.rebuildWidgets();
            }));
        addIfVisible(col2, r, halfW, Button.builder(
            Component.literal("Drag: " + (config.isUseCustomPosition() ? "Custom" : "Preset")),
            b -> {
                config.setUseCustomPosition(!config.isUseCustomPosition());
                if (config.isUseCustomPosition()) NotificationManager.showInfo("Drag overlay to move it");
                this.rebuildWidgets();
            }));
        
        // ── Visuals (NEW) ────────────────────────────────────────────
        section(c, "Visuals & Alerts");
        r = c.row();
        addToggle(config, contentX, r, halfW, "Edge Alert", config.isShowLagSpikeAlert(), config::setShowLagSpikeAlert);
        addToggle(config, col2, r, halfW, "Notifications", config.isShowNotifications(), config::setShowNotifications);
        r = c.row();
        addToggle(config, contentX, r, halfW, "Auto Switch", config.isUseContextProfiles(), config::setUseContextProfiles);
        
        // ── Tools ────────────────────────────────────────────────────
        section(c, "Tools");
        r = c.row();
        if (benchmark.isRunning()) {
            addIfVisible(contentX, r, contentW, Button.builder(
                Component.literal("Benchmarking... " + (int) (benchmark.getProgress() * 100) + "%"), b -> {}));
        } else {
            String sc = config.getLastBenchmarkScore() > 0 ? " (Score: " + config.getLastBenchmarkScore() + ")" : "";
            addIfVisible(contentX, r, contentW, Button.builder(
                Component.literal("Run Benchmark" + sc),
                b -> {
                    benchmark.startBenchmark(() -> this.rebuildWidgets());
                    NotificationManager.showInfo("Benchmark started - play for 10 sec");
                    this.rebuildWidgets();
                }));
        }
        
        // ── Reset ────────────────────────────────────────────────────
        section(c, "Reset");
        r = c.row();
        addIfVisible(contentX, r, contentW, Button.builder(
            Component.literal("\u21BA Reset to Defaults"),
            b -> {
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
                config.setShowLagSpikeAlert(true);
                config.setUseContextProfiles(false);
                pm.applyProfile(OptimizationProfile.MAX_FPS);
                NotificationManager.showInfo("Settings reset to defaults");
                this.rebuildWidgets();
            }));
        
        // total content height + scroll clamp
        int contentHeight = c.vy + 8;
        int viewH = contentBottom - contentTop;
        maxScroll = Math.max(0, contentHeight - viewH);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        
        // Done button (fixed)
        this.addRenderableWidget(Button.builder(
            Component.literal("Done"), b -> this.onClose()
        ).bounds(centerX - 52, panelTop + PANEL_HEIGHT - 28, 104, 20).build());
    }
    
    // ── Layout helpers ───────────────────────────────────────────────
    private class Cursor {
        int vy = 8;
        int row() { int y = contentTop + vy - scrollOffset; vy += 24; return y; }
    }
    
    private void section(Cursor c, String label) {
        sectionMarks.add(new int[]{ c.vy });
        sectionLabels.add(label);
        c.vy += 15;
    }
    
    private boolean visible(int y) {
        return y >= contentTop - 1 && y + 20 <= contentBottom + 1;
    }
    
    private void addIfVisible(int x, int y, int w, Button.Builder builder) {
        if (visible(y)) this.addRenderableWidget(builder.bounds(x, y, w, 20).build());
    }
    
    private void addToggle(ModConfig config, int x, int y, int w, String label, boolean state, java.util.function.Consumer<Boolean> setter) {
        if (!visible(y)) return;
        this.addRenderableWidget(Button.builder(
            Component.literal(label + ": " + onOff(state)),
            b -> { setter.accept(!state); b.setMessage(Component.literal(label + ": " + onOff(!state))); }
        ).bounds(x, y, w, 20).build());
    }
    
    private static String onOff(boolean b) { return b ? "ON" : "OFF"; }
    private static String cap(String s) { return s.charAt(0) + s.substring(1).toLowerCase(); }
    private static String posName(ModConfig c) {
        return switch (c.getOverlayPosition()) {
            case TOP_LEFT -> "Top-Left"; case TOP_RIGHT -> "Top-Right";
            case BOTTOM_LEFT -> "Bot-Left"; case BOTTOM_RIGHT -> "Bot-Right";
        };
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        Theme.backdrop(g, this.width, this.height);
        Theme.panel(g, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);
        Theme.titleBar(g, this.font, panelLeft, panelTop, PANEL_WIDTH, "\u2699 Settings");
        
        // section labels (clipped to content band)
        for (int i = 0; i < sectionMarks.size(); i++) {
            int ay = contentTop + sectionMarks.get(i)[0] - scrollOffset;
            if (ay >= contentTop - 1 && ay <= contentBottom - 6) {
                Theme.section(g, this.font, contentX, ay, contentW, sectionLabels.get(i));
            }
        }
        
        // scrollbar
        if (maxScroll > 0) {
            int trackX = panelLeft + PANEL_WIDTH - 7;
            int trackY = contentTop;
            int trackH = contentBottom - contentTop;
            g.fill(trackX, trackY, trackX + 3, trackY + trackH, Theme.TRACK);
            int thumbH = Math.max(24, trackH * trackH / (trackH + maxScroll));
            int thumbY = trackY + (scrollOffset * (trackH - thumbH) / maxScroll);
            g.fillGradient(trackX, thumbY, trackX + 3, thumbY + thumbH, Theme.ACCENT_LIGHT, Theme.ACCENT);
        }
        
        Theme.centered(g, this.font, "v2.0.0", panelLeft + PANEL_WIDTH / 2, panelTop + PANEL_HEIGHT - 12, Theme.TEXT_MUTED);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll <= 0) return false;
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (verticalAmount * 20)));
        this.rebuildWidgets();
        return true;
    }
    
    @Override
    public void onClose() {
        SmartFPSBoosterClient.getInstance().getConfig().save();
        this.minecraft.setScreenAndShow(parent);
    }
    
    @Override
    public boolean isPauseScreen() { return false; }
}
