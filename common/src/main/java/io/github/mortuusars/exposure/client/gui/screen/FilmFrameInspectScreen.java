package io.github.mortuusars.exposure.client.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.Widgets;
import io.github.mortuusars.exposure.client.gui.component.SteppedZoom;
import io.github.mortuusars.exposure.client.gui.screen.element.Pager;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.camera.FilmColor;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.sound.SoundEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilmFrameInspectScreen extends Screen {
    // TODO: MC 26.1 - Screen/Rendering API redesigned. Stubbed.

    public static final Identifier TEXTURE = Exposure.resource("textures/gui/film_frame_inspect.png");
    public static final int BG_SIZE = 78;
    public static final int FRAME_SIZE = 54;

    protected final Pager pager = new Pager()
            .setChangeSound(new SoundEffect(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK))
            .onPageChanged(this::pageChanged);

    protected final SteppedZoom zoom = new SteppedZoom()
            .zoomInSteps(4)
            .zoomOutSteps(4)
            .zoomPerStep(1.4)
            .defaultZoom(1);

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(Minecrft.options().keyInventory).executes(this::onClose),
            Key.press(InputConstants.KEY_ADD).or(Key.press(InputConstants.KEY_EQUALS)).executes(zoom::zoomIn),
            Key.press(GLFW.GLFW_KEY_KP_SUBTRACT).or(Key.press(InputConstants.KEY_MINUS)).executes(zoom::zoomOut),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::previousPage),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::nextPage),
            Key.release(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(pager::resetCooldown),
            Key.release(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(pager::resetCooldown)
    );

    protected final List<Frame> frames;

    protected float zoomFactor;
    protected float x;
    protected float y;

    public FilmFrameInspectScreen(List<Frame> frames) {
        this(frames, frames.size() - 1);
    }

    public FilmFrameInspectScreen(List<Frame> frames, int startingFrame) {
        super(Component.empty());
        this.frames = new ArrayList<>(frames);
        initPager(startingFrame);
    }

    protected void initPager(int startingFrame) {
        pager.setPagesCount(frames.size());
        pager.setPage(startingFrame);
        Collections.rotate(frames, -startingFrame);
    }

    // TODO: MC 26.1 - init signature
    protected void init() {
        // Stubbed
    }

    protected Frame getCurrentFrame() {
        return frames.getFirst();
    }

    protected void pageChanged(int oldPage, int newPage) {
        int distance = newPage - oldPage;
        Collections.rotate(frames, -distance);
    }

    // TODO: MC 26.1 - render signature changed, RenderSystem/pushPose/translate/popPose API changed
    public void render(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderBackground signature changed
    public void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - keyPressed now takes KeyEvent
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - keyReleased now takes KeyEvent
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    // TODO: MC 26.1 - mouseScrolled signature
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    // TODO: MC 26.1 - mouseDragged now takes MouseButtonEvent,double,double
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return false;
    }

    // TODO: MC 26.1
    public boolean isPauseScreen() {
        return false;
    }
}
