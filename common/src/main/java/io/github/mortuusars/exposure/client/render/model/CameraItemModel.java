package io.github.mortuusars.exposure.client.render.model;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.camcom.Attachment;
import io.github.mortuusars.exposure.world.item.camcom.CameraItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
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

        boolean isActive = camera.isActive(stack);
        boolean isSelfie = camera.isInSelfieMode(stack);

        LivingEntity entity = owner instanceof LivingEntity le ? le : null;

        if (isSelfie) {
            var layer = state.newLayer();
            resolver.updateForLiving(layer, new ItemStack(Exposure.Items.CAMERA_SELFIE_STICK.get()), displayContext, entity);
        }

        if (isActive) {
            var layer = state.newLayer();
            resolver.updateForLiving(layer, new ItemStack(Exposure.Items.CAMERA_VIEWFINDER.get()), displayContext, entity);
        }

        if (Attachment.FLASH.isPresent(stack)) {
            var layer = state.newLayer();
            resolver.updateForLiving(layer, new ItemStack(Exposure.Items.CAMERA_FLASH.get()), displayContext, entity);
        }

        if (Attachment.LENS.isPresent(stack)) {
            var layer = state.newLayer();
            resolver.updateForLiving(layer, new ItemStack(Exposure.Items.CAMERA_LENS.get()), displayContext, entity);
        }
    }
}
