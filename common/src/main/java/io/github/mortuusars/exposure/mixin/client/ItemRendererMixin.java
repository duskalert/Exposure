package io.github.mortuusars.exposure.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.ModelHooks;
import io.github.mortuusars.exposure.client.render.model.CameraModel;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    BakedModel renderItem(BakedModel model, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand,
                          PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        return ModelHooks.renderItem(model, stack, displayContext, leftHand, poseStack, buffer, combinedLight, combinedOverlay);
    }

    @ModifyReturnValue(method = "getModel", at = @At(value = "RETURN"))
    private BakedModel getModel(BakedModel original, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true) @Nullable Level level, @Local(argsOnly = true) @Nullable LivingEntity entity, @Local(argsOnly = true) int seed) {
        return ModelHooks.getModel(original, stack, level, entity, seed);
    }
}
