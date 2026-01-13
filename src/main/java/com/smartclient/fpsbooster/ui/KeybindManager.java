package com.smartclient.fpsbooster.ui;

import com.smartclient.fpsbooster.SmartFPSBoosterClient;
import com.smartclient.fpsbooster.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {
    private static KeyBinding openDashboard;
    private static KeyBinding toggleAutoOptimize;
    private static KeyBinding cycleProfiles;
    
    public static void register() {
        // F8 - Open Dashboard
        openDashboard = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.smartfpsbooster.dashboard",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "category.smartfpsbooster"
        ));
        
        // F9 - Toggle Auto-Optimize
        toggleAutoOptimize = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.smartfpsbooster.toggle_auto",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "category.smartfpsbooster"
        ));
        
        // F10 - Cycle Profiles
        cycleProfiles = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.smartfpsbooster.cycle_profiles",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F10,
            "category.smartfpsbooster"
        ));
        
        // Register tick handler for keybinds
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openDashboard.wasPressed()) {
                openDashboardScreen();
            }
            
            while (toggleAutoOptimize.wasPressed()) {
                toggleAutoOptimizeWithToast();
            }
            
            while (cycleProfiles.wasPressed()) {
                cycleProfileWithToast();
            }
        });
    }
    
    private static void openDashboardScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new DashboardScreen());
    }
    
    private static void toggleAutoOptimizeWithToast() {
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        mod.getOptimizationManager().toggleAutoOptimize();
        
        ModConfig config = mod.getConfig();
        MinecraftClient client = MinecraftClient.getInstance();
        
        String status = config.isAutoOptimize() ? "§aON" : "§cOFF";
        client.getToastManager().add(new SystemToast(
            SystemToast.Type.PERIODIC_NOTIFICATION,
            Text.literal("⚡ Smart FPS Booster"),
            Text.literal("Auto-Optimize: " + status)
        ));
    }
    
    private static void cycleProfileWithToast() {
        SmartFPSBoosterClient mod = SmartFPSBoosterClient.getInstance();
        mod.getProfileManager().cycleProfile();
        
        MinecraftClient client = MinecraftClient.getInstance();
        String profileName = mod.getProfileManager().getCurrentProfile().getDisplayName();
        
        client.getToastManager().add(new SystemToast(
            SystemToast.Type.PERIODIC_NOTIFICATION,
            Text.literal("⚡ Smart FPS Booster"),
            Text.literal("Profile: §b" + profileName)
        ));
    }
}
