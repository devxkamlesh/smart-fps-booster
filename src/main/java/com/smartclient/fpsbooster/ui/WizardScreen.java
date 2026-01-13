package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.hardware.HardwareDetector;
import com.smartclient.fpsbooster.profile.OptimizationProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class WizardScreen extends Screen {
    private int step = 0;
    private OptimizationProfile selectedProfile = OptimizationProfile.BALANCED;
    private boolean isLaptop = false;
    
    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 220;
    
    public WizardScreen() {
        super(Text.literal("Setup Wizard"));
    }
    
    @Override
    protected void init() {
        clearChildren();
        
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
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Get Started →"),
            button -> { step = 1; init(); }
        ).dimensions(left + PANEL_WIDTH / 2 - 60, top + 150, 120, 20).build());
    }
    
    private void initPriorityChoice(int left, int top) {
        int y = top + 70;
        int buttonWidth = PANEL_WIDTH - 40;
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("⚡ PERFORMANCE - Smooth gameplay, high FPS"),
            button -> { selectedProfile = OptimizationProfile.MAX_FPS; step = 2; init(); }
        ).dimensions(left + 20, y, buttonWidth, 25).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("⚖️ BALANCED - Best of both worlds"),
            button -> { selectedProfile = OptimizationProfile.BALANCED; step = 2; init(); }
        ).dimensions(left + 20, y + 35, buttonWidth, 25).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("🎨 VISUALS - Beautiful graphics first"),
            button -> { selectedProfile = OptimizationProfile.QUALITY; step = 2; init(); }
        ).dimensions(left + 20, y + 70, buttonWidth, 25).build());
    }
    
    private void initLaptopChoice(int left, int top) {
        int y = top + 90;
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Yes, optimize for battery"),
            button -> { 
                isLaptop = true; 
                selectedProfile = OptimizationProfile.BATTERY_SAVER;
                step = 3; 
                init(); 
            }
        ).dimensions(left + 20, y, PANEL_WIDTH - 40, 25).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("No, I'm on desktop"),
            button -> { isLaptop = false; step = 3; init(); }
        ).dimensions(left + 20, y + 35, PANEL_WIDTH - 40, 25).build());
    }
    
    private void initComplete(int left, int top) {
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Start Playing"),
            button -> {
                SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
                mod.getProfileManager().applyProfile(selectedProfile);
                mod.getConfig().setFirstLaunch(false);
                this.close();
            }
        ).dimensions(left + PANEL_WIDTH / 2 - 60, top + 160, 120, 25).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Don't call super.renderBackground - just draw a semi-transparent overlay
        context.fill(0, 0, this.width, this.height, 0x90000000);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelLeft = centerX - PANEL_WIDTH / 2;
        int panelTop = centerY - PANEL_HEIGHT / 2;
        
        context.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, 0xFF16162a);
        context.drawBorder(panelLeft, panelTop, PANEL_WIDTH, PANEL_HEIGHT, 0xFF3282b8);
        
        switch (step) {
            case 0 -> renderWelcome(context, centerX, panelTop);
            case 1 -> renderPriorityChoice(context, centerX, panelTop);
            case 2 -> renderLaptopChoice(context, centerX, panelTop);
            case 3 -> renderComplete(context, centerX, panelTop);
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    private void renderWelcome(DrawContext context, int centerX, int top) {
        context.drawCenteredTextWithShadow(textRenderer, "🎮 Welcome to Smart FPS Booster", centerX, top + 20, 0xFFbbe1fa);
        context.drawCenteredTextWithShadow(textRenderer, "Let's optimize your Minecraft", centerX, top + 60, 0xFF888888);
        context.drawCenteredTextWithShadow(textRenderer, "experience in 30 seconds!", centerX, top + 75, 0xFF888888);
        
        HardwareDetector hw = SmartFPSBoosterClient.getInstance().getHardwareDetector();
        context.drawCenteredTextWithShadow(textRenderer, "Detected: " + hw.getTier().getDisplayName() + " PC", centerX, top + 110, 0xFF4ecca3);
    }
    
    private void renderPriorityChoice(DrawContext context, int centerX, int top) {
        context.drawCenteredTextWithShadow(textRenderer, "What matters more to you?", centerX, top + 25, 0xFFbbe1fa);
    }
    
    private void renderLaptopChoice(DrawContext context, int centerX, int top) {
        context.drawCenteredTextWithShadow(textRenderer, "🔋 Are you on a laptop?", centerX, top + 25, 0xFFbbe1fa);
        context.drawCenteredTextWithShadow(textRenderer, "We can optimize for battery life", centerX, top + 50, 0xFF888888);
    }
    
    private void renderComplete(DrawContext context, int centerX, int top) {
        context.drawCenteredTextWithShadow(textRenderer, "✅ All Set!", centerX, top + 25, 0xFF4ecca3);
        
        context.drawCenteredTextWithShadow(textRenderer, "Profile: " + selectedProfile.getDisplayName(), centerX, top + 60, 0xFFbbe1fa);
        context.drawCenteredTextWithShadow(textRenderer, "Target FPS: " + selectedProfile.getTargetFps() + "+", centerX, top + 80, 0xFF888888);
        
        context.drawCenteredTextWithShadow(textRenderer, "Press F8 to open dashboard", centerX, top + 115, 0xFF3282b8);
        context.drawCenteredTextWithShadow(textRenderer, "Settings auto-adjust as you play", centerX, top + 130, 0xFF666666);
    }
    
    @Override
    public boolean shouldCloseOnEsc() {
        return step == 3;
    }
    
    @Override
    public boolean shouldPause() {
        return true;
    }
}
