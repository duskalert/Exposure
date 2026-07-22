package io.github.mortuusars.exposure.mixin.client;

import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.entity.CameraOperator;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements CameraOperator {
    @Unique
    private Camera activeExposureCamera;
    @Unique
    private float oExposureCameraActionAnim;
    @Unique
    private float exposureCameraActionAnim;

    @Override
    public Camera getActiveExposureCamera() {
        if (activeExposureCamera != null && !activeExposureCamera.isActive()) return null;
        return activeExposureCamera;
    }

    @Override
    public void setActiveExposureCamera(Camera camera) {
        activeExposureCamera = camera;
        CameraClient.setupViewfinder(camera);
    }

    @Override
    public void removeActiveExposureCamera() {
        activeExposureCamera = null;
        CameraClient.removeViewfinder();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        CameraClient.tick();
        if (activeExposureCamera != null && !activeExposureCamera.isActive()) {
            removeActiveExposureCamera();
        }
    }
}
