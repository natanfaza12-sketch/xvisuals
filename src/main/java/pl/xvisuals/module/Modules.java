package pl.xvisuals.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import pl.xvisuals.module.impl.*;

import java.util.List;

public final class Modules {
    public static final SkyModule SKY = new SkyModule();
    public static final AuraModule AURA = new AuraModule();
    public static final TrailModule TRAIL = new TrailModule();
    public static final HitSoundModule HIT_SOUND = new HitSoundModule();
    public static final HitFxModule HIT_FX = new HitFxModule();
    public static final WorldModule WORLD = new WorldModule();
    public static final InterfaceModule INTERFACE = new InterfaceModule();

    public static final List<Module> ALL = List.of(SKY, AURA, TRAIL, HIT_SOUND, HIT_FX, WORLD, INTERFACE);

    private Modules() {
    }

    public static List<Module> of(Category category) {
        return ALL.stream().filter(m -> m.category == category).toList();
    }

    public static void tick(MinecraftClient mc) {
        for (Module m : ALL) {
            if (!m.isEnabled()) continue;
            try {
                m.onTick(mc);
            } catch (Exception e) {
                System.err.println("[X Visuals] Błąd w module " + m.name + ": " + e);
            }
        }
    }

    public static void attack(MinecraftClient mc, Entity target) {
        for (Module m : ALL) {
            if (!m.isEnabled()) continue;
            try {
                m.onAttack(mc, target);
            } catch (Exception e) {
                System.err.println("[X Visuals] Błąd w module " + m.name + ": " + e);
            }
        }
    }
}
