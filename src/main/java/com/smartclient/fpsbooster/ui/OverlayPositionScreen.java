package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OverlayPositionScreen extends Screen {
    private final Screen parent;
    private int dragX, dragY;
    private boolean dragging = false;
    private int overlayWidth = 95;
    private int overlayHeight = 50;
    
    public OverlayPositionScreen(Screen parent) {
        super(Component.literal("Position Overlay"));
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
        
        this.addRenderableWidget(Button.builder(
            Component.literal("Reset to Corner"),
            button -> {
                config.setUseCustomPosition(false);
                this.onClose();
            }
        ).bounds(this.width / 2 - 155, this.height - 35, 150, 20).build());
        
        this.addRenderableWidget(Button.builder(
            Component.literal("Save Position"),
            button -> {
                config.setOverlayX(dragX);
                config.setOverlayY(dragY);
                config.setUseCustomPosition(true);
                config.saveOverlayPosition();
                this.onClose();
            }
        ).bounds(this.width / 2 + 5, this.height - 35, 150, 20).build());
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Theme.backdrop(context, this.width, this.height);
        
        Theme.centered(context, this.font, "Drag the overlay to position it", this.width / 2, 16, Theme.TEXT);
        Theme.centered(context, this.font, "Click and drag the preview box", this.width / 2, 30, Theme.TEXT_DIM);
        
        int borderColor = dragging ? Theme.GOOD : Theme.ACCENT;
        context.fillGradient(dragX - 5, dragY - 5, dragX + overlayWidth, dragY + overlayHeight, 0xEE1B1E33, 0xEE0F1120);
        context.fill(dragX - 5, dragY - 5, dragX - 2, dragY + overlayHeight, borderColor);
        context.outline(dragX - 5, dragY - 5, overlayWidth + 5, overlayHeight + 5, borderColor);
        
        context.text(this.font, "FPS  60", dragX, dragY, Theme.GOOD, true);
        context.text(this.font, "Avg  58", dragX, dragY + 12, Theme.TEXT_DIM, true);
        
        int barY = dragY + 28;
        int barWidth = overlayWidth - 12;
        Theme.bar(context, dragX, barY, barWidth, 8, 0.45f, Theme.GOOD);
        
        Theme.centered(context, this.font,
            String.format("Position: X=%d, Y=%d", dragX, dragY), this.width / 2, this.height - 55, 0xFF888888);
    }
    
    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click, boolean doubled) {
        if (click.button() == 0) {
            double mouseX = click.x();
            double mouseY = click.y();
            if (mouseX >= dragX - 4 && mouseX <= dragX + overlayWidth &&
                mouseY >= dragY - 4 && mouseY <= dragY + overlayHeight) {
                dragging = true;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }
    
    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent click) {
        if (click.button() == 0) {
            dragging = false;
        }
        return super.mouseReleased(click);
    }
    
    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent click, double deltaX, double deltaY) {
        if (dragging) {
            dragX = (int) Math.max(4, Math.min(click.x() - overlayWidth / 2, this.width - overlayWidth - 4));
            dragY = (int) Math.max(4, Math.min(click.y() - overlayHeight / 2, this.height - overlayHeight - 70));
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
