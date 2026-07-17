package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.render.model.CameraItemModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModelResolver.class)
public class ItemModelResolverMixin {
    @ModifyVariable(method = "getItemModel", at = @At("RETURN"), ordinal = 0)
    private ItemModel exposure$wrapCameraModel(ItemModel model, Identifier id) {
        if (id.equals(Exposure.resource("camera")) || id.equals(Exposure.resource("camera_gold"))) {
            return new CameraItemModel(model);
        }
        return model;
    }
}
