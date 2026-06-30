package com.smartclient.fpsbooster.mixin;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.ui.FPSOverlay;
import com.smartclient.fpsbooster.ui.WizardScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    private boolean overlayRegistered = false;
    private boolean wizardShown = false;
    
    @Inject(method = "setScreenAndShow", at = @At("HEAD"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        // Register overlay on first screen
        if (!overlayRegistered) {
            FPSOverlay.register();
            overlayRegistered = true;
        }
        
        // Show wizard on first launch when reaching title screen
        if (!wizardShown && screen instanceof TitleScreen) {
            SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
            if (mod != null && mod.getConfig().isFirstLaunch()) {
                wizardShown = true;
                Minecraft.getInstance().execute(() -> {
                    Minecraft.getInstance().setScreenAndShow(new WizardScreen());
                });
            }
        }
    }
}
