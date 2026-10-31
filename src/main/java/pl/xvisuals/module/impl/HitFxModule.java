package pl.xvisuals.module.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import pl.xvisuals.module.Category;
import pl.xvisuals.module.Module;
import pl.xvisuals.setting.BoolSetting;
import pl.xvisuals.setting.ColorSetting;
import pl.xvisuals.setting.FloatSetting;
import pl.xvisuals.setting.ModeSetting;
import pl.xvisuals.util.Fx;
import pl.xvisuals.util.ParticleChoices;

import java.util.concurrent.ThreadLocalRandom;

/** Wybuch cząsteczek w trafionym celu. */
public class HitFxModule extends Module {
    public final ModeSetting particle = new ModeSetting("Particle", ParticleChoices.NAMES);
    public final ColorSetting color = new ColorSetting("Color", 0.0f);
    public final BoolSetting rainbow = new BoolSetting("Rainbow", false);
    public final FloatSetting amount = new FloatSetting("Amount", 1f, 40f, 12f, 1f);
    public final FloatSetting spread = new FloatSetting("Spread", 0.1f, 1f, 0.4f, 0.05f);
    public final FloatSetting speed = new FloatSetting("Speed", 0f, 0.6f, 0.15f, 0.01f);
    public final FloatSetting size = new FloatSetting("Size", 0.4f, 2f, 1f, 0.1f);

    public HitFxModule() {
        super("Hit FX", "Efekt cząsteczkowy po trafieniu", Category.VISUAL, true);
        particle.select("Crit");
        add(particle);
        add(color).visibleIf(this::colorable);
        add(rainbow).visibleIf(this::colorable);
        add(amount);
        add(spread);
        add(speed);
        add(size).visibleIf(this::colorable);
    }

    private boolean colorable() {
        return ParticleChoices.get(particle.index()).colorable();
    }

    @Override
    public void onAttack(MinecraftClient mc, Entity target) {
        if (!(target instanceof LivingEntity) || mc.world == null) return;
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        ParticleChoices.Choice choice = ParticleChoices.get(particle.index());
        double sp = spread.get();
        double v = speed.get();
        int n = amount.getInt();
        for (int i = 0; i < n; i++) {
            int rgb = Fx.color(rainbow.get(), color.rgb(), i / (float) n * 0.4f);
            Fx.spawn(mc, choice, rgb, size.get(),
                    target.getX() + (rnd.nextDouble() - 0.5) * sp,
                    target.getBodyY(0.5) + (rnd.nextDouble() - 0.5) * sp,
                    target.getZ() + (rnd.nextDouble() - 0.5) * sp,
                    (rnd.nextDouble() - 0.5) * 2 * v,
                    (rnd.nextDouble() - 0.3) * 2 * v,
                    (rnd.nextDouble() - 0.5) * 2 * v);
        }
    }
}
