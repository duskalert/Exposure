package io.github.mortuusars.exposure.mixin.client;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @ModifyVariable(method = "getItemModel", at = @At("RETURN"), ordinal = 0)
    private ItemModel exposure$wrapCameraModel(ItemModel model) {
        // TODO: MC 26.1 - CameraItemModel needs port
        return model;
    }
}
