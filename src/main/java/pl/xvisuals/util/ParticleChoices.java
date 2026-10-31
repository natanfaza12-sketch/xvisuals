package pl.xvisuals.util;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

import java.util.List;
import java.util.function.BiFunction;

/** Lista cząsteczek do wyboru w GUI. Fabryki są leniwe, więc rejestry MC nie są ruszane przy starcie. */
public final class ParticleChoices {
    public record Choice(String name, boolean colorable, BiFunction<Integer, Float, ParticleEffect> factory) {
    }

    public static final List<Choice> ALL = List.of(
            new Choice("End Rod", false, (c, s) -> ParticleTypes.END_ROD),
            new Choice("Dust", true, (c, s) -> new DustParticleEffect(c, s)),
            new Choice("Flame", false, (c, s) -> ParticleTypes.FLAME),
            new Choice("Soul Flame", false, (c, s) -> ParticleTypes.SOUL_FIRE_FLAME),
            new Choice("Heart", false, (c, s) -> ParticleTypes.HEART),
            new Choice("Happy", false, (c, s) -> ParticleTypes.HAPPY_VILLAGER),
            new Choice("Glow", false, (c, s) -> ParticleTypes.GLOW),
            new Choice("Snow", false, (c, s) -> ParticleTypes.SNOWFLAKE),
            new Choice("Spark", false, (c, s) -> ParticleTypes.ELECTRIC_SPARK),
            new Choice("Magic", false, (c, s) -> ParticleTypes.WITCH),
            new Choice("Crit", false, (c, s) -> ParticleTypes.CRIT),
            new Choice("Enchanted Hit", false, (c, s) -> ParticleTypes.ENCHANTED_HIT)
    );

    public static final String[] NAMES = ALL.stream().map(Choice::name).toArray(String[]::new);

    private ParticleChoices() {
    }

    public static Choice get(int index) {
        return ALL.get(Math.max(0, Math.min(ALL.size() - 1, index)));
    }
}
