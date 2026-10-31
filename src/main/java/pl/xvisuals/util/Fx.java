package pl.xvisuals.util;

import net.minecraft.client.MinecraftClient;

public final class Fx {
    private Fx() {
    }

    /** Kolor tęczowy (zmienia się w czasie, offset przesuwa odcień) albo stały. */
    public static int color(boolean rainbow, int base, float offset) {
        if (!rainbow) return base;
        return ColorUtil.hsv(((System.currentTimeMillis() % 3000L) / 3000f) + offset, 0.85f, 1f);
    }

    public static void spawn(MinecraftClient mc, ParticleChoices.Choice choice, int rgb, float size,
                             double x, double y, double z, double vx, double vy, double vz) {
        if (mc.world == null) return;
        mc.world.addParticle(choice.factory().apply(rgb, size), x, y, z, vx, vy, vz);
    }
}
