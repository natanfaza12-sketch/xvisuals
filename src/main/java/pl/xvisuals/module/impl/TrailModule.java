package pl.xvisuals.module.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import pl.xvisuals.module.Category;
import pl.xvisuals.module.Module;
import pl.xvisuals.setting.BoolSetting;
import pl.xvisuals.setting.ColorSetting;
import pl.xvisuals.setting.FloatSetting;
import pl.xvisuals.setting.ModeSetting;
import pl.xvisuals.util.Fx;
import pl.xvisuals.util.ParticleChoices;

import java.util.concurrent.ThreadLocalRandom;

/** Smuga cząsteczek za graczem podczas ruchu. */
public class TrailModule extends Module {
    public final ModeSetting particle = new ModeSetting("Particle", ParticleChoices.NAMES);
    public final ModeSetting position = new ModeSetting("Position", "Feet", "Waist", "Head");
    public final ColorSetting color = new ColorSetting("Color", 0.5f);
    public final BoolSetting rainbow = new BoolSetting("Rainbow", true);
    public final FloatSetting density = new FloatSetting("Density", 1f, 8f, 3f, 1f);
    public final FloatSetting spread = new FloatSetting("Spread", 0f, 0.6f, 0.12f, 0.02f);
    public final FloatSetting size = new FloatSetting("Size", 0.4f, 2f, 1f, 0.1f);

    private Vec3d last;
    private ClientWorld lastWorld;

    public TrailModule() {
        super("Trail", "Smuga za postacią podczas ruchu", Category.VISUAL, true);
        particle.select("Dust");
        add(particle);
        add(position);
        add(color).visibleIf(this::colorable);
        add(rainbow).visibleIf(this::colorable);
        add(density);
        add(spread);
        add(size).visibleIf(this::colorable);
    }

    private boolean colorable() {
        return ParticleChoices.get(particle.index()).colorable();
    }

    @Override
    public void onTick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null) return;

        Vec3d cur = p.getPos();
        if (mc.world != lastWorld || last == null) {
            lastWorld = mc.world;
            last = cur;
            return;
        }
        double dist2 = last.squaredDistanceTo(cur);
        if (dist2 > 64.0) {          // teleport - nie rysujemy linii przez pół mapy
            last = cur;
            return;
        }
        if (dist2 > 0.0004) {
            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            ParticleChoices.Choice choice = ParticleChoices.get(particle.index());
            int n = density.getInt();
            double yOff = switch (position.get()) {
                case "Waist" -> p.getHeight() * 0.5;
                case "Head" -> p.getHeight() * 0.9;
                default -> 0.1;
            };
            double sp = spread.get();
            for (int k = 0; k < n; k++) {
                Vec3d pos = last.lerp(cur, (k + rnd.nextDouble()) / n);
                int rgb = Fx.color(rainbow.get(), color.rgb(), k * 0.03f);
                Fx.spawn(mc, choice, rgb, size.get(),
                        pos.x + (rnd.nextDouble() - 0.5) * sp * 2,
                        pos.y + yOff + (rnd.nextDouble() - 0.5) * sp,
                        pos.z + (rnd.nextDouble() - 0.5) * sp * 2,
                        0, 0, 0);
            }
        }
        last = cur;
    }
}
