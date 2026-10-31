package pl.xvisuals.mixin;

import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.xvisuals.module.Modules;
import pl.xvisuals.module.impl.WorldModule;

/** Własny czas dnia (tylko po stronie klienta). */
@Mixin(ClientWorld.Properties.class)
public abstract class ClientWorldPropertiesMixin {

    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true, require = 0)
    private void xvisuals$timeOfDay(CallbackInfoReturnable<Long> cir) {
        WorldModule world = Modules.WORLD;
        if (world.isEnabled() && world.changeTime.get()) {
            cir.setReturnValue(world.timeValue());
        }
    }
}
