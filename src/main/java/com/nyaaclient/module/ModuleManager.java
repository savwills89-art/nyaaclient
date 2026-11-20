package com.nyaaclient.module;

import com.nyaaclient.NyaaClient;
import com.nyaaclient.modules.ClickGuiModule;
import com.nyaaclient.modules.Esp;
import com.nyaaclient.modules.Fly;
import com.nyaaclient.modules.Fullbright;
import com.nyaaclient.modules.HudModule;
import com.nyaaclient.modules.KillAura;
import com.nyaaclient.modules.MainMenu;
import com.nyaaclient.modules.NoFall;
import com.nyaaclient.modules.Speed;
import com.nyaaclient.modules.Sprint;
import com.nyaaclient.modules.Step;
import com.nyaaclient.modules.Tracers;
import com.nyaaclient.modules.TriggerBot;
import com.nyaaclient.modules.Velocity;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        register(new KillAura());
        register(new TriggerBot());
        register(new Velocity());

        register(new Sprint());
        register(new Speed());
        register(new Fly());
        register(new NoFall());
        register(new Step());

        register(new Esp());
        register(new Tracers());
        register(new Fullbright());

        register(new HudModule());
        register(new MainMenu());
        register(new ClickGuiModule());
    }

    private void register(Module module) {
        modules.add(module);
    }

    public List<Module> all() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> byCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category && !module.isHidden()) {
                result.add(module);
            }
        }
        return result;
    }

    public <T extends Module> T get(Class<T> type) {
        for (Module module : modules) {
            if (type.isInstance(module)) {
                return type.cast(module);
            }
        }
        throw new IllegalStateException("Module not registered: " + type.getSimpleName());
    }

    public void tick() {
        for (Module module : modules) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onTick();
            } catch (Throwable t) {
                fail(module, "tick", t);
            }
        }
    }

    public void renderWorld(WorldRenderContext context) {
        for (Module module : modules) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onWorldRender(context);
            } catch (Throwable t) {
                fail(module, "world render", t);
            }
        }
    }

    public void renderHud(DrawContext context) {
        for (Module module : modules) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onHud(context);
            } catch (Throwable t) {
                fail(module, "hud render", t);
            }
        }
    }

    private void fail(Module module, String phase, Throwable t) {
        NyaaClient.LOGGER.error("Module {} failed during {} and was disabled", module.getName(), phase, t);
        try {
            module.setEnabled(false);
        } catch (Throwable inner) {
            NyaaClient.LOGGER.error("Module {} also failed while disabling", module.getName(), inner);
        }
    }
}
