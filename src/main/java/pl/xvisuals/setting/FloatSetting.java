package pl.xvisuals.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

public class FloatSetting extends Setting {
    public final float min;
    public final float max;
    public final float step;
    private final String suffix;
    private float value;

    public FloatSetting(String name, float min, float max, float defaultValue, float step) {
        this(name, min, max, defaultValue, step, "");
    }

    public FloatSetting(String name, float min, float max, float defaultValue, float step, String suffix) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
        this.value = clamp(defaultValue);
    }

    public float get() {
        return value;
    }

    public int getInt() {
        return Math.round(value);
    }

    public void set(float v) {
        this.value = clamp(snap(v));
    }

    /** Ustawia wartość z pozycji suwaka 0..1. */
    public void setFraction(float fraction) {
        set(min + (max - min) * Math.max(0f, Math.min(1f, fraction)));
    }

    public float fraction() {
        return (value - min) / (max - min);
    }

    public String display() {
        if (step >= 1f) return Math.round(value) + suffix;
        return String.format(Locale.ROOT, "%.2f", value) + suffix;
    }

    private float snap(float v) {
        if (step <= 0f) return v;
        return Math.round((v - min) / step) * step + min;
    }

    private float clamp(float v) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public int height() {
        return 32;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) value = clamp(element.getAsFloat());
    }
}
