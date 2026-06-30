package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.hardware.HardwareDetector;
import com.smartclient.fpsbooster.profile.OptimizationProfile;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WizardScreen extends Screen {
    private int step = 0;
    private OptimizationProfile selectedProfile = OptimizationProfile.BALANCED;
    private boolean isLaptop = false;
    
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 220;
    
    public WizardScreen() {
        super(Component.literal("Setup Wizard"));
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = centerY - PANEL_HEIGHT / 2;
        
        switch (step) {
            case 0 -> initWelcome(panelLeft, panelTop);
            case 1 -> initPriorityChoice(panelLeft, panelTop);
            case 2 -> initLaptopChoice(panelLeft, panelTop);
            case 3 -> initComplete(panelLeft, panelTop);
        }
    }
    
    private void initWelcome(int left, int top) {
        this.addRenderableWidget(Button.builder(
            Component.literal("Get Started >"),
            button -> { step = 1; this.rebuildWidgets(); }
        ).bounds(left + PANEL_WIDTH / 2 - 60, top + 150, 120, 20).build());
    }
    
    private void initPriorityChoice(int left, int top) {
        int y = top + 70;
        int buttonWidth = PANEL_WIDTH - 40;
        
        this.addRenderableWidget(Button.builder(
            Component.literal("PERFORMANCE - Smooth gameplay, high FPS"),
            button -> { selectedProfile = OptimizationProfile.MAX_FPS; step = 2; this.rebuildWidgets(); }
        ).bounds(left + 20, y, buttonWidth, 25).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("BALANCED - Best of both worlds"),
            button -> { selectedProfile = OptimizationProfile.BALANCED; step = 2; this.rebuildWidgets(); }
        ).bounds(left + 20, y + 35, buttonWidth, 25).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("VISUALS - Beautiful graphics first"),
            button -> { selectedProfile = OptimizationProfile.QUALITY; step = 2; this.rebuildWidgets(); }
        ).bounds(left + 20, y + 70, buttonWidth, 25).build());
    }
    
    private void initLaptopChoice(int left, int top) {
        int y = top + 90;
        
        this.addRenderableWidget(Button.builder(
            Component.literal("Yes, optimize for battery"),
            button -> { 
                isLaptop = true; 
                selectedProfile = OptimizationProfile.BATTERY_SAVER;
                step = 3; 
                this.rebuildWidgets(); 
            }
        ).bounds(left + 20, y, PANEL_WIDTH - 40, 25).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("No, I'm on desktop"),
            button -> { isLaptop = false; step = 3; this.rebuildWidgets(); }
        ).bounds(left + 20, y + 35, PANEL_WIDTH - 40, 25).build());
    }
    
    private void initComplete(int left, int top) {
        this.addRenderableWidget(Button.builder(
            Component.literal("Start Playing"),
            button -> {
                SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
                mod.getProfileManager().applyProfile(selectedProfile);
                mod.getConfig().setFirstLaunch(false);
                this.onClose();
            }
        ).bounds(left + PANEL_WIDTH / 2 - 60, top + 160, 120, 25).build());
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Theme.backdrop(context, this.width, this.height);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = centerY - PANEL_HEIGHT / 2;
        
        Theme.panel(context, panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT);
        Theme.titleBar(context, this.font, panelLeft, panelTop, PANEL_WIDTH, "\u26A1 Setup Wizard");
        
        // step progress dots
        int dots = 4;
        int dotY = panelTop + PANEL_HEIGHT - 14;
        int dotGap = 14;
        int startX = centerX - (dots - 1) * dotGap / 2;
        for (int i = 0; i < dots; i++) {
            int col = i == step ? Theme.ACCENT : (i < step ? Theme.GOOD : Theme.TRACK);
            context.fill(startX + i * dotGap - 3, dotY, startX + i * dotGap + 3, dotY + 3, col);
        }
        
        switch (step) {
            case 0 -> renderWelcome(context, centerX, panelTop);
            case 1 -> renderPriorityChoice(context, centerX, panelTop);
            case 2 -> renderLaptopChoice(context, centerX, panelTop);
            case 3 -> renderComplete(context, centerX, panelTop);
        }
    }
    
    private void renderWelcome(GuiGraphicsExtractor context, int centerX, int top) {
        Theme.centered(context, this.font, "Welcome to Smart FPS Booster", centerX, top + 40, Theme.TEXT);
        Theme.centered(context, this.font, "Let's optimize your Minecraft", centerX, top + 66, Theme.TEXT_DIM);
        Theme.centered(context, this.font, "experience in 30 seconds", centerX, top + 80, Theme.TEXT_DIM);
        
        HardwareDetector hw = SmartFPSBoosterClient.getInstance().getHardwareDetector();
        Theme.centered(context, this.font, "Detected: " + hw.getTier().getDisplayName() + " PC", centerX, top + 110, Theme.GOOD);
    }
    
    private void renderPriorityChoice(GuiGraphicsExtractor context, int centerX, int top) {
        Theme.centered(context, this.font, "What matters more to you?", centerX, top + 40, Theme.TEXT);
    }
    
    private void renderLaptopChoice(GuiGraphicsExtractor context, int centerX, int top) {
        Theme.centered(context, this.font, "Are you on a laptop?", centerX, top + 45, Theme.TEXT);
        Theme.centered(context, this.font, "We can optimize for battery life", centerX, top + 65, Theme.TEXT_DIM);
    }
    
    private void renderComplete(GuiGraphicsExtractor context, int centerX, int top) {
        Theme.centered(context, this.font, "\u2713 All Set!", centerX, top + 40, Theme.GOOD);
        Theme.centered(context, this.font, "Profile: " + selectedProfile.getDisplayName(), centerX, top + 70, Theme.ACCENT_LIGHT);
        Theme.centered(context, this.font, "Target FPS: " + selectedProfile.getTargetFps() + "+", centerX, top + 88, Theme.TEXT_DIM);
        Theme.centered(context, this.font, "Press F8 to open the dashboard", centerX, top + 116, Theme.TEXT);
        Theme.centered(context, this.font, "Settings auto-adjust as you play", centerX, top + 130, Theme.TEXT_MUTED);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return step == 3;
    }
    
    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
