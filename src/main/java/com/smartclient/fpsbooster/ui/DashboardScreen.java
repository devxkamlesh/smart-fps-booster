package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.compat.SodiumCompat;
import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.hardware.HardwareDetector;
import com.smartclient.fpsbooster.optimization.DynamicTuner;
import com.smartclient.fpsbooster.optimization.FPSMonitor;
import com.smartclient.fpsbooster.profile.ProfileManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;

public class DashboardScreen extends Screen {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 348;
    
    private static final String[] TIPS = {
        "Use 'Max FPS' profile for competitive play",
        "Lower render distance = more FPS",
        "Turn off clouds and particles for a boost",
        "Enable Auto-Optimize for hands-free tuning",
        "Press F9 to quickly toggle Auto-Optimize"
    };
    private int currentTip = 0;
    private long lastTipChange = 0;
    
    public DashboardScreen() {
        super(Component.literal("Smart FPS Booster"));
        currentTip = (int) (System.currentTimeMillis() % TIPS.length);
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int panelTop = this.height / 2 - PANEL_HEIGHT / 2;
        int bottomY = panelTop + PANEL_HEIGHT - 28;
        
        this.addRenderableWidget(Button.builder(
            Component.literal("\u2699 Settings"),
            button -> this.minecraft.setScreenAndShow(new SettingsScreen(this))
        ).bounds(centerX - 108, bottomY, 104, 20).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("Close"),
            button -> this.onClose()
        ).bounds(centerX + 4, bottomY, 104, 20).build());
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        Theme.backdrop(g, this.width, this.height);
        
        int centerX = this.width / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = this.height / 2 - PANEL_HEIGHT / 2;
        
        Theme.panel(g, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);
        int y = Theme.titleBar(g, this.font, panelLeft, panelTop, PANEL_WIDTH, "\u26A1 Smart FPS Booster");
        
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        FPSMonitor monitor = mod.getOptimizationManager().getFpsMonitor();
        DynamicTuner tuner = mod.getOptimizationManager().getDynamicTuner();
        ProfileManager profileManager = mod.getProfileManager();
        HardwareDetector hardware = mod.getHardwareDetector();
        ModConfig config = mod.getConfig();
        
        int x = panelLeft + 14;
        int cw = PANEL_WIDTH - 28;
        int target = config.getTargetFps();
        int fps = monitor.getCurrentFps();
        
        y += 10;
        
        // ── Stat tiles ───────────────────────────────────────────────
        int gap = 8;
        int tileW = (cw - 2 * gap) / 3;
        int tileH = 36;
        Theme.stat(g, this.font, x, y, tileW, tileH, "FPS", String.valueOf(fps), Theme.fpsColor(fps, target));
        Theme.stat(g, this.font, x + tileW + gap, y, tileW, tileH, "AVERAGE", String.valueOf(monitor.getAverageFps()), Theme.TEXT);
        Theme.stat(g, this.font, x + 2 * (tileW + gap), y, tileW, tileH, "1% LOW", String.valueOf(monitor.getOnePercentLow()), Theme.TEXT);
        y += tileH + 12;
        
        // ── Memory ───────────────────────────────────────────────────
        Theme.section(g, this.font, x, y, cw, "Memory");
        String ft = String.format("%.1f ms", monitor.getFrameTimeMs());
        g.text(this.font, ft, x + cw - this.font.width(ft), y, Theme.TEXT_DIM, true);
        y += 13;
        float memPct = monitor.getMemoryPercent();
        int memColor = memPct > 80 ? Theme.BAD : (memPct > 60 ? Theme.WARN : Theme.GOOD);
        Theme.bar(g, x, y, cw, 12, memPct / 100f, memColor);
        Theme.centered(g, this.font, String.format("%d / %d MB", monitor.getUsedMemoryMB(), monitor.getMaxMemoryMB()), x + cw / 2, y + 2, 0xFFFFFFFF);
        y += 12 + 12;
        
        // ── Live FPS chart ───────────────────────────────────────────
        Theme.section(g, this.font, x, y, cw, "Live FPS");
        y += 13;
        int chartH = 44;
        Theme.card(g, x, y, cw, chartH);
        drawChart(g, monitor, config, x, y, cw, chartH, target);
        y += chartH + 12;
        
        // ── Status ───────────────────────────────────────────────────
        Theme.section(g, this.font, x, y, cw, "Status");
        y += 13;
        int colW = cw / 2;
        kv(g, x, y, "Profile", profileManager.getCurrentProfile().getDisplayName(), Theme.GOOD);
        kv(g, x + colW, y, "Tier", hardware.getTier().getDisplayName(), Theme.TEXT);
        y += 12;
        String stateText = tuner.isInCombat() ? "Combat" : (tuner.isHeavyScene() ? "Heavy" : (tuner.isMoving() ? "Moving" : "Idle"));
        int stateColor = tuner.isInCombat() ? Theme.BAD : (tuner.isHeavyScene() ? Theme.WARN : (tuner.isMoving() ? Theme.GOOD : Theme.TEXT_DIM));
        kv(g, x, y, "State", stateText, stateColor);
        String dim = tuner.getCurrentDimension();
        if (dim != null && !dim.isEmpty()) dim = Character.toUpperCase(dim.charAt(0)) + dim.substring(1);
        kv(g, x + colW, y, "Dimension", dim, Theme.ACCENT_LIGHT);
        y += 12;
        kv(g, x, y, "Mods", SodiumCompat.getOptimizationStatus(), SodiumCompat.isSodiumLoaded() ? Theme.GOOD : Theme.WARN);
        kv(g, x + colW, y, "Auto", config.isAutoOptimize() ? "ON" : "OFF", config.isAutoOptimize() ? Theme.GOOD : Theme.BAD);
        y += 16;
        
        // ── Session history ──────────────────────────────────────────
        int histAvg = config.getAverageHistoricalFps();
        Theme.section(g, this.font, x, y, cw, "Session History");
        if (histAvg > 0) {
            String a = "avg " + histAvg;
            g.text(this.font, a, x + cw - this.font.width(a), y, Theme.TEXT_DIM, true);
        }
        y += 13;
        int histH = 24;
        Theme.card(g, x, y, cw, histH);
        if (histAvg > 0) {
            int[] history = config.getFpsHistory();
            int maxHist = 1;
            for (int hh : history) if (hh > maxHist) maxHist = hh;
            int barW = (cw - 16) / 10;
            for (int i = 0; i < 10; i++) {
                int v = history[i];
                if (v > 0) {
                    int bh = Math.max(2, v * (histH - 6) / maxHist);
                    int bx = x + 8 + i * barW;
                    int bc = v >= target ? Theme.GOOD : Theme.WARN;
                    g.fillGradient(bx, y + histH - 3 - bh, bx + barW - 3, y + histH - 3, bc, Theme.CARD_BOT);
                }
            }
        } else {
            Theme.centered(g, this.font, "No history yet \u2014 play more sessions", x + cw / 2, y + histH / 2 - 4, Theme.TEXT_MUTED);
        }
        y += histH + 8;
        
        // ── Tip ──────────────────────────────────────────────────────
        long now = System.currentTimeMillis();
        if (now - lastTipChange > 5000) { currentTip = (currentTip + 1) % TIPS.length; lastTipChange = now; }
        Theme.centered(g, this.font, "\uD83D\uDCA1 " + TIPS[currentTip], centerX, y, Theme.TEXT_MUTED);
    }
    
    private void kv(GuiGraphicsExtractor g, int x, int y, String key, String value, int valueColor) {
        g.text(this.font, key, x, y, Theme.TEXT_MUTED, true);
        g.text(this.font, value, x + 56, y, valueColor, true);
    }
    
    private void drawChart(GuiGraphicsExtractor g, FPSMonitor monitor, ModConfig config, int x, int y, int w, int h, int target) {
        LinkedList<Integer> data = monitor.getFpsGraphData();
        int maxFps = Math.max(target + 30, monitor.getMaxFps() + 10);
        // target guide line
        int targetY = y + h - (target * h / maxFps);
        for (int tx = x + 2; tx < x + w - 2; tx += 5) g.fill(tx, targetY, tx + 2, targetY + 1, 0x66F3C24B);
        if (data.size() > 1) {
            int prevX = x, prevY = y + h;
            int i = 0;
            for (int v : data) {
                int px = x + (i * (w - 2) / 60);
                int py = y + h - Math.min(h - 2, v * h / maxFps);
                int col = v >= target ? Theme.GOOD : (v >= 30 ? Theme.WARN : Theme.BAD);
                if (i > 0) line(g, prevX, prevY, px, py, col);
                prevX = px; prevY = py;
                i++;
            }
        }
        g.text(this.font, String.valueOf(maxFps), x + 3, y + 2, Theme.TEXT_MUTED, true);
        g.text(this.font, "0", x + 3, y + h - 10, Theme.TEXT_MUTED, true);
    }
    
    private void line(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            g.fill(x1, y1, x1 + 2, y1 + 2, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }
    
    @Override
    public boolean isPauseScreen() { return false; }
}
