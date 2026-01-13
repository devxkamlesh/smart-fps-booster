package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class OverlayPositionScreen extends Screen {
    private final Screen parent;
    private int dragX, dragY;
    private boolean dragging = false;
    private int overlayWidth = 95;
    private int overlayHeight = 50;
    
    public OverlayPositionScreen(Screen parent) {
        super(Text.literal("Position Overlay"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        ModConfig config = SmartFPSBoosterClient.getInstance().getConfig();
        
        if (config.isUseCustomPosition()) {
            dragX = config.getOverlayX();
            dragY = config.getOverlayY();
        } else {
            dragX = 50;
            dragY = 50;
        }
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Reset to Corner"),
            button -> {
                config.setUseCustomPosition(false);
                this.close();
            }
        ).dimensions(this.width / 2 - 155, this.height - 35, 150, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Save Position"),
            button -> {
                config.setOverlayX(dragX);
                config.setOverlayY(dragY);
                config.setUseCustomPosition(true);
                config.saveOverlayPosition();
                this.close();
            }
        ).dimensions(this.width / 2 + 5, this.height - 35, 150, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Don't call super.renderBackground - just draw a semi-transparent overlay
        context.fill(0, 0, this.width, this.height, 0x90000000);
        
        context.drawCenteredTextWithShadow(this.textRenderer, "Drag the overlay to position it", this.width / 2, 15, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Click and drag the box", this.width / 2, 30, 0xFF888888);
        
        int borderColor = dragging ? 0xFF4ecca3 : 0xFF3282b8;
        context.fill(dragX - 4, dragY - 4, dragX + overlayWidth, dragY + overlayHeight, 0xDD16162a);
        context.drawBorder(dragX - 4, dragY - 4, overlayWidth + 4, overlayHeight + 4, borderColor);
        
        context.drawTextWithShadow(this.textRenderer, "FPS: 60", dragX, dragY, 0xFF4ecca3);
        context.drawTextWithShadow(this.textRenderer, "Avg: 58", dragX, dragY + 12, 0xFFaaaaaa);
        
        int barY = dragY + 28;
        int barWidth = overlayWidth - 12;
        context.fill(dragX, barY, dragX + barWidth, barY + 8, 0xFF333333);
        context.fill(dragX, barY, dragX + (int)(barWidth * 0.45), barY + 8, 0xFF4ecca3);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            String.format("Position: X=%d, Y=%d", dragX, dragY), this.width / 2, this.height - 55, 0xFF888888);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (mouseX >= dragX - 4 && mouseX <= dragX + overlayWidth &&
                mouseY >= dragY - 4 && mouseY <= dragY + overlayHeight) {
                dragging = true;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            dragX = (int) Math.max(4, Math.min(mouseX - overlayWidth / 2, this.width - overlayWidth - 4));
            dragY = (int) Math.max(4, Math.min(mouseY - overlayHeight / 2, this.height - overlayHeight - 70));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public void close() {
        this.client.setScreen(parent);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
