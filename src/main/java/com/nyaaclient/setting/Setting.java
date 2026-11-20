package com.nyaaclient.setting;

import com.google.gson.JsonElement;

public abstract class Setting<T> {
    private final String name;
    protected T value;

    protected Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement element);
}
