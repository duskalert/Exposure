package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.client.camera.CameraClient;
import io.github.mortuusars.exposure.client.input.*;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Viewfinder {
    protected final Camera camera;
    protected final ViewfinderZoom zoom;
    protected final ViewfinderOverlay overlay;
    protected final ViewfinderShader shader;
    protected final ViewfinderSelfie selfie;

    protected KeyBindings keyBindings = KeyBindings.of(
            Key.press(Minecrft.options().keyAttack).executes(() -> !canAttack()),
            Key.press(Minecrft.options().keyTogglePerspective).executes(() -> selfie().toggle()),
            Key.press(Minecrft.options().keyInventory).or(Key.press(InputConstants.KEY_ESCAPE)).executes(() -> {
                if (Minecrft.get().screen instanceof ViewfinderCameraControlsScreen viewfinderControlsScreen) {
                    viewfinderControlsScreen.onClose();
                    controlsScreen = null;
                } else {
                    CameraClient.deactivate();
                    close();
                }
            }),
            Key.press(KeyboardHandler.getCameraControlsKey())
                    .onlyIf(this::isLookingThrough)
                    .onlyIf(() -> !controlsActive())
                    .executes(() -> {
                        openControlsScreen();
                        return false; // false not handle and keep moving/sneaking
                    }),
            Key.press(Modifier.CONTROL, InputConstants.KEY_ADD)
                    .or(Key.press(Modifier.CONTROL, InputConstants.KEY_EQUALS))
                    .or(Key.press(Modifier.CONTROL, 333 /*KEY_SUBTRACT*/))
                    .or(Key.press(Modifier.CONTROL, InputConstants.KEY_MINUS))
                    .onlyIf(this::isLookingThrough)
                    .onlyIf(() -> !controlsActive())
                    .onlyIf(() -> camera().inSelfieMode())
                    .executes(() -> selfie().rotateCamera(1, false)),
            Key.press(Modifier.CONTROL, 333 /*KEY_SUBTRACT*/)
                    .or(Key.press(Modifier.CONTROL, InputConstants.KEY_MINUS))
                    .onlyIf(this::isLookingThrough)
                    .onlyIf(() -> !controlsActive())
                    .onlyIf(() -> camera().inSelfieMode())
                    .executes(() -> selfie().rotateCamera(-1, false))
    );

    protected @Nullable ViewfinderCameraControlsScreen controlsScreen;

    public Viewfinder(@NotNull Camera camera) {
        this.camera = camera;
        this.zoom = createZoom(camera);
        this.overlay = createOverlay(camera);
        this.shader = createShader(camera);
        this.selfie = createSelfie(camera);
    }

    protected ViewfinderZoom createZoom(Camera camera) {
        return new ViewfinderZoom(camera, this);
    }

    protected ViewfinderOverlay createOverlay(Camera camera) {
        return new ViewfinderOverlay(camera, this);
    }

    protected ViewfinderShader createShader(Camera camera) {
        return new ViewfinderShader(camera, this);
    }

    protected ViewfinderSelfie createSelfie(Camera camera) {
        return new ViewfinderSelfie(camera, this);
    }

    protected ViewfinderCameraControlsScreen createControlsScreen(Camera camera) {
        return new ViewfinderCameraControlsScreen(camera, this);
    }

    // --

    public Camera camera() {
        return camera;
    }

    public ViewfinderZoom zoom() {
        return zoom;
    }

    public ViewfinderOverlay overlay() {
        return overlay;
    }

    public ViewfinderShader shader() {
        return shader;
    }

    public ViewfinderSelfie selfie() {
        return selfie;
    }

    public Optional<ViewfinderCameraControlsScreen> controlsScreen() {
        return Optional.ofNullable(controlsScreen);
    }

    public void tick() {
        shader().update();
        shader().setActive(isLookingThrough());
        selfie().updateSelfieMode();
    }

    public boolean isLookingThrough() {
        CameraType cameraType = Minecrft.options().getCameraType();
        return cameraType == CameraType.FIRST_PERSON || cameraType == CameraType.THIRD_PERSON_FRONT;
    }

    public boolean controlsActive() {
        return Minecrft.get().screen instanceof ViewfinderCameraControlsScreen;
    }

    public boolean canAttack() {
        return Config.Server.CAMERA_VIEWFINDER_ATTACK.get()
                && !camera.map(CameraItem::isInSelfieMode).orElse(false); // Attacking in selfie mode has weird anim.
    }

    public void openControlsScreen() {
        Preconditions.checkNotNull(camera, "No active camera");
        controlsScreen = createControlsScreen(camera);
        Minecrft.get().setScreen(controlsScreen);
    }

    public void close() {
        if (shader != null) {
            shader.close();
        }

        if (controlsActive()) {
            Minecrft.get().setScreen(null);
        }
    }

    public boolean keyPressed(int key, int scanCode, int action, int modifiers) {
//        if (!canAttack() && Minecrft.options().keyAttack.matches(key, scanCode)) {
//            return true;
//        }

//        if (Minecrft.options().keyTogglePerspective.matches(key, scanCode)) {
        //TODO: TEST
//            if (action == InputConstants.PRESS) {
//                return true;
//            }
//
//            selfie().toggle();
//            return true;
//        }

//        if (key == InputConstants.KEY_ESCAPE || Minecrft.options().keyInventory.matches(key, scanCode)) {
//            if (action == InputConstants.PRESS) {
//                if (Minecrft.get().screen instanceof ViewfinderCameraControlsScreen viewfinderControlsScreen) {
//                    viewfinderControlsScreen.onClose();
//                } else {
//                    CameraClient.deactivate();
//                    close();
//                }
//            }
//            return true;
//        }

//        if (!isLookingThrough() || controlsActive()) {
//            return false;
//        }
//
//        if (KeyboardHandler.getCameraControlsKey().matches(key, scanCode)) {
//            openControlsScreen();
//            return false; // false not handle and keep moving/sneaking
//        }

//        if (Screen.hasControlDown() && camera.inSelfieMode()) {
//            if (key == InputConstants.KEY_ADD || key == InputConstants.KEY_EQUALS) {
//                rotateSelfieCamera(1, false);
//                return true;
//            }
//
//            if (key == 333 /*KEY_SUBTRACT*/ || key == InputConstants.KEY_MINUS) {
//                rotateSelfieCamera(-1, false);
//                return true;
//            }
//        }

//        if (zoom.keyPressed(key, scanCode, action, modifiers)) {
//            return true;
//        }
//
//        return false;

        return (action == InputConstants.PRESS && keyBindings.keyPressed(key, scanCode, modifiers))
                || (action == InputConstants.RELEASE && keyBindings.keyReleased(key, scanCode, modifiers))
                || zoom().keyPressed(key, scanCode, action, modifiers);
    }

    public boolean mouseClicked(int button, int action) {
        if (!isLookingThrough()) {
            return false;
        }

        if (controlsActive()) return false;

        if (!canAttack() && Minecrft.options().keyAttack.matchesMouse(button))
            return true; // Block attacks

        if (KeyboardHandler.getCameraControlsKey().matchesMouse(button)) {
            openControlsScreen();
            return false; // Do not cancel the event to keep sneaking
        }

        if (Config.Client.VIEWFINDER_MIDDLE_CLICK_CONTROLS.get() && button == InputConstants.MOUSE_BUTTON_MIDDLE) {
            openControlsScreen();
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double amount) {
        if (isLookingThrough() && !controlsActive()) {
            if (Screen.hasControlDown() && camera.inSelfieMode()) {
                selfie().rotateCamera(Mth.sign(amount), false);
                return true;
            } else {
                return zoom.mouseScrolled(amount);
            }
        }
        return false;
    }

    public double modifyMouseSensitivity(double original) {
        if (!isLookingThrough())
            return original;

        double scale = original / Minecraft.getInstance().options.fov().get();
        double scaledSensitivity = zoom.getCurrentFov() * scale;

        double normalizedDifference = Mth.map(original - scaledSensitivity, 0, original, 0, 1);
        double influence = Config.Client.VIEWFINDER_ZOOM_SENSITIVITY_INFLUENCE.get();
        double strength = 1f - normalizedDifference * influence;
        strength *= strength; // more influence at smaller FOVs

        return Mth.lerp(strength, scaledSensitivity, original);
    }

    @FunctionalInterface
    public interface ComponentConstructor<T> {
        T construct(Camera camera, Viewfinder viewfinder);
    }
}
