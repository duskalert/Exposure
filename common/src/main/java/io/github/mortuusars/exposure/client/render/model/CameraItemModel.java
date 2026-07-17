package io.github.mortuusars.exposure.client.render.model;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.world.item.camera.Attachment;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CameraItemModel implements ItemModel {
    private static boolean resolving = false;

    private final ItemModel baseModel;

    public CameraItemModel(ItemModel baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, ClientLevel level, ItemOwner owner, int seed) {
        // Render base camera model
        baseModel.update(state, stack, resolver, displayContext, level, owner, seed);

        if (resolving) return;
        if (!(stack.getItem() instanceof CameraItem camera)) return;
        if (displayContext == ItemDisplayContext.GUI) return;

        resolving = true;
        try {
            LivingEntity entity = owner instanceof LivingEntity le ? le : null;
            boolean isActive = camera.isActive(stack);
            boolean isSelfie = camera.isInSelfieMode(stack);

            if (isSelfie) renderPart(state, resolver, Exposure.Items.CAMERA.get().getDefaultInstance(), displayContext, entity);
            if (isActive) renderPart(state, resolver, Exposure.Items.CAMERA.get().getDefaultInstance(), displayContext, entity);
            if (Attachment.FLASH.isPresent(stack)) renderPart(state, resolver, Exposure.Items.CAMERA.get().getDefaultInstance(), displayContext, entity);
            if (Attachment.LENS.isPresent(stack)) renderPart(state, resolver, Exposure.Items.CAMERA.get().getDefaultInstance(), displayContext, entity);
        } finally {
            resolving = false;
        }
    }

    private void renderPart(ItemStackRenderState state, ItemModelResolver resolver, ItemStack partStack,
                            ItemDisplayContext displayContext, LivingEntity entity) {
        var layer = state.newLayer();
        resolver.updateForLiving(layer, partStack, displayContext, entity);
    }
}
