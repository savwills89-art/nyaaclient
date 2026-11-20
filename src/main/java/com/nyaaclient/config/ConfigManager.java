package com.nyaaclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nyaaclient.NyaaClient;
import com.nyaaclient.module.Module;
import com.nyaaclient.module.ModuleManager;
import com.nyaaclient.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigManager {
    private static final int CONFIG_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file = FabricLoader.getInstance().getConfigDir().resolve("nyaaclient.json");
    private final ModuleManager modules;

    public ConfigManager(ModuleManager modules) {
        this.modules = modules;
    }

    public void load() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                NyaaClient.LOGGER.warn("Config root is not an object, using defaults");
                return;
            }
            JsonObject root = parsed.getAsJsonObject();
            if (!root.has("modules") || !root.get("modules").isJsonObject()) {
                return;
            }
            JsonObject saved = root.getAsJsonObject("modules");
            for (Module module : modules.all()) {
                JsonElement entry = saved.get(module.getName());
                if (entry == null || !entry.isJsonObject()) {
                    continue;
                }
                applyModule(module, entry.getAsJsonObject());
            }
        } catch (Exception e) {
            NyaaClient.LOGGER.error("Could not read {}, using defaults", file, e);
        }
    }

    private void applyModule(Module module, JsonObject json) {
        JsonElement enabled = json.get("enabled");
        if (enabled != null && enabled.isJsonPrimitive() && enabled.getAsJsonPrimitive().isBoolean()) {
            module.restoreEnabled(enabled.getAsBoolean());
        }
        JsonElement key = json.get("key");
        if (key != null && key.isJsonPrimitive() && key.getAsJsonPrimitive().isNumber()) {
            module.setKey(key.getAsInt());
        }
        JsonElement settings = json.get("settings");
        if (settings != null && settings.isJsonObject()) {
            JsonObject values = settings.getAsJsonObject();
            for (Setting<?> setting : module.getSettings()) {
                if (values.has(setting.getName())) {
                    setting.fromJson(values.get(setting.getName()));
                }
            }
        }
    }

    public void save() {
        JsonObject root = new JsonObject();
        root.addProperty("configVersion", CONFIG_VERSION);
        JsonObject saved = new JsonObject();
        for (Module module : modules.all()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("enabled", module.isEnabled());
            entry.addProperty("key", module.getKey());
            JsonObject values = new JsonObject();
            for (Setting<?> setting : module.getSettings()) {
                values.add(setting.getName(), setting.toJson());
            }
            entry.add("settings", values);
            saved.add(module.getName(), entry);
        }
        root.add("modules", saved);

        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            NyaaClient.LOGGER.error("Could not write {}", file, e);
        }
    }
}
