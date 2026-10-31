package pl.xvisuals.mixin;

import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.xvisuals.module.Modules;
import pl.xvisuals.module.impl.SkyModule;

/**
 * Kolor nieba i chmur. Handlery celowo mają tylko CallbackInfoReturnable<Object>,
 * żeby działały niezależnie od tego, czy metoda zwraca int, czy Vec3d.
 */
@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void xvisuals$skyColor(CallbackInfoReturnable<Object> cir) {
        SkyModule sky = Modules.SKY;
        if (sky.isEnabled()) {
            cir.setReturnValue(sky.applySky(cir.getReturnValue()));
        }
    }

    @Inject(method = "getCloudsColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void xvisuals$cloudsColor(CallbackInfoReturnable<Object> cir) {
        SkyModule sky = Modules.SKY;
        if (sky.isEnabled()) {
            cir.setReturnValue(sky.applyClouds(cir.getReturnValue()));
        }
    }
}
