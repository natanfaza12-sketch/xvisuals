package pl.xvisuals.mixin;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.xvisuals.module.Modules;
import pl.xvisuals.module.impl.WorldModule;

/** "Clear weather": wyłącza wizualny deszcz i burzę po stronie klienta. */
@Mixin(World.class)
public abstract class WorldMixin {

    @Inject(method = "getRainGradient", at = @At("RETURN"), cancellable = true, require = 0)
    private void xvisuals$rain(CallbackInfoReturnable<Float> cir) {
        WorldModule world = Modules.WORLD;
        if (((Object) this) instanceof ClientWorld && world.isEnabled() && world.clearWeather.get()) {
            cir.setReturnValue(0f);
        }
    }

    @Inject(method = "getThunderGradient", at = @At("RETURN"), cancellable = true, require = 0)
    private void xvisuals$thunder(CallbackInfoReturnable<Float> cir) {
        WorldModule world = Modules.WORLD;
        if (((Object) this) instanceof ClientWorld && world.isEnabled() && world.clearWeather.get()) {
            cir.setReturnValue(0f);
        }
    }
}
