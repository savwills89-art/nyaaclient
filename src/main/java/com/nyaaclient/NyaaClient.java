package com.nyaaclient;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nyaaclient.config.ConfigManager;
import com.nyaaclient.gui.NyaaMainMenuScreen;
import com.nyaaclient.module.Module;
import com.nyaaclient.module.ModuleManager;
import com.nyaaclient.modules.MainMenu;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.InputUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class NyaaClient implements ClientModInitializer {
    public static final String MOD_ID = "nyaaclient";
    public static final Logger LOGGER = LoggerFactory.getLogger("NyaaClient");

    private static NyaaClient instance;

    private ModuleManager modules;
    private ConfigManager config;
    private final Map<Module, Boolean> keyDown = new HashMap<>();

    public static NyaaClient instance() {
        return instance;
    }

    public ModuleManager modules() {
        return modules;
    }

    public ConfigManager config() {
        return config;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        modules = new ModuleManager();
        config = new ConfigManager(modules);
        config.load();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        HudRenderCallback.EVENT.register((context, tickCounter) -> modules.renderHud(context));
        WorldRenderEvents.LAST.register(context -> {
            if (context.matrixStack() == null) {
                return;
            }
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.lineWidth(2.0f);
            try {
                modules.renderWorld(context);
            } finally {
                RenderSystem.lineWidth(1.0f);
                RenderSystem.disableBlend();
                RenderSystem.enableDepthTest();
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> config.save());

        LOGGER.info("Nyaa Client loaded with {} modules", modules.all().size());
    }

    private void onClientTick(MinecraftClient client) {
        // Exact class match so subclasses and our own screen are never swapped.
        if (client.currentScreen != null
                && client.currentScreen.getClass() == TitleScreen.class
                && modules.get(MainMenu.class).isEnabled()) {
            client.setScreen(new NyaaMainMenuScreen());
        }
        pollKeys(client);
        modules.tick();
    }

    private void pollKeys(MinecraftClient client) {
        if (client.getWindow() == null) {
            return;
        }
        long window = client.getWindow().getHandle();
        for (Module module : modules.all()) {
            int key = module.getKey();
            if (key < 0) {
                keyDown.remove(module);
                continue;
            }
            boolean down = InputUtil.isKeyPressed(window, key);
            boolean wasDown = keyDown.getOrDefault(module, false);
            keyDown.put(module, down);
            if (down && !wasDown && client.currentScreen == null) {
                try {
                    module.onKeyPress();
                } catch (Throwable t) {
                    LOGGER.error("Module {} failed in key handler", module.getName(), t);
                }
            }
        }
    }
}
