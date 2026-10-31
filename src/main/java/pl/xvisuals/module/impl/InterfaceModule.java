package pl.xvisuals.module.impl;

import pl.xvisuals.module.Category;
import pl.xvisuals.module.Module;
import pl.xvisuals.setting.BoolSetting;
import pl.xvisuals.setting.ColorSetting;
import pl.xvisuals.setting.FloatSetting;

/** Ustawienia samego menu (zawsze aktywne). */
public class InterfaceModule extends Module {
    public final ColorSetting accent = new ColorSetting("Accent color", 0.72f, 0.6f, 1f);
    public final FloatSetting animSpeed = new FloatSetting("Animation speed", 0.5f, 2f, 1f, 0.1f, "x");
    public final BoolSetting dim = new BoolSetting("Dim background", true);

    public InterfaceModule() {
        super("Interface", "Wygląd menu X Visuals", Category.SETTINGS, false);
        add(accent);
        add(animSpeed);
        add(dim);
    }

    public int accent() {
        return accent.rgb();
    }

    public float speed() {
        return animSpeed.get();
    }
}
