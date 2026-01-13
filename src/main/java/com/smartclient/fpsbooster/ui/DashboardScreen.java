package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.compat.SodiumCompat;
import com.smartclient.fpsbooster.config.ModConfig;
import com.smartclient.fpsbooster.hardware.HardwareDetector;
import com.smartclient.fpsbooster.optimization.DynamicTuner;
import com.smartclient.fpsbooster.optimization.FPSMonitor;
import com.smartclient.fpsbooster.profile.ProfileManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.LinkedList;

public class DashboardScreen extends Screen {
    private static final int PANEL_WIDTH = 340;
    private static final int PANEL_HEIGHT = 320;
    
    private static final String[] TIPS = {
        "Use 'Max FPS' profile for competitive play",
        "Lower render distance = more FPS",
        "Turn off clouds and particles for boost",
        "Enable Auto-Optimize for hands-free tuning",
        "Press F9 to quickly toggle Auto-Optimize"
    };
    private int currentTip = 0;
    private long lastTipChange = 0;
    
    public DashboardScreen() {
        super(Text.literal("Smart FPS Booster"));
        currentTip = (int) (System.currentTimeMillis() % TIPS.length);
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelTop = centerY - PANEL_HEIGHT / 2;
        int bottomY = panelTop + PANEL_HEIGHT - 30;
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Settings"),
            button -> client.setScreen(new SettingsScreen(this))
        ).dimensions(centerX - 105, bottomY, 100, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Close"),
            button -> this.close()
        ).dimensions(centerX + 5, bottomY, 100, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x90000000);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = centerY - PANEL_HEIGHT / 2;
        
        context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xE0101018);
        context.drawBorder(panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT, 0xFF3080c0);
        
        context.drawCenteredTextWithShadow(this.textRenderer, "Performance Dashboard", centerX, panelTop + 10, 0xFFbbe1fa);
        
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        FPSMonitor monitor = mod.getOptimizationManager().getFpsMonitor();
        DynamicTuner tuner = mod.getOptimizationManager().getDynamicTuner();
        ProfileManager profileManager = mod.getProfileManager();
        HardwareDetector hardware = mod.getHardwareDetector();
        ModConfig config = mod.getConfig();
        
        int y = panelTop + 28;
        int col1 = panelLeft + 15;
        int col2 = centerX + 10;
        
        // ═══════════════════════════════════════════
        // FPS SECTION
        // ═══════════════════════════════════════════
        int fps = monitor.getCurrentFps();
        int fpsColor = fps >= config.getTargetFps() ? 0xFF4ecca3 : (fps >= 30 ? 0xFFffcc00 : 0xFFff4444);
        
        // Big FPS with label
        context.drawTextWithShadow(this.textRenderer, "FPS", col1, y, 0xFF4080c0);
        context.drawTextWithShadow(this.textRenderer, String.valueOf(fps), col1 + 30, y, fpsColor);
        
        // Stats on same row
        context.drawTextWithShadow(this.textRenderer, "Avg:" + monitor.getAverageFps(), col1 + 80, y, 0xFFaaaaaa);
        context.drawTextWithShadow(this.textRenderer, "1%:" + monitor.getOnePercentLow(), col1 + 140, y, 0xFFaaaaaa);
        context.drawTextWithShadow(this.textRenderer, String.format("%.1fms", monitor.getFrameTimeMs()), col1 + 200, y, 0xFFaaaaaa);
        y += 16;
        
        // Memory bar
        context.drawTextWithShadow(this.textRenderer, "RAM", col1, y, 0xFF4080c0);
        int barX = col1 + 30;
        int barWidth = PANEL_WIDTH - 60;
        context.fill(barX, y, barX + barWidth, y + 10, 0xFF202030);
        int fillWidth = (int) (barWidth * monitor.getMemoryPercent() / 100);
        int memColor = monitor.getMemoryPercent() > 80 ? 0xFFfc5185 : 0xFF4ecca3;
        context.fill(barX, y, barX + fillWidth, y + 10, memColor);
        String memText = String.format("%dMB/%dMB", monitor.getUsedMemoryMB(), monitor.getMaxMemoryMB());
        context.drawCenteredTextWithShadow(this.textRenderer, memText, barX + barWidth / 2, y + 1, 0xFFFFFFFF);
        y += 16;
        
        // ═══════════════════════════════════════════
        // LIVE FPS CHART
        // ═══════════════════════════════════════════
        context.drawTextWithShadow(this.textRenderer, "Live FPS", col1, y, 0xFF4080c0);
        y += 12;
        
        LinkedList<Integer> graphData = monitor.getFpsGraphData();
        int chartX = col1;
        int chartWidth = PANEL_WIDTH - 30;
        int chartHeight = 40;
        
        // Chart background
        context.fill(chartX, y, chartX + chartWidth, y + chartHeight, 0xFF151520);
        context.drawBorder(chartX, y, chartWidth, chartHeight, 0xFF303050);
        
        // Target line
        int targetFps = config.getTargetFps();
        int maxChartFps = Math.max(targetFps + 30, monitor.getMaxFps() + 10);
        int targetY = y + chartHeight - (targetFps * chartHeight / maxChartFps);
        for (int tx = chartX; tx < chartX + chartWidth; tx += 4) {
            context.fill(tx, targetY, tx + 2, targetY + 1, 0x80ffcc00);
        }
        
        // Draw live graph line
        if (graphData.size() > 1) {
            int pointWidth = chartWidth / 60;
            int prevX = chartX;
            int prevY = y + chartHeight;
            
            int i = 0;
            for (int fpsVal : graphData) {
                int px = chartX + (i * chartWidth / 60);
                int py = y + chartHeight - Math.min(chartHeight - 2, fpsVal * chartHeight / maxChartFps);
                
                // Line color based on FPS
                int lineColor = fpsVal >= targetFps ? 0xFF4ecca3 : (fpsVal >= 30 ? 0xFFffcc00 : 0xFFff4444);
                
                if (i > 0) {
                    // Draw line segment
                    drawLine(context, prevX, prevY, px, py, lineColor);
                }
                
                prevX = px;
                prevY = py;
                i++;
            }
        }
        
        // Chart labels
        context.drawTextWithShadow(this.textRenderer, String.valueOf(maxChartFps), chartX + 2, y + 2, 0xFF666666);
        context.drawTextWithShadow(this.textRenderer, "0", chartX + 2, y + chartHeight - 10, 0xFF666666);
        context.drawTextWithShadow(this.textRenderer, "Target:" + targetFps, chartX + chartWidth - 55, y + 2, 0xFFffcc00);
        
        y += chartHeight + 8;
        
        // ═══════════════════════════════════════════
        // STATUS SECTION
        // ═══════════════════════════════════════════
        context.drawTextWithShadow(this.textRenderer, "Profile:", col1, y, 0xFF888888);
        context.drawTextWithShadow(this.textRenderer, profileManager.getCurrentProfile().getDisplayName(), col1 + 50, y, 0xFF4ecca3);
        context.drawTextWithShadow(this.textRenderer, "Tier:", col2, y, 0xFF888888);
        context.drawTextWithShadow(this.textRenderer, hardware.getTier().getDisplayName(), col2 + 30, y, 0xFFFFFFFF);
        y += 12;
        
        context.drawTextWithShadow(this.textRenderer, "Status:", col1, y, 0xFF888888);
        String status = tuner.isInCombat() ? "COMBAT" : (tuner.isHeavyScene() ? "HEAVY" : (tuner.isMoving() ? "MOVING" : "IDLE"));
        int statusColor = tuner.isInCombat() ? 0xFFff6b6b : (tuner.isHeavyScene() ? 0xFFffcc00 : (tuner.isMoving() ? 0xFF4ecca3 : 0xFF888888));
        context.drawTextWithShadow(this.textRenderer, status, col1 + 50, y, statusColor);
        
        context.drawTextWithShadow(this.textRenderer, "Dim:", col2, y, 0xFF888888);
        String dim = tuner.getCurrentDimension();
        dim = dim.substring(0, 1).toUpperCase() + dim.substring(1);
        context.drawTextWithShadow(this.textRenderer, dim, col2 + 30, y, 0xFFbbe1fa);
        y += 12;
        
        // Mods + Auto status
        context.drawTextWithShadow(this.textRenderer, "Mods:", col1, y, 0xFF888888);
        String modsStatus = SodiumCompat.getOptimizationStatus();
        int modsColor = SodiumCompat.isSodiumLoaded() ? 0xFF4ecca3 : 0xFFffcc00;
        context.drawTextWithShadow(this.textRenderer, modsStatus, col1 + 40, y, modsColor);
        
        boolean autoOn = config.isAutoOptimize();
        String autoStatus = "Auto: " + (autoOn ? "ON" : "OFF");
        int autoColor = autoOn ? 0xFF4ecca3 : 0xFFff6b6b;
        context.drawTextWithShadow(this.textRenderer, autoStatus, col2 + 50, y, autoColor);
        y += 16;
        
        // ═══════════════════════════════════════════
        // SESSION HISTORY (Last 10 sessions)
        // ═══════════════════════════════════════════
        context.drawTextWithShadow(this.textRenderer, "Session History", col1, y, 0xFF4080c0);
        y += 12;
        
        int[] history = config.getFpsHistory();
        int histAvg = config.getAverageHistoricalFps();
        
        int histChartX = col1;
        int histChartWidth = PANEL_WIDTH - 30;
        int histChartHeight = 25;
        
        // History chart background
        context.fill(histChartX, y, histChartX + histChartWidth, y + histChartHeight, 0xFF151520);
        context.drawBorder(histChartX, y, histChartWidth, histChartHeight, 0xFF303050);
        
        if (histAvg > 0) {
            int maxHistFps = 1;
            for (int h : history) if (h > maxHistFps) maxHistFps = h;
            
            int barW = (histChartWidth - 20) / 10;
            for (int i = 0; i < 10; i++) {
                int fpsVal = history[i];
                if (fpsVal > 0) {
                    int barH = Math.max(3, fpsVal * (histChartHeight - 4) / maxHistFps);
                    int bx = histChartX + 10 + i * barW;
                    int barCol = fpsVal >= targetFps ? 0xFF4ecca3 : 0xFFffcc00;
                    context.fill(bx, y + histChartHeight - 2 - barH, bx + barW - 3, y + histChartHeight - 2, barCol);
                }
            }
            
            // Session numbers
            for (int i = 0; i < 10; i++) {
                int bx = histChartX + 10 + i * barW + barW / 2 - 2;
                context.drawTextWithShadow(this.textRenderer, String.valueOf(i + 1), bx, y + histChartHeight - 10, 0xFF444444);
            }
            
            context.drawTextWithShadow(this.textRenderer, "Avg: " + histAvg, histChartX + histChartWidth - 50, y + 3, 0xFFaaaaaa);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, "No history - play more sessions!", centerX, y + 8, 0xFF555555);
        }
        y += histChartHeight + 8;
        
        // ═══════════════════════════════════════════
        // BENCHMARK SCORE
        // ═══════════════════════════════════════════
        int benchScore = config.getLastBenchmarkScore();
        if (benchScore > 0) {
            context.drawTextWithShadow(this.textRenderer, "Benchmark Score:", col1, y, 0xFF888888);
            int scoreColor = benchScore >= 100 ? 0xFF4ecca3 : (benchScore >= 60 ? 0xFFffcc00 : 0xFFff6b6b);
            context.drawTextWithShadow(this.textRenderer, String.valueOf(benchScore), col1 + 100, y, scoreColor);
        }
        y += 14;
        
        // Tip
        long now = System.currentTimeMillis();
        if (now - lastTipChange > 5000) { currentTip = (currentTip + 1) % TIPS.length; lastTipChange = now; }
        context.drawCenteredTextWithShadow(this.textRenderer, "💡 " + TIPS[currentTip], centerX, y, 0xFF555588);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    // Simple line drawing helper
    private void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        
        while (true) {
            context.fill(x1, y1, x1 + 2, y1 + 2, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }
    
    @Override
    public boolean shouldPause() { return false; }
}
