package com.smartclient.fpsbooster.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath(SmartFPSBoosterClient.MOD_ID, "general"));
    
    private static KeyMapping openDashboard;
    private static KeyMapping toggleAutoOptimize;
    private static KeyMapping cycleProfiles;
    
    public static void register() {
        // F8 - Open Dashboard
        openDashboard = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.smartfpsbooster.dashboard",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            CATEGORY
        ));
        
        // F9 - Toggle Auto-Optimize
        toggleAutoOptimize = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.smartfpsbooster.toggle_auto",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            CATEGORY
        ));
        
        // F10 - Cycle Profiles
        cycleProfiles = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.smartfpsbooster.cycle_profiles",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            CATEGORY
        ));
        
        // Register tick handler for keybinds
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openDashboard.consumeClick()) {
                openDashboardScreen();
            }
            
            while (toggleAutoOptimize.consumeClick()) {
                toggleAutoOptimizeWithToast();
            }
            
            while (cycleProfiles.consumeClick()) {
                cycleProfileWithToast();
            }
        });
    }
    
    private static void openDashboardScreen() {
        Minecraft client = Minecraft.getInstance();
        client.setScreenAndShow(new DashboardScreen());
    }
    
    private static void toggleAutoOptimizeWithToast() {
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        mod.getOptimizationManager().toggleAutoOptimize();
        
        ModConfig config = mod.getConfig();
        String status = config.isAutoOptimize() ? "ON" : "OFF";
        NotificationManager.showInfo("Auto-Optimize: " + status);
    }
    
    private static void cycleProfileWithToast() {
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        mod.getProfileManager().cycleProfile();
        
        String profileName = mod.getProfileManager().getCurrentProfile().getDisplayName();
        NotificationManager.showInfo("Profile: " + profileName);
    }
}
