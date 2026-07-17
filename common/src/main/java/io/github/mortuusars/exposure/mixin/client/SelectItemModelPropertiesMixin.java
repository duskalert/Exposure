package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.render.model.CameraModeProperty;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectItemModelProperties.class)
public class SelectItemModelPropertiesMixin {
    @Shadow
    private static ExtraCodecs.LateBoundIdMapper<Identifier, SelectItemModelProperty.Type<?, ?>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("TAIL"))
    private static void exposure$registerProperties(CallbackInfo ci) {
        ID_MAPPER.put(Exposure.resource("camera_mode"), CameraModeProperty.TYPE);
    }
}
