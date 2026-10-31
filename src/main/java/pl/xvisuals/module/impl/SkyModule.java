package pl.xvisuals.module.impl;

import net.minecraft.util.math.Vec3d;
import pl.xvisuals.module.Category;
import pl.xvisuals.module.Module;
import pl.xvisuals.setting.BoolSetting;
import pl.xvisuals.setting.ColorSetting;
import pl.xvisuals.setting.FloatSetting;
import pl.xvisuals.setting.ModeSetting;
import pl.xvisuals.util.ColorUtil;

/** Własny kolor nieba i chmur. Zmiana następuje w ClientWorldMixin. */
public class SkyModule extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Color", "Rainbow", "Pulse");
    public final ColorSetting color = new ColorSetting("Color", 0.55f, 0.6f, 1f);
    public final FloatSetting speed = new FloatSetting("Speed", 0.1f, 5f, 1f, 0.1f, "x");
    public final FloatSetting brightness = new FloatSetting("Brightness", 0.2f, 1f, 1f, 0.05f);
    public final BoolSetting keepCycle = new BoolSetting("Keep day/night", true);
    public final BoolSetting clouds = new BoolSetting("Recolor clouds", true);

    public SkyModule() {
        super("Sky", "Własny kolor nieba i chmur", Category.VISUAL, true);
        add(mode);
        add(color).visibleIf(() -> !mode.is("Rainbow"));
        add(speed).visibleIf(() -> !mode.is("Color"));
        add(brightness);
        add(keepCycle);
        add(clouds);
    }

    public int skyRgb() {
        long t = System.currentTimeMillis();
        float hue = color.hue();
        float mul = brightness.get();
        if (mode.is("Rainbow")) {
            hue = (t * speed.get() / 8000f) % 1f;
        } else if (mode.is("Pulse")) {
            mul *= 0.65f + 0.35f * (float) Math.sin(t * speed.get() / 600.0);
        }
        return scale(ColorUtil.hsv(hue, 0.6f, 1f), mul);
    }

    public int cloudRgb() {
        return scale(ColorUtil.mix(skyRgb(), 0xFFFFFF, 0.55f), 1f);
    }

    /** Wywoływane z mixina; obsługuje zarówno zwrot typu int, jak i Vec3d. */
    public Object applySky(Object original) {
        return recolor(original, skyRgb());
    }

    public Object applyClouds(Object original) {
        return clouds.get() ? recolor(original, cloudRgb()) : original;
    }

    private Object recolor(Object original, int rgb) {
        float factor = keepCycle.get() ? dayFactor(original) : 1f;
        int out = scale(rgb, factor);
        if (original instanceof Integer) {
            return 0xFF000000 | out;
        }
        if (original instanceof Vec3d) {
            return new Vec3d(((out >> 16) & 0xFF) / 255.0, ((out >> 8) & 0xFF) / 255.0, (out & 0xFF) / 255.0);
        }
        return original;
    }

    /** Jasność oryginalnego koloru (0..1) - dzięki temu noc i deszcz nadal przyciemniają niebo. */
    private static float dayFactor(Object original) {
        if (original instanceof Integer i) {
            int r = (i >> 16) & 0xFF, g = (i >> 8) & 0xFF, b = i & 0xFF;
            return Math.max(r, Math.max(g, b)) / 255f;
        }
        if (original instanceof Vec3d v) {
            return (float) Math.max(v.x, Math.max(v.y, v.z));
        }
        return 1f;
    }

    private static int scale(int rgb, float f) {
        f = Math.max(0f, Math.min(1f, f));
        int r = (int) (((rgb >> 16) & 0xFF) * f);
        int g = (int) (((rgb >> 8) & 0xFF) * f);
        int b = (int) ((rgb & 0xFF) * f);
        return (r << 16) | (g << 8) | b;
    }
}
