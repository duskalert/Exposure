package io.github.mortuusars.exposure.client.camera.viewfinder;

import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.animation.Animation;
import io.github.mortuusars.exposure.client.animation.EasingFunction;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.camera.CameraSettings;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;

public class ViewfinderSelfie {
    protected final Camera camera;
    protected final Viewfinder viewfinder;

    protected double selfieCameraRotationDegrees = 0;
    protected double prevSelfieCameraRotationDegrees = 0;
    protected Animation selfieCameraRotationAnimation = new Animation(200, EasingFunction.EASE_OUT_EXPO);

    public ViewfinderSelfie(Camera camera, Viewfinder viewfinder) {
        this.camera = camera;
        this.viewfinder = viewfinder;
    }

    public float getMaxCameraDistance() {
        return Config.Server.SELFIE_CAMERA_DISTANCE.get().floatValue();
    }

    public double getCameraRotationDegrees() {
        return Mth.lerp(selfieCameraRotationAnimation.getValue(), prevSelfieCameraRotationDegrees, CameraSettings.SELFIE_ROTATION.getOrDefault(camera));
    }

    public double getMaxCameraRotationDegrees() {
        return 40.0;
    }

    public double getCameraRotationStepDegrees() {
        return 10.0;
    }

    public void rotateCamera(int direction, boolean precise) {
        double change = getCameraRotationStepDegrees() * direction * (precise ? 0.25 : 1);
        setCameraRotation(selfieCameraRotationDegrees + change);
    }

    public void resetCameraRotation() {
        setCameraRotation(0);
    }

    public void setCameraRotation(double rotation) {
        prevSelfieCameraRotationDegrees = getCameraRotationDegrees();
        double max = getMaxCameraRotationDegrees();
        CameraSettings.SELFIE_ROTATION.setAndSync(camera, selfieCameraRotationDegrees = Mth.clamp(rotation, -max, max));
        selfieCameraRotationAnimation.resetProgress();
    }

    public void toggle() {
        CameraType newCameraType = Minecrft.options().getCameraType() == CameraType.FIRST_PERSON
                ? CameraType.THIRD_PERSON_FRONT
                : CameraType.FIRST_PERSON;

        Minecrft.options().setCameraType(newCameraType);
    }

    public void updateSelfieMode() {
        boolean inSelfieMode = Minecrft.options().getCameraType() == CameraType.THIRD_PERSON_FRONT;
        if (camera.inSelfieMode() != inSelfieMode) {
            CameraSettings.SELFIE_MODE.setAndSync(camera, inSelfieMode);
            resetCameraRotation();
        }
    }
}