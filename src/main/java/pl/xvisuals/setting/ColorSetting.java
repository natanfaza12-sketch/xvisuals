package pl.xvisuals.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import pl.xvisuals.util.ColorUtil;

/** Kolor wybierany suwakiem odcienia (hue), z osobnymi stałymi nasyceniem i jasnością. */
public class ColorSetting extends Setting {
    private float hue;
    private final float saturation;
    private final float brightness;

    public ColorSetting(String name, float defaultHue) {
        this(name, defaultHue, 0.85f, 1f);
    }

    public ColorSetting(String name, float defaultHue, float saturation, float brightness) {
        super(name);
        this.hue = defaultHue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public float hue() {
        return hue;
    }

    public void setHue(float hue) {
        this.hue = Math.max(0f, Math.min(1f, hue));
    }

    /** Kolor RGB (bez alfy). */
    public int rgb() {
        return ColorUtil.hsv(hue, saturation, brightness);
    }

    @Override
    public int height() {
        return 32;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(hue);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element != null && element.isJsonPrimitive()) setHue(element.getAsFloat());
    }
}
