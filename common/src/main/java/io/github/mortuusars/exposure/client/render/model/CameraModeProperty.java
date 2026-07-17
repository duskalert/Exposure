package io.github.mortuusars.exposure.client.render.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CameraModeProperty implements SelectItemModelProperty<String> {
    public static final MapCodec<CameraModeProperty> CODEC = MapCodec.unit(CameraModeProperty::new);

    @Override
    public String get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        if (!(stack.getItem() instanceof CameraItem camera)) return "default";

        boolean isGold = camera.isGold(stack);
        boolean isActive = camera.isActive(stack);
        boolean isSelfie = camera.isInSelfieMode(stack);
        boolean isGui = displayContext == ItemDisplayContext.GUI;

        String prefix = isGold ? "gold_" : "";
        if (isGui) return prefix + "gui";
        if (isSelfie) return prefix + "selfie";
        if (isActive) return prefix + "active";
        return "default";
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public Type<? extends SelectItemModelProperty<String>, String> type() {
        return CameraModelProperties.CAMERA_MODE;
    }
}
