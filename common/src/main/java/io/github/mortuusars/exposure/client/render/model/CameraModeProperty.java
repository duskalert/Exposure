package io.github.mortuusars.exposure.client.render.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.mortuusars.exposure.world.item.camcom.CameraItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CameraModeProperty implements SelectItemModelProperty<String> {
    public static final MapCodec<CameraModeProperty> CODEC = MapCodec.unit(new CameraModeProperty());
    public static final SelectItemModelProperty.Type<CameraModeProperty, String> TYPE =
            SelectItemModelProperty.Type.create(CODEC, Codec.STRING);

    @Override
    public String get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity,
                      int seed, ItemDisplayContext displayContext) {
        if (!(stack.getItem() instanceof CameraItem camera)) return "default";

        boolean isGold = camera.isGold(stack);
        boolean isActive = camera.isActive(stack);
        boolean isSelfie = camera.isInSelfieMode(stack);
        boolean isGui = displayContext == ItemDisplayContext.GUI;

        String prefix = isGold ? "gold_" : "";
        if (isGui) return prefix + "gui";
        if (isSelfie) return prefix + "selfie";
        if (isActive) return prefix + "active";
        return isGold ? "gold_default" : "default";
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }

    @Override
    public Type<? extends SelectItemModelProperty<String>, String> type() {
        return TYPE;
    }
}
