package com.nyaaclient.module;

import com.nyaaclient.setting.Setting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final boolean hidden;
    private final List<Setting<?>> settings = new ArrayList<>();

    private volatile boolean enabled;
    private int key = -1;

    protected Module(String name, String description, Category category) {
        this(name, description, category, false);
    }

    protected Module(String name, String description, Category category, boolean hidden) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean value) {
        if (this.enabled == value) {
            return;
        }
        this.enabled = value;
        if (value) {
            onEnable();
        } else {
            onDisable();
        }
    }

    /** Applies a saved state without running onEnable, which can need a live world. Tick logic is idempotent. */
    public void restoreEnabled(boolean value) {
        this.enabled = value;
    }

    /** Called when the bound key is pressed with no screen open. */
    public void onKeyPress() {
        toggle();
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick() {
    }

    public void onWorldRender(WorldRenderContext context) {
    }

    public void onHud(DrawContext context) {
    }
}
