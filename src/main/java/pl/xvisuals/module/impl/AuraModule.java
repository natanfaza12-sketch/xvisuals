package pl.xvisuals.module.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import pl.xvisuals.module.Category;
import pl.xvisuals.module.Module;
import pl.xvisuals.setting.BoolSetting;
import pl.xvisuals.setting.ColorSetting;
import pl.xvisuals.setting.FloatSetting;
import pl.xvisuals.setting.ModeSetting;
import pl.xvisuals.util.Fx;
import pl.xvisuals.util.ParticleChoices;

import java.util.concurrent.ThreadLocalRandom;

/** Cząsteczki krążące wokół gracza. */
public class AuraModule extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Ring", "Halo", "Helix", "Cloud");
    public final ModeSetting particle = new ModeSetting("Particle", ParticleChoices.NAMES);
    public final ColorSetting color = new ColorSetting("Color", 0.8f);
    public final BoolSetting rainbow = new BoolSetting("Rainbow", false);
    public final FloatSetting amount = new FloatSetting("Amount", 1f, 16f, 4f, 1f);
    public final FloatSetting radius = new FloatSetting("Radius", 0.3f, 2f, 0.8f, 0.05f);
    public final FloatSetting height = new FloatSetting("Height", 0.2f, 2.2f, 1.0f, 0.05f);
    public final FloatSetting speed = new FloatSetting("Speed", 0.5f, 4f, 1.5f, 0.1f, "x");
    public final FloatSetting size = new FloatSetting("Size", 0.4f, 2f, 1f, 0.1f);
    public final BoolSetting firstPerson = new BoolSetting("Show in first person", true);

    private int age;

    public AuraModule() {
        super("Aura", "Cząsteczki wokół postaci", Category.VISUAL, true);
        particle.select("Dust");
        add(mode);
        add(particle);
        add(color).visibleIf(this::colorable);
        add(rainbow).visibleIf(this::colorable);
        add(amount);
        add(radius);
        add(height).visibleIf(() -> !mode.is("Halo") && !mode.is("Cloud"));
        add(speed);
        add(size).visibleIf(this::colorable);
        add(firstPerson);
    }

    private boolean colorable() {
        return ParticleChoices.get(particle.index()).colorable();
    }

    @Override
    public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;
        if (!firstPerson.get() && mc.options.getPerspective().isFirstPerson()) return;

        age++;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        ParticleChoices.Choice choice = ParticleChoices.get(particle.index());
        int n = amount.getInt();
        double px = p.getX(), py = p.getY(), pz = p.getZ();
        double r = radius.get();
        double sp = speed.get();

        for (int i = 0; i < n; i++) {
            double x, y, z;
            switch (mode.get()) {
                case "Halo" -> {
                    double a = age * 0.15 * sp + i * (Math.PI * 2 / n);
                    double hr = Math.min(r, 0.5);
                    x = px + Math.cos(a) * hr;
                    y = py + p.getHeight() + 0.2;
                    z = pz + Math.sin(a) * hr;
                }
                case "Helix" -> {
                    double t = (age * 0.04 * sp + i / (double) n) % 1.0;
                    double a = age * 0.2 * sp + t * Math.PI * 4 + (i % 2) * Math.PI;
                    x = px + Math.cos(a) * r;
                    y = py + t * (height.get() + 0.8);
                    z = pz + Math.sin(a) * r;
                }
                case "Cloud" -> {
                    x = px + (rnd.nextDouble() - 0.5) * r * 2;
                    y = py + rnd.nextDouble() * p.getHeight();
                    z = pz + (rnd.nextDouble() - 0.5) * r * 2;
                }
                default -> {
                    double a = age * 0.15 * sp + i * (Math.PI * 2 / n);
                    x = px + Math.cos(a) * r;
                    y = py + height.get();
                    z = pz + Math.sin(a) * r;
                }
            }
            int rgb = Fx.color(rainbow.get(), color.rgb(), i / (float) n * 0.3f);
            Fx.spawn(mc, choice, rgb, size.get(), x, y, z, 0, 0, 0);
        }
    }
}
