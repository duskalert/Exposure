package io.github.mortuusars.exposure.mixin;

import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.entity.CameraHolder;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements CameraOperator, CameraHolder {
    @Unique
    private Camera activeExposureCamera;

    @Override
    public Camera getActiveExposureCamera() {
        if (activeExposureCamera != null && !activeExposureCamera.isActive()) return null;
        return activeExposureCamera;
    }

    @Override
    public void setActiveExposureCamera(Camera camera) {
        activeExposureCamera = camera;
    }

    @Override
    public void removeActiveExposureCamera() {
        activeExposureCamera = null;
    }
}
