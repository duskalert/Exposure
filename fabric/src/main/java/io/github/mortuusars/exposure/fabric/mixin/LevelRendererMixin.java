package io.github.mortuusars.exposure.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.entity.CameraStandEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.*;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow private int renderedEntities;
    @Final @Shadow private RenderBuffers renderBuffers;
    @Shadow protected abstract boolean shouldShowEntityOutlines();
    @Shadow private void renderEntity(Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {}

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
    private void onRenderLevel(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (camera.getEntity() instanceof CameraStandEntity) {
            exposure$renderEntity(poseStack, partialTick, camera, Minecrft.player());
        }
    }

    @Unique
    private void exposure$renderEntity(PoseStack poseStack, float deltaTracker, Camera camera, Entity entity) {
        this.renderedEntities++;
        if (entity.tickCount == 0) {
            entity.xOld = entity.getX();
            entity.yOld = entity.getY();
            entity.zOld = entity.getZ();
        }

        this.renderedEntities++;
        if (entity.tickCount == 0) {
            entity.xOld = entity.getX();
            entity.yOld = entity.getY();
            entity.zOld = entity.getZ();
        }

        MultiBufferSource multiBufferSource;
        if (this.shouldShowEntityOutlines() && Minecrft.get().shouldEntityAppearGlowing(entity)) {
            OutlineBufferSource outlineBufferSource = this.renderBuffers.outlineBufferSource();
            multiBufferSource = outlineBufferSource;
            int teamColor = entity.getTeamColor();
            outlineBufferSource.setColor(FastColor.ARGB32.red(teamColor), FastColor.ARGB32.green(teamColor), FastColor.ARGB32.blue(teamColor), 255);
        } else {
            multiBufferSource = this.renderBuffers.bufferSource();
        }

        Vec3 cameraPos = camera.getPosition();
        this.renderEntity(entity, cameraPos.x, cameraPos.y, cameraPos.z, deltaTracker, poseStack, multiBufferSource);
    }
}
