package io.github.mortuusars.exposure.client.camera.viewfinder;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Config;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.FocalLengthButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.FrameCounterButton;
import io.github.mortuusars.exposure.client.gui.screen.camera.button.ShutterSpeedButton;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.input.MouseHandler;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.client.util.ZoomDirection;
import io.github.mortuusars.exposure.world.camera.Camera;
import io.github.mortuusars.exposure.world.camera.component.*;
import io.github.mortuusars.exposure.world.item.camcom.CameraSettings;
import io.github.mortuusars.exposure.network.Packets;
import io.github.mortuusars.exposure.network.packet.serverbound.ActiveCameraReleaseC2SP;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ViewfinderCameraControlsScreen extends Screen {
    // TODO: MC 26.1 - All method signatures changed. Bodies stubbed.

    public static final WidgetSprites SHUTTER_SPEED_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/shutter_speed_dial"),
            Exposure.resource("camera_controls/shutter_speed_dial_disabled"),
            Exposure.resource("camera_controls/shutter_speed_dial_highlighted"));

    public static final WidgetSprites FOCAL_LENGTH_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/focal_length"),
            Exposure.resource("camera_controls/focal_length_disabled"),
            Exposure.resource("camera_controls/focal_length_highlighted"));

    public static final WidgetSprites FRAME_COUNTER_SPRITES = new WidgetSprites(
            Exposure.resource("camera_controls/frame_counter"),
            Exposure.resource("camera_controls/frame_counter_disabled"),
            Exposure.resource("camera_controls/frame_counter_highlighted"));

    public static final Identifier SEPARATOR_SPRITE = Exposure.resource("camera_controls/button_separator");

    protected static final int SEPARATOR_WIDTH = 1;
    protected static final int BUTTON_HEIGHT = 18;
    protected static final int SIDE_BUTTONS_WIDTH = 49;
    protected static final int BUTTON_WIDTH = 15;

    protected final Camera camera;
    protected final Viewfinder viewfinder;
    protected final long openedAt;

    protected int leftPos;
    protected int topPos;

    public ViewfinderCameraControlsScreen(Camera camera, Viewfinder viewfinder) {
        super(CommonComponents.EMPTY);
        this.camera = camera;
        this.viewfinder = viewfinder;
        this.openedAt = Minecrft.level().getGameTime();
    }

    public Camera getCamera() {
        return camera;
    }

    public Viewfinder getViewfinder() {
        return viewfinder;
    }

    // TODO: MC 26.1 - isPauseScreen signature
    public boolean isPauseScreen() {
        return false;
    }

    // TODO: MC 26.1 - tick signature
    public void tick() {
        // Stubbed
    }

    // TODO: MC 26.1 - init signature
    protected void init() {
        // Stubbed
    }

    // TODO: MC 26.1 - createShutterSpeedButton
    protected @NotNull Button createShutterSpeedButton() {
        return null;
    }

    // TODO: MC 26.1 - createCompositionGuideButton
    protected @NotNull Button createCompositionGuideButton() {
        return null;
    }

    // TODO: MC 26.1 - createSelfTimerButton
    protected @NotNull Button createSelfTimerButton() {
        return null;
    }

    // TODO: MC 26.1 - createFlashModeButton
    protected @NotNull Button createFlashModeButton() {
        return null;
    }

    // TODO: MC 26.1 - addSeparator
    protected void addSeparator(int x, int y) {
        // Stubbed
    }

    // TODO: MC 26.1 - refreshMovementKeys
    protected void refreshMovementKeys() {
        // Stubbed
    }

    // TODO: MC 26.1 - render signature changed (GuiGraphicsExtractor -> GuiGraphicsExtractor src)
    public void render(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderBackground signature changed
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent instead of int
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    // TODO: MC 26.1 - mouseReleased now takes MouseButtonEvent
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    // TODO: MC 26.1 - mouseDragged now takes MouseButtonEvent
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    // TODO: MC 26.1 - keyReleased now takes KeyEvent
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - isToggleTimeReached
    protected boolean isToggleTimeReached() {
        return false;
    }

    // TODO: MC 26.1 - keyPressed now takes KeyEvent
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - mouseScrolled signature
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }
}
