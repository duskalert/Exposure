package io.github.mortuusars.exposure.forge.mixin;

import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IForgeKeyMapping.class)
public interface KeyMappingExtensionNeoForgeMixin {
    /*@Inject(method = "isConflictContextAndModifierActive", at = @At("HEAD"), cancellable = true)
    private void modify(CallbackInfoReturnable<Boolean> cir) {
        if (CameraClient.viewfinder() != null
                && CameraClient.viewfinder().controlsScreen().map(screen -> screen == Minecrft.get().screen).orElse(false)) {
            cir.setReturnValue(true);
        }
    }*/
}