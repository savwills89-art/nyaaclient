package com.nyaaclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        if (!(min < max) || !(step > 0.0)) {
            throw new IllegalArgumentException("Invalid range for setting " + name);
        }
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = normalize(defaultValue);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public float getFloat() {
        return value.floatValue();
    }

    public int getInt() {
        return (int) Math.round(value);
    }

    public void set(double newValue) {
        this.value = normalize(newValue);
    }

    public int decimals() {
        if (step >= 1.0) {
            return 0;
        }
        return step >= 0.1 ? 1 : 2;
    }

    private double normalize(double raw) {
        if (Double.isNaN(raw) || Double.isInfinite(raw)) {
            return min;
        }
        double snapped = min + Math.round((raw - min) / step) * step;
        double clamped = Math.max(min, Math.min(max, snapped));
        return Math.round(clamped * 10000.0) / 10000.0;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
            set(element.getAsDouble());
        }
    }
}
