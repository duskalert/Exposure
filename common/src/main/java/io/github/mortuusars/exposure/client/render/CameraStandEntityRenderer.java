package io.github.mortuusars.exposure.client.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.PlatformHelperClient;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.NotNull;

public class CameraStandEntityRenderer <T extends CameraStandEntity> extends EntityRenderer<T> {
    public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/item/camera.png");

    protected final BlockRenderDispatcher blockRenderer;

    public CameraStandEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(T entity) {
        return TEXTURE_LOCATION;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.pushPose();
        poseStack.translate(-0.5f, 0f, -0.5f);
        ModelResourceLocation modelLocation = ExposureClient.Models.CAMERA_STAND;
        BakedModel model = PlatformHelperClient.getModel(modelLocation);
        blockRenderer.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(RenderType.solid()),
                null, model, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();

        if (!entity.getCamera().isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0, 1.25, 0);
            float scale = 0.9f;
            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() + 180)); // Yaw first
            poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot()));
            poseStack.translate(0, 0.1875, 0);
            Minecrft.get().getItemRenderer().renderStatic(entity.getCamera(), ItemDisplayContext.NONE, packedLight, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.level(), 0);
            poseStack.popPose();
        }

    }
}
