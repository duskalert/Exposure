package io.github.mortuusars.exposure.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.render.model.CameraModel;
import io.github.mortuusars.exposure.client.util.Minecrft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ModelHooks {

    public static BakedModel renderItem(BakedModel model, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand,
                          PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (Minecraft.getInstance().level == null) return model;
        if (!stack.is(Exposure.Items.CAMERA.get())) return model;

        if (displayContext == ItemDisplayContext.GUI) {
            BakedModel guiModel = PlatformHelperClient.getModel(ExposureClient.Models.CAMERA_GUI);
            BakedModel resolve = guiModel.getOverrides().resolve(guiModel, stack, Minecrft.level(), Minecrft.player(), 0);
            return resolve;
        }

        return model;
    }

    public static BakedModel getModel(BakedModel original, ItemStack stack, @Nullable Level level,  @Nullable LivingEntity entity, int seed) {
        if (stack.is(Exposure.Items.CAMERA.get())) {
            @Nullable ClientLevel clientLevel = level instanceof ClientLevel lv ? lv : null;
            return CameraModel.modifyCameraModel(stack, clientLevel, entity, seed, original);
        }

        return original;
    }
}
