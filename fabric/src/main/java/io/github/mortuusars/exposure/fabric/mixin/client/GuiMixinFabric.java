package io.github.mortuusars.exposure.fabric.mixin.client;

import io.github.mortuusars.exposure.client.ModelHooks;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixinFabric {
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private void renderGui(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        if (ModelHooks.renderGui(guiGraphics,partialTick)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At(value = "HEAD"), cancellable = true)
    private void renderCrosshair(CallbackInfo ci) {
        if (ModelHooks.renderCrosshair())ci.cancel();
    }
}
