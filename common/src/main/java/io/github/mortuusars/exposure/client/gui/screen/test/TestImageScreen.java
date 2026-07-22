package io.github.mortuusars.exposure.client.gui.screen.test;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.capture.Capture;
import io.github.mortuusars.exposure.client.capture.action.CaptureAction;
import io.github.mortuusars.exposure.client.capture.palettizer.Palettizer;
import io.github.mortuusars.exposure.client.image.Image;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.data.ColorPalettes;
import io.github.mortuusars.exposure.util.UnixTimestamp;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.world.camera.film.properties.Levels;
import io.github.mortuusars.exposure.world.camera.component.ShutterSpeed;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class TestImageScreen extends Screen {
    // TODO: MC 26.1 - Screen/Rendering API redesigned. Stubbed.

    protected float scale = 1f;

    protected boolean isCapturing;
    @Nullable
    protected Image image;
    @Nullable
    protected RenderableImage renderableImage;

    protected List<AbstractWidget> rightPaneWidgets = new ArrayList<>();

    protected Slider sizeSlider;
    protected ShutterSpeedSlider shutterSpeedSlider;
    protected Slider exposureSlider;
    protected Slider contrastSlider;
    protected Slider shadowsSlider;
    protected Slider midtonesSlider;
    protected Slider highlightsSlider;
    protected Slider blackSlider;
    protected Slider whiteSlider;
    protected Slider balanceRedSlider;
    protected Slider balanceGreenSlider;
    protected Slider balanceBlueSlider;
    protected Slider hueSlider;
    protected Slider saturationSlider;
    protected Slider brightnessSlider;
    protected Slider noiseSlider;
    protected Checkbox bw;
    protected Checkbox aged;

    protected float rightPaneScroll = 0f;
    protected long applyEditsAt = -1;

    public TestImageScreen() {
        super(Component.empty());
    }

    // TODO: MC 26.1
    public boolean isPauseScreen() {
        return false;
    }

    // TODO: MC 26.1
    protected void init() {
        // Stubbed
    }

    protected void capture() {
        // Stubbed
    }

    protected void applyEdits() {
        // Stubbed
    }

    protected void onChanged() {
        applyEditsAt = UnixTimestamp.Milliseconds.now();
    }

    private void setImage(Image image) {
        if (this.image != null) this.image.close();
        if (this.renderableImage != null) this.renderableImage.close();
        this.image = image;
        this.renderableImage = RenderableImage.of("test_image", image);
        applyEdits();
        ExposureClient.imageRenderer().clearCacheOf("test_image");
        isCapturing = false;
    }

    // TODO: MC 26.1 - render signature changed
    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - mouseScrolled signature
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    // TODO: MC 26.1 - mouseDragged now takes MouseButtonEvent
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    // TODO: MC 26.1 - fillHorizontalGradient stubbed
    private void fillHorizontalGradient(GuiGraphicsExtractor guiGraphics, int x1, int y1, int x2, int y2, int colorFrom, int colorTo) {
        // Stubbed
    }
}
