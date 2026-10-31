package pl.xvisuals.module.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import pl.xvisuals.module.Category;
import pl.xvisuals.module.Module;
import pl.xvisuals.setting.BoolSetting;
import pl.xvisuals.setting.ButtonSetting;
import pl.xvisuals.setting.FloatSetting;
import pl.xvisuals.setting.ModeSetting;

import java.util.concurrent.ThreadLocalRandom;

/** Własny dźwięk przy trafieniu. "Custom" = plik assets/xvisuals/sounds/custom_hit.ogg (można nadpisać resource packiem). */
public class HitSoundModule extends Module {
    public final ModeSetting sound = new ModeSetting("Sound",
            "Pop", "Ding", "Snap", "Blip", "Thud", "Custom", "Orb", "Arrow", "Pling");
    public final FloatSetting volume = new FloatSetting("Volume", 0f, 1f, 0.8f, 0.05f);
    public final FloatSetting pitch = new FloatSetting("Pitch", 0.5f, 2f, 1f, 0.05f);
    public final BoolSetting randomPitch = new BoolSetting("Random pitch", false);
    public final ButtonSetting preview = new ButtonSetting("Preview sound", () -> play(MinecraftClient.getInstance()));

    public HitSoundModule() {
        super("Hit Sound", "Własny dźwięk uderzenia", Category.VISUAL, true);
        add(sound);
        add(volume);
        add(pitch);
        add(randomPitch);
        add(preview);
    }

    private static SoundEvent own(String name) {
        return SoundEvent.of(Identifier.of("xvisuals", name));
    }

    private SoundEvent pick() {
        return switch (sound.get()) {
            case "Ding" -> own("hit_ding");
            case "Snap" -> own("hit_snap");
            case "Blip" -> own("hit_blip");
            case "Thud" -> own("hit_thud");
            case "Custom" -> own("custom_hit");
            case "Orb" -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            case "Arrow" -> SoundEvents.ENTITY_ARROW_HIT_PLAYER;
            case "Pling" -> SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
            default -> own("hit_pop");
        };
    }

    public void play(MinecraftClient mc) {
        if (mc == null) return;
        float p = pitch.get();
        if (randomPitch.get()) p += (ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.2f;
        mc.getSoundManager().play(PositionedSoundInstance.master(pick(), p, volume.get()));
    }

    @Override
    public void onAttack(MinecraftClient mc, Entity target) {
        if (target instanceof LivingEntity) play(mc);
    }
}
