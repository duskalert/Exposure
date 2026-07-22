package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.toast.BetterTutorialToast;
import io.github.mortuusars.exposure.client.gui.toast.ToastIcon;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.block.entity.Lightroom;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.world.camera.FilmColor;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.lightroom.PrintingMode;
import io.github.mortuusars.exposure.world.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.inventory.LightroomMenu;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LightroomScreen extends AbstractContainerScreen<LightroomMenu> {
    // TODO: MC 26.1 - ContainerScreen/Rendering API redesigned. Stubbed.

    public static final Identifier MAIN_TEXTURE = Exposure.resource("textures/gui/lightroom.png");
    public static final Identifier FILM_OVERLAYS_TEXTURE = Exposure.resource("textures/gui/lightroom_film_overlays.png");

    public static final WidgetSprites PRINT_BUTTON_SPRITES = new WidgetSprites(
            Exposure.resource("lightroom/print_button"),
            Exposure.resource("lightroom/print_button_disabled"),
            Exposure.resource("lightroom/print_button_highlighted"));

    public static final WidgetSprites PRINTING_MODE_TOGGLE_REGULAR_SPRITES = new WidgetSprites(
            Exposure.resource("lightroom/printing_mode_regular"),
            Exposure.resource("lightroom/printing_mode_regular_highlighted"));

    public static final WidgetSprites PRINTING_MODE_TOGGLE_CHROMATIC_SPRITES = new WidgetSprites(
            Exposure.resource("lightroom/printing_mode_chromatic"),
            Exposure.resource("lightroom/printing_mode_chromatic_highlighted"));

    public static final int FRAME_SIZE = 54;

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(InputConstants.KEY_ADD).or(Key.press(InputConstants.KEY_EQUALS)).executes(this::enterFrameInspectMode),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(() -> changeFrame(PagingDirection.PREVIOUS)),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(() -> changeFrame(PagingDirection.NEXT))
    );

    protected Player player;
    protected Button printButton;
    protected PrintingMode mode;
    protected CycleButton<PrintingMode> printingModeToggleButton;

    protected Map<Integer, Rect2i> slotPlaceholders = Collections.emptyMap();

    protected boolean hasShownDevelopingToast;

    public LightroomScreen(LightroomMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.player = playerInventory.player;
        this.mode = getMenu().getBlockEntity().getActualPrintingMode();
    }

    // TODO: MC 26.1 - init signature, imageWidth/height are final, hasShiftDown removed
    protected void init() {
        // Stubbed
    }

    protected void updatePrintButtonTooltip() {
        // Stubbed
    }

    // TODO: MC 26.1 - createPrintingModeToggleButton
    protected CycleButton<PrintingMode> createPrintingModeToggleButton() {
        return null;
    }

    protected void clickButton(int buttonId) {
        getMenu().clickMenuButton(player, buttonId);
        Minecrft.gameMode().handleInventoryButtonClick(getMenu().containerId, buttonId);
    }

    // TODO: MC 26.1 - containerTick
    protected void containerTick() {
        // Stubbed
    }

    // TODO: MC 26.1 - render signature changed, hasShiftDown removed
    public void render(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    protected void updateButtons() {
        // TODO: MC 26.1 - hasShiftDown removed
        // Stubbed
    }

    // TODO: MC 26.1 - renderBg signature changed, RenderSystem/blit API changed
    protected void renderBg(@NotNull GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderSlotPlaceholders stubbed
    private void renderSlotPlaceholders(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Stubbed
    }

    // TODO: MC 26.1 - renderTooltip signature changed
    protected void renderTooltip(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // Stubbed
    }

    private void addFrameInfoTooltipLines(List<Component> tooltipLines, int frameIndex, boolean isAdvancedTooltips) {
        // Stubbed
    }

    private boolean isOverLeftFrame(int mouseX, int mouseY) {
        return false;
    }

    private boolean isOverCenterFrame(int mouseX, int mouseY) {
        return false;
    }

    private boolean isOverRightFrame(int mouseX, int mouseY) {
        return false;
    }

    // TODO: MC 26.1 - renderFrame uses PoseStack
    public void renderFrame(@NotNull Frame frame, PoseStack poseStack,
                            float x, float y, float size, float alpha, ExposureType exposureType) {
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

    // TODO: MC 26.1 - mouseClicked now takes MouseButtonEvent, getToasts removed
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public void changeFrame(PagingDirection navigation) {
        // Stubbed
    }

    private void enterFrameInspectMode() {
        Minecraft.getInstance().setScreen(new LightroomFrameInspectScreen(this));
        player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 1.3f);
    }

    // TODO: MC 26.1 - hasClickedOutside signature
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return false;
    }
}
