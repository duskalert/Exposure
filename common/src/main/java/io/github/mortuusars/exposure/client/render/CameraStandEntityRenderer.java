package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class CameraStandEntityRenderer <T extends CameraStandEntity> extends EntityRenderer<T, EntityRenderState> {
    public static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/item/camera.png");
    public static final float MOUNT_SCALE = 0.9f;

    protected final BlockModelResolver blockModelResolver;

    public CameraStandEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public @NotNull Identifier getTextureLocation(T entity) {
        return TEXTURE_LOCATION;
    }

    @Override
    public void extractRenderState(T entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector, 
                       net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, collector, cameraRenderState);
    }
}
