package com.smartclient.fpsbooster.mixin;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class GameOptionsMixin {
    
    @Inject(method = "save", at = @At("HEAD"))
    private void onWrite(CallbackInfo ci) {
        // Log when game options are saved
        SmartFPSBoosterClient.LOGGER.debug("Game options saved");
    }
}
