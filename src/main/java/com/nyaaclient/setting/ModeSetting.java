package com.nyaaclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.List;

public final class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = List.of(modes);
        if (!this.modes.contains(defaultMode)) {
            throw new IllegalArgumentException("Default mode not in list for setting " + name);
        }
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean is(String mode) {
        return value.equals(mode);
    }

    public void cycle() {
        int next = (modes.indexOf(value) + 1) % modes.size();
        this.value = modes.get(next);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String candidate = element.getAsString();
            if (modes.contains(candidate)) {
                this.value = candidate;
            }
        }
    }
}
