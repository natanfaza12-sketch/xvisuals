package pl.xvisuals.module.impl;

import pl.xvisuals.module.Category;
import pl.xvisuals.module.Module;
import pl.xvisuals.setting.BoolSetting;
import pl.xvisuals.setting.FloatSetting;

/** Czas dnia i pogoda tylko po stronie klienta (serwer o tym nie wie). */
public class WorldModule extends Module {
    public final BoolSetting changeTime = new BoolSetting("Change time", true);
    public final BoolSetting animate = new BoolSetting("Animate time", false);
    public final FloatSetting time = new FloatSetting("Time", 0f, 24000f, 6000f, 100f);
    public final FloatSetting timeSpeed = new FloatSetting("Time speed", 0.2f, 10f, 1f, 0.1f, "x");
    public final BoolSetting clearWeather = new BoolSetting("Clear weather", false);

    public WorldModule() {
        super("World", "Czas dnia i pogoda (tylko wizualnie)", Category.VISUAL, true);
        add(changeTime);
        add(animate).visibleIf(changeTime::get);
        add(time).visibleIf(() -> changeTime.get() && !animate.get());
        add(timeSpeed).visibleIf(() -> changeTime.get() && animate.get());
        add(clearWeather);
    }

    public long timeValue() {
        if (animate.get()) {
            return (long) ((System.currentTimeMillis() / 50.0 * timeSpeed.get()) % 24000.0);
        }
        return time.getInt();
    }
}
