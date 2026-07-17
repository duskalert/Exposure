package io.github.mortuusars.exposure.client.render.model;

import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CameraItemModel implements ItemModel {
    private final ItemModel baseModel;

    public CameraItemModel(ItemModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, ClientLevel level, ItemOwner owner, int seed) {
        baseModel.update(state, stack, resolver, displayContext, level, owner, seed);

        if (!(stack.getItem() instanceof CameraItem camera)) return;
        if (displayContext == ItemDisplayContext.GUI) return;

        LivingEntity livingEntity = owner instanceof LivingEntity le ? le : null;
        boolean isActive = camera.isActive(stack);
        boolean isSelfie = camera.isInSelfieMode(stack);

        if (isSelfie) {
            addModelLayer(state, resolver, ExposureClient.Models.CAMERA_SELFIE_STICK, stack, displayContext, livingEntity, seed);
        }

        if (isActive) {
            addModelLayer(state, resolver, ExposureClient.Models.CAMERA_VIEWFINDER, stack, displayContext, livingEntity, seed);
        }

        if (Attachment.FLASH.isPresent(stack)) {
            addModelLayer(state, resolver, ExposureClient.Models.CAMERA_FLASH, stack, displayContext, livingEntity, seed);
        }

        if (Attachment.LENS.isPresent(stack)) {
            addModelLayer(state, resolver, ExposureClient.Models.CAMERA_LENS, stack, displayContext, livingEntity, seed);
        }
    }

    private void addModelLayer(ItemStackRenderState state, ItemModelResolver resolver, Identifier modelId,
                               ItemStack stack, ItemDisplayContext displayContext, LivingEntity entity, int seed) {
        var layer = state.newLayer();
        resolver.updateForLiving(layer, stack, displayContext, entity);
    }
}
